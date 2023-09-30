package nl.maas.bankbook.providers

import nl.maas.bankbook.domain.*
import nl.maas.bankbook.domain.enums.MutationTypes
import org.iban4j.CountryCode
import org.iban4j.Iban
import org.iban4j.IbanUtil
import org.junit.Assert
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month
import java.util.*
import kotlin.random.Random

class PredictionsTest {

    val instance = Predictions.predict(createTransactions())
    val source = IBAN("NL04OVBN0385427441")

    private fun createTransactions(): List<Transaction> {
        val transactions: MutableList<Transaction> = mutableListOf()
        val now = LocalDate.now().withDayOfMonth(1)
        transactions.addAll(createPayments(now))
        transactions.addAll(createTransfers(now))
        return transactions
    }

    private fun createTransfers(now: LocalDate): List<Transaction> {
        val transfers: MutableList<Transfer> = mutableListOf()
        (now.minusMonths(4).monthValue..now.minusMonths(1).monthValue).toList().forEach {
            val counter = fetchRandomIBAN()
            transfers.add(
                Transfer(
                    UUID.fromString("${Month.of(it)}").mostSignificantBits,
                    now.withMonth(it),
                    source,
                    counter,
                    counter.value,
                    Currency.getInstance(Locale.getDefault()),
                    Amount(BigDecimal.valueOf(Random.nextDouble()), Currency.getInstance(Locale.getDefault()).symbol),
                    MutationTypes.DIV,
                    "Test transfer for ${Month.of(it)}"
                )
            )
            if (it.mod(3) == 0) {
                transfers.add(
                    Transfer(
                        UUID.fromString("${Month.of(it)}").mostSignificantBits,
                        now.withMonth(it),
                        source,
                        counter,
                        counter.value,
                        Currency.getInstance(Locale.getDefault()),
                        Amount(
                            BigDecimal.valueOf(Random.nextDouble()),
                            Currency.getInstance(Locale.getDefault()).symbol
                        ),
                        MutationTypes.DIV,
                        "Incidental test transfer for ${Month.of(it)}"
                    )
                )
            }
        }
        return transfers
    }

    private fun fetchRandomIBAN(): IBAN {
        return IBAN("NL00OVBN${Random.nextLong(9999999999)}")
    }

    private fun createPayments(now: LocalDate): List<Transaction> {
        val payments: MutableList<Payment> = mutableListOf()
        (now.minusMonths(4).monthValue..now.minusMonths(1).monthValue).toList().forEach {
            val counter = fetchRandomIBAN()
            payments.add(
                Payment(
                    UUID.fromString("${Month.of(it)}").mostSignificantBits,
                    now.withMonth(it),
                    source,
                    Currency.getInstance(Locale.getDefault()),
                    Amount(BigDecimal.valueOf(Random.nextDouble()), Currency.getInstance(Locale.getDefault()).symbol),
                    MutationTypes.MAN,
                    "Test payment for ${Month.of(it)}"
                )
            )
            if (it.mod(3) == 0) {
                payments.add(
                    Payment(
                        UUID.fromString("${Month.of(it)}").mostSignificantBits,
                        now.withMonth(it),
                        source,
                        Currency.getInstance(Locale.getDefault()),
                        Amount(
                            BigDecimal.valueOf(Random.nextDouble()),
                            Currency.getInstance(Locale.getDefault()).symbol
                        ),
                        MutationTypes.MAN,
                        "Incidental test payment for ${Month.of(it)}"
                    )
                )
            }
        }
        return payments
    }

    @Test
    fun testExpectedFutureTransactionsThisMonth() {
        val predictions = Predictions.predict(createTransactions()).expectedFutureTransactionsThisMonth()
        Assert.assertTrue(predictions.isNotEmpty())
    }
}