package nl.maas.bankbook.frontend.wicket.components

import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapAjaxLink
import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons
import nl.maas.bankbook.providers.Translator
import org.apache.commons.text.StringEscapeUtils
import org.apache.wicket.AttributeModifier
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.head.IHeaderResponse
import org.apache.wicket.markup.head.OnDomReadyHeaderItem
import java.util.*

abstract class SimpleAjaxButton(
    id: String,
    val label: String,
    type: Buttons.Type = Buttons.Type.Primary,
    val size: Size = Size.NORMAL,
    val translator: Translator? = null,
    val block: Boolean = false
) : BootstrapAjaxLink<String>("applyFilters", Buttons.Type.Primary) {


    enum class Size() {
        SMALL,
        NORMAL,
        LARGE;
    }

    init {
        markupId = StringEscapeUtils.escapeHtml4(UUID.randomUUID().toString())
        when (size) {
            Size.LARGE -> add(AttributeModifier.append("class", "btn-lg"))
            Size.SMALL -> add(AttributeModifier.append("class", "btn-sm"))
        }
        if (block) {
            add(AttributeModifier.append("class", "btn-block"))
        }
    }

    override fun renderHead(response: IHeaderResponse) {
        super.renderHead(response)
        response.render(
            OnDomReadyHeaderItem.forScript("\$(\"#${markupId}\").text('${translator?.translate(label) ?: label}');")
        )
    }

    override abstract fun onClick(target: AjaxRequestTarget)
}
