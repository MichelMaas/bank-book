package nl.maas.bankbook.frontend.wicket.objects

import nl.maas.bankbook.frontend.wicket.pages.BasePage
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses

data class I10N(
    val languages: List<Language>
) {
    fun <T : KClass<out BasePage>> translate(page: T, label: String, languageCode: String): String {
        return languages.first { languageCode.equals(it.code) }.pages.filter {
            page.simpleName.equals(it.name)
        }.flatMap { specific -> specific.labels.entries }
            .plus(languages.first { languageCode.equals(it.code) }.pages.filter {
                page.allSuperclasses.map { it.simpleName }.contains(it.name)
            }.flatMap { it.labels.entries }).distinctBy { it.key }
            .find { label.uppercase().equals(it.key.uppercase()) }?.value ?: label
    }
}