package nl.maas.bankbook.frontend.wicket.pages

import nl.maas.fxanalyzer.frontend.wicket.panels.BarPanel
import org.apache.wicket.request.mapper.parameter.PageParameters

@ExperimentalStdlibApi
class EntryPreferencePage(parameters: PageParameters) : BasePage(parameters) {

    override fun onInitialize() {
        super.onInitialize()
        val profitOrLossPerEntryType = modelCache.fxDataSet.profitOrLossPerEntryType()
        addOrReplace(
            BarPanel("graph", "Entry preference", profitOrLossPerEntryType)
        )
    }

}