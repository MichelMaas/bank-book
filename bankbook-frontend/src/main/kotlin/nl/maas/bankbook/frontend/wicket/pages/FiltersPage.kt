package nl.maas.bankbook.frontend.wicket.pages

import nl.maas.bankbook.domain.properties.Categories
import nl.maas.bankbook.frontend.wicket.components.AjaxSearchField
import nl.maas.bankbook.frontend.wicket.components.DynamicFormComponent
import nl.maas.bankbook.frontend.wicket.components.DynamicTableComponent
import nl.maas.bankbook.frontend.wicket.objects.Filter
import org.apache.commons.lang3.StringUtils.EMPTY
import org.apache.wicket.Component
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.WebMarkupContainer
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.model.CompoundPropertyModel
import org.apache.wicket.model.Model
import org.apache.wicket.request.mapper.parameter.PageParameters

class FiltersPage : BasePage(PageParameters()) {

    private val transactionsContainer = object : WebMarkupContainer("transactionsContainer") {init {
        outputMarkupId = true
    }
    }
    private val importContainer = object : WebMarkupContainer("categoriesContainer") {init {
        outputMarkupId = true
    }
    }
    private val filtersContainer = object : WebMarkupContainer("filtersContainer") {init {
        outputMarkupId = true
    }
    }

    private var transactionsFilter = EMPTY

    override fun onBeforeRender() {
        super.onBeforeRender()
        addOrReplace(importContainer, transactionsContainer, filtersContainer)
        setUpCategories()
        setUpTransactions()
        setUpSearchBar()
        setUpFilters()
    }

    private fun setUpFilters() {
        val filters = DynamicTableComponent(
            "filters",
            modelCache.dataContainer.findSimilarFilters(transactionsFilter).toMutableList()
        )
        filtersContainer.addOrReplace(filters)
    }

    private fun setUpSearchBar() {
        val search = object : AjaxSearchField("search", Model.of(EMPTY)) {
            override fun onChange(target: AjaxRequestTarget) {
                transactionsFilter = modelObject ?: EMPTY
                setUpTransactions()
                setUpFilters()
                target.add(transactionsContainer, filtersContainer)
            }
        }
        addOrReplace(search, Label("searchLabel", propertiesCache.translator.translate(this::class, "Search")))
    }

    private fun setUpCategories() {
        val categories = object : DynamicFormComponent<Filter>(
            "categories",
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
                setUpFilters()
                target.add(transactionsContainer, filtersContainer)
            }
        }.addSelect("category", "Categories", Categories.values().toList()).addCheckBox("saveFilter", "Persistent")
        importContainer.addOrReplace(categories)
    }

    private fun setUpTransactions(filter: String = transactionsFilter): Component {
        val transactions = DynamicTableComponent(
            "transactions",
            modelCache.dataContainer.findByFilter(filter).toMutableList()
        )
        transactionsContainer.addOrReplace(transactions)
        return transactions
    }

}