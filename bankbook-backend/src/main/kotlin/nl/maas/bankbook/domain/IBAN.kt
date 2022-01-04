package nl.maas.bankbook.domain

data class IBAN(val value:String) {
    companion object{
        fun validate(candidate:String):Boolean{
            return candidate.matches(Regex("^([A-Z]{2}[ \\-]?[0-9]{2})(?=(?:[ \\-]?[A-Z0-9]){9,30}\$)((?:[ \\-]?[A-Z0-9]{3,5}){2,7})([ \\-]?[A-Z0-9]{1,3})?\$"))
        }
    }
    init {
        require(validate(value))
    }
}