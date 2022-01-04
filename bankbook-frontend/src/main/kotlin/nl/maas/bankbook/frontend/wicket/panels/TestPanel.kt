package nl.maas.bankbook.frontend.wicket.panels

import de.agilecoders.wicket.core.markup.html.bootstrap.components.progress.UpdatableProgressBar
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.ajax.markup.html.AjaxLink
import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.model.IModel
import org.apache.wicket.model.Model

class TestPanel : Panel {
    constructor(id: String) : super(id)

    val striped = object : UpdatableProgressBar("content", Model.of(0)) {
        override fun newValue(): IModel<Int> {
            return Model.of(this.modelObject + 10)
        }
    }.active(true)

    init {
        outputMarkupId = true
    }

    override fun onBeforeRender() {
        super.onBeforeRender()
        striped.setOutputMarkupId(true)
        add(object : AjaxLink<String>("button") {
            override fun onClick(target: AjaxRequestTarget) {
                start(target)
            }
        })
        add(striped)
    }

    fun start(target: AjaxRequestTarget) {
        while (!striped.complete()) {
            Thread.sleep(1000)
            target.add(striped.parent)
        }
    }


}