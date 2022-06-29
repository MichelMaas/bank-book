package nl.maas.bankbook.frontend.wicket.objects

import nl.maas.bankbook.frontend.wicket.pages.BasePage
import kotlin.reflect.KClass

data class I10N(
    val languages: List<Language>
) : java.io.Serializable {
    fun <T : KClass<out BasePage>> translate(page: T, label: String, languageCode: String): String {
        return findPage(page, languageCode, label).labels.entries.firstOrNull {
            it.key.equals(
                label,
                true
            )
        }?.value ?: label
    }

    private fun <T : KClass<out BasePage>> findPage(page: T, languageCode: String, label: String): Page {
        val language = languages.first { languageCode.equals(it.code) }
        return (language.pages.firstOrNull {
            it.labels.keys.any { k ->
                k.equals(
                    label,
                    true
                )
            }
                    && it.name.equals(page.simpleName)
        } ?: language.pages.first { it.name.equals(BasePage::class.simpleName) })
    }

    fun <T : KClass<out BasePage>> untranslate(page: T, label: String, languageCode: String): String {
        return findPage(page, languageCode, label).labels.entries.firstOrNull {
            it.value.equals(
                label,
                true
            )
        }?.key ?: label
    }
}