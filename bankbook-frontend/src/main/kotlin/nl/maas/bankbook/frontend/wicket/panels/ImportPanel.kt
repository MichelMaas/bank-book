package nl.maas.bankbook.frontend.wicket.panels

import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.fileinput.BootstrapFileInputField
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.fileinput.FileInputConfig
import nl.maas.fxanalyzer.frontend.services.FxParserService
import nl.maas.fxanalyzer.frontend.wicket.caches.ModelCache
import nl.maas.fxanalyzer.frontend.wicket.caches.PropertiesCache
import nl.maas.fxanalyzer.frontend.wicket.objects.SearchCriteria
import nl.maas.fxanalyzer.frontend.wicket.pages.ImportPage
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.markup.html.form.Form
import org.apache.wicket.model.IModel
import org.apache.wicket.model.Model
import javax.inject.Inject

class ImportPanel : AbstractPanel {
    constructor(id: String, model: IModel<SearchCriteria>) : super(id, model)

    @Inject
    lateinit var fxParserService: FxParserService

    @Inject
    lateinit var modelCache: ModelCache

    @Inject
    lateinit var propertiesCache: PropertiesCache

    override fun onBeforeRender() {
        super.onBeforeRender()
        val form = SearchForm("searchForm")
        addOrReplace(form)
    }

    fun typedModel() = defaultModel as IModel<SearchCriteria>


    private inner class SearchForm(id: String) : Form<SearchCriteria>(id) {
        lateinit var fileUploadField: BootstrapFileInputField
        override fun onInitialize() {
            super.onInitialize()
            isMultiPart = true
            fileUploadField = object : BootstrapFileInputField(
                "formFileSm",
                Model.ofList(mutableListOf()),
                FileInputConfig().showPreview(false).maxFileCount(1)
                    .withLocale(propertiesCache.translator.currentLanguage())
            ) {
            }
        }

        override fun onBeforeRender() {
            super.onBeforeRender()
            addOrReplace(fileUploadField)
            addOrReplace(Label("submitName", propertiesCache.translator.translate(containingPage(), "confirm")))
            add(Submit())
        }

        private inner class Submit :
            AjaxFormSubmitBehavior(this, "submit") {
            override fun onSubmit(target: AjaxRequestTarget) {
                super.onSubmit(target)
                val importPage = findParent(ImportPage::class.java)
                val export =
                    this@ImportPanel.fxParserService.parseExportHtmFile(fileUploadField.fileUpload.writeToTempFile())
                modelCache.fxDataSet.addTransactions(export.transactions).addTransfers(export.transfers)
                    .setCurrency(export.currency).store()
                target.add(importPage)
            }


        }
    }
}