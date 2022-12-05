package nl.maas.bankbook.frontend.wicket.panels

import de.martinspielmann.wicket.chartjs.chart.impl.Bar
import de.martinspielmann.wicket.chartjs.core.internal.IndexableOption
import de.martinspielmann.wicket.chartjs.data.dataset.BarDataset
import de.martinspielmann.wicket.chartjs.data.dataset.property.TextLabel
import de.martinspielmann.wicket.chartjs.data.dataset.property.data.Data
import de.martinspielmann.wicket.chartjs.data.dataset.property.data.NumberDataValue
import de.martinspielmann.wicket.chartjs.panel.BarChartPanel
import nl.maas.bankbook.frontend.wicket.caches.PropertiesCache
import nl.maas.bankbook.frontend.wicket.objects.Colors
import org.apache.wicket.model.IModel
import org.apache.wicket.model.LoadableDetachableModel
import javax.inject.Inject

class TablePanel<T : Comparable<T>>(
    id: String,
    val data: Map<String, Map<String, T>>
) : AbstractPanel(id) {

    @Inject
    lateinit var propertiesCache: PropertiesCache

    override fun onInitialize() {
        super.onInitialize()
        addOrReplace(BarChartPanel("graph", createBarData()))
    }

    private fun createBarData(): IModel<out Bar> {
        require(data[data.keys.last()]?.values?.last().toString().matches("-?\\d+(\\.\\d+)?".toRegex()))
        val bar = Bar()
        val labels = mutableSetOf<String>()
        data.keys.forEach { key ->
            labels.addAll(data[key]!!.keys.map { propertiesCache.translator.translate(it) })
            val barDataSet = BarDataset()
            barDataSet.label = propertiesCache.translator.translate(key)
            barDataSet.data = Data(NumberDataValue.of(data[key]!!.values.toList().map { it as Number }))
            barDataSet.backgroundColor =
                IndexableOption(Colors.colors())
            bar.data.datasets.add(barDataSet)
        }
        bar.data.labels.addAll(TextLabel.of(labels.toList()))
        return object : LoadableDetachableModel<Bar>(bar) {
            override fun load(): Bar {
                return defaultModelObject as Bar
            }
        }

    }
}