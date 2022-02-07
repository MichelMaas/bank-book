package nl.maas.filerenamer.utils

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.function.BiPredicate
import java.util.stream.Collectors

class FileUtils {
    companion object {
        fun findFile(fileName: String): String {
            val files = Files.find(
                Paths.get("../../").toRealPath(), 8,
                BiPredicate({ path, basicFileAttributes ->
                    val file: File = path.toFile()
                    !file.isDirectory() &&
                            file.getName().equals(fileName, true)
                })
            ).collect(Collectors.toList())
            return files.first { it.toRealPath().toString().contains("bank-book") }.toRealPath()
                .toString()
        }
    }
}