package nl.maas.bankbook.frontend.persistence.repositories

import nl.maas.bankbook.domain.enums.Categories
import nl.maas.bankbook.frontend.ContextProvider
import nl.maas.bankbook.frontend.persistence.entities.Payment
import nl.maas.bankbook.frontend.persistence.entities.Payment_
import nl.maas.bankbook.frontend.persistence.entities.Payment_.category
import nl.maas.bankbook.frontend.persistence.entities.Payment_.date
import nl.maas.jpa.framework.repository.Repository
import org.springframework.stereotype.Component
import java.time.LocalDate
import javax.persistence.EntityManager
import kotlin.reflect.KClass

@Component
class PaymentRepository : Repository<Payment>() {
    override val entityManager: EntityManager
        get() = ContextProvider.ctx.getBean(EntityManager::class.java)
    override val entityType: KClass<Payment> = Payment::class

    fun findFromToDates(startDate: LocalDate, endDate: LocalDate): List<Payment> {
        return findBy(between(date, startDate to endDate, INCLUSIONS.INCLUSIVE))
    }

    fun findByCategory(value: Categories): List<Payment> {
        return findBy(equals(category, value))
    }

    fun byFilter(filter: String): List<Payment> {
        return findByOn(filter, Payment_.description, Payment_.category, Payment_.mutation)
    }
}