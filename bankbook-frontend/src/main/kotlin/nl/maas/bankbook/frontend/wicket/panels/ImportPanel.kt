package nl.maas.bankbook.frontend.wicket.panels

import kotlinx.coroutines.runBlocking
import nl.maas.bankbook.domain.Transaction
import nl.maas.bankbook.frontend.services.ParserService
import nl.maas.bankbook.frontend.wicket.caches.ModelCache
import nl.maas.bankbook.frontend.wicket.tools.TupleUtils
import nl.maas.wicket.framework.components.base.DynamicDataTable
import nl.maas.wicket.framework.components.base.DynamicFormComponent
import nl.maas.wicket.framework.components.base.DynamicPanel
import nl.maas.wicket.framework.components.base.DynamicPanel.Companion.ROW_CONTENT_ID
import nl.maas.wicket.framework.panels.RIAPanel
import nl.maas.wicket.framework.services.Translator
import org.apache.wicket.Component
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.form.upload.FileUpload
import org.apache.wicket.model.CompoundPropertyModel
import org.apache.wicket.spring.injection.annot.SpringBean
import java.io.Serializable

class ImportPanel : RIAPanel() {

    @SpringBean
    private lateinit var translator: Translator

    @SpringBean
    private lateinit var parserService: ParserService

    @SpringBean
    private lateinit var modelCache: ModelCache

    @SpringBean
    private lateinit var tupleUtils: TupleUtils
    private var transactions: List<Transaction> = listOf()
    override fun onBeforeRender() {
        super.onBeforeRender()
        addOrReplace(createContent())
    }

    private fun createContent(): Component {
        return DynamicPanel("panel")
            .addRows("ImportForm" to intArrayOf(12), "ImportTable" to intArrayOf(12))
            .addOrReplaceComponentToColumn("ImportForm", 0, createForm())
            .addOrReplaceComponentToColumn("ImportTable", 0, createTable())

    }

    private fun createTable(): Component {
        val tuples = runBlocking { tupleUtils.transactionsToTuples(transactions, false, ModelCache.PERIOD.NONE) }
        return DynamicDataTable.get(
            ROW_CONTENT_ID,
            tuples,
            30,
            translator = translator,
            translateContent = arrayOf("Category")
        ).sm().striped().invertHeader()
    }

    private fun createForm(): Component {
        return object : DynamicFormComponent<FileWrapper>(
            ROW_CONTENT_ID,
            "",
            CompoundPropertyModel.of(FileWrapper()),
            translator = translator
        ) {
            init {
                showButons = false
            }

            override fun onFileUpload(target: AjaxRequestTarget, fileUpload: FileUpload) {
                super.onFileUpload(target, fileUpload)
                val file = fileUpload.writeToTempFile()
                super.onFileUpload(target, fileUpload)
                transactions = parserService.parseFile(file, fileUpload.clientFileName.substringAfterLast("."))
                modelCache.addOrUpdateTransactions(transactions)
                reload(target)
            }
        }
            .addFileUploadField("file")
    }

    private inner class FileWrapper(var file: FileUpload? = null) : Serializable
}