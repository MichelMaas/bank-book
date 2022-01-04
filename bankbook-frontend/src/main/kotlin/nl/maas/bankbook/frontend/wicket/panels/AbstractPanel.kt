package nl.maas.bankbook.frontend.wicket.panels

import nl.maas.fxanalyzer.frontend.wicket.pages.BasePage
import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.model.IModel
import kotlin.reflect.KClass

abstract class AbstractPanel(id: String, model: IModel<*>?) : Panel(id, model) {

    constructor(id: String) : this(id, null)

    protected fun containingPage() = findPage()::class as KClass<out BasePage>
}