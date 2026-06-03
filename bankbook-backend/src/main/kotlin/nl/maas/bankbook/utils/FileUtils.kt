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

    fun getResourceAsFile(resourcePath: String): File {
        return File(FileUtils::class.java.getResource(resourcePath).toURI())
    }

    fun saveFileToAppFolder(fileName: String, content: String): File {
        val userHome = System.getProperty("user.home")
        val appFolder = Paths.get("$userHome/.bankbook").toRealPath().toFile()
        val file = File("$appFolder/$fileName")
        file.writeText(content)
        return file
    }

    fun saveFileToAppFolder(file: File): File {
        val userHome = System.getProperty("user.home")
        val appFolder = Paths.get("$userHome/.bankbook").toRealPath().toFile()
        val definitivePath = "$appFolder/${file.name}"
        val destinationFile = file.copyTo(File(definitivePath))
        if (file.exists() && !file.absolutePath.equals(definitivePath)) {
            file.delete()
        }
        return destinationFile
    }

}