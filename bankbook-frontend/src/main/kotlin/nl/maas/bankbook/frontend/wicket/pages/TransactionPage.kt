package nl.maas.bankbook.frontend.wicket.pages

import nl.maas.bankbook.domain.Transaction
import nl.maas.bankbook.domain.properties.Categories
import nl.maas.bankbook.frontend.wicket.components.DynamicFormComponent
import nl.maas.bankbook.frontend.wicket.components.SingleDataViewPanel
import nl.maas.bankbook.frontend.wicket.objects.Account.Companion.DEFAULT_DATE_FORMATTER
import org.apache.wicket.markup.html.list.ListItem
import org.apache.wicket.markup.html.list.ListView
import org.apache.wicket.model.CompoundPropertyModel

class TransactionPage(val transaction: Transaction) : BasePage() {
    private val TRANSACTION = "TRANSACTION"

    override fun onBeforeRender() {
        super.onBeforeRender()
        setUpOverview()
        setUpForm()
    }

    private fun setUpForm() {
        val form = DynamicFormComponent<Transaction>(
            "form",
            "${propertiesCache.translator.translate("transaction")} ${transaction.id}",
            CompoundPropertyModel.of(transaction)
        ).addSelect("category", "category", Categories.values().toList())
        addOrReplace(form)
    }

    private fun setUpOverview() {
        val pairs = listOf<Pair<String, String>>(
            Pair(
                propertiesCache.translator.translate("date"),
                transaction.date.format(DEFAULT_DATE_FORMATTER)
            ),
            Pair(propertiesCache.translator.translate("amount"), transaction.mutation.toString()),
            Pair(propertiesCache.translator.translate("type"), transaction.mutationType.tidy),
            Pair(propertiesCache.translator.translate("counter"), transaction.counter()),
            Pair(propertiesCache.translator.translate("description"), transaction.description)
        )
        val overview = object : ListView<Pair<String, String>>("overview", pairs) {
            override fun populateItem(item: ListItem<Pair<String, String>>) {
                item.addOrReplace(SingleDataViewPanel("data", item.modelObject))
            }

        }
        addOrReplace(overview)
    }
}