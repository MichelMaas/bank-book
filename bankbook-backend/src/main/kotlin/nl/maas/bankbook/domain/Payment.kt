package nl.maas.bankbook.domain

import nl.maas.bankbook.domain.enums.MutationTypes
import java.time.LocalDate
import java.util.*

class Payment(
    id: Long,
    date: LocalDate,
    baseAccount: IBAN,
    currency: Currency,
    mutation: Amount,
    mutationType: MutationTypes,
    description: String
) : Transaction(id, date, baseAccount, currency, mutation, mutationType, description) {
    fun shortDescription() = description.substringBefore(">").substringAfter("'")
    override fun counter() = shortDescription()
}