package nl.maas.bankbook.frontend.wicket.objects

import nl.maas.bankbook.IterativeStorable
import nl.maas.bankbook.domain.*
import nl.maas.bankbook.domain.enums.Categories
import nl.maas.bankbook.domain.enums.MutationTypes
import nl.maas.bankbook.frontend.ContextProvider
import nl.maas.bankbook.frontend.wicket.caches.PropertiesCache
import nl.maas.bankbook.frontend.wicket.pages.BasePage
import nl.maas.filerenamer.domain.Storable
import org.apache.commons.lang3.StringUtils
import java.math.BigDecimal
import java.time.Month
import java.time.Year
import java.time.format.DateTimeFormatter

class Account private constructor(transactions: List<Transaction>) : Storable<Account> {

    var payments: List<Payment>
        private set

    var transfers: List<Transfer>
        private set

    @Transient
    private var filters: List<Filter>? = mutableListOf()

    val transactions get() = payments.plus(transfers).sortedBy { it.date }

    val currencySymbol: String
    val iban: IBAN?

    init {
        payments = listOf()
        transfers = listOf()
        currencySymbol = transfers.findLast { true }?.currency?.symbol ?: "€"
    }

    constructor(transactions: List<Transaction>, filters: List<Filter>) : this(transactions) {
        addFilters(filters)
        applyFilters()
    }

    private fun applyFilters() {
        filters?.let { filters ->
            filters.groupBy { findByFilter(it.filter).size }.entries.sortedByDescending { it.key }
                .map { it.key to it.value }.toMap()
                .forEach { it.value.forEach { flt -> changeCategoriesForAll(flt.filter, flt.category) } }
        }
    }


    companion object {
        private val defaultDateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

        fun loadOrCreate(): Account {
            val filters = IterativeStorable.load(Filter::class)
            return ((Storable.load(Account::class) ?: Account(listOf()))).addFilters(filters)
        }
    }

    private constructor(vararg transactions: Transaction) : this(transactions.distinctBy { it.id }) {
        if (transactions.isNotEmpty()) store()
    }

    init {
        payments = transactions.filter { Payment::class.isInstance(it) }.map { it as Payment }
        transfers = transactions.filter { Transfer::class.isInstance(it) }.map { it as Transfer }
        iban = transfers.findLast { true }?.baseAccount ?: null
    }

    fun totalIn(year: Year, month: Month? = null): Amount {
        return Amount(transactionsForPeriod(month, year).filter { it.mutation > BigDecimal.ZERO }
            .sumOf { it.mutation.value }, currencySymbol)
    }

    fun totalOut(year: Year, month: Month? = null): Amount {
        return Amount(transactionsForPeriod(month, year).filter { it.mutation < BigDecimal.ZERO }
            .sumOf { it.mutation.value }, currencySymbol)
    }

    fun totalTransactionsFor(year: Year, month: Month? = null): Int {
        return transactionsForPeriod(month, year).size
    }

    fun groupedCategoriesFor(year: Year, month: Month? = null): List<Tuple> {
        return transactionsForPeriod(month, year).filterNot { MutationTypes.IOB.equals(it.mutationType) }
            .groupBy { it.category.name }.map {
                Tuple(
                    mapOf(
                        Pair(
                            "category",
                            it.value.map { it.category.name }.distinct().joinToString("\n")
                        ),
                        Pair(
                            "amount",
                            Amount(it.value.sumOf { it.mutation.value }, currencySymbol).toString()
                        )
                    )
                )
            }
    }

    fun groupedReceiversFor(year: Year, month: Month? = null): List<Tuple> {
        return transactionsForPeriod(month, year)
            .filterNot { MutationTypes.IOB.equals(it.mutationType) }.groupBy { it::class.simpleName }
            .map { entry ->
                if (entry.key.equals(Transfer::class.simpleName)) {
                    entry.value.groupBy { (it as Transfer).counterAccount }.map {
                        it.key.value to
                                Tuple(
                                    mapOf(
                                        Pair("counter", (it.value.last() as Transfer).counterHolder),
                                        Pair(
                                            "amount",
                                            Amount(
                                                it.value.sumOf { it.mutation.value },
                                                currencySymbol
                                            ).toString()
                                        ),
                                        Pair(
                                            "description",
                                            it.value.map { it.description }.joinToString("\n\n")
                                        ),
                                        Pair(
                                            "category",
                                            it.value.map { it.category.name }.distinct().joinToString("\n")
                                        )
                                    )
                                )
                    }
                } else {
                    entry.value.groupBy { (it as Payment).shortDescription() }.map {
                        it.key to
                                Tuple(
                                    mapOf(
                                        Pair("counter", (it.value.last() as Payment).shortDescription()),
                                        Pair(
                                            "amount",
                                            Amount(
                                                it.value.sumOf { it.mutation.value },
                                                currencySymbol
                                            ).toString()
                                        ),
                                        Pair(
                                            "description",
                                            it.value.map { it.description }.joinToString("\n\n")
                                        ),
                                        Pair(
                                            "category",
                                            it.value.map { it.category.name }.distinct().joinToString("\n")
                                        )
                                    )
                                )
                    }
                }.map { it.second }
            }.flatMap { it }
    }

    private fun transactionsForPeriod(
        month: Month?,
        year: Year
    ) = transactions.filter { month?.equals(it.date.month) ?: true && it.date.year.equals(year.value) }

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
                Pair("category", it.category.name),
                Pair("date", it.date.format(defaultDateFormatter))
            )
        )

    private fun transferToTuple(it: Transfer) =
        Tuple(
            mapOf(
                Pair("counter", it.counterHolder),
                Pair("amount", it.mutation.toString()),
                Pair("description", it.description),
                Pair("category", it.category.name),
                Pair("date", it.date.format(defaultDateFormatter))
            )
        )

    fun changeCategoriesForAll(filter: String, categories: Categories) {
        val filtered = filterTransactions(filter)
        filtered.forEach { it.category = categories }
        store()
    }

    private fun filterTransactions(filter: String) = transactions.filter { tr ->
        val properties = ContextProvider.ctx.getBean(PropertiesCache::class.java)
        filter.split(StringUtils.SPACE).all { flt ->
            filter.split(StringUtils.SPACE)
                .map { prd ->
                    properties.translator.translate(
                        BasePage::class,
                        tr.filterValues().map { properties.translator.translate(BasePage::class, it) }
                            .joinToString(StringUtils.SPACE)
                    )
                        .contains(prd, true)
                }
                .none { bln -> !bln }
        }
    }

    fun addNewFrom(transactions: List<Transaction>): Account {
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
            this.filters?.plus(filters.filter { new -> this.filters!!.none { old -> old.filter.equals(new.filter) } })
                ?: filters
        return this
    }


}