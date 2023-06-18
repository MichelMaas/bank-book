package nl.maas.bankbook.frontend.persistence.converters

import java.util.*
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class CurrencyConverter : AttributeConverter<Currency, String> {
    override fun convertToDatabaseColumn(currency: Currency): String {
        return currency.currencyCode
    }

    override fun convertToEntityAttribute(s: String): Currency {
        return Currency.getInstance(s)
    }
}