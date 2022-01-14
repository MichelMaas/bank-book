package nl.maas.bankbook.frontend.wicket.pages

import de.agilecoders.wicket.themes.markup.html.bootswatch.BootswatchTheme
import nl.maas.bankbook.frontend.WicketApplication
import nl.maas.bankbook.frontend.wicket.components.DynamicFormComponent
import nl.maas.bankbook.frontend.wicket.config.BootstrapConfig
import nl.maas.bankbook.frontend.wicket.objects.Options
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.model.CompoundPropertyModel
import org.apache.wicket.model.Model
import org.apache.wicket.request.mapper.parameter.PageParameters
import javax.inject.Inject

open class OptionsPage(parameters: PageParameters) : BasePage(parameters) {

    @Inject
    lateinit var bootstrapConfig: BootstrapConfig

    override fun onBeforeRender() {
        super.onBeforeRender()
        addOrReplace(createForm())
    }

    private fun createForm(): DynamicFormComponent<Options> {
        val form = object : DynamicFormComponent<Options>(
            "panel", propertiesCache.translator.translate(OptionsPage::class, "title"), CompoundPropertyModel.of(
                Model.of(propertiesCache.options)
            )
        ) {
            override fun onSubmit(target: AjaxRequestTarget) {
                (defaultModelObject as Options).store()
                target.add(this@OptionsPage)
                WicketApplication.restart()
            }

        }.addSelect(
            "language",
            propertiesCache.translator.translate(OptionsPage::class, "Language"),
            propertiesCache.translator.supportedLanguages
        ).addSelect(
            "theme",
            propertiesCache.translator.translate(OptionsPage::class, "Theme"),
            BootswatchTheme.values().toList().map { it.name }
        ).addTextBox("watchedFolder", "Folder to monitor")
        return form
    }

}