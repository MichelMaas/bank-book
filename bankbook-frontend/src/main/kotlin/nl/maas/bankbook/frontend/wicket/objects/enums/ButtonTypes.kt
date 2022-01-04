package nl.maas.bankbook.frontend.wicket.objects.enums

import de.agilecoders.wicket.core.markup.html.bootstrap.image.IconType
import de.agilecoders.wicket.extensions.markup.html.bootstrap.icon.FontAwesome5IconType
import nl.maas.fxanalyzer.frontend.ContextProvider
import nl.maas.fxanalyzer.frontend.wicket.caches.PropertiesCache
import nl.maas.fxanalyzer.frontend.wicket.pages.*
import kotlin.reflect.KClass

@OptIn(ExperimentalStdlibApi::class)
enum class ButtonTypes(val pageClass: KClass<out BasePage>, val iconType: IconType) {


    OVERVIEW(OverviewPage::class, FontAwesome5IconType.sticky_note_s),
    ProfitCalendar(ProfitCalendarPage::class, FontAwesome5IconType.calendar_s),
    ProfitPerDay(ProfitDayPage::class, FontAwesome5IconType.chart_bar_s),
    SellBuy(SellBuyPage::class, FontAwesome5IconType.chart_pie_s),
    WinLoss(WinLossPage::class, FontAwesome5IconType.chart_line_s),
    EntryPreference(EntryPreferencePage::class, FontAwesome5IconType.chart_bar_s),
    TimeZones(ProfitTimezonePage::class, FontAwesome5IconType.times_circle_s),
    Import(ImportPage::class, FontAwesome5IconType.file_import_s),
    Options(OptionsPage::class, FontAwesome5IconType.cogs_s),

    //    Transactions(TransactionsPage::class, FontAwesome5IconType.coins_s),
    TEST(TestPage::class, FontAwesome5IconType.cogs_s);

    fun label(): String {
        return ContextProvider.ctx.getBean(PropertiesCache::class.java).translator.translate(pageClass, "title")
    }
}