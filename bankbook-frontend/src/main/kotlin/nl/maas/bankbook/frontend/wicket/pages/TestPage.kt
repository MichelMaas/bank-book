package nl.maas.bankbook.frontend.wicket.pages

import nl.maas.fxanalyzer.frontend.wicket.components.SingleDataViewPanel
import org.apache.wicket.request.mapper.parameter.PageParameters

class TestPage(parameters: PageParameters?) : BasePage(parameters) {
    override fun onBeforeRender() {
        super.onBeforeRender()

        val profitPerMonth = modelCache.fxDataSet.profitPerMonth(2021)
        val linePanel = SingleDataViewPanel("panel", Pair("Worth", modelCache.fxDataSet.worth().toString()))
        addOrReplace(linePanel)
    }
}