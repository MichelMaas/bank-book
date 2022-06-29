package nl.maas.bankbook.frontend.wicket.pages

import nl.maas.bankbook.domain.IBAN
import nl.maas.bankbook.domain.Transaction
import nl.maas.bankbook.domain.properties.Categories
import nl.maas.bankbook.frontend.wicket.components.AjaxSearchField
import nl.maas.bankbook.frontend.wicket.components.DynamicFormComponent
import nl.maas.bankbook.frontend.wicket.components.DynamicTableComponent
import nl.maas.bankbook.frontend.wicket.objects.Tuple
import org.apache.commons.lang3.StringUtils.EMPTY
import org.apache.wicket.AttributeModifier
import org.apache.wicket.Component
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.WebMarkupContainer
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.markup.html.form.upload.FileUpload
import org.apache.wicket.model.CompoundPropertyModel
import org.apache.wicket.model.Model
import java.io.File
import java.io.Serializable
import java.util.*

class TransactionsPage : BasePage() {

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

    private var transactionsFilter = EMPTY

    override fun onBeforeRender() {
        super.onBeforeRender()
        addOrReplace(importContainer, transactionsContainer, detailsContainer)
        setUpImport()
        setUpTransactionsList()
        setUpDetailView()
        setUpSearchBar()
    }

    private fun setUpSearchBar() {
        val search = object : AjaxSearchField("search", Model.of(EMPTY)) {
            override fun onChange(target: AjaxRequestTarget) {
                transactionsFilter = modelObject ?: EMPTY
                setUpTransactionsList()
                target.add(transactionsContainer)
            }
        }
        addOrReplace(search, Label("searchLabel", propertiesCache.translator.translate(this::class, "Search")))
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

    private fun setUpTransactionsList(filter: String = transactionsFilter): Component {
        val transactions = object : DynamicTableComponent(
            "transactions",
            modelCache.dataContainer.findByFilter(filter).toMutableList()
        ) {
            override fun onTupleClick(target: AjaxRequestTarget, tuple: Tuple) {
                super.onTupleClick(target, tuple)
                setUpDetailView(modelCache.dataContainer.findTransaction(tuple.toFilterString()))
                target.add(detailsContainer)
            }
        }
        transactionsContainer.addOrReplace(transactions)
        return transactions
    }

    private fun setUpDetailView(transaction: Transaction? = null): Component {
        if (transaction == null) {
            detailsContainer.add(AttributeModifier.append("class", "d-none"))
        } else {
            detailsContainer.add(object : AttributeModifier("class", "d-none") {
                override fun newValue(currentValue: String, replacementValue: String): Serializable {
                    return super.newValue(currentValue, currentValue.replace(replacementValue, EMPTY))
                }
            })
        }
        val component = object : DynamicFormComponent<Transaction>(
            "details",
            arrayOf(transaction?.counter(), transaction?.mutation).joinToString(", "),
            CompoundPropertyModel.of(
                transaction ?: Transaction.EMPTY(
                    IBAN("NL00SNSB0000000000"),
                    Currency.getInstance(Locale.getDefault())
                )
            )
        ) {

            override fun onSubmit(target: AjaxRequestTarget) {
                super.onSubmit(target)
                modelCache.dataContainer.store()
                setUpTransactionsList()
                setUpDetailView()
                target.add(transactionsContainer, detailsContainer)
            }

            override fun onAfterCancel(target: AjaxRequestTarget) {
                setUpDetailView()
                target.add(detailsContainer)
            }


        }.addSelect(
            "category",
            propertiesCache.translator.translate(this::class, "Category"),
            Categories.values().toList()
        )
        detailsContainer.addOrReplace(component)
        return component
    }
}