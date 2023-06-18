package nl.maas.bankbook.frontend.persistence.mappers

import nl.maas.bankbook.frontend.ContextProvider
import nl.maas.bankbook.frontend.persistence.entities.Account
import nl.maas.bankbook.frontend.persistence.repositories.AccountRepository
import org.mapstruct.Mapper

@Mapper
interface AccountMapper {

    fun entityToDomain(account: Account): nl.maas.bankbook.frontend.domain.Account {
        return nl.maas.bankbook.frontend.domain.Account(account.iban)
    }

    fun domainToEntity(account: nl.maas.bankbook.frontend.domain.Account): Account {
        return ContextProvider.ctx.getBean(AccountRepository::class.java).findBy(account.iban)
    }
}