package nl.maas.bankbook.parsers

import nl.maas.bankbook.domain.*
import nl.maas.bankbook.domain.enums.MutationTypes
import org.apache.commons.lang3.math.NumberUtils
import org.apache.commons.validator.GenericValidator
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class SNSBParser internal constructor(map: Map<Int, MutableList<String>>) :
    CSVParser(map, 15, 0, 1, 2, 3, 7, 10, 13, 14, 17) {


    private val datePattern = "dd-MM-yyyy"

    override fun createPayment(record: List<String>): Transaction {
        val currency =
            Currency.getAvailableCurrencies().first { it.currencyCode.equals(record[POSITIONS.CURRENCY], true) }
        return Payment(
            record[POSITIONS.ID].toLong(),
            LocalDate.parse(record[POSITIONS.DATE], DateTimeFormatter.ofPattern(datePattern)),
            IBAN(record[POSITIONS.SOURCE]),
            currency,
            Amount(record[POSITIONS.AMOUNT], currency.symbol),
            MutationTypes.byCode(record[POSITIONS.TYPE_CODE].toInt()),
            record[POSITIONS.DESCRIPTION]
        )
    }

    override fun createTransfer(record: List<String>): Transaction {
        val currency =
            Currency.getAvailableCurrencies().first { it.currencyCode.equals(record[POSITIONS.CURRENCY], true) }
        return Transfer(
            record[POSITIONS.ID].toLong(),
            LocalDate.parse(record[POSITIONS.DATE], DateTimeFormatter.ofPattern(datePattern)),
            IBAN(record[POSITIONS.SOURCE]),
            IBAN(record[POSITIONS.COUNTER]),
            record[POSITIONS.COUNTER_NAME],
            Currency.getAvailableCurrencies().first { it.currencyCode.equals(record[POSITIONS.CURRENCY], true) },
            Amount(record[POSITIONS.AMOUNT], currency.symbol),
            MutationTypes.byCode(record[POSITIONS.TYPE_CODE].toInt()),
            record[POSITIONS.DESCRIPTION]
        )
    }

    override fun validateForBank(): Boolean {
        return map.values.toList().all { record ->
            NumberUtils.isDigits(record[POSITIONS.ID]) &&
                    GenericValidator.isDate(record[POSITIONS.DATE], datePattern, true) &&
                    IBAN.validate(record[POSITIONS.SOURCE]) &&
                    IBAN.validate(record[POSITIONS.COUNTER]) &&
                    Currency.getAvailableCurrencies()
                        .filter { it.currencyCode.equals(record[POSITIONS.CURRENCY], true) }.isNotEmpty() &&
                    GenericValidator.isDouble(record[POSITIONS.AMOUNT]) &&
                    MutationTypes.values().any { it.codes.contains(record[POSITIONS.TYPE_CODE].toInt()) }
        }
    }


}