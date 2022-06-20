package nl.maas.bankbook.frontend.wicket.components

import org.apache.wicket.AttributeModifier
import org.apache.wicket.Component
import org.apache.wicket.markup.html.WebMarkupContainer
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.markup.html.form.FormComponent
import org.apache.wicket.markup.html.list.ListItem
import org.apache.wicket.markup.html.list.ListView
import org.apache.wicket.markup.html.panel.Fragment
import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.model.Model
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

class DynamicPanel(id: String) : Panel(id) {

    companion object {
        val CONTENT_ID = "content";
    }

    private val pageConfig: MutableMap<String, Pair<Int, IntArray>> = mutableMapOf()
    private var rows: MutableMap<String, Pair<WebMarkupContainer, Array<WebMarkupContainer>>> = mutableMapOf()
    private var contents: MutableMap<Pair<Int, Component>, String> = mutableMapOf()

    override fun onBeforeRender() {
        super.onBeforeRender()
        setUpPanel()
//        addContents()
    }

    private fun setUpPanel() {
        val rows = object : ListView<String>("rows", pageConfig.keys.toMutableList()) {
            override fun populateItem(row: ListItem<String>) {
                row.addOrReplace(createRow(row.modelObject))
            }

            private fun createRow(rowName: String): WebMarkupContainer {
                val webMarkupContainer = object : WebMarkupContainer("row") {
                    override fun onBeforeRender() {
                        super.onBeforeRender()
                        add(
                            AttributeModifier.append(
                                "class",
                                "rw-${pageConfig[rowName]!!.first}"
                            )
                        )
                    }
                }
                val columns = (0..pageConfig[rowName]!!.second.size - 1).map { index ->
                    object : WebMarkupContainer("column") {init {
                        outputMarkupId = true
                    }

                        override fun onBeforeRender() {
                            super.onBeforeRender()
                            add(
                                AttributeModifier.append(
                                    "class",
                                    "col-${pageConfig[rowName]!!.second[index]}"
                                )
                            )
                            addOrReplace(getContent())
                        }

                        private fun getContent(): Component {
                            val component = contents.filter { it.value.equals(rowName) && it.key.first == index }
                                .map { it.key.second }.first()

                            return if (FormComponent::class.isInstance(component)) InputFragment(
                                component as FormComponent<*>, (component as FormComponent<*>).defaultLabel
                            ) else NormalFragment(component)
                        }

                    } as WebMarkupContainer
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


//    private fun addContents() {
//        contents.forEach { row ->
//            rows[row.value]!!.second[row.key.first].addOrReplace(
//                if (FormComponent::class.isInstance(row.key.second)) InputFragment(
//                    row.key.second as FormComponent<*>, (row.key.second as FormComponent<*>).defaultLabel
//                ) else NormalFragment(row.key.second)
//            )
//        }
//    }

    fun addRow(name: String, height: Int, vararg columnSizes: Int): DynamicPanel {
        require(columnSizes.sumOf { it } <= 12, { "Column sizes should add up to 12" })
        pageConfig.put(name, height to columnSizes)
        return this
    }

    fun addRows(vararg rows: Row): DynamicPanel {
        rows.forEach { addRow(it.name, it.height, *it.columnWidths) }
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

    fun <T : Component> addOrReplaceToColumn(
        rowName: String,
        columnIndex: Int,
        content: T
    ): DynamicPanel {
        contents.put(columnIndex to content, rowName)
        return this
    }

    fun getRow(name: String): WebMarkupContainer {
        require(rows.keys.contains(name))
        return rows[name]!!.first
    }

    fun getColumn(name: String, columnIndex: Int): WebMarkupContainer {
        require(rows.keys.contains(name))
        require(rows[name]!!.second.size > columnIndex)
        return rows[name]!!.second[columnIndex]
    }

    private inner class InputFragment(private val component: FormComponent<*>, private val label: String = "Input") :
        Fragment("fragment", "inputFragment", this) {
        override fun onBeforeRender() {
            super.onBeforeRender()
            addOrReplace(component, Label("label", label))
        }
    }

    private inner class NormalFragment(val component: Component) : Fragment("fragment", "normalFragment", this) {
        override fun onBeforeRender() {
            super.onBeforeRender()
            addOrReplace(component)
        }
    }

    class Row(
        val name: String,
        val height: Int,
        vararg val columnWidths: Int
    ) {

        companion object {
            fun from(
                name: String,
                height: Int,
                vararg columnWidths: Int
            ) = Row(name, height, *columnWidths)
        }

        init {
            require(height.mod(5) == 0, { "Row height can only be set in steps of '5'" })
        }
    }


}
