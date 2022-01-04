package nl.maas.bankbook.frontend.wicket.panels

import nl.maas.fxanalyzer.frontend.wicket.caches.PropertiesCache
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.markup.html.list.ListItem
import org.apache.wicket.markup.html.list.ListView
import org.apache.wicket.model.IModel
import javax.inject.Inject
import kotlin.reflect.full.memberProperties


class TransfersPanel(id: String, model: IModel<FXDataSet>) : AbstractPanel(id, model) {

    @Inject
    lateinit var propertiesCache: PropertiesCache

    override fun onBeforeRender() {
        super.onBeforeRender()
        addHeader()
        addMembers()
    }

    fun addHeader() {
        val headerLabels = arrayOf(
            "Ticket",
            "Time",
            "Type",
            "Amount"
        )
        headerLabels.forEach { addOrReplace(Label(it, propertiesCache.translator.translate(containingPage(), it))) }
    }

    fun addMembers() {
        val transfers = (defaultModelObject as FXDataSet).transfers
        val view = object : ListView<Transfer>("transfers", transfers.toList()) {
            override fun populateItem(item: ListItem<Transfer>) {
                Transfer::class.memberProperties
                    .forEach { item.addOrReplace(Label(it.name, it.getter.call(item.modelObject)?.toString())) }
            }
        }

        addOrReplace(view)
    }


}