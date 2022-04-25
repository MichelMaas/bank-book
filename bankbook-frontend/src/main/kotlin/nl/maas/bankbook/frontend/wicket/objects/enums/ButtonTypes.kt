package nl.maas.bankbook.frontend.wicket.objects.enums

import de.agilecoders.wicket.core.markup.html.bootstrap.image.IconType
import de.agilecoders.wicket.extensions.markup.html.bootstrap.icon.FontAwesome5IconType
import nl.maas.bankbook.frontend.ContextProvider
import nl.maas.bankbook.frontend.wicket.caches.PropertiesCache
import nl.maas.bankbook.frontend.wicket.pages.*
import kotlin.reflect.KClass

@OptIn(ExperimentalStdlibApi::class)
enum class ButtonTypes(val pageClass: KClass<out BasePage>, val iconType: IconType) {


    YEAR_OVERVIEW(YearOverviewPage::class, FontAwesome5IconType.sticky_note_s),
    MONTH_OVERVIEW(MonthOverviewPage::class, FontAwesome5IconType.sticky_note_s),
    CATEGORIES(CategoriesPage::class, FontAwesome5IconType.cat_s),
    TRANSACTIONS(TransactionsPage::class, FontAwesome5IconType.cat_s),
    OPTIONS(OptionsPage::class, FontAwesome5IconType.cogs_s);

    fun label(): String {
        return ContextProvider.ctx.getBean(PropertiesCache::class.java).translator.translate(pageClass, "title")
    }
}