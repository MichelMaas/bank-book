package nl.maas.bankbook.frontend.wicket.components

import org.apache.wicket.AttributeModifier
import org.apache.wicket.markup.html.WebComponent

class Favicon(id: String?) : WebComponent(id) {
    public override fun onInitialize() {
        super.onInitialize()
        add(AttributeModifier.replace("href", "open/images/icon.png"))
    }
}