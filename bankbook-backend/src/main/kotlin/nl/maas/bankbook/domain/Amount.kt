package nl.maas.bankbook.domain

import org.apache.commons.lang3.StringUtils
import java.math.BigDecimal
import java.math.RoundingMode

class Amount(val value: String) : BigDecimal(value) {
    init {
        this.setScale(2, RoundingMode.HALF_UP)
    }

    override fun toByte() = super.toString().toByte()

    override fun toChar() = super.toString().toCharArray()[0]

    override fun toShort() = super.shortValueExact()

    override fun toString(): String {
        return super.toString().replace(",", StringUtils.EMPTY).replace('.', ',')
    }
}