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

class FileUtils {
    companion object {
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
    }
}