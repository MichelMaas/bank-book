package nl.maas.bankbook.frontend.services

import nl.maas.bankbook.domain.Storable
import nl.maas.bankbook.frontend.translation.IBMTranslate
import nl.maas.bankbook.frontend.translation.Translations
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component


@Component
class TranslationService {

    fun translate(source: String, pretty: Boolean = true): String {
        val translations = Storable.Companion.load(Translations::class) ?: Translations()
        var translation = translations[source]
        if (translation.isNullOrBlank()) {
            translation = webTranslation(pretty, source)
            translations.putChained(source, translation).store()
        }
        return translation
    }

    private fun webTranslation(pretty: Boolean, source: String) = IBMTranslate().translate(
        if (!pretty) source else source.lowercase().replaceFirstChar { source[0].uppercase() }
            .replace("_", StringUtils.SPACE))

}