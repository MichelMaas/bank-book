package nl.maas.bankbook.frontend.wicket.objects.enums

import de.agilecoders.wicket.core.markup.html.bootstrap.image.IconType
import de.agilecoders.wicket.extensions.markup.html.bootstrap.icon.FontAwesome5IconType
import nl.maas.bankbook.frontend.ContextProvider
import nl.maas.bankbook.frontend.translation.CachingGoogleTranslator
import nl.maas.bankbook.frontend.wicket.caches.ModelCache
import nl.maas.bankbook.frontend.wicket.pages.OverviewPage
import nl.maas.wicket.framework.components.elemental.BaseNavbarButton
import nl.maas.wicket.framework.objects.enums.ButtonType
import nl.maas.wicket.framework.pages.BasePage
import org.apache.commons.lang3.StringUtils
import kotlin.reflect.KClass

@OptIn(ExperimentalStdlibApi::class)
enum class ButtonTypes(
    override val pageClass: KClass<out BasePage<*>>,
    override val iconType: IconType,
    vararg val params: Pair<String, Any>
) : ButtonType {
    YEAR_OVERVIEW(OverviewPage::class, FontAwesome5IconType.calendar_alt_s, "period" to ModelCache.PERIOD.YEAR),
    MONTH_OVERVIEW(OverviewPage::class, FontAwesome5IconType.calendar_alt_s, "period" to ModelCache.PERIOD.MONTH);

    override val label
        get() = ContextProvider.ctx.getBean(CachingGoogleTranslator::class.java)
            .translate(name.replace("_", StringUtils.SPACE))

    fun toNavBarButton(): BaseNavbarButton = BaseNavbarButton(this, *params)
}