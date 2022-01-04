package nl.maas.bankbook.frontend.wicket.pages

import nl.maas.fxanalyzer.frontend.wicket.panels.CalendarPanel
import org.apache.wicket.request.mapper.parameter.PageParameters
import java.time.LocalDate

@ExperimentalStdlibApi
class ProfitCalendarPage(parameters: PageParameters) : BasePage(parameters) {

    override fun onInitialize() {
        super.onInitialize()
        val month = LocalDate.now()
        addOrReplace(
            object : CalendarPanel(
                "calendar",
                month,
                modelCache.fxDataSet.profitOrLossPerDay(month)
                    .map { it.key to "${modelCache.fxDataSet.currency.symbol.takeLast(1)} ${it.value}" }.toMap()
            ) {
                override fun onBackClicked(newMonth: LocalDate) {
                    replaceValue(
                        modelCache.fxDataSet.profitOrLossPerDay(newMonth)
                            .map { it.key to "${modelCache.fxDataSet.currency.symbol.takeLast(1)} ${it.value}" }.toMap()
                    )
                }

                override fun onForwardClicked(newMonth: LocalDate) {
                    replaceValue(
                        modelCache.fxDataSet.profitOrLossPerDay(newMonth)
                            .map { it.key to "${modelCache.fxDataSet.currency.symbol.takeLast(1)} ${it.value}" }.toMap()
                    )
                }


            }
        )
    }

}