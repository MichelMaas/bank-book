package nl.maas.bankbook.frontend.wicket.objects

import nl.maas.bankbook.IterativeStorable
import nl.maas.bankbook.domain.*
import nl.maas.bankbook.domain.enums.MutationTypes
import nl.maas.bankbook.frontend.ContextProvider
import nl.maas.bankbook.frontend.wicket.caches.PropertiesCache
import nl.maas.bankbook.frontend.wicket.pages.BasePage
import org.apache.commons.lang3.StringUtils
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalTime
import java.time.Month
import java.time.Year
import java.time.format.DateTimeFormatter

class Account private constructor(
    transactions: List<Transaction> = IterativeStorable.load(Transaction::class),
    filters: List<Filter> = IterativeStorable.load(Filter::class)
) : Storable<Account> {

    private constructor() : this(
        IterativeStorable.load(Transaction::class),
        IterativeStorable.load(Filter::class)
    ) {

    }

    @Transient
    var transactions: List<Transaction> = transactions

    val payments: List<Payment> get() = transactions.filter { it::class.isInstance(Payment::class) } as List<Payment>

    val transfers: List<Transfer> get() = transactions.filter { it::class.isInstance(Transfer::class) } as List<Transfer>

    @Transient
    private var filters: List<Filter> = filters


    val currencySymbol get() = if (transactions.isNotEmpty()) transactions.first().currency.symbol else "€"
    val iban get() = if (transactions.isNotEmpty()) transactions.first().baseAccount else IBAN("NL00AAAA0000000000")

    private fun applyFilters(transactions: List<Transaction> = this.transactions, store: Boolean = true) {
        filters.let { filters ->
            filters.parallelStream().forEach { it.foundTransactions = filterTransactions(it.filter, transactions) }
            filters.sortedByDescending { it.foundTransactions.size }
                .forEach { changeCategoriesForAll(it.foundTransactions, it.category) }
        }
        if (store) store()
    }


    companion object {
        val DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy")

        fun loadOrCreate(): Account {
            return ((Storable.load(Account::class) ?: Account(listOf()))).reload()
        }
    }

    private constructor(vararg transactions: Transaction) : this(transactions.distinctBy { it.id }) {
        if (transactions.isNotEmpty()) store()
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
            .groupBy { it.category }.map {
                val amount = Amount(it.value.sumOf { it.mutation.value }, currencySymbol)
                val previous = Amount(
                    transactionsForPeriod(
                        month?.minus(1),
                        if (month == null) year.minusYears(1) else year
                    ).filter { pr -> it.value.first().category.equals(pr.category) }
                        .sumOf { pr -> pr.mutation.value }, currencySymbol
                )
                Tuple(
                    mapOf(
                        Pair(
                            "category",
                            it.value.map { it.category }.distinct().joinToString("\n")
                        ),
                        Pair(
                            "amount",
                            amount.toString()
                        ),
                        Pair(
                            "previous",
                            previous.toString()
                        ),
                        Pair(
                            "difference",
                            Amount(amount.value.minus(previous.value), currencySymbol).toString()
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
                                            it.value.map { it.category }.distinct().joinToString("\n")
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
                                            it.value.map { it.category }.distinct().joinToString("\n")
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
                Pair("category", it.category),
                Pair("date", it.date.format(DEFAULT_DATE_FORMATTER))
            )
        )

    private fun transferToTuple(it: Transfer) =
        Tuple(
            mapOf(
                Pair("counter", it.counterHolder),
                Pair("amount", it.mutation.toString()),
                Pair("description", it.description),
                Pair("category", it.category),
                Pair("date", it.date.format(DEFAULT_DATE_FORMATTER))
            )
        )

    fun changeCategoriesForAll(filter: String, categories: String) {
        val filtered = filterTransactions(filter)
        changeCategoriesForAll(filtered, categories)
    }

    fun changeCategoriesForAll(transactions: List<Transaction>, categories: String) {
        transactions.forEach {
            it.category = categories
        }
    }

    fun filterTransactions(filter: String, transactions: List<Transaction> = this.transactions): List<Transaction> {
        val start = LocalTime.now()
        val properties = ContextProvider.ctx.getBean(PropertiesCache::class.java)
        val filterWords = filter.split(StringUtils.SPACE).filterNot { it.isBlank() }
        var filtered: List<Transaction> = transactions.filter { tr ->
            filterWords.map { properties.translator.untranslate(BasePage::class, it) }.all { filterWord ->
                tr.filterValues().joinToString(StringUtils.SPACE).contains(filterWord, true)
            }
        }
        val end = LocalTime.now()
        val between = Duration.between(start, end)
        println("Filtering took: ${between}")
        return filtered
    }

    fun findTransaction(filter: String, transactions: List<Transaction> = this.transactions): Transaction {
        val start = LocalTime.now()
        val filterWords = filter.split(StringUtils.SPACE).filterNot { it.isBlank() }.iterator()
        var filtered = transactions
        while (filtered.size > 1 || filterWords.hasNext()) {
            val next = filterWords.next()
            filtered = filterTransactions(next, filtered)
        }
        if (filtered.size > 1) {
            throw IllegalStateException("Filter was not specific enough!!!!")
        }
        val end = LocalTime.now()
        val between = Duration.between(start, end)
        println("Filtering took: ${between}")
        return filtered.first()
    }

    fun addNewFrom(transactions: List<Transaction>): Account {
        val newTransactions = transactions.filter { new -> this.transactions.none { old -> old.id.equals(new.id) } }
        applyFilters(newTransactions, false)
        this.transactions = newTransactions
        return this
    }

    fun findSimilarFilters(transactionsFilter: String): List<Tuple> {
        return filters.filter {
            transactionsFilter.isBlank() || (transactionsFilter.split(StringUtils.SPACE)
                .all { wrd -> it.filter.contains(wrd) })
        }.map { Tuple(mapOf("Filter" to it.filter, "Category" to it.category)) }
    }

    fun addFilter(filter: Filter) {
        filters = filters.plus(filter)
    }

    override fun store(): Account {
        IterativeStorable.storeAll(filters)
        IterativeStorable.storeAll(transactions)
        return super.store()
    }

}