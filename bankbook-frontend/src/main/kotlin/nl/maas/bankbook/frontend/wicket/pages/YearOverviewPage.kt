package nl.maas.bankbook.frontend.wicket.pages

import com.giffing.wicket.spring.boot.context.scan.WicketHomePage
import nl.maas.bankbook.domain.Amount
import nl.maas.bankbook.frontend.wicket.components.DatePickerButton
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

@WicketHomePage
class YearOverviewPage : BasePage() {

    override fun onBeforeRender() {
        super.onBeforeRender()
        if (categories) {
            tuples = modelCache.dataContainer.groupedCategoriesFor(Year.of(modelCache.localDate.year))
        } else {
            tuples = modelCache.dataContainer.groupedReceiversFor(Year.of(modelCache.localDate.year))
        }
        makeUpLeftDataColumn()
        makeUpRightDataColumn()
        makeUpDataTable()
        makeUpDatePicker()
    }


    private var tuples: List<Tuple> = listOf()
    private var categories = true

    private fun makeUpDatePicker() {
        addOrReplace(object : DatePickerButton(
            "datePicker",
            modelCache.localDate,
            Companion.PickerTypes.YEAR_ONLY
        ) {
            override fun onDateChanged(target: AjaxRequestTarget, date: LocalDate) {
                super.onDateChanged(target, date)
                modelCache.localDate = date
                target.add(this@YearOverviewPage)
            }
        })
    }

    private fun makeUpDataTable() {
        val dynamicTableComponent = DynamicTableComponent("table", tuples.toMutableList())
        val switchLabel = Label("switchLabel", propertiesCache.translator.translate("Categories")).add(
            AttributeModifier(
                "for",
                "customSwitches"
            )
        )
        val switch = object : AjaxCheckBox("switch", Model.of(categories)) {

            override fun onInitialize() {
                super.onInitialize()
                markupId = "customSwitches"
            }

            override fun onUpdate(target: AjaxRequestTarget) {
                this@YearOverviewPage.categories = convertedInput
                target.add(this@YearOverviewPage)
            }
        }

        addOrReplace(switch, switchLabel, dynamicTableComponent)
    }

    private fun makeUpRightDataColumn() {
        val right = listOf(
            Pair(
                propertiesCache.translator.translate("Account"),
                modelCache.dataContainer.iban.toString()
            ),
            Pair(
                propertiesCache.translator.translate("totIn"),
                modelCache.dataContainer.totalIn(Year.of(modelCache.localDate.year)).toString()
            ),
            Pair(
                propertiesCache.translator.translate("totOut"),
                modelCache.dataContainer.totalOut(Year.of(modelCache.localDate.year)).toString()
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
                propertiesCache.translator.translate("totTrans"),
                modelCache.dataContainer.totalTransactionsFor(Year.of(modelCache.localDate.year)).toString()
            ),
            Pair(
                propertiesCache.translator.translate("result"),
                Amount(
                    modelCache.dataContainer.totalIn(Year.of(modelCache.localDate.year)).value.plus(
                        modelCache.dataContainer.totalOut(
                            Year.of(modelCache.localDate.year)
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