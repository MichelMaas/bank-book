package nl.maas.bankbook.domain

import nl.maas.bankbook.domain.enums.MutationTypes
import nl.maas.fxanalyzer.domain.Transaction
import java.time.LocalDate
import java.util.*

class Transfer(
    id: Long,
    date: LocalDate,
    baseAccount: IBAN,
    val counterAccount: IBAN,
    val counterHolder: String,
    currency: Currency,
    mutation: Amount,
    mutationType: MutationTypes,
    description: String
) : Transaction(id, date, baseAccount, currency, mutation, mutationType, description) {
}