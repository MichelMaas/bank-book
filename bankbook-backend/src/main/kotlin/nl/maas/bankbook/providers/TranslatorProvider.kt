package nl.maas.bankbook.providers

import nl.maas.bankbook.utils.FileUtils
import java.io.File

open class TranslatorProvider {

    val translators: Map<String, Translator>

    init {
        val files = File(FileUtils.findFile("default.json")).parentFile.listFiles()
        translators =
            files.map { Translator(it.nameWithoutExtension) }.map { it.language to it }
                .toMap()
    }

    fun getTranslatorFor(language: String): Translator {
        return translators.get(language) ?: Translator("default")
    }

}