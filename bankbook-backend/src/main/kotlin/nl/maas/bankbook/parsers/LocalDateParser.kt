package nl.maas.bankbook.parsers

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*
import javax.transaction.NotSupportedException

class LocalDateParser {
    companion object {
        fun parseStringTo(dateString: String, locale: Locale = Locale.getDefault()): LocalDate {
            return LocalDate.from(findBestFormat(dateString, locale).parse(dateString))
        }

        private fun findBestFormat(dateString: String, locale: Locale): DateTimeFormatter {
            val formatters =
                DateTimeFormatter::class.java.declaredFields.filter { it.type.isAssignableFrom(DateTimeFormatter::class.java) }
                    .map { (it.get(null) as DateTimeFormatter).withLocale(locale) }
                    .plus(proprietaryFormats(locale, "d MMM yyyy", "d MMMM yyyy"))
                    .filter { isParsable(it, dateString) }
            if (formatters.size == 0) {
                throw NotSupportedException()
            }
            return formatters[0]
        }

        private fun isParsable(dateTimeFormatter: DateTimeFormatter, dateString: String): Boolean {
            try {
                dateTimeFormatter.parse(dateString)
                return true
            } catch (e: DateTimeParseException) {
                return false
            }
        }

        private fun proprietaryFormats(
            locale: Locale = Locale.getDefault(),
            vararg patterns: String
        ): List<DateTimeFormatter> {
            return patterns.map { DateTimeFormatter.ofPattern(it, locale) }
        }
    }
}