package nl.maas.bankbook.frontend.wicket.pages

import nl.maas.bankbook.frontend.wicket.components.DynamicPanel
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.model.Model
import org.apache.wicket.request.mapper.parameter.PageParameters

class TestPage(parameters: PageParameters?) : BasePage(parameters) {
    override fun onBeforeRender() {
        super.onBeforeRender()
        val dyn = DynamicPanel("panel").addRows(
            "Top" to intArrayOf(4, 8),
            "Middel" to intArrayOf(4, 4, 4),
            "Bodem" to intArrayOf(12)
        ).addOrReplaceToColumn("Top", 0, Label::class, Model.of("Top left"))
            .addOrReplaceToColumn("Top", 1, Label::class, Model.of("Top right"))
            .addOrReplaceToColumn("Middel", 0, Label::class, Model.of("Middle left"))
            .addOrReplaceToColumn("Middel", 2, Label::class, Model.of("Middle right"))
            .addOrReplaceToColumn("Middel", 1, Label::class, Model.of("Middle center"))
            .addOrReplaceToColumn("Bodem", 0, Label::class, Model.of("Bodem"))
        addOrReplace(dyn)

    }
}