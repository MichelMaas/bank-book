package nl.maas.bankbook.frontend.wicket.tools

import nl.maas.bankbook.frontend.wicket.caches.PropertiesCache
import nl.maas.bankbook.frontend.wicket.pages.BasePage
import java.time.DayOfWeek
import kotlin.reflect.KClass

class TranslatedDayOfWeek private constructor(val propertiesCache: PropertiesCache) {

    companion object {
        fun translatedDays(cls: KClass<out BasePage>, propertiesCache: PropertiesCache) =
            TranslatedDayOfWeek(propertiesCache).getDays(cls)
    }

    fun getDays(cls: KClass<out BasePage>): Array<String> {
        return DayOfWeek.values().map { propertiesCache.translator.translate(cls, it.name) }.toTypedArray()
    }
}