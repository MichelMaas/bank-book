package nl.maas.bankbook.frontend.wicket.pages

import nl.maas.fxanalyzer.frontend.services.FxParserService
import nl.maas.fxanalyzer.frontend.wicket.components.DynamicFormComponent
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.form.upload.FileUpload
import org.apache.wicket.model.CompoundPropertyModel
import org.apache.wicket.request.mapper.parameter.PageParameters
import javax.inject.Inject

open class ImportPage(parameters: PageParameters) : BasePage(parameters) {

    @Inject
    lateinit var fxParserService: FxParserService

    override fun onInitialize() {
        super.onInitialize()
        addOrReplace(
            object : DynamicFormComponent<FXExport>(
                "panel", propertiesCache.translator.translate(this::class, "title"), CompoundPropertyModel.of(
                    FXExport()
                )
            ) {
                override fun onFileUpload(target: AjaxRequestTarget, fileUpload: FileUpload) {
                    val importPage = findParent(ImportPage::class.java)
                    val export =
                        fxParserService.parseExportHtmFile(fileUpload.writeToTempFile())
                    modelCache.fxDataSet.addTransactions(export.transactions).addTransfers(export.transfers)
                        .setCurrency(export.currency).store()
                    target.add(importPage)
                }
            }.addFileUploadField(
                "file",
                propertiesCache.translator.translate(ImportPage::class, "title"),
                propertiesCache.translator.translate(this::class, "SelectFile"),
                propertiesCache.translator.translate(this::class, "Browse"),
                propertiesCache.translator.translate(this::class, "Cancel"),
                propertiesCache.translator.translate(this::class, "Upload"),
                propertiesCache.translator.translate(this::class, "Remove")
            )
        )
    }

}