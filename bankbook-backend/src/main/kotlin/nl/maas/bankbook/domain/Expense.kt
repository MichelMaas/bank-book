package nl.maas.bankbook.domain

class Expense(override val id: String, override val mutation: Amount, val categories: String) : Event,
    Storable<Expense> {
}