package nl.maas.bankbook.frontend.wicket.objects

import java.io.Serializable

data class Tuple(val columns: Map<String, Serializable>) : Serializable {

    override fun toString(): String {
        return columns.map { "${it.key}: ${it.value}" }.joinToString("\n")
    }

    override fun equals(other: Any?): Boolean {
        require(this::class.isInstance(other))
        val tuple = other as Tuple
        return this.columns.keys.equals(tuple.columns.keys)
    }
}
