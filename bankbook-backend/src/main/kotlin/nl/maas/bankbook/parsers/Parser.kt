package nl.maas.bankbook.parsers

import nl.maas.bankbook.domain.IBAN
import nl.maas.bankbook.domain.Transaction
import java.io.File

interface Parser<T, M> {
    fun createTransactions(): List<Transaction>
    fun determineSourceAccount(metaInfo: M): IBAN
    fun createTransaction(record: T): Transaction
    fun isValid(): Boolean
    fun validate(): Boolean
    fun createPayment(record: T): Transaction
    fun createTransfer(record: T): Transaction

    companion object {
        fun parse(file: String, extension: String): Parser<*, *> {
            return parse(File(file), extension)
        }

        fun parse(file: File, extension: String): Parser<*, *> {
            return when (extension) {
                "xml" -> XMLParser.parse(file)
                else -> CSVParser.parse(file)

            }
        }
    }
}