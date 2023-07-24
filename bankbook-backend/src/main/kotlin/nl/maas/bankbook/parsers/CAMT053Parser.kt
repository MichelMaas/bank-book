package nl.maas.bankbook.parsers

import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.Unmarshaller
import nl.maas.bankbook.domain.*
import nl.maas.bankbook.domain.enums.MutationTypes
import nl.maas.bankbook.generated.AcctType
import nl.maas.bankbook.generated.DocumentType
import nl.maas.bankbook.generated.NtryType
import org.springframework.stereotype.Component
import java.io.File
import java.util.*
import javax.xml.XMLConstants
import javax.xml.validation.SchemaFactory

@Component
class CAMT053Parser(val file: File) : XMLParser<NtryType, AcctType>() {

    private var document: DocumentType? = null
    private fun parse(file: File) {
        document = createUnmarshaller().unmarshal(file) as DocumentType
    }

    private fun createUnmarshaller(): Unmarshaller {
        val schema = SchemaFactory.newInstance(XMLConstants.ACCESS_EXTERNAL_SCHEMA)
            .newSchema(this::class.java.getResource("/tmp/camt.053.001.02.xsd"))
        val context = JAXBContext.newInstance(DocumentType::class.java)
        val unmarshaller = context.createUnmarshaller()
        unmarshaller.schema = schema
        return unmarshaller
    }

    override fun determineSourceAccount(records: AcctType): IBAN {
        return IBAN(records.id.iban)
    }

    override fun createTransactions(): List<Transaction> {
        parse(file)
        return document!!.bkToCstmrStmt.stmt.flatMap { stmt -> stmt.ntry.map { createTransaction(it) } }
    }

    override fun isValid(): Boolean {
        try {
            createUnmarshaller().unmarshal(file)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    override fun validate(): Boolean {
        return isValid()
    }

    override fun createTransfer(record: ReportEntry2): Transaction {
        return Transfer(
            record.ntryRef.toLong(),
            record.bookgDt.dt.toGregorianCalendar().toZonedDateTime().toLocalDate(),
            determineSourceAccount(document!!.bkToCstmrStmt.stmt.first()),
            IBAN(record.ntryDtls.first().txDtls.first().rltdPties.cdtrAcct.id.iban),
            record.ntryDtls.first().txDtls.first().rltdPties.cdtr.nm,
            Currency.getInstance(record.amt.ccy),
            Amount(record.amt.value, Currency.getInstance(record.amt.ccy).symbol),
            MutationTypes.byCode(record.bkTxCd.prtry.cd.toInt()),
            record.ntryDtls.first().txDtls.first().rmtInf.ustrd.joinToString(",")
        )
    }

    override fun createPayment(record: ReportEntry2): Transaction {
        return Payment(
            record.ntryRef.toLong(),
            record.bookgDt.dt.toGregorianCalendar().toZonedDateTime().toLocalDate(),
            determineSourceAccount(document!!.bkToCstmrStmt.stmt.first()),
            Currency.getInstance(record.amt.ccy),
            Amount(record.amt.value, Currency.getInstance(record.amt.ccy).symbol),
            MutationTypes.byCode(record.bkTxCd.prtry.cd.toInt()),
            record.ntryDtls.first().txDtls.first().rmtInf.ustrd.joinToString(",")
        )
    }

    override fun createTransaction(record: ReportEntry2): Transaction {
        return if (record.ntryDtls.first().txDtls.first().rltdAgts != null) {
            createTransfer(record)
        } else {
            createPayment(record)
        }
    }

}