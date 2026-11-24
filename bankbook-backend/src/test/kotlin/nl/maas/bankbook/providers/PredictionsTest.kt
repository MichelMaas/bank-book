package nl.maas.bankbook.providers

import nl.maas.bankbook.domain.*
import nl.maas.bankbook.domain.enums.MutationTypes
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Currency

class PredictionsTest {
    @Test fun categoryAmountsUseRoundedAverage() {
        val eur = Currency.getInstance("EUR")
        val payments = listOf("10.00", "20.00").mapIndexed { index, value ->
            Payment(index.toLong(), LocalDate.of(2026, 1, index + 1), IBAN("NL00TEST0000000001"), eur,
                Amount(BigDecimal(value), eur.symbol), MutationTypes.MAN, "Synthetic payment")
        }
        val amounts = Predictions.predict(payments).expectedFutureAmountsPerCategory()
        assertEquals(1, amounts.size)
        assertEquals(0, BigDecimal("15.00").compareTo(amounts.single().second.value))
    }
    @Test fun emptyHistoryHasNoPredictions() {
        assertTrue(Predictions.predict(emptyList()).expectedFutureTransactionsThisMonth().isEmpty())
    }
}
