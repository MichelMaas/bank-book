package nl.maas.bankbook.frontend.wicket.pages

import nl.maas.bankbook.domain.properties.Categories
import nl.maas.bankbook.frontend.wicket.components.AjaxSearchField
import nl.maas.bankbook.frontend.wicket.components.DynamicFormComponent
import nl.maas.bankbook.frontend.wicket.components.DynamicPanel
import nl.maas.bankbook.frontend.wicket.components.DynamicTableComponent
import nl.maas.bankbook.frontend.wicket.objects.Filter
import org.apache.commons.lang3.StringUtils.EMPTY
import org.apache.wicket.Component
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.model.CompoundPropertyModel
import org.apache.wicket.model.Model
import org.apache.wicket.request.mapper.parameter.PageParameters

class FiltersPage : BasePage(PageParameters()) {

    private var transactionsFilter = EMPTY

    override fun onBeforeRender() {
        super.onBeforeRender()
        addOrReplace(createDynamicPanel())
        setUpCategories()
        setUpTransactions()
        setUpSearchBar()
        setUpFilters()
    }

    private val dynamicPanel: DynamicPanel = DynamicPanel("panel").addRows(
        DynamicPanel.Row.from("search", 10, 12),
        DynamicPanel.Row.from("filters", 40, 8, 4),
        DynamicPanel.Row.from("transactions", 40, 12)
    )


    private fun createDynamicPanel(): Component {
        dynamicPanel.addOrReplaceToColumn("filters", 1, setUpCategories())
            .addOrReplaceToColumn("filters", 0, setUpFilters())
            .addOrReplaceToColumn("transactions", 0, setUpTransactions())
            .addOrReplaceToColumn("search", 0, setUpSearchBar())
        return dynamicPanel
    }

    private fun setUpFilters(): Component {
        val filters = DynamicTableComponent(
            DynamicPanel.CONTENT_ID,
            modelCache.dataContainer.findSimilarFilters(transactionsFilter).toMutableList()
        )
        return filters
    }

    private fun setUpSearchBar(): Component {
        return object : AjaxSearchField(DynamicPanel.CONTENT_ID, Model.of(EMPTY)) {
            override fun onChange(target: AjaxRequestTarget) {
                transactionsFilter = modelObject ?: EMPTY
                val transactions = dynamicPanel.getColumn("transactions", 0)
                val filters = dynamicPanel.getColumn("filters", 0)
                transactions.addOrReplace(setUpTransactions())
                filters.addOrReplace(setUpFilters())
                target.add(transactions, filters)
            }
        }
    }


    private fun setUpCategories(): Component {
        val categories = object : DynamicFormComponent<Filter>(
            DynamicPanel.CONTENT_ID,
            "Import transactions",
            CompoundPropertyModel.of(Filter())
        ) {
            override fun onSubmit(target: AjaxRequestTarget) {
                super.onSubmit(target)
                modelCache.dataContainer.changeCategoriesForAll(
                    transactionsFilter,
                    ((defaultModelObject as Filter).category)
                )
                if ((defaultModelObject as Filter).saveFilter) {
                    (defaultModelObject as Filter).filter = this@FiltersPage.transactionsFilter
                    (defaultModelObject as Filter).store()
                }
                setUpTransactions()
                target.add(dynamicPanel.getColumn("transactions", 0))
            }
        }.addSelect("category", "Categories", Categories.values().toList()).addCheckBox("saveFilter", "Persistent")
        return categories
    }

    private fun setUpTransactions(filter: String = transactionsFilter): Component {
        val transactions = DynamicTableComponent(
            DynamicPanel.CONTENT_ID,
            modelCache.dataContainer.findByFilter(filter).toMutableList()
        )
        return transactions
    }

}