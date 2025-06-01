package nl.maas.bankbook.frontend.wicket.tools

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import nl.maas.bankbook.domain.*
import nl.maas.bankbook.domain.enums.MutationTypes
import nl.maas.bankbook.frontend.wicket.caches.ModelCache
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*

class TupleUtilsTest {

    val INSTANCE = TupleUtils()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun transactionsToTuples() {
        val tuples = runBlocking {
            async {
                INSTANCE.transactionsToTuples(
                    createTransactions(),
                    false,
                    ModelCache.PERIOD.MONTH
                )
            }
        }.getCompleted()
        assertEquals(tuples.size, 4)
    }

    @Test
    fun filtersToTuples() {
        val tuples = INSTANCE.filtersToTuples(createFilters())
        assertEquals(tuples.size, 6)
    }

    @Test
    fun groupTuplesBy() {
        val tuples = INSTANCE.filtersToTuples(createFilters())
        val result = INSTANCE.groupTuplesBy(tuples, "Category")
        assertEquals(result.size, 2)
        assertEquals(result.first { it.getValueForColumn("Category").equals("Tested") }.getValueForColumn("Count"), 4)
        assertEquals(result.first { it.getValueForColumn("Category").equals("Test") }.getValueForColumn("Count"), 2)
    }

    private fun createTransactions(): List<Transaction> {
        val date = LocalDate.now()
        val currency = Currency.getInstance(Locale.getDefault())
        return listOf(
            Payment(0, date, IBAN.EMPTY, currency, Amount("1", currency.symbol), MutationTypes.MAN, "Test", "Tester"),
            Payment(0, date, IBAN.EMPTY, currency, Amount("2", currency.symbol), MutationTypes.MAN, "Test", "Tester"),
            Payment(0, date, IBAN.EMPTY, currency, Amount("3", currency.symbol), MutationTypes.MAN, "Test", "Tester02"),
            Payment(0, date, IBAN.EMPTY, currency, Amount("4", currency.symbol), MutationTypes.MAN, "Test", "Tester02")
        )
    }

    private fun createFilters(): List<CategoryFilter> {
        return listOf(
            CategoryFilter("Test1", "Test", true),
            CategoryFilter("Test2", "Test", true),
            CategoryFilter("Test3", "Tested", true),
            CategoryFilter("Test4", "Tested", true),
            CategoryFilter("Test5", "Tested", false),
            CategoryFilter("Test6", "Tested", false)
        )
    }
}