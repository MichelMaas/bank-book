package nl.maas.bankbook.frontend.services

import nl.maas.bankbook.frontend.translation.LibreTranslate
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component


@Component
class TranslationService {

    fun translate(source: String): String {
        return LibreTranslate().translate(source)
    }

    fun translatePerWord(words: String) =
        words.split(StringUtils.SPACE).map { translate(it) }.joinToString(StringUtils.SPACE)

}