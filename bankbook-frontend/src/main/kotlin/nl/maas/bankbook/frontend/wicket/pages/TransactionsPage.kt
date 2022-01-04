package nl.maas.bankbook.frontend.wicket.pages

import nl.maas.fxanalyzer.frontend.wicket.panels.TransactionsPanel
import org.apache.wicket.model.Model
import org.apache.wicket.request.mapper.parameter.PageParameters

class TransactionsPage(parameters: PageParameters) : BasePage(parameters) {

    override fun onInitialize() {
        super.onInitialize()
        add(TransactionsPanel("transactionsPanel", Model.of(modelCache.fxDataSet)))
    }
}