package nl.maas.bankbook.domain

import org.apache.commons.lang3.StringUtils
import java.math.BigDecimal
import java.math.RoundingMode

class Amount(var value: BigDecimal, val symbol: String) : java.io.Serializable,
    Comparable<BigDecimal> by value {

    constructor(str: String, symbol: String) : this(BigDecimal(str.replace(",", ".")), symbol)

    init {
        this.value.setScale(2, RoundingMode.HALF_UP)
    }

    override fun toString(): String {
        return "${symbol} ${value.toString().replace(",", StringUtils.EMPTY).replace('.', ',')}"
    }

}