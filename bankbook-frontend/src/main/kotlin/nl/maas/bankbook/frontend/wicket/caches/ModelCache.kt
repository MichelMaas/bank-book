package nl.maas.bankbook.frontend.wicket.caches

import kotlinx.coroutines.*
import nl.maas.bankbook.domain.CategoryFilter
import nl.maas.bankbook.domain.IBAN
import nl.maas.bankbook.domain.IterativeStorable
import nl.maas.bankbook.domain.Transaction
import nl.maas.bankbook.frontend.translation.CachingGoogleTranslator
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component
import java.time.*
import javax.inject.Inject

@Component
class ModelCache : nl.maas.wicket.framework.services.ModelCache {

    @Inject
    private lateinit var translator: CachingGoogleTranslator

    private var _transactions: List<Transaction> =
        IterativeStorable.load(Transaction::class)
    private var categoryFilters: List<CategoryFilter> = IterativeStorable.load(CategoryFilter::class)
    var account = _transactions.firstOrNull()?.baseAccount ?: IBAN("NL00NOBN000000000")
    val accounts get() = _transactions.map { it.baseAccount }.distinct()
    var allAccounts: Boolean = true

    private val transactions
        get() = if (allAccounts) _transactions else _transactions.filter {
            it.baseAccount.equals(
                account
            )
        }
    var date = LocalDate.now()

    enum class PERIOD {
        YEAR,
        MONTH,
        NONE;
    }

    override fun isEmpty(): Boolean {
        return transactions.isEmpty()
    }

    override fun refresh() {
        _transactions = IterativeStorable.load(Transaction::class)
        categoryFilters = IterativeStorable.load(CategoryFilter::class)
    }

    fun transactionsForPeriod(localDate: LocalDate, period: PERIOD, category: String): List<Transaction> {
        val start = LocalDateTime.now()
        var transactionsForPeriod = when (period) {
            PERIOD.MONTH -> transactions.filter {
                runBlocking {
                    async {
                        between(
                            localDate.withDayOfMonth(1),
                            localDate.withDayOfMonth(localDate.month.length(localDate.isLeapYear)),
                            it.date
                        )
                    }.await()
                }
            }

            PERIOD.YEAR -> transactions.filter {
                runBlocking {
                    async {
                        between(
                            localDate.withDayOfYear(1),
                            localDate.withDayOfYear(Year.of(localDate.year).length()),
                            it.date
                        )
                    }.await()
                }
            }

            else -> transactions.sortedByDescending { runBlocking { async { it.date }.await() } }
        }
        val end = LocalDateTime.now()
        if (category.isNotBlank()) transactionsForPeriod = transactionsForPeriod.filter { it.category.equals(category) }
        println("Fetching transactions took ${Duration.between(start, end).toString()}")

        return transactionsForPeriod
    }

    fun transactionsForPreviousPeriod(
        localDate: LocalDate,
        period: ModelCache.PERIOD
    ): List<Transaction> {
        return when (period) {
            ModelCache.PERIOD.MONTH -> runCatching {
                transactionsForPeriod(
                    localDate.minusMonths(1),
                    period,
                    StringUtils.EMPTY
                )
            }.getOrDefault(
                listOf()
            )

            else -> transactionsForPeriod(localDate.minusYears(1), period, StringUtils.EMPTY)
        }
    }

    private fun between(startDate: LocalDate, endDate: LocalDate, date: LocalDate): Boolean {
        return !date.isBefore(startDate) && !date.isAfter(endDate)
    }

    fun applyCategorieOn(
        transactions: List<Transaction> = this.transactions,
        categoryFilter: CategoryFilter
    ) {
        runBlocking {
            filterTransactions(categoryFilter.filterString, transactions).forEach {
                async { it.category = categoryFilter.category.name }
            }
        }
        GlobalScope.launch { async { IterativeStorable.storeAll(transactions) } }.invokeOnCompletion { refresh() }
    }

    suspend fun filterTransactions(
        filter: String,
        transactions: List<Transaction>
    ): List<Transaction> {
        val start = LocalTime.now()
        val filterWords = filter.split(StringUtils.SPACE).filterNot { it.isBlank() }
        var filtered: List<Transaction> = coroutineScope {
            transactions.filter { tr ->
                async {
                    filterWords.all { filterWord ->
                        async {
                            tr.filterValues(translator).map { it }.joinToString(StringUtils.SPACE)
                                .contains(filterWord, true)
                        }.await()
                    }
                }.await()
            }.sortedByDescending { it.date }
        }

        val end = LocalTime.now()
        val between = Duration.between(start, end)
        println("Filtering took: ${between}")
        return filtered
    }

    fun transactionsForFilter(filter: String): List<Transaction> {
        return runBlocking { filterTransactions(filter, transactions) }
    }

    fun applyCategorieOnAll(categoryFilter: CategoryFilter) {
        applyCategorieOn(transactions, categoryFilter)
    }

    fun findFilters(filter: String): List<CategoryFilter> {
        if (filter.isNullOrBlank()) {
            return categoryFilters
        }
        val filterWords = filter.split(StringUtils.SPACE).filterNot { it.isBlank() }
        return runBlocking {
            categoryFilters.filter { tr ->
                async {
                    filterWords.all { filterWord ->
                        async {
                            tr.filterValues().map { it }.joinToString(StringUtils.SPACE)
                                .contains(filterWord, true)
                        }.await()
                    }
                }.await()
            }
        }
    }

    fun addOrUpdateTransactions(newTransactions: List<Transaction>) {
        categoryFilters.forEach { runBlocking { async { applyCategorieOn(newTransactions, it) }.await() } }
        _transactions =
            _transactions.filter { runBlocking { async { !newTransactions.contains(it) }.await() } }
                .plus(newTransactions)
        GlobalScope.launch { IterativeStorable.storeAll(transactions) }
    }

    suspend fun deleteFilter(filter: CategoryFilter): Deferred<List<CategoryFilter>> {
        categoryFilters = categoryFilters.minus(filter)
        return GlobalScope.async { IterativeStorable.remove(listOf(filter)) }
    }

}