package nl.maas.bankbook.frontend.persistence.repositories

import nl.maas.bankbook.domain.enums.Categories
import nl.maas.bankbook.frontend.ContextProvider
import nl.maas.bankbook.frontend.persistence.entities.Transfer
import nl.maas.bankbook.frontend.persistence.entities.Transfer_
import nl.maas.bankbook.frontend.persistence.entities.Transfer_.category
import nl.maas.bankbook.frontend.persistence.entities.Transfer_.date
import nl.maas.jpa.framework.repository.Repository
import org.springframework.stereotype.Component
import java.time.LocalDate
import javax.persistence.EntityManager
import kotlin.reflect.KClass

@Component
class TransferRepository : Repository<Transfer>() {
    override val entityType: KClass<Transfer> = Transfer::class
    override val entityManager: EntityManager
        get() = ContextProvider.ctx.getBean(EntityManager::class.java)

    fun findFromToDates(startDate: LocalDate, endDate: LocalDate): List<Transfer> {
        return findBy(between(date, startDate to endDate, INCLUSIONS.INCLUSIVE))
    }

    fun findByCategory(value: Categories): List<Transfer> {
        return findBy(equals(category, value))
    }

    fun byFilter(filter: String): List<Transfer> {
        return findByOn(filter, Transfer_.description, Transfer_.category, Transfer_.mutation)
    }
}