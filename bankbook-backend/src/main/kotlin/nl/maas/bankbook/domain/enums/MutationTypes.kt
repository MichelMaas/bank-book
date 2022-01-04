package nl.maas.bankbook.domain.enums

enum class MutationTypes(val tidy:String) {
    INC("Incasso"),
    BEA("PIN Transaction"),
    OVB("Manual transfer"),
    IOB("Internal transfer"),
    DIV("Account cost"),
    IDB("IDEAL Payment");
}