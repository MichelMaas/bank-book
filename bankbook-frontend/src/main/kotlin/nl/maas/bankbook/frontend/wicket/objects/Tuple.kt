package nl.maas.bankbook.frontend.wicket.objects

import org.apache.commons.lang3.StringUtils
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

    fun toFilterString(): String {
        return columns.filterNot { it.key.equals("description", true) }.map { "${it.value}" }
            .joinToString(StringUtils.SPACE)
    }
}
