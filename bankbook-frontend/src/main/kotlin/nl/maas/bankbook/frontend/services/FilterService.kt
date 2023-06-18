package nl.maas.bankbook.frontend.services

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import nl.maas.bankbook.domain.CategoryFilter
import nl.maas.bankbook.frontend.persistence.mappers.FilterMapper
import nl.maas.bankbook.frontend.persistence.repositories.FilterRepository
import org.mapstruct.factory.Mappers
import org.springframework.stereotype.Component
import javax.inject.Inject

@Component
class FilterService {

    @Inject
    private lateinit var filterRepository: FilterRepository

    private val mapper = Mappers.getMapper(FilterMapper::class.java)
    fun getFilters(): List<CategoryFilter> {
        val filters = filterRepository.getAll()
        return runBlocking { filters.map { async { mapper.filterToCategoryFilter(it) }.await() } }
    }

    fun findFilterByFilterString(filterString: String): List<CategoryFilter> {
        val filters = filterRepository.findByFilterString(filterString)
        return runBlocking { filters.map { async { mapper.filterToCategoryFilter(it) }.await() } }
    }

    fun delete(filter: CategoryFilter): List<CategoryFilter> {
        filterRepository.delete(mapper.categoryFilterToFilter(filter))
        return getFilters()
    }
}