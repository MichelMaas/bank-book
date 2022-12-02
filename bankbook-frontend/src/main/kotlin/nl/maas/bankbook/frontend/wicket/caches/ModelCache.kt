package nl.maas.bankbook.frontend.wicket.caches

import nl.maas.bankbook.frontend.wicket.objects.Account
import org.springframework.stereotype.Component
import java.time.LocalDate
import javax.inject.Singleton

@Component
@Singleton
class ModelCache {
    lateinit var dataContainer: Account;
    var localDate: LocalDate = LocalDate.now()

    init {
        refresh()
    }

    fun refresh() {
        dataContainer = Account.loadOrCreate()
    }

    fun isEmpty(): Boolean {
        return dataContainer.transactions.isEmpty()
    }

}