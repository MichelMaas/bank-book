package nl.maas.bankbook.frontend.translation

import nl.maas.bankbook.domain.IterativeStorable

class Translation(val language: String, val original: String, var translation: String) :
    IterativeStorable<Translation> {
    override fun replace(source: List<Translation>): List<Translation> {
        return source.filter { it.equals(this) }
    }

    override fun equals(other: Any?): Boolean {
        if (!Translation::class.isInstance(other)) {
            return false
        } else {
            val ot = other as Translation
            return this.language.equals(ot.language, true) && this.original.equals(
                ot.original, true
            )
        }
    }
}