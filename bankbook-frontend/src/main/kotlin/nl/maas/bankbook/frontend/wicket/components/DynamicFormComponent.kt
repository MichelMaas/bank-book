package nl.maas.bankbook.frontend.wicket.components

import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.fileinput.BootstrapFileInputField
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.fileinput.FileInputConfig
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.select.BootstrapSelect
import nl.maas.bankbook.frontend.wicket.caches.PropertiesCache
import nl.maas.bankbook.frontend.wicket.pages.BasePage
import nl.maas.bankbook.frontend.wicket.panels.AbstractPanel
import org.apache.wicket.MarkupContainer
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.ajax.markup.html.AjaxLink
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink
import org.apache.wicket.markup.head.IHeaderResponse
import org.apache.wicket.markup.head.OnDomReadyHeaderItem
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.markup.html.form.CheckBox
import org.apache.wicket.markup.html.form.ChoiceRenderer
import org.apache.wicket.markup.html.form.Form
import org.apache.wicket.markup.html.form.TextField
import org.apache.wicket.markup.html.form.upload.FileUpload
import org.apache.wicket.markup.html.panel.Fragment
import org.apache.wicket.markup.repeater.RepeatingView
import org.apache.wicket.model.CompoundPropertyModel
import org.apache.wicket.model.IComponentInheritedModel
import org.apache.wicket.model.IModel
import org.apache.wicket.model.Model
import java.io.Serializable
import javax.inject.Inject
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.javaType

