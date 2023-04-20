package nl.maas.bankbook.frontend.wicket.objects

import nl.maas.bankbook.frontend.ContextProvider
import nl.maas.bankbook.frontend.translation.CachingGoogleTranslator
import nl.maas.bankbook.frontend.wicket.caches.ModelCache
import nl.maas.bankbook.frontend.wicket.panels.ImportPanel
import nl.maas.bankbook.frontend.wicket.panels.YearOverviewPanel
import nl.maas.bankbook.utils.FileUtils
import nl.maas.wicket.framework.objects.RiaPageProperties
import nl.maas.wicket.framework.objects.enums.NavbarOrientation

class BankbookRIAProperties : RiaPageProperties<ModelCache>(
    "Bank book",
    if (ContextProvider.ctx.getBean(ModelCache::class.java).isEmpty()) ImportPanel() else YearOverviewPanel(),
    ContextProvider.ctx.getBean(ModelCache::class.java),
    ContextProvider.ctx.getBean(CachingGoogleTranslator::class.java),
    brandName = "Bankbook",
    iconPath = FileUtils.findFile("icon.png"),
    brandPath = FileUtils.findFile("brand.png"),
    navbarOrientation = NavbarOrientation.VERTICAL
) {
}