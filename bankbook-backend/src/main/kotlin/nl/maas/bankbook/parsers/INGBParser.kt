package nl.maas.bankbook.parsers

import nl.maas.bankbook.domain.*
import nl.maas.bankbook.domain.enums.MutationTypes.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class INGBParser internal constructor(map: Map<Int, MutableList<String>>) :
    Parser(map, -1, 0, 2, 3, 1, -1, 6, 4, 7, 8) {
    private val datePattern: String = "yyyyMMdd"
    private val currency = Currency.getInstance(Locale.getDefault())
    private val mutationTypesMap = mapOf("BA" to BEA, "ID" to IDB, "GT" to OVB, "GM" to GEA, "OV" to OVB, "VZ" to DPS)

    override fun createPayment(record: MutableList<String>): Transaction {
        return Payment(
            makeUUID(),
            toDate(record[POSITIONS.DATE]),
            IBAN(record[POSITIONS.SOURCE]),
            currency,
            Amount(posNeg(record[POSITIONS.AMOUNT], record[5]), currency.symbol),
            mutationTypesMap[record[POSITIONS.TYPE_CODE]] ?: UNK,
            escapeJSON(record[POSITIONS.DESCRIPTION]),
            record[POSITIONS.COUNTER_NAME]
        )
    }

    private fun posNeg(amount: String, type: String): String {
        return if (type.equals("Af", true)) "-$amount" else amount
    }

    private fun toDate(date: String): LocalDate {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern(datePattern))
    }

    private fun extractCounterName(description: String): String {
        return description.substringAfter("Naam: ").substringBefore(": ")
    }

    override fun createTransfer(record: MutableList<String>): Transaction {
        return Transfer(
            makeUUID(), toDate(record[POSITIONS.DATE]),
            IBAN(record[POSITIONS.SOURCE]),
            IBAN(record[POSITIONS.COUNTER]),
            record[POSITIONS.COUNTER_NAME],
            currency,
            Amount(posNeg(record[POSITIONS.AMOUNT], record[5]), currency.symbol),
            mutationTypesMap[record[POSITIONS.TYPE_CODE]] ?: UNK,
            record[POSITIONS.DESCRIPTION]
        )
    }

    private fun makeUUID() = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE

    override fun validateForBank(): Boolean {
        return map.all { IBAN.validate(it.value[POSITIONS.SOURCE]) }
    }
}