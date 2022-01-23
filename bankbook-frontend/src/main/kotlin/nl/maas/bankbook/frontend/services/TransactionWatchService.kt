package nl.maas.bankbook.frontend.services

import nl.maas.bankbook.frontend.wicket.caches.ModelCache
import nl.maas.bankbook.frontend.wicket.caches.PropertiesCache
import org.springframework.stereotype.Component
import java.nio.file.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Component
class TransactionWatchService private constructor(
    val propertiesCache: PropertiesCache,
    val parserService: ParserService,
    val modelCache: ModelCache,
    val watchService: WatchService
) {


//    @Inject
//    lateinit var propertiesCache: PropertiesCache
//
//    @Inject
//    lateinit var parserService: ParserService
//
//    @Inject
//    lateinit var modelCache: ModelCache

    @Inject
    constructor(propertiesCache: PropertiesCache, parserService: ParserService, modelCache: ModelCache) : this(
        propertiesCache, parserService, modelCache,
        FileSystems.getDefault().newWatchService()
    ) {
        val path = Paths.get(propertiesCache.options.watchedFolder)
        path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE)
        Timer().schedule(WatchTask(), TimeUnit.MINUTES.toMillis(1))
    }

    private inner class WatchTask : TimerTask() {

        fun watch() {
            var watchKey = watchService.take()
            val creates = watchKey.pollEvents().filter { it.kind().equals(StandardWatchEventKinds.ENTRY_CREATE) }
            creates.forEach {
                try {
                    val path = it.context() as Path
                    val file = "${watchKey.watchable().toString()}/${path.toString()}"
                    require(path.toFile().extension.equals("csv", true))
                    val transactions = parserService.parseFile(file)
                    modelCache.dataContainer.addNewFrom(transactions).store()

                    Files.delete(Paths.get(file))
                } catch (ex: Exception) {
                    println(ex.message)
                    ex.printStackTrace()
                }
            }
            Timer().schedule(WatchTask(), TimeUnit.MINUTES.toMillis(1))
        }

        override fun run() {
            watch()
        }
    }

}