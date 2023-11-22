package nl.maas.bankbook.frontend.wicket.tools

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import nl.maas.bankbook.domain.Amount
import nl.maas.bankbook.domain.CategoryFilter
import nl.maas.bankbook.domain.Transaction
import nl.maas.bankbook.frontend.wicket.caches.ModelCache
import nl.maas.wicket.framework.objects.Tuple
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject

@Component
class TupleUtils {

    @Inject
    lateinit var modelCache: ModelCache

    suspend fun transactionsToTuples(
        _transactions: List<Transaction>,
        categorized: Boolean,
        period: ModelCache.PERIOD
    ): List<Tuple> {
        if (_transactions.isEmpty()) {
            return listOf()
        }
        val transactions =
            if (_transactions.size > 100) _transactions.sortedByDescending { runBlocking { async { it.date }.await() } }
                .subList(0, 99) else _transactions
        val tuples = coroutineScope {
            when (categorized) {
                true -> {
                    val previous =
                        modelCache.transactionsForPreviousPeriod(transactions.first().date, period).groupBy {
                            async { it.category }.await()
                        }
                    return@coroutineScope transactions.groupBy { async { it.category }.await() }
                        .map {
                            async {
                                createCategoryTuple(
                                    previous[it.key]?.sumOf { it.mutation.value } ?: BigDecimal.ZERO,
                                    it.key to it.value,
                                    transactions.first().date,
                                    period
                                )
                            }.await()
                        }
                }

                else -> async { transactions.map { createTransactionTuple(it) } }.await()
            }
        }
        return tuples
    }

    fun filtersToTuples(filters: List<CategoryFilter>): List<Tuple> {
        return filters.map {
            Tuple(
                "Filter" to it.filterString,
                "Category" to it.category
            )
        }
    }


    private fun createTransactionTuple(transaction: Transaction): Tuple {
        return Tuple(
            "Receiver" to transaction.counter(),
            "Amount" to transaction.mutation.toString(),
            "Description" to transaction.description,
            "Category" to transaction.category
        )
    }

    private fun createCategoryTuple(
        previousAmount: BigDecimal,
        pair: Pair<String, List<Transaction>>,
        localDate: LocalDate,
        period: ModelCache.PERIOD
    ): Tuple {
        val currentAmount = pair.second.sumOf { it.mutation.value }
        val symbol = pair.second.first().currency.symbol
        return Tuple(
            "Category" to pair.first,
            "Amount" to Amount(currentAmount, symbol).toString(),
            "Previous period" to previousAmount,
            "Difference" to Amount(currentAmount.minus(previousAmount), symbol).toString()
        )
    }
}