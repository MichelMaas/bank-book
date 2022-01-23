package nl.maas.bankbook.frontend.wicket.caches

import nl.maas.bankbook.frontend.wicket.objects.Account
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class ModelCache {
    var dataContainer: Account = Account.loadOrCreate()
    var localDate: LocalDate = LocalDate.now()

    fun isEmpty(): Boolean {
        return dataContainer.transactions.isEmpty()
    }

}