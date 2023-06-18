package nl.maas.bankbook.frontend.wicket.objects

import nl.maas.bankbook.frontend.ContextProvider
import nl.maas.bankbook.frontend.services.DataManagementService
import nl.maas.bankbook.frontend.translation.CachingGoogleTranslator
import nl.maas.bankbook.frontend.wicket.panels.ImportPanel
import nl.maas.bankbook.frontend.wicket.panels.YearOverviewPanel
import nl.maas.bankbook.utils.FileUtils
import nl.maas.wicket.framework.objects.RiaPageProperties
import nl.maas.wicket.framework.objects.enums.NavbarOrientation

class BankbookRIAProperties : RiaPageProperties<DataManagementService>(
    "Bank book",
    if (ContextProvider.ctx.getBean(DataManagementService::class.java)
            .isEmpty()
    ) ImportPanel() else YearOverviewPanel(),
    ContextProvider.ctx.getBean(DataManagementService::class.java),
    ContextProvider.ctx.getBean(CachingGoogleTranslator::class.java),
    brandName = "Bankbook",
    iconPath = FileUtils.findFile("icon.png"),
    brandPath = FileUtils.findFile("brand.png"),
    navbarOrientation = NavbarOrientation.VERTICAL
) {
}