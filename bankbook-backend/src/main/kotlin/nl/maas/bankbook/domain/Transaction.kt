package nl.maas.bankbook.domain

import nl.maas.bankbook.domain.enums.Categories
import nl.maas.bankbook.domain.enums.MutationTypes
import nl.maas.filerenamer.domain.Event
import org.apache.commons.lang3.StringUtils
import java.math.BigDecimal
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

    companion object {
        fun EMPTY(account: IBAN, currency: Currency) = object : Transaction(
            0L, LocalDate.now(), account, currency,
            Amount(BigDecimal.ZERO, currency.symbol), MutationTypes.MAN, StringUtils.EMPTY
        ) {
            override fun counter(): String {
                return "Manually Added"
            }

        }
    }

    var splitExpenses: List<Expense> = listOf()

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

    fun splitExpense(amount: Amount, category: Categories) {
        splitExpenses = listOf(Expense("${this.id}_${splitExpenses.size}", amount, category))
    }

    abstract fun counter(): String
}