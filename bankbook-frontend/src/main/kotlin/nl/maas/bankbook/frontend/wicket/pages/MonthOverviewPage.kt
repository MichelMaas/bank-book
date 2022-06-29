package nl.maas.bankbook.frontend.wicket.pages

import nl.maas.bankbook.domain.Amount
import nl.maas.bankbook.frontend.wicket.components.DatePickerButton
import nl.maas.bankbook.frontend.wicket.components.DatePickerButton.Companion.PickerTypes.MONTH_YEAR
import nl.maas.bankbook.frontend.wicket.components.DynamicTableComponent
import nl.maas.bankbook.frontend.wicket.components.SingleDataViewPanel
import nl.maas.bankbook.frontend.wicket.objects.Tuple
import org.apache.wicket.AttributeModifier
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.markup.html.list.ListItem
import org.apache.wicket.markup.html.list.ListView
import org.apache.wicket.model.Model
import java.time.LocalDate
import java.time.Year


class MonthOverviewPage : BasePage() {

    override fun onBeforeRender() {
        super.onBeforeRender()
        if (categories) {
            tuples = modelCache.dataContainer.groupedCategoriesFor(
                Year.of(modelCache.localDate.year),
                modelCache.localDate.month
            )
        } else {
            tuples = modelCache.dataContainer.groupedReceiversFor(
                Year.of(modelCache.localDate.year),
                modelCache.localDate.month
            )
        }
        makeUpDatePicker()
        makeUpLeftDataColumn()
        makeUpRightDataColumn()
        makeUpDataTable()
    }

    private fun makeUpDatePicker() {
        addOrReplace(object : DatePickerButton(
            "datePicker",
            modelCache.localDate,
            MONTH_YEAR
        ) {
            override fun onDateChanged(target: AjaxRequestTarget, date: LocalDate) {
                super.onDateChanged(target, date)
                modelCache.localDate = date
                target.add(this@MonthOverviewPage)
            }
        })
    }

    var categories = true
    var tuples: List<Tuple> = listOf()


    private fun makeUpDataTable() {
        val dynamicTableComponent = DynamicTableComponent("table", tuples.toMutableList())
        val switchLabel = Label("switchLabel", "Categories").add(AttributeModifier("for", "customSwitches"))
        val switch = object : AjaxCheckBox("switch", Model.of(categories)) {

            override fun onInitialize() {
                super.onInitialize()
                markupId = "customSwitches"
            }

            override fun onUpdate(target: AjaxRequestTarget) {
                this@MonthOverviewPage.categories = convertedInput
                target.add(this@MonthOverviewPage)
            }
        }
        addOrReplace(switch, switchLabel, dynamicTableComponent)
    }

    private fun makeUpRightDataColumn() {
        val right = listOf(
            Pair(
                propertiesCache.translator.translate(this::class, "Account"),
                modelCache.dataContainer.iban.toString()
            ),
            Pair(
                propertiesCache.translator.translate(this::class, "totIn"),
                modelCache.dataContainer.totalIn(Year.of(modelCache.localDate.year), modelCache.localDate.month)
                    .toString()
            ),
            Pair(
                propertiesCache.translator.translate(this::class, "totOut"),
                modelCache.dataContainer.totalOut(Year.of(modelCache.localDate.year), modelCache.localDate.month)
                    .toString()
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
                modelCache.dataContainer.totalTransactionsFor(
                    Year.of(modelCache.localDate.year),
                    modelCache.localDate.month
                ).toString()
            ),
            Pair(
                propertiesCache.translator.translate(this::class, "result"),
                Amount(
                    modelCache.dataContainer.totalIn(
                        Year.of(modelCache.localDate.year), modelCache.localDate.month
                    ).value.plus(
                        modelCache.dataContainer.totalOut(
                            Year.of(modelCache.localDate.year),
                            modelCache.localDate.month
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