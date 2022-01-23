package nl.maas.bankbook.domain

import nl.maas.bankbook.domain.enums.Categories
import nl.maas.bankbook.domain.enums.MutationTypes
import nl.maas.filerenamer.domain.Event
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.starProjectedType

abstract class Transaction(
    override val id: Long,
    val date: LocalDate,
    val baseAccount: IBAN,
    val currency: Currency,
    override val mutation: Amount,
    val mutationType: MutationTypes,
    val description: String,
    var category: Categories = Categories.OTHER
) : Event {

    override fun equals(other: Any?): Boolean {
        return this::class.isInstance(other) && (other as Transaction).id.equals(this.id)
    }

    override fun toString(): String {
        return "${mutationType.tidy} {\n${
            this::class.memberProperties.map {
                " ${it.name}: ${
                    if (LocalDate::class.starProjectedType.equals(it.returnType)) {
                        (it.call(this) as LocalDate).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                    } else {
                        it.call(this).toString()
                    }
                }"
            }.joinToString("\n")
        }\n}"
    }

    fun filterValues(): Array<String> {
        return this::class.memberProperties.filterNot { "id".equals(it.name) }.map {

            if (LocalDate::class.starProjectedType.equals(it.returnType)) {
                (it.call(this) as LocalDate).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            } else {
                it.call(this).toString()
            }

        }.toTypedArray()
    }
}