package nl.maas.bankbook.parsers

import nl.maas.bankbook.domain.IBAN
import nl.maas.bankbook.domain.Transaction
import nl.maas.bankbook.domain.enums.Banks
import nl.maas.bankbook.utils.CSVUtils
import org.apache.commons.lang3.NotImplementedException
import java.io.File
import java.nio.file.Paths

abstract class Parser private constructor(protected val map: Map<Int, MutableList<String>>) {

    protected lateinit var POSITIONS: Positions
        private set

    protected constructor(
        map: Map<Int, MutableList<String>>,
        ID: Int,
        DATE: Int,
        SOURCE: Int,
        COUNTER: Int,
        COUNTER_NAME: Int,
        CURRENCY: Int,
        AMOUNT: Int,
        TYPE_CODE: Int,
        TYPE: Int,
        DESCRIPTION: Int
    ) : this(map) {
        this.POSITIONS =
            Positions(ID, DATE, SOURCE, COUNTER, COUNTER_NAME, CURRENCY, AMOUNT, TYPE_CODE, TYPE, DESCRIPTION)
    }

    companion object {
        fun parse(file: String): Parser {
            return parse(Paths.get(file).toFile())
        }

        fun parse(file: File): Parser {
            val parsedFile = CSVUtils.parseFile(file)
            val bankName = CSVUtils.findBaseAccount(parsedFile).substring(4, 8)
            return when (Banks.valueOf(bankName)) {
                Banks.SNSB -> SNSBParser(parsedFile)
                else -> throw NotImplementedException("No parser yet implemented for bank ${bankName}")
            }
        }

    }

    fun createTransactions(): List<Transaction> {
        return map.values.map { createTransaction(it) }.distinctBy { it.id }
    }

    fun determineSourceAccount(doc: Map<Int, MutableList<String>>): IBAN {
        return IBAN(CSVUtils.findBaseAccount(doc))
    }

    protected fun createTransaction(record: MutableList<String>): Transaction {
        if (record[POSITIONS.COUNTER].isNullOrBlank()) {
            return createPayment(record)
        } else {
            return createTransfer(record)
        }
    }

    fun isValid() = validate()

    private fun validate(): Boolean {
        val isValidCSV = true
        return isValidCSV && validateForBank()
    }

    protected abstract fun createPayment(record: MutableList<String>): Transaction
    protected abstract fun createTransfer(record: MutableList<String>): Transaction
    protected abstract fun validateForBank(): Boolean

    protected inner class Positions(
        internal val ID: Int,
        internal val DATE: Int,
        internal val SOURCE: Int,
        internal val COUNTER: Int,
        internal val COUNTER_NAME: Int,
        internal val CURRENCY: Int,
        internal val AMOUNT: Int,
        internal val TYPE_CODE: Int,
        internal val TYPE: Int,
        internal val DESCRIPTION: Int
    ) {}
}