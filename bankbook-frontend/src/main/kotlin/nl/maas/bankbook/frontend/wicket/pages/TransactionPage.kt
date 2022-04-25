package nl.maas.bankbook.frontend.wicket.pages

import nl.maas.bankbook.domain.Transaction
import nl.maas.bankbook.domain.properties.Categories
import nl.maas.bankbook.frontend.wicket.components.DynamicFormComponent
import nl.maas.bankbook.frontend.wicket.components.SingleDataViewPanel
import nl.maas.bankbook.frontend.wicket.objects.Account.Companion.DEFAULT_DATE_FORMATTER
import org.apache.wicket.markup.html.list.ListItem
import org.apache.wicket.markup.html.list.ListView
import org.apache.wicket.model.CompoundPropertyModel
import org.apache.wicket.request.mapper.parameter.PageParameters

class TransactionPage(val transaction: Transaction) : BasePage(PageParameters()) {

    private val TRANSACTION = "TRANSACTION"

    override fun onBeforeRender() {
        super.onBeforeRender()
        setUpOverview()
        setUpForm()
    }

    private fun setUpForm() {
        val form = DynamicFormComponent<Transaction>(
            "form",
            "${propertiesCache.translator.translate(this::class, "transaction")} ${transaction.id}",
            CompoundPropertyModel.of(transaction)
        ).addSelect("category", "category", Categories.values().toList())
        addOrReplace(form)
    }

    private fun setUpOverview() {
        val pairs = listOf<Pair<String, String>>(
            Pair(
                propertiesCache.translator.translate(this::class, "date"),
                transaction.date.format(DEFAULT_DATE_FORMATTER)
            ),
            Pair(propertiesCache.translator.translate(this::class, "amount"), transaction.mutation.toString()),
            Pair(propertiesCache.translator.translate(this::class, "type"), transaction.mutationType.tidy),
            Pair(propertiesCache.translator.translate(this::class, "counter"), transaction.counter()),
            Pair(propertiesCache.translator.translate(this::class, "description"), transaction.description)
        )
        val overview = object : ListView<Pair<String, String>>("overview", pairs) {
            override fun populateItem(item: ListItem<Pair<String, String>>) {
                item.addOrReplace(SingleDataViewPanel("data", item.modelObject))
            }

        }
        addOrReplace(overview)
    }
}