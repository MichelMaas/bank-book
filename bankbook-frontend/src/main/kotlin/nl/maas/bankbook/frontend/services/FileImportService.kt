package nl.maas.bankbook.frontend.services

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import nl.maas.bankbook.domain.Transaction
import nl.maas.bankbook.frontend.wicket.caches.ModelCache
import nl.maas.bankbook.utils.TimerUtil
import org.springframework.stereotype.Component
import java.io.File
import javax.inject.Inject

@Component
class FileImportService {

    @Inject
    private lateinit var parserService: ParserService

    @Inject
    private lateinit var modelCache: ModelCache
    suspend fun importFile(file: File): List<Transaction> {
        TimerUtil.start()
        modelCache.requestBlockWhile(10, { m -> !m.isStoring() })
        println("Import of file ${file.name} started")
        val transactions: MutableList<Transaction> = mutableListOf()
        try {
            GlobalScope.async {
                transactions.addAll(parserService.parseFile(file))
                modelCache.addOrUpdateTransactions(*transactions.toTypedArray())
            }.await()
        } catch (e: Exception) {
            println("An error occurred while importing ${file.name}:")
            e.printStackTrace()
        }
        TimerUtil.stop("File imported in")
        return transactions
    }
}