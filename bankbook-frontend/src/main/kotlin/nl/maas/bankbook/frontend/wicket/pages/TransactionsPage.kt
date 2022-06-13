package nl.maas.bankbook.frontend.wicket.pages

import nl.maas.bankbook.domain.Transaction
import nl.maas.bankbook.domain.properties.Categories
import nl.maas.bankbook.frontend.wicket.components.DynamicFormComponent
import nl.maas.bankbook.frontend.wicket.components.DynamicTableComponent
import nl.maas.bankbook.frontend.wicket.objects.Tuple
import org.apache.commons.lang3.StringUtils
import org.apache.wicket.Component
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.WebMarkupContainer
import org.apache.wicket.markup.html.form.upload.FileUpload
import org.apache.wicket.model.CompoundPropertyModel
import org.apache.wicket.request.mapper.parameter.PageParameters
import java.io.File

class TransactionsPage : BasePage(PageParameters()) {

    private val transactionsContainer = object : WebMarkupContainer("transactionsContainer") {init {
        outputMarkupId = true
    }
    }
    private val importContainer = object : WebMarkupContainer("importContainer") {init {
        outputMarkupId = true
    }
    }
    private val detailsContainer = object : WebMarkupContainer("detailsContainer") {init {
        outputMarkupId = true
    }
    }

    override fun onBeforeRender() {
        super.onBeforeRender()
        addOrReplace(importContainer, transactionsContainer, detailsContainer)
        setUpImport()
        setUpTransactionsList()
        setUpDetailView()
    }

    private fun setUpImport() {
        val import = object : DynamicFormComponent<File>(
            "import",
            "Import transactions",
            CompoundPropertyModel.of(File.createTempFile("tmp", "file"))
        ) {
            override fun onFileUpload(target: AjaxRequestTarget, fileUpload: FileUpload) {
                super.onFileUpload(target, fileUpload)
                fileUpload.writeTo(defaultModelObject as File)
                val transactions = parserService.parseFile(defaultModelObject as File)
                modelCache.dataContainer.addNewFrom(transactions).store()
                setUpTransactionsList()
                target.add(transactionsContainer)
            }
        }.addFileUploadField("file", "Import")
        importContainer.addOrReplace(import)
    }

    private fun setUpTransactionsList(): Component {
        val transactions = object : DynamicTableComponent(
            "transactions",
            modelCache.dataContainer.findByFilter(StringUtils.EMPTY).toMutableList()
        ) {
            override fun onTupleClick(target: AjaxRequestTarget, tuple: Tuple) {
                super.onTupleClick(target, tuple)
                val detailView = setUpDetailView(
                    modelCache.dataContainer.filterTransactions(
                        tuple.toFilterString(),
                        modelCache.dataContainer.transactions
                    ).firstOrNull()
                )
                target.add(detailsContainer)
            }
        }
        transactionsContainer.addOrReplace(transactions)
        return transactions
    }

    private fun setUpDetailView(transaction: Transaction? = null): Component {
        val component: Component
        if (transaction == null) {
            component = WebMarkupContainer("details")
        } else {
            component =
                object : DynamicFormComponent<Transaction>(
                    "details",
                    "${transaction.description}, ${transaction.category}: ${transaction.mutation}",
                    CompoundPropertyModel.of(transaction)
                ) {

                    init {
                        outputMarkupId = true
                    }

                }.addSelect(
                    "category",
                    propertiesCache.translator.translate(this::class, "Category"),
                    Categories.values().toList()
                )
        }
        detailsContainer.addOrReplace(component)
        return component
    }
}