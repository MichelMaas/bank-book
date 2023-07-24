package nl.maas.bankbook.parsers

import java.io.File

abstract class XMLParser<T, M> : Parser<T, M> {

    companion object {

        fun parse(file: File): XMLParser<*, *> {
            return CAMT053Parser(file)
        }

    }

}