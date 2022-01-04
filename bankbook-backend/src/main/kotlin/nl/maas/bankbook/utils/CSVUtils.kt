package nl.maas.bankbook.utils

import nl.maas.bankbook.domain.IBAN
import nl.maas.filerenamer.utils.FileUtils
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.FileReader
import java.nio.file.Paths

class CSVUtils {
    companion object{
        fun parseFile(path:String): Map<Int, MutableList<String>> {
            val file = Paths.get(path).toFile()
            return  CSVFormat.DEFAULT.parse(FileReader(file)).mapIndexed { id, it -> id to it.toList() }.toMap()
        }

        fun findBaseAccount(csv:Map<Int, MutableList<String>>):String{
            val values = csv.values.toList()
            val baseRecord = values[0]
            val filterIndexed = baseRecord.filterIndexed { idx, cell -> allEquals(idx, cell, values) }
            return filterIndexed.first { IBAN.validate(it) }.orEmpty()
        }

        fun allEquals(idx:Int,cell:String,values:List<List<String>>):Boolean{
            return values.map { it[idx] }.all{cell.equals(it)}
        }
    }


}