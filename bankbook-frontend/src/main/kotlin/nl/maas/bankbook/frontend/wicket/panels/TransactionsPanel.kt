package nl.maas.bankbook.frontend.wicket.panels

import nl.maas.fxanalyzer.domain.Transaction
import nl.maas.fxanalyzer.frontend.wicket.caches.PropertiesCache
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.markup.html.list.ListItem
import org.apache.wicket.markup.html.list.ListView
import org.apache.wicket.model.IModel
import javax.inject.Inject
import kotlin.reflect.full.memberProperties


class TransactionsPanel(id: String, model: IModel<FXDataSet>) : AbstractPanel(id, model) {

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
            "EntryTime",
            "BuySell",
            "Currency",
            "EntryPrice",
            "ExitTime",
            "ExitPrice",
            "WonLost",
            "Amount"
        )
        headerLabels.forEach { addOrReplace(Label(it, propertiesCache.translator.translate(containingPage(), it))) }
    }

    fun addMembers() {
        val transactions = (defaultModelObject as FXDataSet).transactions
        val view = object : ListView<Transaction>("transactions", transactions.toList()) {
            override fun populateItem(item: ListItem<Transaction>) {
                Transaction::class.memberProperties
                    .forEach { item.addOrReplace(Label(it.name, it.getter.call(item.modelObject)?.toString())) }
            }
        }

        addOrReplace(view)
    }


}