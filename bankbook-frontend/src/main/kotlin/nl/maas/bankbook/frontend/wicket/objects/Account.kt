package nl.maas.bankbook.frontend.wicket.objects

import nl.maas.bankbook.IterativeStorable
import nl.maas.bankbook.domain.Amount
import nl.maas.bankbook.domain.Payment
import nl.maas.bankbook.domain.Transaction
import nl.maas.bankbook.domain.Transfer
import nl.maas.bankbook.domain.enums.Categories
import nl.maas.bankbook.domain.enums.MutationTypes
import nl.maas.filerenamer.domain.Storable
import org.apache.commons.lang3.StringUtils
import java.math.BigDecimal
import java.time.Month
import java.time.Year
import java.time.format.DateTimeFormatter

class Account private constructor(transactions: List<Transaction>) : Storable<Account> {

    constructor(transactions: List<Transaction>, filters: List<Filter>) : this(transactions) {
        addFilters(filters)
        applyFilters()
    }

    private fun applyFilters() {
        filters.groupBy { findByFilter(it.filter).size }.entries.sortedByDescending { it.key }
            .map { it.key to it.value }.toMap()
            .forEach { it.value.forEach { flt -> changeCategoriesForAll(flt.filter, flt.category) } }
    }

    lateinit var payments: List<Payment>
        private set

    lateinit var transfers: List<Transfer>
        private set

    private var filters: List<Filter> = mutableListOf()

    val transactions get() = payments.plus(transfers).sortedBy { it.date }

    companion object {
        private val defaultDateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

        fun loadOrCreate(transactions: List<Transaction>): Account {
            val filters = IterativeStorable.load(Filter::class)
            return Storable.load(Account::class)?.addNewFrom(transactions)
                ?.addFilters(filters)
                ?: Account(transactions, filters)
        }
    }

    private constructor(vararg transactions: Transaction) : this(transactions.distinctBy { it.id }) {
        if (transactions.isNotEmpty()) store()
    }

    init {
        payments = transactions.filter { Payment::class.isInstance(it) }.map { it as Payment }
        transfers = transactions.filter { Transfer::class.isInstance(it) }.map { it as Transfer }
    }

    val currencySymbol = transfers.findLast { true }?.currency?.symbol ?: "€"
    val iban = transfers.findLast { true }?.baseAccount ?: null

    fun totalInYear(year: Year): Amount {
        return Amount(transactions.filter { it.date.year == year.value && it.mutation > BigDecimal.ZERO }
            .sumOf { it.mutation.value }, currencySymbol)
    }

    fun totalOutYear(year: Year): Amount {
        return Amount(transactions.filter { it.date.year == year.value && it.mutation < BigDecimal.ZERO }
            .sumOf { it.mutation.value }, currencySymbol)
    }

    fun totalInMonth(year: Year, month: Month): Amount {
        return Amount(transactions.filter { it.date.year == year.value && it.date.month.equals(month) && it.mutation > BigDecimal.ZERO }
            .sumOf { it.mutation.value }, currencySymbol)
    }

    fun totalOutMonth(year: Year, month: Month): Amount {
        return Amount(transactions.filter { it.date.year == year.value && it.date.month.equals(month) && it.mutation < BigDecimal.ZERO }
            .sumOf { it.mutation.value }, currencySymbol)
    }

    fun groupedTransactionsForMonth(year: Year, month: Month): Map<String, Map<String, String>> {
        val result = mutableMapOf<String, Map<String, String>>()
        val map =
            transactions.filter { it.date.month.equals(month) && it.date.year.equals(year.value) }
                .filterNot { MutationTypes.IOB.equals(it.mutationType) }.groupBy { it::class.simpleName }
                .forEach { s, transactions ->
                    result.putAll(
                        transactions.groupBy { it.category }.map {
                            it.key.name to
                                    mapOf(
                                        Pair(
                                            "catagory",
                                            it.value.map { it.category.name }.distinct().joinToString("\n")
                                        ),
                                        Pair(
                                            "amount",
                                            Amount(it.value.sumOf { it.mutation.value }, currencySymbol).toString()
                                        )
                                    )
                        }.toMap()
                    )
                }
        return result
    }

