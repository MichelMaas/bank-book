package nl.maas.bankbook.frontend.wicket.panels

import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import nl.maas.bankbook.frontend.translation.CachingGoogleTranslator
import nl.maas.bankbook.frontend.translation.Translation
import nl.maas.wicket.framework.components.base.CollapsablePanel
import nl.maas.wicket.framework.components.base.CollapsablePanelGroup
import nl.maas.wicket.framework.components.base.DynamicFormComponent
import nl.maas.wicket.framework.components.base.DynamicPanel
import nl.maas.wicket.framework.components.base.DynamicPanel.Companion.ROW_CONTENT_ID
import nl.maas.wicket.framework.panels.RIAPanel
import org.apache.wicket.Component
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.model.CompoundPropertyModel
import org.apache.wicket.spring.injection.annot.SpringBean

class SettingsPanel : RIAPanel() {

    @SpringBean
    private lateinit var translator: CachingGoogleTranslator

    override fun onBeforeRender() {
        super.onBeforeRender()
        addOrReplace(createContent())
    }

    private fun createContent(): Component {
        return DynamicPanel("panel").addRow("Collapsables", 12)
            .addOrReplaceComponentToColumn("Collapsables", 0, createCollapsables())
    }

    private fun createCollapsables(): Component {
        return CollapsablePanelGroup(ROW_CONTENT_ID, 60, createTranslationsSettings())
    }

    private fun createTranslationsSettings(): CollapsablePanel {
        return CollapsablePanel(CollapsablePanelGroup.CONTENT_ID, "Translations", createTranslationsForms())
    }

    private fun createTranslationsForms(): Component {
        val collapsablePanels =
            runBlocking {
                translator.translations().sortedBy { it.translation }
                    .map { async { createTranslationForm(it) }.await() }
                    .toTypedArray()
            }
        return runBlocking {
            async {
                CollapsablePanelGroup(
                    CollapsablePanel.CONTENT_ID, 40,
                    *collapsablePanels
                )
            }.await()
        }
    }

    private fun createTranslationForm(it: Translation): CollapsablePanel {
        return CollapsablePanel(
            CollapsablePanelGroup.CONTENT_ID,
            it.translation,
            createForm(it),
            collapseButtonType = Buttons.Type.Outline_Primary
        )
    }

    private fun createForm(it: Translation): Component {
        return object :
            DynamicFormComponent<Translation>(CollapsablePanel.CONTENT_ID, "", CompoundPropertyModel.of(it)) {
            override fun onSubmit(target: AjaxRequestTarget, typedModelObject: Translation) {
                super.onSubmit(target, typedModelObject)
                this@SettingsPanel.translator.updateTranslations(
                    typedModelObject.original,
                    typedModelObject.translation
                )
            }

            override fun onSubmitCompleted(target: AjaxRequestTarget, typedModelObject: Translation) {
                super.onSubmitCompleted(target, typedModelObject)
                reload(target)
            }
        }.addTextBox("translation", it.original)
    }
}