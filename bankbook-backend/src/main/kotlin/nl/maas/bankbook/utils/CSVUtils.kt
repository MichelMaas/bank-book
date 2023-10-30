package nl.maas.bankbook.utils

import nl.maas.bankbook.domain.IBAN
import org.apache.commons.csv.CSVFormat
import java.io.File
import java.io.FileReader
import java.nio.file.Paths

class CSVUtils {
    companion object {
        fun parseFile(path: String): Map<Int, MutableList<String>> {
            val file = Paths.get(path).toFile()
            return parseFile(file)
        }

        fun parseFile(file: File): Map<Int, MutableList<String>> {
            return CSVFormat.DEFAULT.parse(FileReader(file)).mapIndexed { id, it -> id to it.toList() }.toMap()
                .filterNot { it.value.none { el -> IBAN.validate(el) } }
        }

        fun findBaseAccount(csv: Map<Int, MutableList<String>>): String {
            val values = csv.values.toList()
            val baseRecord = values[0]
            val filterIndexed = baseRecord.filterIndexed { idx, cell -> allEquals(idx, cell, values) }
            return filterIndexed.first { IBAN.validate(it) }.orEmpty()
        }

        fun allEquals(idx: Int, cell: String, values: List<List<String>>): Boolean {
            return values.map { it[idx] }.all { IBAN.validate(it) }
        }
    }


}