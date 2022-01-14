package nl.maas.bankbook.frontend.services

import nl.maas.bankbook.domain.Transaction
import nl.maas.bankbook.frontend.wicket.caches.PropertiesCache
import nl.maas.bankbook.parsers.Parser
import org.springframework.stereotype.Component
import java.nio.file.Paths
import javax.inject.Inject

@Component
class ParserService {

    @Inject
    lateinit var propertiesCache: PropertiesCache

    fun parseFile(file: String): List<Transaction> {
        return Parser.parse(file).createTransactions()
    }

    fun fetchTransactions(): List<Transaction> {
        val watchFolder = Paths.get(propertiesCache.options.watchedFolder).toFile()
        val filter = watchFolder.listFiles().filter { it.isFile && it.extension.equals("csv", true) }
        return filter.flatMap { parseFile(it.absolutePath) }
    }
}