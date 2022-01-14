package nl.maas.bankbook.parsers

import nl.maas.bankbook.domain.*
import nl.maas.bankbook.domain.enums.MutationTypes
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class SNSBParser internal constructor(map: Map<Int, MutableList<String>>) : Parser(map, 15, 0, 1, 2, 3, 7, 10, 14, 17) {


    override fun createPayment(record: MutableList<String>): Transaction {
        val currency =
            Currency.getAvailableCurrencies().first { it.currencyCode.equals(record[POSITIONS.CURRENCY], true) }
        return Payment(
            record[POSITIONS.ID].toLong(),
            LocalDate.parse(record[POSITIONS.DATE], DateTimeFormatter.ofPattern("dd-MM-yyyy")),
            IBAN(record[POSITIONS.SOURCE]),
            currency,
            Amount(record[POSITIONS.AMOUNT], currency.symbol),
            MutationTypes.valueOf(record[POSITIONS.TYPE]),
            record[POSITIONS.DESCRIPTION]
        )
    }

    override fun createTransfer(record: MutableList<String>): Transaction {
        val currency =
            Currency.getAvailableCurrencies().first { it.currencyCode.equals(record[POSITIONS.CURRENCY], true) }
        return Transfer(
            record[POSITIONS.ID].toLong(),
            LocalDate.parse(record[POSITIONS.DATE], DateTimeFormatter.ofPattern("dd-MM-yyyy")),
            IBAN(record[POSITIONS.SOURCE]),
            IBAN(record[POSITIONS.COUNTER]),
            record[POSITIONS.COUNTER_NAME],
            Currency.getAvailableCurrencies().first { it.currencyCode.equals(record[POSITIONS.CURRENCY], true) },
            Amount(record[POSITIONS.AMOUNT], currency.symbol),
            MutationTypes.valueOf(record[POSITIONS.TYPE]),
            record[POSITIONS.DESCRIPTION]
        )
    }


}