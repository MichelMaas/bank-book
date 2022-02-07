package nl.maas.bankbook.frontend.wicket.components

import org.apache.wicket.ajax.AjaxEventBehavior
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior
import org.apache.wicket.markup.html.form.TextField
import org.apache.wicket.model.IModel

abstract class AjaxSearchField(id: String, model: IModel<String>) : TextField<String>(id, model) {

    init {
        add(object : OnChangeAjaxBehavior() {
            override fun onUpdate(target: AjaxRequestTarget) {
                this@AjaxSearchField.onChange(target)
            }
        }, object : AjaxEventBehavior("keyup") {
            override fun onEvent(p0: AjaxRequestTarget?) {
                println("key up")
            }

        })
    }

    open protected fun onChange(target: AjaxRequestTarget) {}

    open protected fun onEnter(target: AjaxRequestTarget) {}

}