open class DynamicFormComponent<T>(id: String, val formTitle: String, model: IComponentInheritedModel<T>) :
    AbstractPanel(id, model) {
    @Inject
    lateinit var propertiesCache: PropertiesCache
    private val formComponents = RepeatingView("fragments")
    private val form = InnerForm(model)
    val changedProperties = mutableListOf<String>()

    init {
        outputMarkupPlaceholderTag = true
    }

    fun isMultiPart(value: Boolean) {
        form.isMultiPart = value
    }

    fun isMultiPart(): Boolean {
        return form.isMultiPart
    }

    override fun onBeforeRender() {
        super.onBeforeRender()
        form.addOrReplace(Label("formTitle", Model.of(formTitle)))
        form.addOrReplace(formComponents)
        addOrReplace(form)
    }

    fun addPlainText(id: String, label: String, value: IComponentInheritedModel<String>): DynamicFormComponent<T> {
        formComponents.add(TextFragment(id, label, value))
        return this
    }

    fun addTextBox(id: String, label: String): DynamicFormComponent<T> {
        formComponents.add(TextBoxFragment<Serializable>(id, label))
        return this
    }

    fun addCheckBox(id: String, label: String, defaultOn: Boolean = false): DynamicFormComponent<T> {
        formComponents.add(CheckBoxFragment(id, label, defaultOn))
        return this
    }

    fun addFileUploadField(
        id: String,
        label: String = "File Upload",
        placeholder: String = "Select file",
        browseLabel: String = "Browse",
        cancelLabel: String = "Cancel",
        uploadLabel: String = "Upload",
        removeLabel: String = "Remove"
    ): DynamicFormComponent<T> {
        formComponents.add(
            FileUploadFragment(
                id,
                label,
                placeholder,
                browseLabel,
                cancelLabel,
                uploadLabel,
                removeLabel
            )
        )
        isMultiPart(true)
        return this
    }

    fun <M : Serializable> addSelect(
        id: String,
        label: String,
        options: List<M>
    ): DynamicFormComponent<T> {
        formComponents.addOrReplace(SelectFragment(id, label, Model.ofList(options)))
        return this
    }

    open fun onSubmit(target: AjaxRequestTarget) {
    }

    open fun onAfterSubmit(target: AjaxRequestTarget) {
    }

    open fun onBeforeCancel(target: AjaxRequestTarget) {
    }

    open fun onAfterCancel(target: AjaxRequestTarget) {
    }

    open fun onFileUpload(target: AjaxRequestTarget, fileUpload: FileUpload) {}

    private inner class TextFragment(
        val propertyName: String,
        val label: String,
        val value: IComponentInheritedModel<String>
    ) :
        ResettableFormFragment<String>(
            "${propertyName}-${formComponents.newChildId()}",
            "textFragment",
            this,
            value.`object`
        ) {

        override fun onBeforeRender() {
            super.onBeforeRender()
            addOrReplace(Label("text", value))
            addOrReplace(Label("textLabel", Model.of(label)))
        }

        override fun onReset(target: AjaxRequestTarget, originalValue: String?) {
            defaultModelObject = originalValue
            target.add(this)
        }
    }

    private inner class TextBoxFragment<M : Serializable>(val propertyName: String, val label: String) :
        ResettableFormFragment<M>(
            "propertyName",
            "textBoxFragment",
            this,
            this@DynamicFormComponent.readInstanceProperty(form.modelObject, propertyName) as M
        ) {
        override fun onBeforeRender() {
            super.onBeforeRender()
            val m = readInstanceProperty(form.modelObject, propertyName)
            addOrReplace(object : TextField<String>("textBox", CompoundPropertyModel.of(m)) {
                override fun onModelChanged() {
                    super.onModelChanged()
                    form.modelObject::class.declaredMemberProperties.filterIsInstance<KMutableProperty<*>>()
                        .find { propertyName.equals(it.name) }?.let {
                            it.setter.call(form.modelObject, modelObject)
                        }
                    changedProperties.add(propertyName)
                }
            })
            addOrReplace(Label("textBoxLabel", label))
        }

        override fun onReset(target: AjaxRequestTarget, originalValue: M?) {
            defaultModelObject = originalValue
            target.add(this)
        }

    }

    private inner class CheckBoxFragment(val propertyName: String, val label: String, val defaultOn: Boolean = false) :
        ResettableFormFragment<Boolean>(
            "propertyName",
            "checkBoxFragment",
            this,
            defaultOn
        ) {
        override fun onBeforeRender() {
            super.onBeforeRender()
            val m = readInstanceProperty(form.modelObject, propertyName).toBoolean().or(defaultOn)
            addOrReplace(object : CheckBox("checkBox", CompoundPropertyModel.of(m)) {
                override fun onModelChanged() {
                    super.onModelChanged()
                    form.modelObject::class.declaredMemberProperties.filterIsInstance<KMutableProperty<*>>()
                        .find { propertyName.equals(it.name) }?.let {
                            it.setter.call(form.modelObject, modelObject)
                        }
                    changedProperties.add(propertyName)
                }
            })
            addOrReplace(Label("checkBoxLabel", label))
        }

        override fun onReset(target: AjaxRequestTarget, originalValue: Boolean?) {
            defaultModelObject = originalValue
            target.add(this)
        }

    }

    private inner class FileUploadFragment(
        val propertyName: String,
        val fieldLabel: String,
        val placeholder: String,
        val browseLabel: String,
        val cancelLabel: String,
        val uploadLabel: String,
        val removeLabel: String
    ) :
        ResettableFormFragment<FileUpload>(propertyName, "fileUploadFragment", this, null) {
        var fileUpload: FileUpload? = null
        private val bootstrapFileInputField = object : BootstrapFileInputField(
            "file",
            Model.ofList(mutableListOf()),
            FileInputConfig().showPreview(false).maxFileCount(1)
                .withLocale(propertiesCache.translator.currentLanguage())
        ) {
            override fun onSubmit(target: AjaxRequestTarget) {
                super.onSubmit(target)
                this@FileUploadFragment.fileUpload = this.fileUpload
                this.isEnabled = false
                target.add(this@DynamicFormComponent.form)
            }
        }

        override fun onBeforeRender() {
            super.onBeforeRender()
            addOrReplace(bootstrapFileInputField)
        }

        override fun renderHead(response: IHeaderResponse) {
            super.renderHead(response)
            response.render(
                OnDomReadyHeaderItem.forScript("${browseButtonJS()};${cancelButtonJS()};${placeholderJS()};${fileLabelJS()};${uploadButtonJS()};${removeButtonJS()}")
            )
        }

        fun browseButtonJS() = "\$( \"span:contains('Browse')\" ).text('${browseLabel}');"

        fun cancelButtonJS() = "\$( \"span:contains('Cancel')\" ).text('${cancelLabel}');"

        fun uploadButtonJS() = "\$( \"span:contains('Upload')\" ).text('${uploadLabel}');"

        fun removeButtonJS() = "\$( \"span:contains('Remove')\" ).text('${removeLabel}');"

        fun placeholderJS() =
            "\$(\$('#${this@DynamicFormComponent.form.markupId}').find(\"input[placeholder='Select file...']\")[0]).attr('placeholder','${placeholder}')"

        fun fileLabelJS() = "\$('label:contains(\"File\")').text('${fieldLabel}')"

        fun onFileUpload(target: AjaxRequestTarget) {
            this.fileUpload?.let {
                this@DynamicFormComponent.onFileUpload(target, it)
            }
        }

        override fun onReset(target: AjaxRequestTarget, fileUpload: FileUpload?) {
            this.fileUpload = null
            bootstrapFileInputField.isEnabled = true
            target.add(this)
        }
    }

    private inner class SelectFragment<M : Serializable>(
        val propertyName: String,
        val label: String,
        val options: IModel<List<M>>,
        vararg val default: M
    ) :
        ResettableFormFragment<M>(
            "${propertyName}-${formComponents.newChildId()}",
            "selectFragment",
            this,
            this@DynamicFormComponent.readInstanceProperty(form.modelObject, propertyName) as M
        ) {
        @OptIn(ExperimentalStdlibApi::class)
        override fun onBeforeRender() {
            super.onBeforeRender()
            val m: Serializable = readInstanceProperty(form.modelObject, propertyName)
            addOrReplace(object :
                BootstrapSelect<Serializable>("select", CompoundPropertyModel.of(m), options, I18NChoiceRenderer()) {

                override fun onBeforeRender() {
                    super.onBeforeRender()
                    if (modelObject == null && !default.isNullOrEmpty()) {
                        modelObject = default
                    }
                }

                override fun onModelChanged() {
                    super.onModelChanged()
                    form.modelObject::class.declaredMemberProperties.filterIsInstance<KMutableProperty<*>>()
                        .find { propertyName.equals(it.name) }?.let {
                            val typeName = it.getter.returnType.javaType.typeName
                            if (Enum::class.isSuperclassOf(Class.forName(typeName).kotlin)) {
                                it.setter.call(form.modelObject, getEnumValue(typeName, modelObject.toString()))
                            } else {
                                it.setter.call(form.modelObject, modelObject)
                            }
                        }
                    changedProperties.add(propertyName)
                }

                fun getEnumValue(enumClassName: String, enumValue: String): Any {
                    val enumClz = Class.forName(enumClassName).enumConstants as Array<Enum<*>>
                    return enumClz.first { it.name.uppercase().equals(enumValue.uppercase()) }
                }
            })
            addOrReplace(Label("selectLabel", label))
        }

        override fun onReset(target: AjaxRequestTarget, originalValue: M?) {
            defaultModelObject = originalValue
            target.add(this)
        }

    }

    private inner class I18NChoiceRenderer : ChoiceRenderer<Serializable>() {
        override fun getDisplayValue(item: Serializable): String {
            return propertiesCache.translator.translate(findBasePage(), item.toString())
        }
    }

    private fun findBasePage(): KClass<out BasePage> {
        return findParent(BasePage::class.java)::class
    }

    private inner class InnerForm(model: IComponentInheritedModel<T>) : Form<T>("dynamicForm", model) {

        init {
            add(object : AjaxSubmitLink("submit", this) {
                override fun onBeforeRender() {
                    super.onBeforeRender()
                    addOrReplace(
                        Label(
                            "submitName",
                            propertiesCache.translator.translate(containingPage(), "confirm")
                        )
                    )
                }

                override fun onSubmit(target: AjaxRequestTarget) {
                    super.onSubmit(target)
                    this@DynamicFormComponent.onSubmit(target)
                    fetchFileUploadFragments().forEach { it.onFileUpload(target) }
                    target.add(this@DynamicFormComponent)
                }

                override fun onAfterSubmit(target: AjaxRequestTarget) {
                    super.onAfterSubmit(target)
                    this@DynamicFormComponent.onAfterSubmit(target)
                    target.add(this@DynamicFormComponent)
                }
            })
            add(object : AjaxLink<String>("reset", Model.of("Reset")) {
                override fun onBeforeRender() {
                    super.onBeforeRender()
                    addOrReplace(Label("resetName", propertiesCache.translator.translate(containingPage(), "reset")))
                }

                override fun onClick(target: AjaxRequestTarget) {
                    this@DynamicFormComponent.onBeforeCancel(target)
                    target.add(this@InnerForm)
                    clearInput()
                    this@DynamicFormComponent.onAfterCancel(target)
                }

            })

        }

        private fun fetchFileUploadFragments(): List<FileUploadFragment> {
            return formComponents.filter { FileUploadFragment::class.isInstance(it) }
                .toList() as List<FileUploadFragment>
        }

    }

    abstract inner class ResettableFormFragment<T>(
        id: String?,
        markupId: String?,
        markupProvider: MarkupContainer?,
        val originalValue: T?
    ) : Fragment(id, markupId, markupProvider) {

        fun reset(target: AjaxRequestTarget) {
            onReset(target, originalValue)
        }

        protected abstract fun onReset(target: AjaxRequestTarget, originalValue: T?)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <I> readInstanceProperty(instance: I, propertyName: String): String {
        val property = instance!!::class.members
            .first { it.name == propertyName } as KProperty1<Any, *>
        val get = property.get(instance)
        return if (get != null) get.toString() else "NONE"
    }


}