package nl.maas.bankbook.frontend.wicket.pages

import com.giffing.wicket.spring.boot.context.scan.WicketHomePage
import nl.maas.bankbook.domain.Amount
import nl.maas.bankbook.frontend.wicket.components.DatePickerButton
import nl.maas.bankbook.frontend.wicket.components.DynamicTableComponent
import nl.maas.bankbook.frontend.wicket.components.SingleDataViewPanel
import nl.maas.bankbook.frontend.wicket.objects.Tuple
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.list.ListItem
import org.apache.wicket.markup.html.list.ListView
import org.apache.wicket.request.mapper.parameter.PageParameters
import java.time.LocalDate
import java.time.Year

@WicketHomePage
class YearOverviewPage(parameters: PageParameters?) : BasePage(parameters) {

    override fun onBeforeRender() {
        super.onBeforeRender()
        makeUpLeftDataColumn()
        makeUpRightDataColumn()
        makeUpDataTable()
        makeUpDatePicker()
    }

    private var localDate: LocalDate

    init {
        localDate = LocalDate.now()
    }

    private fun makeUpDatePicker() {
        addOrReplace(object : DatePickerButton(
            "datePicker",
            localDate,
            Companion.PickerTypes.YEAR_ONLY
        ) {
            override fun onDateChanged(target: AjaxRequestTarget, date: LocalDate) {
                super.onDateChanged(target, date)
                localDate = date
                target.add(this@YearOverviewPage)
            }
        })
    }

    private fun makeUpDataTable() {
        val tuples =
            modelCache.dataContainer.groupedTransactionsForYear(Year.of(localDate.year)).map { Tuple(it.value) }
        addOrReplace(DynamicTableComponent("table", tuples.toMutableList()))
    }

    private fun makeUpRightDataColumn() {
        val right = listOf(
            Pair(
                propertiesCache.translator.translate(this::class, "Account"),
                modelCache.dataContainer.iban.toString()
            ),
            Pair(
                propertiesCache.translator.translate(this::class, "totIn"),
                modelCache.dataContainer.totalInYear(Year.of(localDate.year)).toString()
            ),
            Pair(
                propertiesCache.translator.translate(this::class, "totOut"),
                modelCache.dataContainer.totalOutYear(Year.of(localDate.year)).toString()
            )
        )
        addOrReplace(object : ListView<Pair<String, String>>("dataR", right) {
            override fun populateItem(p0: ListItem<Pair<String, String>>?) {
                p0!!.addOrReplace(
                    SingleDataViewPanel(
                        "data",
                        p0.modelObject
                    )
                )
            }

        })
    }

    private fun makeUpLeftDataColumn() {
        val left = listOf(
            Pair(
                propertiesCache.translator.translate(this::class, "totTrans"),
                modelCache.dataContainer.transactions.size.toString()
            ),
            Pair(
                propertiesCache.translator.translate(this::class, "result"),
                Amount(
                    modelCache.dataContainer.totalInYear(Year.of(localDate.year)).value.plus(
                        modelCache.dataContainer.totalOutYear(
                            Year.of(localDate.year)
                        ).value
                    ),
                    modelCache.dataContainer.currencySymbol
                ).toString()
            )
        )

        addOrReplace(object : ListView<Pair<String, String>>("dataL", left) {
            override fun populateItem(p0: ListItem<Pair<String, String>>?) {
                p0!!.addOrReplace(
                    SingleDataViewPanel(
                        "data",
                        p0.modelObject
                    )
                )
            }

        })
    }

}