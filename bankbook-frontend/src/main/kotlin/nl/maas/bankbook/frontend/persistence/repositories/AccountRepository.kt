package nl.maas.bankbook.frontend.persistence.repositories

import nl.maas.bankbook.domain.IBAN
import nl.maas.bankbook.frontend.ContextProvider
import nl.maas.bankbook.frontend.persistence.entities.Account
import nl.maas.bankbook.frontend.persistence.entities.Account_
import nl.maas.jpa.framework.repository.Repository
import org.springframework.stereotype.Component
import javax.persistence.EntityManager
import kotlin.reflect.KClass

@Component
class AccountRepository : Repository<Account>() {

    override val entityManager: EntityManager
        get() = ContextProvider.ctx.getBeansOfType(EntityManager::class.java).values.first()
    override val entityType: KClass<Account> = Account::class

    fun findBy(iban: IBAN): Account {
        return findBy(equals(Account_.iban, iban)).first()
    }
}