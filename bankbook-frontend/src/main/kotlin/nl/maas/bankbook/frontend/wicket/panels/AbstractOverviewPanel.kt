package nl.maas.bankbook.frontend.wicket.panels

import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons
import nl.maas.bankbook.domain.Transaction
import nl.maas.bankbook.frontend.wicket.caches.ModelCache
import nl.maas.bankbook.frontend.wicket.tools.TransactionUtils
import nl.maas.wicket.framework.components.base.DynamicDataTable
import nl.maas.wicket.framework.components.base.DynamicPanel
import nl.maas.wicket.framework.components.base.KeyValueView
import nl.maas.wicket.framework.components.base.Switch
import nl.maas.wicket.framework.components.elemental.DatePickerButton
import nl.maas.wicket.framework.panels.RIAPanel
import nl.maas.wicket.framework.services.Translator
import org.apache.wicket.Component
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.model.CompoundPropertyModel
import org.apache.wicket.spring.injection.annot.SpringBean
import java.math.BigDecimal
import java.time.LocalDate

abstract class AbstractOverviewPanel(private val period: ModelCache.PERIOD = ModelCache.PERIOD.YEAR) :
    RIAPanel() {

    @SpringBean
    private lateinit var modelCache: ModelCache

    @SpringBean
    private lateinit var translator: Translator

    private var categorized = true

    @SpringBean
    private lateinit var transactionUtils: TransactionUtils

    override fun onBeforeRender() {
        super.onBeforeRender()
        addOrReplace(createPanel())
    }

    private fun createPanel(): Component {
        val transactions = modelCache.transactionsForPeriod(modelCache.date, period)
        return DynamicPanel("panel").addRows(
            "Year" to intArrayOf(12),
            "Summary" to intArrayOf(6, 6),
            "Toggle" to intArrayOf(2, 10),
            "Table" to intArrayOf(12)
        ).addOrReplaceComponentToColumn("Year", 0, createYearBar())
            .addOrReplaceComponentToColumn("Summary", 0, createSummaryLeft(transactions))
            .addOrReplaceComponentToColumn("Summary", 1, createSummaryRight(transactions))
            .addOrReplaceComponentToColumn("Toggle", 0, createToggle())
            .addOrReplaceComponentToColumn("Table", 0, createTable(transactions))
    }

    private fun createSummaryRight(transactions: List<Transaction>): Component {
        return KeyValueView(
            DynamicPanel.ROW_CONTENT_ID, translator,
            "Total transactions" to transactions.size,
            "Result" to transactions.sumOf { it.mutation.value }
        )
    }

    private fun createSummaryLeft(transactions: List<Transaction>): Component {
        return KeyValueView(
            DynamicPanel.ROW_CONTENT_ID, translator,
            "Account" to modelCache.account,
            "Total in" to transactions.filter { it.mutation.value > BigDecimal.ZERO }
                .sumOf { it.mutation.value },
            "Total out" to transactions.filter { it.mutation.value < BigDecimal.ZERO }
                .sumOf { it.mutation.value }
        )
    }

    private fun createToggle(): Component {
        val type = when (categorized) {
            true -> Buttons.Type.Primary
            else -> Buttons.Type.Outline_Primary
        }
        val switch =
            object : Switch(
                DynamicPanel.ROW_CONTENT_ID,
                CompoundPropertyModel.of(categorized),
                translator.translate("Toggle categories")
            ) {
                override fun onUpdate(target: AjaxRequestTarget, modelObject: Boolean) {
                    categorized = modelObject
                    reload(target)
                }
            }
        return switch
    }


    private fun createTable(transactions: List<Transaction>): Component {
        val tuples = transactionUtils.transactionsToTuples(transactions, categorized, period)
        return DynamicDataTable.get(
            DynamicPanel.ROW_CONTENT_ID,
            tuples,
            15,
            translator
        ).sm().striped().invertHeader()
    }

    private fun createYearBar(): Component {
        val type = when (period) {
            ModelCache.PERIOD.MONTH -> DatePickerButton.Companion.PickerTypes.MONTH_YEAR
            else -> DatePickerButton.Companion.PickerTypes.YEAR_ONLY
        }
        return object : DatePickerButton(DynamicPanel.ROW_CONTENT_ID, modelCache.date, type, translator.language) {
            override fun onDateChanged(target: AjaxRequestTarget, date: LocalDate) {
                super.onDateChanged(target, date)
                modelCache.date = date
                reload(target)
            }
        }
    }
}