package nl.maas.bankbook.frontend.wicket.pages

import com.giffing.wicket.spring.boot.context.scan.WicketHomePage
import nl.maas.bankbook.frontend.wicket.caches.ModelCache
import nl.maas.bankbook.frontend.wicket.objects.BankbookRIAProperties
import nl.maas.bankbook.frontend.wicket.panels.*
import nl.maas.bankbook.utils.FileUtils
import nl.maas.wicket.framework.pages.RIAPage
import org.apache.wicket.markup.head.CssHeaderItem
import org.apache.wicket.markup.head.IHeaderResponse
import org.apache.wicket.markup.head.MetaDataHeaderItem
import org.apache.wicket.resource.FileSystemResourceReference


@WicketHomePage
class MainPage : RIAPage<ModelCache>(BankbookRIAProperties()) {
    val faviconPath = FileUtils.getFileFromAppFolder("bankbook.ico").toPath()

    val faviconRef = FileSystemResourceReference(
        "bankbook.ico",
        faviconPath
    )

    init {
        registerPanels(
            "Year Overview" to YearOverviewPanel(),
            "Month Overview" to MonthOverviewPanel(),
            "Categories" to FiltersPanel(),
            "Import" to ImportPanel(),
            "Settings" to SettingsPanel()
        )
    }

    override fun renderHead(response: IHeaderResponse) {
        super.renderHead(response)
        // Framework loader uses display:flex, which overrides the browser's
        // default hidden rule. Keep a deactivated overlay out of the way.
        response.render(CssHeaderItem.forCSS(".overlay[hidden] { display: none !important; }", "bankbook-hidden-loader"))

        val faviconUrl = urlFor(faviconRef, null).toString() + "?v=bankbook-2"

        println("Favicon URL = $faviconUrl")
        response.render(
            MetaDataHeaderItem
                .forLinkTag("icon", faviconUrl)
                .addTagAttribute("type", "image/vnd.microsoft.icon")
                .addTagAttribute("sizes", "any")
        )
        
        response.render(
            MetaDataHeaderItem
                .forLinkTag("icon", urlFor(faviconRef, null).toString())
                .addTagAttribute("type", "image/x-icon")
        )
    }

}