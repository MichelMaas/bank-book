package nl.maas.bankbook.frontend.persistence.entities

import nl.maas.bankbook.domain.Amount
import nl.maas.bankbook.domain.IBAN
import nl.maas.bankbook.domain.enums.Categories
import nl.maas.bankbook.domain.enums.MutationTypes
import nl.maas.bankbook.frontend.persistence.converters.AmountConverter
import nl.maas.bankbook.frontend.persistence.converters.CurrencyConverter
import nl.maas.bankbook.frontend.persistence.converters.IBANConverter
import nl.maas.jpa.framework.entity.AbstractFullSearchEntity
import org.hibernate.search.annotations.Indexed
import java.time.LocalDate
import java.util.*
import javax.persistence.*

@Entity
@Indexed
@Table(name = "TRANSFERS")
class Transfer : AbstractFullSearchEntity() {
    @Column(name = "date")
    var date: LocalDate? = null

    //    @Column(name = "account")
    @ManyToOne(targetEntity = Account::class, optional = false)
    @JoinColumn(name = "account")
    var account: Account? = null

    @Column(name = "counterAccount")
    @Convert(converter = IBANConverter::class)
    var counterAccount: IBAN? = null

    @Column(name = "counterHolder")
    var counterHolder: String? = null

    @Column(name = "currency")
    @Convert(converter = CurrencyConverter::class)
    var currency: Currency? = null

    @Column(name = "amount")
    @Convert(converter = AmountConverter::class)
    var mutation: Amount? = null

    @Column(name = "mutationType")
    var mutationType: MutationTypes? = null

    @Column(name = "description")
    var description: String? = null

    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    var category: Categories? = null
}