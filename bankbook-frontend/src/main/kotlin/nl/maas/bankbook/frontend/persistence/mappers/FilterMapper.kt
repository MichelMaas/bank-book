package nl.maas.bankbook.frontend.persistence.mappers

import nl.maas.bankbook.domain.CategoryFilter
import nl.maas.bankbook.frontend.persistence.entities.Filter
import org.mapstruct.Mapper

@Mapper
interface FilterMapper {


    fun filterToCategoryFilter(filter: Filter): CategoryFilter

    fun categoryFilterToFilter(categoryFilter: CategoryFilter): Filter
}