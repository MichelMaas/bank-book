package nl.maas.bankbook.frontend.wicket.panels

import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import nl.maas.bankbook.domain.CategoryFilter
import nl.maas.bankbook.domain.Transaction
import nl.maas.bankbook.domain.enums.Categories
import nl.maas.bankbook.frontend.wicket.caches.ModelCache
import nl.maas.bankbook.frontend.wicket.tools.TupleUtils
import nl.maas.wicket.framework.components.base.DynamicDataTable
import nl.maas.wicket.framework.components.base.DynamicFormComponent
import nl.maas.wicket.framework.components.base.DynamicPanel
import nl.maas.wicket.framework.components.base.DynamicPanel.Companion.ROW_CONTENT_ID
import nl.maas.wicket.framework.components.elemental.AjaxSearchField
import nl.maas.wicket.framework.components.elemental.SimpleAjaxButton
import nl.maas.wicket.framework.panels.RIAPanel
import nl.maas.wicket.framework.services.Translator
import org.apache.commons.lang3.StringUtils
import org.apache.wicket.Component
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.model.CompoundPropertyModel
import org.apache.wicket.spring.injection.annot.SpringBean
import java.io.Serializable
import java.time.LocalDate

class FiltersPanel : RIAPanel() {

    @SpringBean
    private lateinit var translator: Translator

    @SpringBean
    private lateinit var modelCache: ModelCache

    @SpringBean
    private lateinit var tupleUtils: TupleUtils
    private var filterString = StringUtils.EMPTY
    private lateinit var panel: DynamicPanel

    private val filterCache = FilterCache()


    override fun onInitialize() {
        super.onInitialize()
        filterCache.filter("")
    }

    override fun onBeforeRender() {
        super.onBeforeRender()
        addOrReplace(createDynamicPanel())
    }

    private val SEARCH = "Search"
    private val FILTERS = "Filters"
    private val FILTER_BUTTON = "FilterButton"
    private val TRANSACTIONS = "Transactions"

    private fun createDynamicPanel(): DynamicPanel {
        val filterTable = createFilterTable()
        val transactionsTable = createTransactionsTable()
        panel = DynamicPanel("panel")
            .addRows(
                SEARCH to intArrayOf(12),
                FILTERS to intArrayOf(4, 8),
                FILTER_BUTTON to intArrayOf(4, 8),
                TRANSACTIONS to intArrayOf(12)
            )
            .addOrReplaceComponentToColumn(SEARCH, 0, createSearchBar(transactionsTable, filterTable))
            .addOrReplaceComponentToColumn(FILTERS, 0, createFilterForm(filterTable, transactionsTable))
            .addOrReplaceComponentToColumn(FILTERS, 1, filterTable)
            .addOrReplaceComponentToColumn(FILTER_BUTTON, 1, createFilterButton())
            .addOrReplaceComponentToColumn(TRANSACTIONS, 0, transactionsTable)
        return panel
    }

    private fun createTransactionsTable(): DynamicDataTable {
        val tuples =
            runBlocking { tupleUtils.transactionsToTuples(filterCache.transactions, false, ModelCache.PERIOD.NONE) }
        return DynamicDataTable.get(ROW_CONTENT_ID, tuples, 15, 50, translator, "Category").invertHeader().sm()
            .striped()
    }

    private fun createFilterButton(): Component {
        return object : SimpleAjaxButton(
            ROW_CONTENT_ID,
            "Apply",
            Buttons.Type.Primary,
            SimpleAjaxButton.Size.SMALL,
            translator,
            true
        ) {
            override fun onClick(target: AjaxRequestTarget) {
                filterCache.filters.forEach { modelCache.applyCategorieOnAll(it) }
                reload(target)
            }

        }
    }

    private fun createFilterTable(): DynamicDataTable {
        val filterTuples = tupleUtils.filtersToTuples(filterCache.filters)
        return DynamicDataTable.get(ROW_CONTENT_ID, filterTuples, 6, 40, translator, "Category").invertHeader().sm()
            .striped()
    }

    private fun createFilterForm(filterTable: DynamicDataTable, transactionTable: DynamicDataTable): Component {
        val categoryFilter = CategoryFilter(filterString, Categories.values().sorted().first(), true)
        return object :
            DynamicFormComponent<CategoryFilter>(
                ROW_CONTENT_ID,
                "Filter",
                CompoundPropertyModel.of(categoryFilter),
                translator
            ) {

            override fun onSubmit(target: AjaxRequestTarget, typedModelObject: CategoryFilter) {
                super.onSubmit(target, typedModelObject)
                typedModelObject.filterString = filterCache.filter
                typedModelObject.store()
                modelCache.applyCategorieOn(filterCache.transactions, typedModelObject)
                modelCache.refresh()
                filterCache.filter(filterCache.filter)
            }

            override fun onSubmitCompleted(target: AjaxRequestTarget, typedModelObject: CategoryFilter) {
                super.onSubmitCompleted(target, typedModelObject)
                val filtersToTuples = tupleUtils.filtersToTuples(filterCache.filters)
                filterTable.update(filtersToTuples, target)
                val transactionsToTuples =
                    runBlocking {
                        tupleUtils.transactionsToTuples(
                            filterCache.transactions,
                            false,
                            ModelCache.PERIOD.NONE
                        )
                    }
                transactionTable.update(transactionsToTuples, target)
            }

        }.addSelect("category", "Category", Categories.values().toList().sortedBy { it.name }, categoryFilter.category)
            .addCheckbox("store", "Store", categoryFilter.store)
    }

    private fun createSearchBar(transactionTable: DynamicDataTable, filterTable: DynamicDataTable): Component {
        return object : AjaxSearchField(ROW_CONTENT_ID, CompoundPropertyModel.of(filterString)) {
            override fun onChange(target: AjaxRequestTarget) {
                filterCache.filter(convertedInput ?: StringUtils.EMPTY)
                runBlocking {
                    val transTuples = async {
                        tupleUtils.transactionsToTuples(
                            filterCache.transactions,
                            false,
                            ModelCache.PERIOD.NONE
                        )
                    }
                    val filTuples = async {
                        tupleUtils.filtersToTuples(filterCache.filters)
                    }

                    transactionTable.update(
                        transTuples.await(), target
                    )

                    filterTable.update(filTuples.await(), target)
                }
            }
        }
    }

    private inner class FilterCache() : Serializable {
        private var _transactions: List<Transaction> =
            modelCache.transactionsForPeriod(LocalDate.now(), ModelCache.PERIOD.NONE)
        private var _filters: List<CategoryFilter> = modelCache.findFilters("s")
        private var _filter = StringUtils.EMPTY

        val transactions: List<Transaction> get() = _transactions
        val filters: List<CategoryFilter> get() = _filters
        val filter: String get() = _filter


        fun filter(filter: String) {
            if (filter.length < this._filter.length) {
                _transactions = modelCache.transactionsForFilter(filter)
            } else {
                runBlocking {
                    val launch1 = async { modelCache.filterTransactions(filter, transactions) }
                    val launch2 = async { modelCache.findFilters(filter) }
                    _transactions = launch1.await()
                    _filters = launch2.await()
                }
            }
            this._filter = filter
        }
    }
}