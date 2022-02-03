package nl.maas.bankbook.domain

import nl.maas.bankbook.domain.enums.Categories
import nl.maas.filerenamer.domain.Event
import nl.maas.filerenamer.domain.Storable

class Expense(override val id: String, override val mutation: Amount, val categories: Categories) : Event,
    Storable<Expense> {
}