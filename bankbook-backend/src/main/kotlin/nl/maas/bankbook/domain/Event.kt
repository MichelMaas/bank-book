package nl.maas.bankbook.domain

import java.io.Serializable

interface Event : Serializable {
    val id: Comparable<*>
}