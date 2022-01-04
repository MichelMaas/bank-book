package nl.maas.bankbook.frontend.wicket.panels

import de.martinspielmann.wicket.chartjs.chart.impl.Bar
import de.martinspielmann.wicket.chartjs.core.internal.IndexableOption
import de.martinspielmann.wicket.chartjs.data.dataset.BarDataset
import de.martinspielmann.wicket.chartjs.data.dataset.property.TextLabel
import de.martinspielmann.wicket.chartjs.data.dataset.property.data.Data
import de.martinspielmann.wicket.chartjs.panel.BarChartPanel
import nl.maas.fxanalyzer.frontend.wicket.caches.PropertiesCache
import nl.maas.fxanalyzer.frontend.wicket.objects.Colors
import nl.maas.fxanalyzer.frontend.wicket.tools.BigDecimalSafeNumberDataValue
import org.apache.wicket.model.IModel
import org.apache.wicket.model.LoadableDetachableModel
import javax.inject.Inject

class BarPanel<T : Number>(
    id: String,
    val label: String,
    val data: Map<String, Map<String, T>>
) : AbstractPanel(id) {

    @Inject
    lateinit var propertiesCache: PropertiesCache

    override fun onBeforeRender() {
        super.onBeforeRender()
        addOrReplace(BarChartPanel("graph", createBarData()))
    }

    private fun createBarData(): IModel<out Bar> {
        val bar = Bar()
        val labels = mutableSetOf<String>()
        data.keys.forEach {
            labels.addAll(data[it]!!.keys.map { propertiesCache.translator.translate(containingPage(), it) })
            val barDataSet = BarDataset()
            barDataSet.label = propertiesCache.translator.translate(containingPage(), it)
            barDataSet.data = Data(BigDecimalSafeNumberDataValue.of(data[it]!!.values))
            bar.data.datasets.add(barDataSet)
        }
        bar.data.labels.addAll(TextLabel.of(labels.toList()))
        bar.data.datasets.forEachIndexed { index, ds ->
            ds.backgroundColor = IndexableOption(Colors.colors()[index])
        }
        return object : LoadableDetachableModel<Bar>(bar) {
            override fun load(): Bar {
                return defaultModelObject as Bar
            }
        }

    }
}