package nl.maas.bankbook.frontend.wicket.tools

import de.martinspielmann.wicket.chartjs.data.dataset.property.data.NumberDataValue
import java.math.BigDecimal

class BigDecimalSafeNumberDataValue(number: Number) : NumberDataValue(number) {
    constructor(number: BigDecimal) : this(number.toDouble())

    companion object {
        fun <T : Number> of(vararg number: T): List<BigDecimalSafeNumberDataValue> {
            return number.map { BigDecimalSafeNumberDataValue(if (it is BigDecimal) it.toDouble() else it) }
        }

        fun <T : Number> of(numbers: Collection<T>): List<BigDecimalSafeNumberDataValue> {
            return numbers.map { BigDecimalSafeNumberDataValue(if (it is BigDecimal) it.toDouble() else it) }
        }
    }
}