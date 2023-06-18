package nl.maas.bankbook.frontend.services

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import nl.maas.bankbook.domain.CategoryFilter
import nl.maas.bankbook.domain.IBAN
import nl.maas.bankbook.domain.Transaction
import nl.maas.bankbook.frontend.ContextProvider
import nl.maas.bankbook.frontend.domain.Account
import nl.maas.bankbook.frontend.wicket.caches.ModelCache
import nl.maas.bankbook.providers.Translator
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@Component
class DataManagementService : DataProvider, nl.maas.wicket.framework.services.ModelCache {

    var account = Account(IBAN("NL00SNSB0000000000"))
    var date: LocalDate = LocalDate.now()

    @Inject
    private lateinit var transactionsService: TransactionsService

    @Inject
    private lateinit var translator: Translator

    @Inject
    private lateinit var accountService: AccountService

    @Inject
    lateinit var filterService: FilterService

    init {
        account = ContextProvider.ctx.getBean(AccountService::class.java).getAccounts().firstOrNull() ?: account
    }

    override fun transactionsForPeriod(localDate: LocalDate, period: ModelCache.PERIOD): List<Transaction> {
        val start = LocalDateTime.now()
        val transactionsForPeriod = when (period) {
            ModelCache.PERIOD.MONTH -> transactionsService.getTransactionsForMonth(localDate)

            ModelCache.PERIOD.YEAR -> transactionsService.getTransactionsForYear(localDate)
            else -> transactionsService.getAll().sortedByDescending { runBlocking { async { it.date }.await() } }
        }
        val end = LocalDateTime.now()
        println("Fetching transactions took ${Duration.between(start, end).toString()}")
        return transactionsForPeriod
    }

    override fun transactionsForPreviousPeriod(localDate: LocalDate, period: ModelCache.PERIOD): List<Transaction> {
        return when (period) {
            ModelCache.PERIOD.MONTH -> transactionsForPeriod(localDate.minusMonths(1), period)
            else -> transactionsForPeriod(localDate.minusYears(1), period)
        }
    }

    override fun applyCategorieOn(transactions: List<Transaction>, categoryFilter: CategoryFilter) {
        runBlocking { filterTransactions(categoryFilter.filterString, transactions, translator) }
    }

    override fun transactionsForFilter(filter: String): List<Transaction> {
        return transactionsService.getTransactionsForFilter(filter)
    }

    override fun applyCategorieOnAll(categoryFilter: CategoryFilter) {
        val transactions = transactionsService.getTransactionsForFilter(categoryFilter.filterString)
        transactions.forEach { it.category = categoryFilter.category.name }
        transactionsService.store(*transactions.toTypedArray())
    }

    override fun findFilters(filter: String): List<CategoryFilter> {
        return if (filter.isBlank()) filterService.getFilters() else filterService.findFilterByFilterString(filter)
    }

    override fun addOrUpdateTransactions(newTransactions: List<Transaction>) {
        transactionsService.store(*newTransactions.toTypedArray())
    }

    override suspend fun deleteFilter(filter: CategoryFilter): List<CategoryFilter> {
        return filterService.delete(filter)
    }

    override fun isEmpty(): Boolean {
        return transactionsService.getAll().isEmpty()
    }

    override fun refresh() {
        //no actions needed
    }
}