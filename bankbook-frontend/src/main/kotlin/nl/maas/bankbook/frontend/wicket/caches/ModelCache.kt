package nl.maas.bankbook.frontend.wicket.caches

import kotlinx.coroutines.*
import nl.maas.bankbook.domain.*
import nl.maas.bankbook.domain.properties.Categories
import nl.maas.bankbook.frontend.translation.CachingGoogleTranslator
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Year
import javax.inject.Inject

@Component
class ModelCache : nl.maas.wicket.framework.services.ModelCache {

    val occupiedIDs: List<Long> get() = _transactions.map { it.id }
    private var storeInProgress = false
    private var blockRequested = false
    val shouldHoldRender get() = storeInProgress || blockRequested

    @Inject
    private lateinit var translator: CachingGoogleTranslator

    private var _transactions: List<Transaction> =
        IterativeStorable.load(Transaction::class)
    private var categoryFilters: List<CategoryFilter> = IterativeStorable.load(CategoryFilter::class)
    var account = _transactions.firstOrNull()?.baseAccount ?: IBAN("NL00NOBN000000000")
    val accounts get() = _transactions.map { it.baseAccount }.distinct()
    var allAccounts: Boolean = true

    @Transient
    var selectedTransaction: Transaction? = null
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

    fun isStoring() = storeInProgress

    fun requestBlockWhile(interval: Long, check: (ModelCache) -> Boolean) {
        GlobalScope.launch {
            blockRequested = true
            while (check(this@ModelCache)) {
                Thread.sleep(interval)
            }
            blockRequested = false
        }
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
        categoryFilter: CategoryFilter,
        storeNow: Boolean
    ) {
        runBlocking {
            filterTransactions(categoryFilter.filterString, transactions).forEach {
                async { it.category = categoryFilter.category.name }
            }
        }
        if (storeNow) invokeStorage(transactions)
    }

    suspend fun filterTransactions(
        filter: String,
        transactions: List<Transaction>
    ): List<Transaction> {
//        TimerUtil.start()
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
//        val end = LocalTime.now()
//        TimerUtil.stop("Filtering took")
        return filtered
    }

    fun transactionsForFilter(filter: String): List<Transaction> {
        return runBlocking { filterTransactions(filter, _transactions) }
    }

    fun applyCategorieOnAll(categoryFilter: CategoryFilter) {
        applyCategorieOn(_transactions, categoryFilter, true)
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

    fun applyCategoriesOnNewTransactions(transactions: List<Transaction>) {
        val filterIterator = categoryFilters.iterator()
        while (transactions.any { Categories.UNKNOWN.equals(it.category) } && filterIterator.hasNext()) {
            val current = filterIterator.next()
            runBlocking { async { applyCategorieOn(transactions, current, false) }.await() }
        }
    }

    fun addOrUpdateTransactions(newTransactions: List<Transaction>) {
//        categoryFilters.forEach { runBlocking { async { applyCategorieOn(newTransactions, it, false) }.await() } }
        applyCategoriesOnNewTransactions(newTransactions)
//        _transactions =
//            newTransactions.filter { nt -> runBlocking { async { !_transactions.contains(nt) }.await() } }
//                .plus(_transactions)
        invokeStorage(newTransactions)
    }

    private fun invokeStorage(transactions: List<Transaction> = _transactions) {
        while (storeInProgress) {
            Thread.sleep(1000)
        }
        storeInProgress = true
        IterativeStorable.storeAll(transactions)
        refresh()
        storeInProgress = false
    }

    suspend fun deleteFilter(filter: CategoryFilter): Deferred<List<CategoryFilter>> {
        categoryFilters = categoryFilters.minus(filter)
        return GlobalScope.async { IterativeStorable.remove(listOf(filter)) }
    }

    fun getChildrenFor(id: Long): List<Transaction> {
        return _transactions.filter { Transfer::class.isInstance(it) && (it as Transfer).parentId.equals(id) }
    }

}