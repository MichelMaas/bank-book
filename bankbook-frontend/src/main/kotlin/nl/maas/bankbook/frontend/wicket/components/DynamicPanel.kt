package nl.maas.bankbook.frontend.wicket.components

import org.apache.wicket.AttributeModifier
import org.apache.wicket.Component
import org.apache.wicket.markup.html.WebMarkupContainer
import org.apache.wicket.markup.html.list.ListItem
import org.apache.wicket.markup.html.list.ListView
import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.model.Model
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

class DynamicPanel(id: String) : Panel(id) {

    val pageConfig: MutableMap<String, IntArray> = mutableMapOf()
    var rows: MutableMap<String, Pair<WebMarkupContainer, Array<WebMarkupContainer>>> = mutableMapOf()
    var contents: MutableMap<Pair<Int, Component>, String> = mutableMapOf()

    override fun onBeforeRender() {
        super.onBeforeRender()
        setUpPanel()
        addContents()
    }

    private fun setUpPanel() {
        val rows = object : ListView<String>("rows", pageConfig.keys.toMutableList()) {
            override fun populateItem(row: ListItem<String>) {
                row.addOrReplace(createRow(row.modelObject))
            }

            private fun createRow(rowName: String): WebMarkupContainer {
                val webMarkupContainer = WebMarkupContainer("row")
                val columns = (0..pageConfig[rowName]!!.size - 1).map { index ->
                    WebMarkupContainer("column").add(
                        AttributeModifier.append(
                            "class",
                            "col-${pageConfig[rowName]!![index]}"
                        )
                    ) as WebMarkupContainer
                }
                rows.put(rowName, webMarkupContainer to columns.toTypedArray())
                val cols = object : ListView<WebMarkupContainer>("columns", columns) {
                    override fun populateItem(column: ListItem<WebMarkupContainer>) {
                        column.addOrReplace(column.modelObject)
                    }
                }
                webMarkupContainer.addOrReplace(cols)
                return webMarkupContainer
            }
        }
        addOrReplace(rows)
    }

    private fun addContents() {
        contents.forEach { row ->
            rows[row.value]!!.second[row.key.first].addOrReplace(row.key.second)
        }
    }

    fun addRow(name: String, vararg columnSizes: Int): DynamicPanel {
        require(columnSizes.sumOf { it } <= 12, { "Column sizes should add up to 12" })
        pageConfig.put(name, columnSizes)
        return this
    }

    fun addRows(vararg rows: Pair<String, IntArray>): DynamicPanel {
        rows.forEach { addRow(it.first, *it.second) }
        return this
    }

    fun <T : Component> addOrReplaceToColumn(
        rowName: String,
        columnIndex: Int,
        clazz: KClass<T>,
        model: Model<*>? = null
    ): DynamicPanel {
        val content =
            clazz.constructors.find {
                it.parameters.size == 2 && (it.parameters[1].type.classifier as KClass<*>?)?.isSuperclassOf(
                    Model::class
                ) ?: false
            }!!.call("content", model)
        contents.put(columnIndex to content, rowName)
        return this
    }

    fun getRow(name: String): WebMarkupContainer {
        require(rows.keys.contains(name))
        return rows[name]!!.first
    }

    fun getColumn(name: String, columnIndex: Int): WebMarkupContainer {
        require(rows.keys.contains(name))
        require(rows[name]!!.second.size <= columnIndex)
        return rows[name]!!.second[columnIndex]
    }
}