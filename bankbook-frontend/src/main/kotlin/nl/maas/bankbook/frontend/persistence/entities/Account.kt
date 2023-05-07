package nl.maas.bankbook.frontend.persistence.entities

import nl.maas.bankbook.domain.IBAN
import nl.maas.jpa.framework.entity.AbstractEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "ACCOUNTS")
class Account : AbstractEntity() {
    @Column(name = "IBAN")
    var iban: IBAN = IBAN("NL00NOBN000000000")
}