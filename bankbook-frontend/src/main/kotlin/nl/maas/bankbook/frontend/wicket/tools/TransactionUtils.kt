package nl.maas.bankbook.frontend.wicket.tools

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import nl.maas.bankbook.domain.Transaction
import nl.maas.bankbook.frontend.wicket.caches.ModelCache
import nl.maas.wicket.framework.objects.Tuple
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject

@Component
class TransactionUtils {

    @Inject
    lateinit var modelCache: ModelCache

    suspend fun transactionsToTuples(
        transactions: List<Transaction>,
        categorized: Boolean,
        period: ModelCache.PERIOD
    ): List<Tuple> {
        if (transactions.isEmpty()) {
            return listOf()
        }
        return coroutineScope {
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
    }


    private fun createTransactionTuple(transaction: Transaction): Tuple {
        return Tuple(
            "Receiver" to transaction.counter(),
            "Amount" to transaction.mutation.value,
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
        return Tuple(
            "Category" to pair.first,
            "Amount" to currentAmount,
            "Previous period" to previousAmount,
            "Difference" to currentAmount.minus(previousAmount)
        )
    }
}