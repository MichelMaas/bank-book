package nl.maas.bankbook.domain.enums

enum class MutationTypes(val tidy: String, vararg val codes: Int) {
    INC("Incasso", 9714),
    BEA("PIN Transaction", 7906, 7913, 7914),
    OVB("Manual transfer", 8809, 8949, 9935, 6853, 9755, 9932, 9747, 9933),
    DPS("Deposit", 2723),
    IOB("Internal transfer", 2754, 3700, 9802, 3754, 2724),
    DIV("Account cost", 7241),
    IDB("IDEAL Payment", 9856, 9806),
    RNT("Debet interest", 7606, 6606, 6607),
    COR("Return payment", 6920, 8920, 8717),
    GEA("ATM withdrawal", 7910),
    STO("Storno", 8716),
    MAN("Manually added", 0);

    companion object {

        fun byCode(code: Int): MutationTypes {
            return values().first { it.codes.contains(code) }
        }
    }
}