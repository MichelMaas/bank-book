package nl.maas.fxanalyzer.domain

import nl.maas.bankbook.domain.Amount
import nl.maas.bankbook.domain.IBAN
import nl.maas.bankbook.domain.enums.MutationTypes
import nl.maas.filerenamer.domain.Event
import java.time.LocalDate
import java.util.*
import kotlin.reflect.full.memberProperties

abstract class Transaction(
    override val id: Long,
    val date: LocalDate,
    val baseAccount: IBAN,
    val currency: Currency,
    override val mutation: Amount,
    val mutationType: MutationTypes,
    val description: String
) : Event {

    override fun equals(other: Any?): Boolean {
        return this::class.isInstance(other) && (other as Transaction).id.equals(this.id)
    }

    override fun toString(): String {
        return "${mutationType.tidy} {\n${
            this::class.memberProperties.map {
                " ${it.name}: ${
                    it.call(this).toString()
                }"
            }.joinToString("\n")
        }\n}"
    }
}