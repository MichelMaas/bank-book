package nl.maas.filerenamer.domain

import nl.maas.bankbook.domain.Amount
import java.io.Serializable
import java.math.BigDecimal

interface Event : Serializable {
    val id: Long
    val mutation: Amount
}