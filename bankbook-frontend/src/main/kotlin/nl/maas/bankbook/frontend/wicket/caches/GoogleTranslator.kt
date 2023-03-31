package nl.maas.bankbook.frontend.wicket.caches

import me.bush.translator.Language
import nl.maas.wicket.framework.services.Translator
import org.springframework.stereotype.Component
import java.util.*

@Component
class GoogleTranslator() : Translator {

    override val language: String
        get() = Locale.getDefault().language

    override fun translate(word: String): String {
        return me.bush.translator.Translator().translateBlocking(
            word,
            Language.valueOf(Locale.getDefault().getDisplayLanguage(Locale.ENGLISH).uppercase()),
            Language.ENGLISH
        ).translatedText.lowercase().replaceFirstChar { it.uppercase() }
    }

    override fun unTranslate(word: String): String {
        return me.bush.translator.Translator().translateBlocking(
            word,
            Language.ENGLISH,
            Language.valueOf(Locale.getDefault().getDisplayLanguage(Locale.ENGLISH).uppercase())
        ).translatedText
    }

}