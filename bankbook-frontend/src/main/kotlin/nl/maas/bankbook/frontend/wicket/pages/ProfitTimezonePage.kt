package nl.maas.bankbook.frontend.wicket.pages

import nl.maas.fxanalyzer.domain.Transaction.Companion.Result.LOST
import nl.maas.fxanalyzer.domain.Transaction.Companion.Result.WON
import nl.maas.fxanalyzer.frontend.wicket.panels.BarPanel
import org.apache.wicket.request.mapper.parameter.PageParameters
import java.math.BigDecimal

@ExperimentalStdlibApi
class ProfitTimezonePage(parameters: PageParameters) : BasePage(parameters) {

    override fun onInitialize() {
        super.onInitialize()
        val timeZones = TimeZones.values().toList()
        addOrReplace(
            BarPanel<BigDecimal>(
                "graphWon",
                "title",
                mapOf(
                    Pair(
                        "AmountWonByTimezone",
                        modelCache.fxDataSet.resultPerTimeZone(WON)
                    ), Pair(
                        "AmountLostByTimezone",
                        modelCache.fxDataSet.resultPerTimeZone(LOST)
                    )
                )
            )
        )
    }

}