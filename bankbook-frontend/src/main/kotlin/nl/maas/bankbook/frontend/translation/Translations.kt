package nl.maas.bankbook.frontend.translation

import nl.maas.bankbook.domain.Storable

class Translations : Storable<Translations>, HashMap<String, String>() {

    fun putChained(key: String, value: String): Translations {
        put(key, value)
        return this
    }
}