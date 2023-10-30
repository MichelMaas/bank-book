package nl.maas.bankbook.domain

import nl.maas.bankbook.domain.enums.MutationTypes
import org.apache.commons.lang3.StringUtils
import java.time.LocalDate
import java.util.*

class Payment(
    id: Long,
    date: LocalDate,
    baseAccount: IBAN,
    currency: Currency,
    mutation: Amount,
    mutationType: MutationTypes,
    description: String,
    counterName: String = StringUtils.EMPTY
) : Transaction(id, date, baseAccount, currency, mutation, mutationType, description, counterName = counterName) {
    fun shortDescription() = description.substringBefore(">").substringAfter("'")
    override fun counter() = if (counterName.isNullOrBlank()) shortDescription() else counterName
}