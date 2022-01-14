package nl.maas.bankbook.frontend.wicket.pages

import nl.maas.bankbook.domain.Amount
import nl.maas.bankbook.frontend.wicket.components.DatePickerButton
import nl.maas.bankbook.frontend.wicket.components.DatePickerButton.Companion.PickerTypes.MONTH_YEAR
import nl.maas.bankbook.frontend.wicket.components.DynamicTableComponent
import nl.maas.bankbook.frontend.wicket.components.SingleDataViewPanel
import nl.maas.bankbook.frontend.wicket.objects.Tuple
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.list.ListItem
import org.apache.wicket.markup.html.list.ListView
import org.apache.wicket.request.mapper.parameter.PageParameters
import java.time.LocalDate
import java.time.Year


class MonthOverviewPage(parameters: PageParameters?) : BasePage(parameters) {

    override fun onBeforeRender() {
        super.onBeforeRender()
        makeUpDatePicker()
        makeUpLeftDataColumn()
        makeUpRightDataColumn()
        makeUpDataTable()
    }

    private fun makeUpDatePicker() {
        addOrReplace(object : DatePickerButton(
            "datePicker",
            localDate,
            MONTH_YEAR
        ) {
            override fun onDateChanged(target: AjaxRequestTarget, date: LocalDate) {
                super.onDateChanged(target, date)
                localDate = date
                target.add(this@MonthOverviewPage)
            }
        })
    }

    lateinit var localDate: LocalDate

    init {
        localDate = LocalDate.now()
    }

    private fun makeUpDataTable() {
        val tuples =
            modelCache.dataContainer.groupedTransactionsForMonth(Year.of(localDate.year), localDate.month)
                .map { Tuple(it.value) }
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
                modelCache.dataContainer.totalInMonth(Year.of(localDate.year), localDate.month).toString()
            ),
            Pair(
                propertiesCache.translator.translate(this::class, "totOut"),
                modelCache.dataContainer.totalOutMonth(Year.of(localDate.year), localDate.month).toString()
            )
        )
        addOrReplace(object : ListView<Pair<String, String>>("dataR", right) {
            override fun populateItem(item: ListItem<Pair<String, String>>) {
                item.addOrReplace(
                    SingleDataViewPanel(
                        "data",
                        item.modelObject
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
                    modelCache.dataContainer.totalInMonth(
                        Year.of(localDate.year), localDate.month
                    ).value.plus(
                        modelCache.dataContainer.totalOutMonth(
                            Year.of(localDate.year),
                            localDate.month
                        ).value
                    ),
                    modelCache.dataContainer.currencySymbol
                ).toString()
            )
        )

        addOrReplace(object : ListView<Pair<String, String>>("dataL", left) {
            override fun populateItem(item: ListItem<Pair<String, String>>) {
                item.addOrReplace(
                    SingleDataViewPanel(
                        "data",
                        item.modelObject
                    )
                )
            }

        })
    }

}