package nl.maas.bankbook.frontend.wicket.panels

import de.martinspielmann.wicket.chartjs.chart.impl.Pie
import de.martinspielmann.wicket.chartjs.core.internal.IndexableOption
import de.martinspielmann.wicket.chartjs.data.dataset.PieDataset
import de.martinspielmann.wicket.chartjs.data.dataset.property.TextLabel
import de.martinspielmann.wicket.chartjs.data.dataset.property.data.Data
import de.martinspielmann.wicket.chartjs.data.dataset.property.data.NumberDataValue
import de.martinspielmann.wicket.chartjs.panel.PieChartPanel
import nl.maas.bankbook.frontend.wicket.caches.PropertiesCache
import nl.maas.bankbook.frontend.wicket.objects.Colors
import org.apache.wicket.model.IModel
import org.apache.wicket.model.LoadableDetachableModel
import javax.inject.Inject

class PiePanel<T : Comparable<T>>(
    id: String,
    val label: String,
    val names: List<String>,
    val data: List<out Number>
) : AbstractPanel(id) {

    @Inject
    lateinit var propertiesCache: PropertiesCache

    override fun onInitialize() {
        super.onInitialize()
        if (!names.size.equals(data.size)) {
            throw IllegalStateException("In a pie chart, the number of names (${names.size}) should equal the number of data values (${data.size})!")
        }
        addOrReplace(PieChartPanel("graph", createBarData()))
    }

    private fun createBarData(): IModel<out Pie> {
        val pie = Pie()
        pie.data.labels
            .addAll(TextLabel.of(names.map { propertiesCache.translator.translate(containingPage(), it) }))
        val pieDataset = PieDataset()
        pieDataset.label = propertiesCache.translator.translate(containingPage(), this.label)
        pieDataset.data = Data(NumberDataValue.of(data))
        pieDataset.backgroundColor = IndexableOption(Colors.colors())
        pie.data.datasets.add(pieDataset)
        return object : LoadableDetachableModel<Pie>(pie) {
            override fun load(): Pie {
                return defaultModelObject as Pie
            }
        }
    }
}