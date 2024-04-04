package nl.maas.bankbook.frontend.services

import nl.maas.bankbook.frontend.wicket.caches.ModelCache
import nl.maas.bankbook.frontend.wicket.objects.enums.StartOfMonth
import java.time.LocalDate
import java.time.Month
import java.time.Year

class PayDateUtility private constructor(val modelCache: ModelCache) {

    companion object {
        fun get(modelCache: ModelCache): PayDateUtility = PayDateUtility(modelCache)
    }

    fun findIncomeStartEndDates(month: Month, year: Year): Pair<LocalDate, LocalDate> {
        return findStartEndDates(month, year)
    }

    private fun findStartEndDates(
        month: Month,
        year: Year
    ): Pair<LocalDate, LocalDate> {
        val startDate = LocalDate.of(year.value, month.value, 1)
        val transactions =
            modelCache.transactionsForPeriod(startDate, ModelCache.PERIOD.MONTH, "SALARY", StartOfMonth.CALENDAR)
                .sortedBy { it.date }
        val transactionsPreviousMonth =
            modelCache.transactionsForPeriod(
                startDate.minusMonths(1),
                ModelCache.PERIOD.MONTH,
                "SALARY",
                StartOfMonth.CALENDAR
            )
                .sortedBy { it.date }
        val transactionsNextMonth =
            modelCache.transactionsForPeriod(
                startDate.plusMonths(1),
                ModelCache.PERIOD.MONTH,
                "SALARY",
                StartOfMonth.CALENDAR
            )
                .sortedBy { it.date }
        val firstDate =
            if ((transactions.firstOrNull()?.date ?: startDate).dayOfMonth >= 15) transactions.firstOrNull()?.date
                ?: startDate else transactionsPreviousMonth.firstOrNull()?.date ?: startDate
        val lastDate =
            if ((transactions.firstOrNull()?.date
                    ?: startDate).dayOfMonth < 15
            ) transactions.firstOrNull()?.date?.minusDays(1)
                ?: startDate.withDayOfMonth(startDate.month.length(year.isLeap)) else transactionsNextMonth.firstOrNull()?.date
                ?: startDate.withDayOfMonth(startDate.month.length(year.isLeap))
        return firstDate to lastDate
    }
}