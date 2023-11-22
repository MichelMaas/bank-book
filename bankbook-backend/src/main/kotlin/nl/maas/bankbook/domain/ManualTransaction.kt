package nl.maas.bankbook.domain

import nl.maas.bankbook.domain.enums.MutationTypes
import nl.maas.bankbook.domain.properties.Categories
import org.apache.commons.lang3.StringUtils
import java.time.LocalDate
import java.util.*

class ManualTransaction(
    id: Long,
    parentId: Long,
    date: LocalDate,
    baseAccount: IBAN,
    currency: Currency,
    mutation: Amount,
    mutationType: MutationTypes,
    description: String,
    counterName: String = StringUtils.EMPTY
) : Transaction(
    id,
    date,
    baseAccount,
    currency,
    mutation,
    mutationType,
    description,
    Categories.MANUAL,
    counterName,
    parentId
) {
    override fun counter() = counterName

    companion object {
        val EMPTY: ManualTransaction = ManualTransaction(
            0, 0, LocalDate.now(), IBAN.EMPTY, Currency.getInstance(Locale.getDefault()),
            Amount("0", ""), MutationTypes.MAN, ""
        )
    }

    val empty get() = id.equals(0L) && baseAccount.empty

}