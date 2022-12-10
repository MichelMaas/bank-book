package nl.maas.bankbook.parsers

import nl.maas.bankbook.AbstractTest
import org.junit.Assert
import org.junit.Test
import java.time.Month
import java.time.format.TextStyle
import java.util.*

class LocalDateParserTest : AbstractTest() {

    @Test
    fun testEnDate() {
        val locale = Locale.forLanguageTag("en-GB")
        val dates =
            Month.values().map { it to "1 ${it.getDisplayName(TextStyle.FULL, locale)} 2018" }.forEach { date ->
                testDate(date.first, date.second, locale)
            }
    }

    @Test
    fun testNlDate() {
        val locale = Locale.forLanguageTag("nl-NL")
        val dates =
            Month.values().map { it to "1 ${it.getDisplayName(TextStyle.FULL, locale)} 2018" }.forEach { date ->
                testDate(date.first, date.second, locale)
            }
    }

    @Test
    fun testJaDate() {
        val locale = Locale.forLanguageTag("ja-Ja")
        val dates =
            Month.values().map { it to "1 ${it.getDisplayName(TextStyle.FULL, locale)} 2018" }.forEach { date ->
                testDate(date.first, date.second, locale)
            }
    }

    private fun testDate(month: Month, date: String, locale: Locale) {
        val localDate = LocalDateParser.parseStringTo(date, locale)
        Assert.assertEquals(1, localDate.dayOfMonth)
        Assert.assertEquals(month, localDate.month)
        Assert.assertEquals(2018, localDate.year)
    }
}