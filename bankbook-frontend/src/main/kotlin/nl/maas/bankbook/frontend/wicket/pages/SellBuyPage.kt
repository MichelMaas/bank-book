package nl.maas.bankbook.frontend.wicket.pages

import nl.maas.fxanalyzer.frontend.wicket.panels.PiePanel
import org.apache.wicket.request.mapper.parameter.PageParameters

class SellBuyPage(parameters: PageParameters) : BasePage(parameters) {

    override fun onInitialize() {
        super.onInitialize()
        val transactionsGroupedBy = modelCache.fxDataSet.transactionsGroupedBy("type")
        addOrReplace(
            PiePanel<Int>(
                "graph",
                "PurchasesVsSales",
                transactionsGroupedBy.keys.sorted().toList(),
                transactionsGroupedBy.keys.sorted().map { transactionsGroupedBy[it]!!.size })
        )
    }

}