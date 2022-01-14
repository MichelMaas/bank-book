package nl.maas.filerenamer.utils

import org.apache.commons.lang3.StringUtils
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class FileUtils {
    companion object {
        fun findFile(fileName: String): String {
            return Files.find(
                Paths.get("../bank-book"),
                10,
                { t, u -> t.toAbsolutePath().toString().endsWith(fileName) })
                .findAny().orElse(
                    Path.of(StringUtils.EMPTY)
                ).toAbsolutePath().toString()
        }
    }
}