package nl.maas.bankbook.frontend.services

import nl.maas.bankbook.domain.Transaction
import nl.maas.bankbook.frontend.wicket.caches.PropertiesCache
import nl.maas.bankbook.parsers.Parser
import org.springframework.stereotype.Component
import java.io.File
import javax.inject.Inject

@Component
class ParserService {

    @Inject
    lateinit var propertiesCache: PropertiesCache

    fun parseFile(file: String): List<Transaction> {
        return Parser.parse(file).createTransactions()
    }

    fun parseFile(file: File): List<Transaction> {
        return Parser.parse(file).createTransactions()
    }

}