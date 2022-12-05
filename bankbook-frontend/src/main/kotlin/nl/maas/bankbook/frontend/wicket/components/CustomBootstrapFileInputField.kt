package nl.maas.bankbook.frontend.wicket.components

import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.fileinput.FileInputConfig
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.fileinput.FileinputJsReference
import de.agilecoders.wicket.jquery.JQuery
import de.agilecoders.wicket.jquery.util.Strings2
import nl.maas.bankbook.frontend.wicket.caches.PropertiesCache
import nl.maas.bankbook.frontend.wicket.pages.BasePage
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior
import org.apache.wicket.markup.ComponentTag
import org.apache.wicket.markup.head.IHeaderResponse
import org.apache.wicket.markup.head.OnDomReadyHeaderItem
import org.apache.wicket.markup.html.form.Form
import org.apache.wicket.markup.html.form.upload.FileUpload
import org.apache.wicket.markup.html.form.upload.FileUploadField
import org.apache.wicket.model.IModel
import org.apache.wicket.util.lang.Args
import org.apache.wicket.util.template.PackageTextTemplate
import javax.inject.Inject
import kotlin.reflect.KClass

/**
 * Integration with [Bootstrap FileInput](https://github.com/kartik-v/bootstrap-fileinput)
 */
class CustomBootstrapFileInputField @JvmOverloads constructor(
    id: String?,
    model: IModel<List<FileUpload?>?>? = null,
    config: FileInputConfig = FileInputConfig()
) : FileUploadField(id, model) {
    /**
     * When the *Upload* button is used it will submit the file input
     * with Ajax. For that it needs to use AjaxFormSubmitBehavior.
     * This form is needed to prevent the submit of all other form components
     */
    var fileForm: Form<*>? = null

    @Inject
    lateinit var propertiesCache: PropertiesCache

    /**
     * A behavior that is needed to be able to upload the files
     * by using the *Upload* button with Ajax
     */
    private var ajaxUploadBehavior: AjaxFormSubmitBehavior? = null
    val config: FileInputConfig

    protected fun containingPage() = findPage()::class as KClass<out BasePage>

    override fun onConfigure() {
        super.onConfigure()
        if (ajaxUploadBehavior == null && config.showUpload()) {
            val ajaxEventName = Strings2.getMarkupId(this).toString() + AJAX_EVENT_NAME_SUFFIX
            ajaxUploadBehavior = newAjaxFormSubmitBehavior(ajaxEventName)
            add(ajaxUploadBehavior)
        } else if (ajaxUploadBehavior != null && !config.showUpload()) {
            remove(ajaxUploadBehavior)
            ajaxUploadBehavior = null
        }
    }

    override fun onComponentTag(tag: ComponentTag) {
        super.onComponentTag(tag)
        val attrs = tag.attributes
        if (config.get(FileInputConfig.MaxFileCount) == 1) {
            attrs.remove("multiple")
        } else {
            attrs["multiple"] = "multiple"
        }
    }

    /**
     * Creates the special Ajax behavior that is used to upload the file(s)
     * with Ajax by using the *Upload* button
     *
     * @param event The name of the JavaScript event that will trigger the Ajax upload
     * @return The Ajax behavior for the file upload
     */
    protected fun newAjaxFormSubmitBehavior(event: String?): AjaxFormSubmitBehavior {
        return object : AjaxFormSubmitBehavior(fileForm, event) {
            override fun onSubmit(target: AjaxRequestTarget) {
                target.add(fileForm)
                this@CustomBootstrapFileInputField.onSubmit(target)
            }

            override fun onAfterSubmit(target: AjaxRequestTarget) {
                super.onAfterSubmit(target)
                this@CustomBootstrapFileInputField.onAfterSubmit(target)
            }

            override fun onError(target: AjaxRequestTarget) {
                this@CustomBootstrapFileInputField.onError(target)
            }
        }
    }

    /**
     * A callback method that is called when there is an error during
     * an Ajax file upload
     *
     * @param target The Ajax request handler
     */
    protected fun onError(target: AjaxRequestTarget?) {}

    /**
     * A callback method that is called on successful file upload triggered
     * by the usage of the *Upload* button.
     *
     * @param target The Ajax request handler
     */
    protected fun onSubmit(target: AjaxRequestTarget?) {}

    /**
     * A callback method that is called after successful file upload triggered
     * by the usage of the *Upload* button.
     *
     * @param target the [AjaxRequestTarget]
     */
    protected fun onAfterSubmit(target: AjaxRequestTarget?) {}
    override fun renderHead(response: IHeaderResponse) {
        FileinputJsReference.INSTANCE.renderHead(response)
        val fileinputJS = JQuery.`$`(this).chain("fileinput", config)
        var ajaxUpload = ""
        if (ajaxUploadBehavior != null) {
            val tmpl = PackageTextTemplate(CustomBootstrapFileInputField::class.java, "/static/js/fileinput.tmpl.js")
            val variables: MutableMap<String, Any?> = HashMap()
            variables["markupId"] = Strings2.getMarkupId(this)
            for (label in LABELS) {
                variables[label] = propertiesCache.translator.translate(label)
            }

            variables["eventName"] = ajaxUploadBehavior!!.event
            ajaxUpload = tmpl.asString(variables)
        }
        response.render(OnDomReadyHeaderItem.forScript(fileinputJS.get() + ajaxUpload))
    }

    companion object {
        /**
         * Make sure that this class is inside the uploadClass config, otherwise the ajax behavior will not work!
         */
        const val JQUERY_IDENTIFIER_UPLOAD_BUTTON_CLASS = "fileinput-upload-button"
        private const val AJAX_EVENT_NAME_SUFFIX = "_fileinput-upload-button-clicked"

        /**
         * The labels supported by Bootstrap FileInput
         */
        private val LABELS =
            arrayOf("browseLabel", "removeLabel", "uploadLabel", "msgLoading", "msgProgress", "msgSelected")
    }
    /**
     * Constructor
     *
     * @param id The component id
     * @param model The model that will store the uploaded files
     * @param config The configuration for this file input
     */
    /**
     * Constructor
     *
     * @param id The component id
     * @param model The model that will store the uploaded files
     */
    /**
     * Constructor
     *
     * @param id The component id
     */
    init {
        this.config = Args.notNull(config, "config")
        if (!config.uploadClass().contains(JQUERY_IDENTIFIER_UPLOAD_BUTTON_CLASS)) {
            val uploadClass = config.uploadClass() + " " + JQUERY_IDENTIFIER_UPLOAD_BUTTON_CLASS
            config.uploadClass(uploadClass)
        }
    }
}