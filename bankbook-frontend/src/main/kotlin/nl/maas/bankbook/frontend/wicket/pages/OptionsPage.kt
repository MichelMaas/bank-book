package nl.maas.bankbook.frontend.wicket.pages

import nl.maas.bankbook.frontend.services.CookieUtil
import nl.maas.bankbook.frontend.wicket.components.DynamicFormComponent
import nl.maas.bankbook.frontend.wicket.objects.Options
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.model.CompoundPropertyModel
import org.apache.wicket.model.Model

open class OptionsPage : BasePage() {

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
                val options = (defaultModelObject as Options).store()
                CookieUtil.saveLanguageCookie(options.language)
                target.add(this@OptionsPage)
//                WicketApplication.restart()
            }

        }.addSelect(
            "language",
            propertiesCache.translator.translate(OptionsPage::class, "Language"),
            propertiesCache.translator.supportedLanguages
        ).addTextBox("watchedFolder", propertiesCache.translator.translate(OptionsPage::class, "Folder"))
        return form
    }

}