package nl.maas.bankbook.frontend.wicket.components

import org.apache.wicket.AttributeModifier
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.model.Model

class TooltipLabel(id: String, val text: String, val maxColumnSize: Int = 50) : Label(id) {
    override fun onBeforeRender() {
        super.onBeforeRender()
        sizeText();
    }

    protected fun sizeText() {
        val tooLarge = text.length > maxColumnSize
        var slice = text
        if (tooLarge) {
            slice = text.slice(0..maxColumnSize - 4).padEnd(maxColumnSize, '.')
            add(
                AttributeModifier("data-toggle", "tooltip"),
                AttributeModifier("data-placement", "bottom"),
                AttributeModifier("title", text)
            )
        }
        defaultModel = Model.of(slice)
    }
}