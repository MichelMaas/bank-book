package nl.maas.bankbook.frontend.wicket.panels

import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons
import kotlinx.coroutines.runBlocking
import nl.maas.bankbook.domain.IBAN
import nl.maas.bankbook.domain.Transaction
import nl.maas.bankbook.frontend.ContextProvider
import nl.maas.bankbook.frontend.wicket.caches.ModelCache
import nl.maas.bankbook.frontend.wicket.config.BootstrapProperties
import nl.maas.bankbook.frontend.wicket.tools.TupleUtils
import nl.maas.wicket.framework.components.base.*
import nl.maas.wicket.framework.components.base.DynamicPanel.Companion.ROW_CONTENT_ID
import nl.maas.wicket.framework.components.charts.PieChart
import nl.maas.wicket.framework.components.charts.data.BarchartData
import nl.maas.wicket.framework.components.charts.data.PieChartData
import nl.maas.wicket.framework.components.elemental.DatePickerButton
import nl.maas.wicket.framework.services.Translator
import org.apache.commons.lang3.StringUtils
import org.apache.wicket.Component
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.model.CompoundPropertyModel
import org.apache.wicket.model.Model
import org.apache.wicket.spring.injection.annot.SpringBean
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

abstract class AbstractOverviewPanel(private val period: ModelCache.PERIOD = ModelCache.PERIOD.YEAR) :
    StoreWaitingPanel() {
    private val creationStart = LocalDateTime.now()
    private lateinit var renderStart: LocalDateTime


    @SpringBean
    private lateinit var translator: Translator

    private var categorized = true
    private var category: String = StringUtils.EMPTY

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
        val transactions = modelCache.transactionsForPeriod(modelCache.date, period, category)
        return DynamicPanel("panel").addRows(
            "Vars" to intArrayOf(5, 6),
            "Summary" to intArrayOf(6, 6),
            "Toggle" to intArrayOf(2, 10),
            "Table" to intArrayOf(12),
            "Graphs" to intArrayOf(6, 6)
        ).addOrReplaceComponentToColumn("Vars", 0, createAccountSelector())
            .addOrReplaceComponentToColumn("Vars", 1, createYearBar())
            .addOrReplaceComponentToColumn("Summary", 0, createSummaryLeft(transactions))
            .addOrReplaceComponentToColumn("Summary", 1, createSummaryRight(transactions))
            .addOrReplaceComponentToColumn("Toggle", 0, createToggle())
            .addOrReplaceComponentToColumn("Table", 0, createTable(transactions))
            .addOrReplaceComponentsToRow("Graphs", createOutChart(transactions), createInChart(transactions))
    }

    private fun createAccountSelector(): Component {
        val form = object :
            DynamicFormComponent<ModelCache>(
                ROW_CONTENT_ID,
                "Account",
                CompoundPropertyModel<ModelCache>(modelCache),
                translator,
            ) {

            override fun onBeforeRender() {
                super.onBeforeRender()
                toggleVisibleFor("account" to !modelCache.allAccounts)
            }

            override fun <M> onSelectChanged(propertyName: String, value: M, target: AjaxRequestTarget) {
                modelCache.account = value as IBAN
                reload(target)
            }

            override fun onSwitchToggled(propertyName: String, switch: Boolean, target: AjaxRequestTarget) {
                reload(target)
            }
        }.addSwitch("allAccounts", "All", modelCache.allAccounts)
            .addSelect("account", "Account", modelCache.accounts, modelCache.account)


        form.showButons = false
        return form
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
        val groupBy =
            if (category.isBlank()) transactions.groupBy { it.category } else transactions.groupBy { it.counter() }
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
        val groupBy =
            if (category.isBlank()) transactions.groupBy { it.category } else transactions.groupBy { it.counter() }
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
                    isEnabled = ModelCache.PERIOD.MONTH.equals(period) || (category.isNotBlank() || !categorized)
                }

                override fun onUpdate(target: AjaxRequestTarget, modelObject: Boolean) {
                    categorized = modelObject
                    if (categorized) category = StringUtils.EMPTY
                    reload(target)
                }
            }
        return switch
    }


    private fun createTable(transactions: List<Transaction>): Component {
        val tuples = runBlocking { tupleUtils.transactionsToTuples(transactions, categorized, period) }
        val rowsPerPage = if (modelCache.allAccounts) 12 else 10
        return DynamicDataTable.get(
            ROW_CONTENT_ID,
            tuples,
            rowsPerPage,
            50,
            translator,
            "Category",
            onTupleClick = { target, tuple ->
                categorized = false
                category = tuple.getValueForColumn("Category").toString()
                reload(target)
            }
        ).sm().striped().invertHeader()
    }

    private fun createYearBar(): Component {
        val type = when (period) {
            ModelCache.PERIOD.MONTH -> DatePickerButton.Companion.PickerTypes.MONTH_YEAR
            else -> DatePickerButton.Companion.PickerTypes.YEAR_ONLY
        }
        val picker = object : DatePickerButton(ROW_CONTENT_ID, modelCache.date, type, translator.language) {
            override fun onDateChanged(target: AjaxRequestTarget, date: LocalDate) {
                super.onDateChanged(target, date)
                modelCache.date = date
                reload(target)
            }
        }
        picker.label = Model.of(translator.translate("Date"))
        return picker
    }


    override fun isAvailable(): Boolean {
        return !modelCache.isEmpty()
    }
}