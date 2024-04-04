package nl.maas.bankbook.domain

import kotlin.reflect.full.memberProperties

class CategoryFilter(var filterString: String, var category: String, var store: Boolean) :
    IterativeStorable<CategoryFilter> {
    override fun replace(source: List<CategoryFilter>): List<CategoryFilter> {
        return source.filter { it.category.equals(this.category) && it.filterString.equals(this.filterString, true) }
    }

    fun filterValues(): Array<String> {
        return this::class.memberProperties.filterNot { "id".equals(it.name) }.map {
            it.call(this).toString()
        }.toTypedArray()
    }
}