package nl.maas.bankbook.frontend.wicket.panels

import de.martinspielmann.wicket.chartjs.chart.impl.Line
import de.martinspielmann.wicket.chartjs.data.dataset.LineDataset
import de.martinspielmann.wicket.chartjs.data.dataset.property.TextLabel
import de.martinspielmann.wicket.chartjs.data.dataset.property.data.Data
import de.martinspielmann.wicket.chartjs.panel.LineChartPanel
import nl.maas.fxanalyzer.frontend.wicket.caches.PropertiesCache
import nl.maas.fxanalyzer.frontend.wicket.objects.Colors
import nl.maas.fxanalyzer.frontend.wicket.tools.BigDecimalSafeNumberDataValue
import org.apache.wicket.model.IModel
import org.apache.wicket.model.LoadableDetachableModel
import javax.inject.Inject

class LinePanel<T : Comparable<T>>(
    id: String,
    val data: Map<String, Map<String, Number>>,
    val xLabel: String? = "X",
    val yLabel: String? = "Y",
) : AbstractPanel(id) {

    @Inject
    lateinit var propertiesCache: PropertiesCache

    override fun onInitialize() {
        super.onInitialize()
        addOrReplace(LineChartPanel("graph", createData()))
    }

    private fun createData(): IModel<out Line> {
        val line = Line()
        val labelsList = mutableSetOf<String>()
//        line.data.labels
//            .addAll(TextLabel.of(data.keys.toList()))
        data.keys.forEachIndexed { index, lineKey ->
            val data = this.data[lineKey]
            labelsList.addAll(data!!.keys.map { propertiesCache.translator.translate(containingPage(), it) })
            val lineDataset = LineDataset()
            lineDataset.label = propertiesCache.translator.translate(containingPage(), lineKey)
            lineDataset.setxAxisID(xLabel)
            lineDataset.setyAxisID(yLabel)
            lineDataset.data = Data(BigDecimalSafeNumberDataValue.of(data.values))
            lineDataset.borderColor = Colors.colors()[index]
            line.data.datasets.add(lineDataset)
        }
        line.data.labels.addAll(TextLabel.of(labelsList.toList()))
        return object : LoadableDetachableModel<Line>(line) {
            override fun load(): Line {
                return defaultModelObject as Line
            }
        }
    }
}