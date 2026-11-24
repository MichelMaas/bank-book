package nl.maas.bankbook.frontend.translation

import me.bush.translator.Language
import nl.maas.bankbook.domain.IterativeStorable
import nl.maas.wicket.framework.services.Translator
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Value
import java.util.*

@Component
class CachingGoogleTranslator(
    @Value("\${bankbook.translation.remote-enabled:false}") private val remoteEnabled: Boolean = false
) : Translator {

    private var translations = IterativeStorable.load(Translation::class)

    override val language: String
        get() = Locale.getDefault().language

    override fun translate(word: String): String {
        if (word.isNullOrBlank()) {
            return StringUtils.EMPTY
        }
        if (word.filter { it.isLetterOrDigit() }.toDoubleOrNull() != null) {
            return word
        }
        translations.firstOrNull { it.language == language && it.original == word }?.let { return it.translation }
        if (!remoteEnabled || language == "en") return word
        try {
            val translation = me.bush.translator.Translator().translateBlocking(
                word,
                Language.valueOf(Locale.getDefault().getDisplayLanguage(Locale.ENGLISH).uppercase()),
                Language.ENGLISH
            ).translatedText.lowercase().replaceFirstChar { it.uppercase() }
            updateTranslations(word, translation)
        } catch (ex: Exception) {
            // Translation availability must never make the banking UI unavailable.
            return word
        }
        return translations.first { it.language.equals(language) && it.original.equals(word) }.translation
    }

    fun updateTranslations(word: String, translation: String) {
        translations = IterativeStorable.storeAll(listOf(Translation(language, word, translation)))
    }

    fun translations() = translations

    override fun unTranslate(word: String): String {
        if (word.isNullOrBlank()) {
            return StringUtils.EMPTY
        }
        return translations.firstOrNull { language.equals(it.language) && word.equals(it.translation) }?.original ?: word
    }

}