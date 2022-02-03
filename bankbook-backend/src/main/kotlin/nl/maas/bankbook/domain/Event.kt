package nl.maas.filerenamer.domain

import nl.maas.bankbook.domain.Amount
import java.io.Serializable

interface Event : Serializable {
    val id: Comparable<*>
    val mutation: Amount
}