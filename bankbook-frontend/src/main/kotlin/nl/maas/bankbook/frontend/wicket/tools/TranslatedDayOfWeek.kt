package nl.maas.bankbook.frontend.wicket.tools

import nl.maas.wicket.framework.pages.BasePage
import nl.maas.wicket.framework.services.Translator
import nl.maas.wicket.framework.services.TranslatorPlaceHolder
import java.time.DayOfWeek
import kotlin.reflect.KClass

class TranslatedDayOfWeek private constructor(val translator: Translator) {

    companion object {
        fun translatedDays(cls: KClass<out BasePage<*>>, translator: Translator = TranslatorPlaceHolder()) =
            TranslatedDayOfWeek(translator).getDays(cls)
    }

    fun getDays(cls: KClass<out BasePage<*>>): Array<String> {
        return DayOfWeek.values().map { translator.translate(it.name) }.toTypedArray()
    }
}