    fun groupedTransactionsForYear(year: Year): Map<String, Map<String, String>> {
        val result = mutableMapOf<String, Map<String, String>>()
        val map =
            transactions.filter { it.date.year.equals(year.value) }
                .filterNot { MutationTypes.IOB.equals(it.mutationType) }.groupBy { it::class.simpleName }
                .forEach { s, transactions ->
                    result.putAll(if (s.equals(Transfer::class.simpleName)) {
                        transactions.groupBy { (it as Transfer).counterAccount }.map {
                            it.key.value to
                                    mapOf(
                                        Pair("counter", (it.value.last() as Transfer).counterHolder),
                                        Pair(
                                            "amount",
                                            Amount(it.value.sumOf { it.mutation.value }, currencySymbol).toString()
                                        ),
                                        Pair("description", it.value.map { it.description }.joinToString("\n\n")),
                                        Pair(
                                            "catagory",
                                            it.value.map { it.category.name }.distinct().joinToString("\n")
                                        )
                                    )
                        }
                    } else {
                        transactions.groupBy { (it as Payment).shortDescription() }.map {
                            it.key to
                                    mapOf(
                                        Pair("counter", (it.value.last() as Payment).shortDescription()),
                                        Pair(
                                            "amount",
                                            Amount(it.value.sumOf { it.mutation.value }, currencySymbol).toString()
                                        ),
                                        Pair("description", it.value.map { it.description }.joinToString("\n\n")),
                                        Pair(
                                            "catagory",
                                            it.value.map { it.category.name }.distinct().joinToString("\n")
                                        )
                                    )
                        }
                    }.toMap())
                }
        return result
    }

    fun findByFilter(filter: String): List<Tuple> {
        return filterTransactions(filter).map { toTuple(it) }
    }

    private fun toTuple(it: Transaction) = if (Payment::class.isInstance(it)) {
        paymentToTuple(it as Payment)
    } else {
        transferToTuple(it as Transfer)
    }

    private fun paymentToTuple(it: Payment) =
        Tuple(
            mapOf(
                Pair("counter", it.shortDescription()),
                Pair("amount", it.mutation.toString()),
                Pair("description", it.description),
                Pair("catagory", it.category.name),
                Pair("date", it.date.format(defaultDateFormatter))
            )
        )

    private fun transferToTuple(it: Transfer) =
        Tuple(
            mapOf(
                Pair("counter", it.counterHolder),
                Pair("amount", it.mutation.toString()),
                Pair("description", it.description),
                Pair("catagory", it.category.name),
                Pair("date", it.date.format(defaultDateFormatter))
            )
        )

    fun changeCategoriesForAll(filter: String, categories: Categories) {
        val filtered = filterTransactions(filter)
        filtered.forEach { it.category = categories }
        store()
    }

    private fun filterTransactions(filter: String) = transactions.filter { tr ->
        filter.split(StringUtils.SPACE).all { flt ->
            filter.split(StringUtils.SPACE).map { prd -> tr.filterString().contains(prd, true) }.none { bln -> !bln }
        }
    }

    protected fun addNewFrom(transactions: List<Transaction>): Account {
        payments =
            payments.plus(transactions.filter {
                Payment::class.isInstance(it) && (!payments.map { it.id }.contains(it.id))
            }.map { it as Payment })
        transfers =
            transfers.plus(transactions.filter {
                Transfer::class.isInstance(it) && (!transfers.map { it.id }.contains(it.id))
            }.map { it as Transfer })
        applyFilters()
        return this
    }

    protected fun addFilters(filters: List<Filter>): Account {
        this.filters =
            this.filters.plus(filters.filter { new -> this.filters.none { old -> old.filter.equals(new.filter) } })
        return this
    }


}