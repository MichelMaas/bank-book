package nl.maas.bankbook.frontend.wicket.pages

import nl.maas.fxanalyzer.frontend.wicket.panels.LinePanel
import org.apache.wicket.request.mapper.parameter.PageParameters

class WinLossPage(parameters: PageParameters) : BasePage(parameters) {

    override fun onInitialize() {
        super.onInitialize()
        val profitPerMonth = modelCache.fxDataSet.profitPerMonth(2021)
        val linePanel = LinePanel<Double>("graph", profitPerMonth)
        addOrReplace(linePanel)
    }

}