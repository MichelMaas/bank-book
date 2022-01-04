package nl.maas.bankbook.frontend.wicket.components

import org.apache.commons.lang3.StringUtils
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.model.CompoundPropertyModel

class SingleDataViewPanel(id: String, val value: Pair<String, String>) : Panel(id, CompoundPropertyModel.of(value)) {
    var unit = StringUtils.EMPTY
    var unitBefore = true

    constructor(id: String, value: Pair<String, String>, unit: String, unitBefore: Boolean?) : this(id, value) {
        this.unit = unit
        this.unitBefore = unitBefore ?: true
    }

    override fun onBeforeRender() {
        super.onBeforeRender()
        addOrReplace(Label("first"), Label("second"))
        if (unitBefore) {
            addOrReplace(Label("unitB", "$unit "), Label("unitA", StringUtils.EMPTY))
        } else {
            addOrReplace(Label("unitA", " $unit"), Label("unitB", StringUtils.EMPTY))
        }
    }
}