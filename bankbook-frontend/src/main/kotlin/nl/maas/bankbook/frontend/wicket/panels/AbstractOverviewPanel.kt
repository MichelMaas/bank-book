package nl.maas.bankbook.frontend.wicket.panels

import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons
import kotlinx.coroutines.runBlocking
import nl.maas.bankbook.domain.Transaction
import nl.maas.bankbook.frontend.ContextProvider
import nl.maas.bankbook.frontend.wicket.caches.ModelCache
import nl.maas.bankbook.frontend.wicket.config.BootstrapProperties
import nl.maas.bankbook.frontend.wicket.tools.TupleUtils
import nl.maas.wicket.framework.components.base.DynamicDataTable
import nl.maas.wicket.framework.components.base.DynamicPanel
import nl.maas.wicket.framework.components.base.DynamicPanel.Companion.ROW_CONTENT_ID
import nl.maas.wicket.framework.components.base.KeyValueView
import nl.maas.wicket.framework.components.base.Switch
import nl.maas.wicket.framework.components.charts.PieChart
import nl.maas.wicket.framework.components.charts.data.BarchartData
import nl.maas.wicket.framework.components.charts.data.PieChartData
import nl.maas.wicket.framework.components.elemental.DatePickerButton
import nl.maas.wicket.framework.panels.RIAPanel
import nl.maas.wicket.framework.services.Translator
import org.apache.wicket.Component
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.model.CompoundPropertyModel
import org.apache.wicket.spring.injection.annot.SpringBean
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

abstract class AbstractOverviewPanel(private val period: ModelCache.PERIOD = ModelCache.PERIOD.YEAR) :
    RIAPanel() {
    private val creationStart = LocalDateTime.now()
    private lateinit var renderStart: LocalDateTime

    @SpringBean
    private lateinit var modelCache: ModelCache

    @SpringBean
    private lateinit var translator: Translator

    private var categorized = true

    @SpringBean
    private lateinit var tupleUtils: TupleUtils

    @Transient
    private val properties = ContextProvider.ctx.getBean(BootstrapProperties::class.java)

    override fun onInitialize() {
        super.onInitialize()
        println(
            "Initializing ${this::class.simpleName} took ${
                Duration.between(creationStart, LocalDateTime.now())
            }"
        )
    }

    override fun onBeforeRender() {
        super.onBeforeRender()
        renderStart = LocalDateTime.now()
        addOrReplace(createPanel())
    }

    override fun onAfterRender() {
        super.onAfterRender()
        val renderEnd = LocalDateTime.now()
        println("Rendering ${this::class.simpleName} took ${Duration.between(renderStart, renderEnd).toString()}")
    }

    private fun createPanel(): Component {
        val transactions = modelCache.transactionsForPeriod(modelCache.date, period)
        return DynamicPanel("panel").addRows(
            "Year" to intArrayOf(12),
            "Summary" to intArrayOf(6, 6),
            "Toggle" to intArrayOf(2, 10),
            "Table" to intArrayOf(12),
            "Graphs" to intArrayOf(6, 6)
        ).addOrReplaceComponentToColumn("Year", 0, createYearBar())
            .addOrReplaceComponentToColumn("Summary", 0, createSummaryLeft(transactions))
            .addOrReplaceComponentToColumn("Summary", 1, createSummaryRight(transactions))
            .addOrReplaceComponentToColumn("Toggle", 0, createToggle())
            .addOrReplaceComponentToColumn("Table", 0, createTable(transactions))
            .addOrReplaceComponentsToRow("Graphs", createOutChart(transactions), createInChart(transactions))
    }

    private fun createInChart(transactions: List<Transaction>): Component {
        return PieChart(
            ROW_CONTENT_ID,
            "Income",
            createPieData(transactions.filter { it.mutation.value > BigDecimal.ZERO }),
            translator,
            properties.theme
        )
    }

    private fun createBarData(transactions: List<Transaction>): BarchartData<BigDecimal> {
        val barchartData = BarchartData<BigDecimal>()
        val groupBy = transactions.groupBy { it.category }
        groupBy.forEach { group ->
            barchartData.addBar(
                group.key,
                BarchartData.BarHistory(period.name, group.value.sumOf { it.mutation.value })
            )
        }
        return barchartData
    }

    private fun createOutChart(transactions: List<Transaction>): Component {
        return PieChart(
            ROW_CONTENT_ID,
            "Expense",
            createPieData(transactions.filter { it.mutation.value < BigDecimal.ZERO }),
            translator,
            properties.theme
        )
    }

    private fun createPieData(transactions: List<Transaction>): PieChartData<out Number> {
        val pieChartData = PieChartData<Int>()
        val groupBy = transactions.groupBy { it.category }
        val totalAmount = transactions.sumOf { it.mutation.value }
        groupBy.forEach {
            pieChartData.addSlice(
                it.key, it.value.sumOf { it.mutation.value }.toInt()
            )
        }
        return pieChartData
    }

    private fun createSummaryRight(transactions: List<Transaction>): Component {
        return KeyValueView(
            ROW_CONTENT_ID, translator,
            "Total transactions" to transactions.size,
            "Result" to transactions.sumOf { it.mutation.value }
        )
    }

    private fun createSummaryLeft(transactions: List<Transaction>): Component {
        return KeyValueView(
            ROW_CONTENT_ID, translator,
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
                ROW_CONTENT_ID,
                CompoundPropertyModel.of(categorized),
                translator.translate("Toggle categories")
            ) {
                override fun onInitialize() {
                    super.onInitialize()
                    isEnabled = ModelCache.PERIOD.MONTH.equals(period)
                }

                override fun onUpdate(target: AjaxRequestTarget, modelObject: Boolean) {
                    categorized = modelObject
                    reload(target)
                }
            }
        return switch
    }


    private fun createTable(transactions: List<Transaction>): Component {
        val tuples = runBlocking { tupleUtils.transactionsToTuples(transactions, categorized, period) }
        return DynamicDataTable.get(
            ROW_CONTENT_ID,
            tuples,
            12,
            50,
            translator,
            "Category"
        ).sm().striped().invertHeader()
    }

    private fun createYearBar(): Component {
        val type = when (period) {
            ModelCache.PERIOD.MONTH -> DatePickerButton.Companion.PickerTypes.MONTH_YEAR
            else -> DatePickerButton.Companion.PickerTypes.YEAR_ONLY
        }
        return object : DatePickerButton(ROW_CONTENT_ID, modelCache.date, type, translator.language) {
            override fun onDateChanged(target: AjaxRequestTarget, date: LocalDate) {
                super.onDateChanged(target, date)
                modelCache.date = date
                reload(target)
            }
        }
    }


    override fun isAvailable(): Boolean {
        return !modelCache.isEmpty()
    }
}