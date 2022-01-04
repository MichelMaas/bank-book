package nl.maas.bankbook.frontend.wicket.pages

import nl.maas.fxanalyzer.frontend.wicket.panels.BarPanel
import org.apache.wicket.request.mapper.parameter.PageParameters
import java.math.BigDecimal

@ExperimentalStdlibApi
class ProfitDayPage(parameters: PageParameters) : BasePage(parameters) {

    override fun onInitialize() {
        super.onInitialize()
        val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
        addOrReplace(
            BarPanel<BigDecimal>(
                "graph",
                "Profit percentage per day of the week",
                modelCache.fxDataSet.salesPurchasesPerDay(days)
            )
        )
    }

}