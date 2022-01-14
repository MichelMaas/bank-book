package nl.maas.bankbook.frontend.wicket.caches

import nl.maas.bankbook.frontend.wicket.objects.Account
import org.springframework.stereotype.Component

@Component
class ModelCache {
    var dataContainer: Account = Account.loadOrCreate(listOf())

    fun isEmpty(): Boolean {
        return dataContainer.transactions.isEmpty()
    }

}