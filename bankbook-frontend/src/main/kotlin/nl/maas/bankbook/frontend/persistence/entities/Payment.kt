package nl.maas.bankbook.frontend.persistence.entities

import nl.maas.bankbook.domain.Amount
import nl.maas.bankbook.domain.enums.MutationTypes
import java.time.LocalDate
import java.util.*
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "Payments")
class Payment {
    var id: Long? = null
    var date: LocalDate? = null
    var account: Account? = null
    var currency: Currency? = null
    var mutation: Amount? = null
    var mutationType: MutationTypes? = null
    var description: String? = null
    var category: String? = null
}