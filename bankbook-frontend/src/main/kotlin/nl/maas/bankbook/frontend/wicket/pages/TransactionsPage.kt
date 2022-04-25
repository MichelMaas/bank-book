package nl.maas.bankbook.frontend.wicket.pages

import nl.maas.bankbook.domain.Transaction
import nl.maas.wicket.framework.components.AjaxSearchField
import nl.maas.wicket.framework.components.forms.DynamicFormComponent
import nl.maas.wicket.framework.components.views.DynamicTableComponent
import nl.maas.wicket.framework.objects.Tuple
import nl.maas.wicket.framework.panels.DividedPanel
import nl.maas.wicket.framework.panels.DividedRow
import org.apache.commons.lang3.StringUtils
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.WebMarkupContainer
import org.apache.wicket.model.CompoundPropertyModel
import org.apache.wicket.model.Model
import org.apache.wicket.request.mapper.parameter.PageParameters

class TransactionsPage(parameters: PageParameters?) : BasePage(parameters) {

    private var filter = StringUtils.EMPTY


    override fun onBeforeRender() {
        super.onBeforeRender()
        addOrReplace(Content())
    }

    private inner class Content : DividedPanel("panel") {
        private var transactionsView: WebMarkupContainer = WebMarkupContainer("bla")

        private var selectedTransaction: Transaction? = null

        init {
            outputMarkupId = true
        }

        override fun getRows(): Array<DividedRow> {
            return arrayOf(createTopRow(), createTransactionsRow())
        }

        private fun createTransactionsRow(): DividedRow {
            return object : DividedRow() {

                init {
                    outputMarkupId = true
                }

                override fun createColumns(): List<Column> {
                    return listOf(Transactions(), Details())
                }

                private inner class Transactions() : Column(8) {

                    override fun provideContent(hookId: String): DynamicTableComponent {
                        val view = object : DynamicTableComponent(
                            hookId,
                            modelCache.dataContainer.findByFilter(filter).toMutableList()
                        ) {
                            init {
                                outputMarkupId = true
                            }

                            override fun onTupleClick(target: AjaxRequestTarget, tuple: Tuple) {
                                super.onTupleClick(target, tuple)
                                selectedTransaction = modelCache.dataContainer.filterTransactions(
                                    tuple.toFilterString(), modelCache.dataContainer.transactions
                                ).firstOrNull()
                                target.add(this)
                            }
                        }
                        transactionsView = view
                        return view
                    }

                }

                private inner class Details() : Column(4) {
                    override fun provideContent(hookId: String): WebMarkupContainer {
                        return selectedTransaction?.let {
                            DynamicFormComponent(
                                hookId,
                                "Transaction",
                                CompoundPropertyModel.of(selectedTransaction)
                            )
                        } ?: kotlin.run { WebMarkupContainer(hookId) }
                    }
                }
            }
        }

        private fun createTopRow(): DividedRow {
            return object : DividedRow() {
                override fun createColumns(): List<Column> {
                    return listOf(TopColumn())
                }

                private inner class TopColumn : Column(12) {
                    override fun provideContent(hookId: String): AjaxSearchField {
                        return object :
                            AjaxSearchField(hookId, "Search", Model.of(this@TransactionsPage.filter)) {
                            override fun onChange(target: AjaxRequestTarget, filterInput: String) {
                                this@TransactionsPage.filter = filterInput
                                target.add(transactionsView)
                            }
                        }
                    }
                }
            }
        }


    }
}