package nl.maas.bankbook.domain

import nl.maas.bankbook.domain.annotations.StoreAs
import nl.maas.bankbook.domain.enums.MutationTypes
import nl.maas.bankbook.domain.properties.Categories.Companion.UNKNOWN
import nl.maas.wicket.framework.services.Translator
import nl.maas.wicket.framework.services.TranslatorPlaceHolder
import org.apache.commons.lang3.StringUtils
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.annotation.OverridingMethodsMustInvokeSuper
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.starProjectedType

@StoreAs("transactions")
abstract class Transaction(
    override val id: Long,
    val date: LocalDate,
    val baseAccount: IBAN,
    val currency: Currency,
    var mutation: Amount,
    val mutationType: MutationTypes,
    var description: String,
    var category: String = UNKNOWN,
    var counterName: String = StringUtils.EMPTY,
    val parentId: Long = 0
) : Event, IterativeStorable<Transaction> {


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

    @OverridingMethodsMustInvokeSuper
    override fun equals(other: Any?): Boolean {
        if (!Transaction::class.isInstance(other)) return false

        val transaction = other as Transaction
        return date.equals(transaction.date) && baseAccount.equals(transaction.baseAccount) && mutation.toString()
            .equals(transaction.mutation.toString()) && counter().equals(transaction.counter())
    }

    override fun hashCode(): Int {
        val dummyTranslator = TranslatorPlaceHolder()
        return filterValues(dummyTranslator).sumOf { it.hashCode() }
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
        }\nCounter: ${counter()}\n}"
    }

    fun filterValues(translator: Translator): Array<String> {
        return this::class.memberProperties.filterNot { "id".equals(it.name) }.map {

            if (LocalDate::class.starProjectedType.equals(it.returnType)) {
                (it.call(this) as LocalDate).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            } else if (it.name.equals("category")) {
                translator.translate(it.call(this).toString())
            } else {
                it.call(this).toString()
            }

        }.toTypedArray()
    }

    override fun replace(source: List<Transaction>): List<Transaction> {
        return source.filter { it.equals(this) }
    }

    abstract fun counter(): String
}