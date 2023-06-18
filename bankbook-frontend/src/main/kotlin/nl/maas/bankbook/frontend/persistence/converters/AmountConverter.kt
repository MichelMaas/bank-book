package nl.maas.bankbook.frontend.persistence.converters

import nl.maas.bankbook.domain.Amount
import java.math.BigDecimal
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class AmountConverter : AttributeConverter<Amount, Double> {
    override fun convertToDatabaseColumn(amount: Amount): Double {
        return amount.value.toDouble()
    }

    override fun convertToEntityAttribute(double: Double): Amount {
        return Amount(BigDecimal.valueOf(double), "")
    }
}