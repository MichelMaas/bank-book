package nl.maas.bankbook.frontend.wicket.pages

import nl.maas.bankbook.frontend.wicket.components.DynamicFormComponent
import nl.maas.bankbook.frontend.wicket.components.DynamicTableComponent
import nl.maas.bankbook.frontend.wicket.objects.Tuple
import org.apache.commons.lang3.StringUtils
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.WebMarkupContainer
import org.apache.wicket.markup.html.form.upload.FileUpload
import org.apache.wicket.model.CompoundPropertyModel
import org.apache.wicket.request.mapper.parameter.PageParameters
import java.io.File

class TransactionsPage : BasePage(PageParameters()) {

    override fun onBeforeRender() {
        super.onBeforeRender()
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
            }
        }.addFileUploadField("file", "Import")
        addOrReplace(import)
    }

    private fun setUpTransactionsList() {
        val transactions = object : DynamicTableComponent(
            "transactions",
            modelCache.dataContainer.findByFilter(StringUtils.EMPTY).toMutableList()
        ) {
            override fun onTupleClick(target: AjaxRequestTarget, tuple: Tuple) {
                super.onTupleClick(target, tuple)
            }
        }
        addOrReplace(transactions)
    }

    private fun setUpDetailView() {
        addOrReplace(WebMarkupContainer("details"))
    }
}