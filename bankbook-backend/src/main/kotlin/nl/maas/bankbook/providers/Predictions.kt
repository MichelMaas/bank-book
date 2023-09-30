package nl.maas.bankbook.providers

import nl.maas.bankbook.domain.Amount
import nl.maas.bankbook.domain.Transaction
import org.apache.commons.lang3.StringUtils
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.Month

class Predictions private constructor(private val transactions: List<Transaction>) {
    companion object {
        fun predict(transactions: List<Transaction>): Predictions {
            return Predictions(transactions)
        }
    }

    fun expectedFutureAmountsPerCategory(): List<Pair<String, Amount>> {
        return transactions.groupBy { it.category }.map {
            it.key to Amount(
                it.value.sumOf { lst -> lst.mutation.value }.divide(
                    BigDecimal.valueOf(it.value.size.toLong()), 2, RoundingMode.HALF_UP
                ), it.value.first().mutation.symbol
            )
        }
    }

    fun expectedFutureTransactionsThisMonth(): List<Transaction> {
        val transactionsPerMonth = transactions.groupBy { it.date.month }
        val now = LocalDate.now()
        val endOfMonth = LocalDate.now().withDayOfMonth(now.month.length(now.isLeapYear))
        val monthlyTransactions = determineMonthlyTransactions(now, transactionsPerMonth)
        val anualTransactions = determineAnualTransactions(now, transactionsPerMonth)
        val predictedTransactions = monthlyTransactions.filter { between(it, now, endOfMonth) }
            .plus(anualTransactions.filter { between(it, now, endOfMonth) })
        return predictedTransactions.map {
            object : Transaction(
                0,
                now.withDayOfMonth(it.date.dayOfMonth),
                it.baseAccount,
                it.currency,
                it.mutation,
                it.mutationType,
                it.description,
                it.category
            ) {
                override fun counter(): String {
                    return StringUtils.EMPTY
                }
            }
        }
    }

    private fun between(
        it: Transaction,
        now: LocalDate,
        endOfMonth: LocalDate
    ) = it.date.dayOfMonth >= now.dayOfMonth && it.date.dayOfMonth <= endOfMonth.dayOfMonth

    private fun determineAnualTransactions(
        now: LocalDate,
        transactionsPerMonth: Map<Month, List<Transaction>>
    ): List<Transaction> {
        val transactionsSameMonth = transactionsPerMonth.filter { it.equals(now.month) }.flatMap { it.value }
        val transactionsOtherMonths = transactionsPerMonth.filter { !it.key.equals(now.month) }
        val anualTransactions = transactionsSameMonth.filter {
            transactionsOtherMonths.none { om ->
                om.value.any { omtr ->
                    isSameTransactionType(
                        it,
                        omtr
                    )
                }
            }
        }
        return anualTransactions
    }

    private fun determineMonthlyTransactions(
        now: LocalDate,
        transactionsPerMonth: Map<Month, List<Transaction>>
    ): List<Transaction> {
        val lastThreeMonths =
            transactionsPerMonth.filter { (now.month.minus(4).value..now.monthValue.minus(1)).contains(it.key.value) }
        val lastMonth = transactionsPerMonth[now.month.minus(1)]
        val monthly =
            lastMonth?.filter {
                lastThreeMonths.all { ltm ->
                    ltm.value.any { ltmv ->
                        isSameTransactionType(
                            it,
                            ltmv
                        )
                    }
                }
            } ?: listOf()
        return monthly
    }

    private fun isSameTransactionType(transaction1: Transaction, transaction2: Transaction): Boolean {
        return transaction1.mutationType.equals(transaction2.mutationType) && transaction1.baseAccount.equals(
            transaction2.baseAccount
        ) && transaction1.counter().equals(transaction2.counter())
    }
}