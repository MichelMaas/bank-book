package nl.maas.bankbook.frontend.wicket.caches

import nl.maas.bankbook.IterativeStorable
import nl.maas.bankbook.domain.IBAN
import nl.maas.bankbook.domain.Transaction
import nl.maas.wicket.framework.objects.Tuple
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Year

@Component
class ModelCache : nl.maas.wicket.framework.services.ModelCache {

    private var transactions: List<Transaction> = IterativeStorable.load(Transaction::class)
    val account get() = transactions.firstOrNull()?.baseAccount ?: IBAN("NL00NOBN000000000")
    private var transactionTuples: List<Tuple> = listOf()
    private var categoryTuples: List<Tuple> = listOf()

    var date = LocalDate.now()

    enum class PERIOD {
        YEAR,
        MONTH;
    }

    override fun isEmpty(): Boolean {
        return true
    }

    override fun refresh() {
        transactions = IterativeStorable.load(Transaction::class)
    }

    fun transactionsForPeriod(localDate: LocalDate, period: PERIOD): List<Transaction> {
        val start = LocalDateTime.now()
        val transactionsForPeriod = when (period) {
            PERIOD.MONTH -> transactions.filter {
                between(
                    localDate.withDayOfMonth(1),
                    localDate.withDayOfMonth(localDate.month.length(localDate.isLeapYear)),
                    it.date
                )
            }

            else -> transactions.filter {
                between(
                    localDate.withDayOfYear(1),
                    localDate.withDayOfYear(Year.of(localDate.year).length()),
                    it.date
                )
            }
        }
        val end = LocalDateTime.now()
        println("Fetching transactions took ${Duration.between(start, end).toString()}")
        return transactionsForPeriod
    }

    fun transactionsForPreviousPeriod(
        localDate: LocalDate,
        period: ModelCache.PERIOD
    ): List<Transaction> {
        return when (period) {
            ModelCache.PERIOD.MONTH -> transactionsForPeriod(localDate.minusMonths(1), period)
            else -> transactionsForPeriod(localDate.minusYears(1), period)
        }
    }

    private fun between(startDate: LocalDate, endDate: LocalDate, date: LocalDate): Boolean {
        return !date.isBefore(startDate) && !date.isAfter(endDate)
    }


}