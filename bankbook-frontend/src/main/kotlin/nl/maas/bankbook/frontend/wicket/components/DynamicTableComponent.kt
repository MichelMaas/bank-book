package nl.maas.bankbook.frontend.wicket.components

import nl.maas.bankbook.frontend.ContextProvider
import nl.maas.bankbook.frontend.services.TranslationService
import nl.maas.bankbook.frontend.wicket.objects.Tuple
import org.apache.commons.lang3.StringUtils
import org.apache.wicket.ajax.AjaxEventBehavior
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.markup.html.list.ListItem
import org.apache.wicket.markup.html.list.ListView
import org.apache.wicket.markup.html.panel.Panel
import java.io.Serializable
import java.math.BigInteger

open class DynamicTableComponent(
    id: String,
    val tuples: MutableList<Tuple>
) : Panel(id) {

    val translationService: TranslationService

    init {
        if (tuples.isEmpty()) {
            tuples.add(Tuple(mapOf(Pair("NOTHING", StringUtils.EMPTY))))
        }
        require(tuples.all { it.equals(tuples.first()) })
        tuples.sortedBy { it.columns.values.first().toString() }
        translationService = ContextProvider.ctx.getBean(TranslationService::class.java)
    }


    override fun onBeforeRender() {
        super.onBeforeRender()
        addOrReplace(HeaderRepeater(), TupleRepeater())
    }

    private inner class HeaderRepeater() : ListView<String>("columnHeader", tuples.first().columns.keys.toList()) {
        override fun populateItem(item: ListItem<String>) {
            item.add(
                Label(
                    "columnLabel",
                    translationService.translate(item.modelObject)
                )
            )
        }

    }

    private inner class TupleRepeater() : ListView<Tuple>("tuple", tuples) {
        override fun populateItem(item: ListItem<Tuple>) {
            item.add(ColumnRepeater(item.modelObject.columns.values.toList()))
            item.add(object : AjaxEventBehavior("click") {
                override fun onEvent(target: AjaxRequestTarget) {
                    this@DynamicTableComponent.onTupleClick(target, item.modelObject)
                }
            })
        }
    }

    private inner class ColumnRepeater(val columns: List<Serializable>) : ListView<Serializable>("column", columns) {
        override fun populateItem(item: ListItem<Serializable>) {
            item.add(
                TooltipLabel(
                    "content",
                    translationService.translate(item.modelObject.toString()),
                    BigInteger.valueOf(150).div(columns.size.toBigInteger()).toInt()
                )
            )
        }
    }

    open fun onTupleClick(target: AjaxRequestTarget, tuple: Tuple) {

    }

}