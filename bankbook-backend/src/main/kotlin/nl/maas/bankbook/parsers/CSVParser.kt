package nl.maas.bankbook.parsers

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import nl.maas.bankbook.domain.IBAN
import nl.maas.bankbook.domain.Transaction
import nl.maas.bankbook.domain.enums.Banks
import nl.maas.bankbook.utils.CSVUtils
import org.apache.commons.lang3.NotImplementedException
import java.io.File

abstract class CSVParser private constructor(protected val map: Map<Int, MutableList<String>>) :
    Parser<List<String>, List<String>> {

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

        fun parse(file: File): CSVParser {
            val parsedFile = CSVUtils.parseFile(file)
            val bankName = findBankName(parsedFile)
            return when (Banks.valueOf(bankName)) {
                Banks.SNSB -> SNSBParser(parsedFile)
                else -> throw NotImplementedException("No parser yet implemented for bank ${bankName}")
            }

        }

        protected fun findBankName(map: Map<Int, MutableList<String>>): String {
            return CSVUtils.findBaseAccount(map).substring(4..7)
        }

    }

    override fun createTransactions(): List<Transaction> {
        return map.values.map { runBlocking { async { createTransaction(it) }.await() } }.distinctBy { it.id }
    }

    override fun determineSourceAccount(record: List<String>): IBAN {
        return IBAN(map.get(POSITIONS.SOURCE)!!.first())
    }

    override fun createTransaction(record: List<String>): Transaction {
        if (record[POSITIONS.COUNTER].isNullOrBlank()) {
            return createPayment(record)
        } else {
            return createTransfer(record)
        }
    }

    override fun isValid() = validate()

    override fun validate(): Boolean {
        val isValidCSV = true
        return isValidCSV && validateForBank()
    }

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