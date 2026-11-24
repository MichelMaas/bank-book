package nl.maas.bankbook.utils

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.function.BiPredicate
import java.util.stream.Collectors

object FileUtils {

    private val userHome = System.getProperty("user.home")
    private val appFolder = Files.createDirectories(Paths.get(System.getProperty("bankbook.dataDir", "$userHome/.bankbook"))).toFile()

    @JvmStatic
    fun findFile(fileName: String): String {
        val files = runBlocking {
            if (Files.exists(Path.of(fileName))) listOf(Path.of(fileName)) else async {
                Files.find(
                    Paths.get("").toRealPath(), 5,
                    BiPredicate({ path, basicFileAttributes ->
                        val file: File = path.toFile()
                        !file.isDirectory() &&
                                file.absolutePath.endsWith(fileName, true)
                    })
                )
            }.await().collect(Collectors.toList())
        }
        return runBlocking {
            files.firstOrNull() { async { it.toRealPath().toString().contains("bank-book") }.await() }?.toRealPath()
                .toString() ?: StringUtils.EMPTY
        }
    }

    fun getFileFromAppFolder(fileName: String): File {
        return File("$appFolder/$fileName")
    }

    @JvmStatic
    fun getResourceAsFile(resourcePath: String): File {
        val target = File(appFolder, resourcePath.substringAfterLast('/'))
        FileUtils::class.java.getResourceAsStream(resourcePath).use { input ->
            requireNotNull(input) { "Missing resource: $resourcePath" }
            target.outputStream().use { input.copyTo(it) }
        }
        return target
    }

    @JvmStatic
    fun saveFileToAppFolder(fileName: String, content: String, overwrite: Boolean = false): File {
        val file = File("$appFolder/$fileName")
        if (!file.exists() || overwrite) {
            file.writeText(content)
        }
        return file
    }

    @JvmStatic
    fun saveFileToAppFolder(file: File, overwrite: Boolean = false): File {
        val definitivePath = "$appFolder/${file.name}"
        val destination = File(definitivePath)
        if (file.canonicalFile == destination.canonicalFile) return file
        val destinationFile = file.copyTo(destination, overwrite)
        return destinationFile
    }

}