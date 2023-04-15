package nl.maas.bankbook.parsers

import nl.maas.bankbook.AbstractTest
import nl.maas.bankbook.domain.Amount
import nl.maas.bankbook.domain.IBAN
import nl.maas.bankbook.domain.Transfer
import org.junit.Assert
import org.junit.jupiter.api.Test

class SNSBParserTest : AbstractTest() {
    val instance = SNSBParser(parsedFile)

    @Test
    fun test() {
        val result = instance.createTransactions()
        val transfer = result.first { it.id.equals(20477741L) } as Transfer
        Assert.assertEquals(IBAN("NL36INGB0003445588"), transfer.counterAccount)
        Assert.assertEquals(Amount("119.00", "€").toString(), transfer.mutation.toString())
        println(transfer.toString())
    }
}