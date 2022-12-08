package nl.maas.bankbook.providers

import nl.maas.bankbook.utils.FileUtils
import nl.maas.bankbook.utils.JsonUtils

class Translator(language: String) : java.io.Serializable {

    val locale: String
    val language: String
    val translate: Map<String, String>

    init {
        translate =
            (JsonUtils.load(
                FileUtils.findFile("$language.json"),
                Map::class.java
            ) as Map<String, String>?)?.map { it.key.uppercase() to it.value }?.toMap() ?: mapOf()
        this.language = translate("code")!!
        this.locale = translate("locale")!!
    }

    fun translate(key: String): String {
        return translate.get(key.uppercase()) ?: key
    }

    fun untranslate(value: String): String {
        return translate.entries.firstOrNull { it.value.equals(value) }?.key ?: value
    }
}