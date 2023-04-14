package nl.maas.bankbook.frontend.wicket.pages

import com.giffing.wicket.spring.boot.context.scan.WicketHomePage
import nl.maas.bankbook.frontend.wicket.caches.ModelCache
import nl.maas.bankbook.frontend.wicket.objects.BankbookRIAProperties
import nl.maas.bankbook.frontend.wicket.panels.FiltersPanel
import nl.maas.bankbook.frontend.wicket.panels.ImportPanel
import nl.maas.bankbook.frontend.wicket.panels.MonthOverviewPanel
import nl.maas.bankbook.frontend.wicket.panels.YearOverviewPanel
import nl.maas.wicket.framework.pages.RIAPage

@WicketHomePage
class MainPage : RIAPage<ModelCache>(BankbookRIAProperties()) {
    init {
        registerPanels(
            "Year Overview" to YearOverviewPanel(),
            "Month Overview" to MonthOverviewPanel(),
            "Categories" to FiltersPanel(),
            "Import" to ImportPanel()
        )
    }
}