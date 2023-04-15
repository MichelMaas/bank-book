package nl.maas.bankbook.providers

interface Translator : java.io.Serializable {
    fun translate(key: String): String
}