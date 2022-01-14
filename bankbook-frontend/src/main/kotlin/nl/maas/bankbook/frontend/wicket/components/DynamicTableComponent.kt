package nl.maas.bankbook.frontend.wicket.components

import nl.maas.bankbook.frontend.wicket.objects.Tuple
import org.apache.commons.lang3.StringUtils
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.markup.html.list.ListItem
import org.apache.wicket.markup.html.list.ListView
import org.apache.wicket.markup.html.panel.Panel
import java.io.Serializable
import java.math.BigInteger

class DynamicTableComponent(
    id: String,
    val tuples: MutableList<Tuple>
) : Panel(id) {
    init {
        if (tuples.isEmpty()) {
            tuples.add(Tuple(mapOf(Pair("NOTHING", StringUtils.EMPTY))))
        }
        require(tuples.all { it.equals(tuples.first()) })
    }

    override fun onBeforeRender() {
        super.onBeforeRender()
        addOrReplace(HeaderRepeater(), TupleRepeater())
    }

    private inner class HeaderRepeater() : ListView<String>("columnHeader", tuples.first().columns.keys.toList()) {
        override fun populateItem(item: ListItem<String>) {
            item.add(Label("columnLabel", item.modelObject))
        }

    }

    private inner class TupleRepeater() : ListView<Tuple>("tuple", tuples) {
        override fun populateItem(item: ListItem<Tuple>) {
            item.add(ColumnRepeater(item.modelObject.columns.values.toList()))
        }
    }

    private inner class ColumnRepeater(val columns: List<Serializable>) : ListView<Serializable>("column", columns) {
        override fun populateItem(item: ListItem<Serializable>) {
            item.add(
                TooltipLabel(
                    "content", item.modelObject.toString(),
                    BigInteger.valueOf(150).div(columns.size.toBigInteger()).toInt()
                )
            )
        }
    }


}