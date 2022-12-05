package nl.maas.bankbook.providers

import org.junit.Assert
import org.junit.jupiter.api.Test

internal class TranslatorProviderTest {

    val instance = TranslatorProvider()

    @Test
    fun getTranslatorForEn() {
        getTranslator("en", "English")
        getTranslator("nl", "Nederlands")
        getTranslator("bl", "English")
    }

    private fun getTranslator(code: String, name: String) {
        val result = instance.getTranslatorFor(code)
        Assert.assertNotNull(result)
        Assert.assertEquals(name, result.translate("name"))
    }
}