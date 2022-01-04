package nl.maas.bankbook.domain

import nl.maas.bankbook.AbstractTest
import nl.maas.bankbook.utils.CSVUtils
import org.junit.Assert
import org.junit.Test

class TestCSVUtil : AbstractTest() {

    @Test
    fun test() {
        val findSource = CSVUtils.findBaseAccount(parsedFile)
        Assert.assertEquals("NL70SNSB0772821372", findSource)
    }


}