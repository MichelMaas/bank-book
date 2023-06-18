package nl.maas.bankbook.frontend.services

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import nl.maas.bankbook.frontend.domain.Account
import nl.maas.bankbook.frontend.persistence.mappers.AccountMapper
import nl.maas.bankbook.frontend.persistence.repositories.AccountRepository
import org.mapstruct.factory.Mappers
import org.springframework.stereotype.Component
import javax.inject.Inject

@Component
class AccountService {

    @Inject
    private lateinit var accountRepository: AccountRepository
    private val mapper = Mappers.getMapper(AccountMapper::class.java)


    fun getAccounts(): List<Account> {
        return runBlocking { accountRepository.getAll().map { async { mapper.entityToDomain(it) }.await() } }
    }


}