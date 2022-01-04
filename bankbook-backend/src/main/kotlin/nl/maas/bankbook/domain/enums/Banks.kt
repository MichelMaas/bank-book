package nl.maas.bankbook.domain.enums

enum class Banks {
    SNSB,
    INGB,
    ABNA,
    OTHR;

    companion object{
        fun valueOf(name:String):Banks{
            return Banks.values().find { name.equals(it.name) } ?: OTHR
        }
    }
}