package nl.maas.bankbook.frontend.persistence.repositories

import nl.maas.bankbook.frontend.ContextProvider
import nl.maas.bankbook.frontend.persistence.entities.Filter
import nl.maas.bankbook.frontend.persistence.entities.Filter_.filterString
import nl.maas.jpa.framework.repository.Repository
import org.springframework.stereotype.Component
import javax.persistence.EntityManager
import kotlin.reflect.KClass

@Component
class FilterRepository : Repository<Filter>() {

    override val entityManager: EntityManager get() = ContextProvider.ctx.getBean(EntityManager::class.java)
    override val entityType: KClass<Filter> = Filter::class

    fun findByFilterString(string: String): List<Filter> {
        return findByOn(string, filterString)
    }
}