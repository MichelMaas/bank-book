package nl.maas.bankbook.frontend.services

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import nl.maas.bankbook.domain.CategoryFilter
import nl.maas.bankbook.domain.Transaction
import nl.maas.bankbook.frontend.wicket.caches.ModelCache
import nl.maas.bankbook.providers.Translator
import org.apache.commons.lang3.StringUtils
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

interface DataProvider {
    fun transactionsForPeriod(localDate: LocalDate, period: ModelCache.PERIOD): List<Transaction>
    fun transactionsForPreviousPeriod(
        localDate: LocalDate,
        period: ModelCache.PERIOD
    ): List<Transaction>

    fun between(startDate: LocalDate, endDate: LocalDate, date: LocalDate): Boolean {
        return !date.isBefore(startDate) && !date.isAfter(endDate)
    }

    fun applyCategorieOn(
        transactions: List<Transaction>,
        categoryFilter: CategoryFilter
    )

    suspend fun filterTransactions(
        filter: String,
        transactions: List<Transaction>,
        translator: Translator
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

    fun transactionsForFilter(filter: String): List<Transaction>
    fun applyCategorieOnAll(categoryFilter: CategoryFilter)
    fun findFilters(filter: String): List<CategoryFilter>
    fun addOrUpdateTransactions(newTransactions: List<Transaction>)

    suspend fun deleteFilter(filter: CategoryFilter): List<CategoryFilter>
}