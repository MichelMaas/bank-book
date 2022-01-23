package nl.maas.bankbook.frontend.wicket.pages

import nl.maas.bankbook.domain.enums.Categories
import nl.maas.bankbook.frontend.wicket.components.AjaxSearchField
import nl.maas.bankbook.frontend.wicket.components.DynamicFormComponent
import nl.maas.bankbook.frontend.wicket.components.DynamicTableComponent
import nl.maas.bankbook.frontend.wicket.objects.Filter
import nl.maas.bankbook.frontend.wicket.objects.Tuple
import org.apache.commons.lang3.StringUtils
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.WebMarkupContainer
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.model.CompoundPropertyModel
import org.apache.wicket.model.Model
import org.apache.wicket.request.mapper.parameter.PageParameters

class CategoriesPage(parameters: PageParameters?) : BasePage(parameters) {

    var filter = ""
    var value: MutableList<Tuple> = mutableListOf()

    override fun onBeforeRender() {
        super.onBeforeRender()
        val tableWrapper = TableWrapper()
        val form = object : DynamicFormComponent<Filter>(
            "form", StringUtils.EMPTY, CompoundPropertyModel.of(
                Model.of(
                    Filter()
                )
            )
        ) {
            override fun onSubmit(target: AjaxRequestTarget) {
                super.onSubmit(target)
                modelCache.dataContainer.changeCategoriesForAll(
                    filter,
                    ((defaultModelObject as Filter).category)
                )
                if ((defaultModelObject as Filter).saveFilter) {
                    (defaultModelObject as Filter).filter = this@CategoriesPage.filter
                    (defaultModelObject as Filter).store()
                }
                target.add(tableWrapper)
            }
        }.addSelect(
            "category",
            propertiesCache.translator.translate(CategoriesPage::class, "Category"),
            Categories.values().sortedBy { it.name }.toList()
        ).addCheckBox("saveFilter", propertiesCache.translator.translate(CategoriesPage::class, "Save"), true)
        val searchLabel = Label("searchLabel", propertiesCache.translator.translate(CategoriesPage::class, "search"))
        addOrReplace(searchLabel, object : AjaxSearchField("search", Model.of(filter)) {

            override fun onChange(target: AjaxRequestTarget) {
                this@CategoriesPage.filter = this.convertedInput.orEmpty()
                target.add(tableWrapper)
            }
        }, form, tableWrapper)
    }

    private inner class TableWrapper() : WebMarkupContainer("tableWrapper") {

        init {
            outputMarkupId = true
        }

        override fun onBeforeRender() {
            super.onBeforeRender()
            value = modelCache.dataContainer.findByFilter(filter).toMutableList()
            addOrReplace(
                DynamicTableComponent(
                    "table",
                    value
                )
            )
        }
    }

}