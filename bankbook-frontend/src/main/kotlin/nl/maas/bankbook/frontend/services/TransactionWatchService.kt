package nl.maas.bankbook.frontend.services

import nl.maas.bankbook.frontend.wicket.caches.ModelCache
import nl.maas.bankbook.frontend.wicket.caches.PropertiesCache
import org.springframework.stereotype.Component
import java.io.File
import java.nio.file.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

@Component
class TransactionWatchService private @Inject constructor(
    val propertiesCache: PropertiesCache,
    val parserService: ParserService,
    val modelCache: ModelCache,
) {

    init {
        Timer().schedule(WatchTask(), 0, 1000)
    }

    private inner class WatchTask : TimerTask() {

        fun watch(watchService: WatchService) {
            var watchKey = watchService.take()
            println("**********Filesystem event registered!**********")
            val creates = watchKey.pollEvents().filter { it.kind().equals(StandardWatchEventKinds.ENTRY_CREATE) }
            creates.forEach {
                try {
                    println("**********File added**********")
                    val path = it.context() as Path
                    println("File path: ${path.toRealPath()}")
                    val file = "${watchKey.watchable().toString()}/${path.toString()}"
                    require(path.toFile().extension.equals("csv", true))
                    println("File recognised as .csv. Processing...")
                    val startTime = LocalDateTime.now()
                    println("Started at: ${startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"))}")
                    val transactions = parserService.parseFile(file)
                    modelCache.dataContainer.addNewFrom(transactions).store()
                    val endTime = LocalDateTime.now()
                    println("Finished at: ${endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"))}")
                    println("Processing took ${Duration.between(startTime, endTime).toMinutes()} minutes")
                    println("*******************************************")
                    Files.delete(Paths.get(file))
                } catch (ex: Exception) {
                    println(ex.message)
                    ex.printStackTrace()
                }
            }
            watchService.poll()
        }

        override fun run() {
//            watch(registerTask(propertiesCache))
            checkFiles()
        }

        private fun checkFiles() {
            val watchedFolder = Paths.get(propertiesCache.options.watchedFolder).toFile()
            require(watchedFolder.isDirectory)
            processFiles(watchedFolder.listFiles())
        }

        private fun processFiles(files: Array<File>) {
            files.forEach { file ->
                if (file.extension.equals("csv", true)) {
                    println("*******************************************")
                    println("CSV-file found in watched folder: processing...")
                    val start = LocalDateTime.now()
                    println("Starttime: ${start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"))}")
                    val parseFile = parserService.parseFile(file)
                    modelCache.dataContainer.addNewFrom(parseFile).store()
                    val end = LocalDateTime.now()
                    println("Endtime: ${end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"))}")
                    println("Processing completed in: ${Duration.between(start, end).toString()} minutes")
                    println("*******************************************")
                } else {
                    println("*******************************************")
                    println("Non-csv file found in watched folder:")
                    println(file.name)
                    println("*******************************************")
                }
                Files.delete(file.toPath())
            }
        }

        private fun registerTask(propertiesCache: PropertiesCache): WatchService {
            println("**********File watcher registring**********")
            val watchService = FileSystems.getDefault().newWatchService()
            val path = Paths.get(propertiesCache.options.watchedFolder)
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE)
            println("Watcher registered on ${path.toRealPath()}")
            println("*******************************************")
            return watchService
        }
    }

}