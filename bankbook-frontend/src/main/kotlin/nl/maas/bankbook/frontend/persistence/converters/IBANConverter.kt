package nl.maas.bankbook.frontend.persistence.converters

import nl.maas.bankbook.domain.IBAN
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class IBANConverter : AttributeConverter<IBAN, String> {
    override fun convertToDatabaseColumn(iban: IBAN): String {
        return iban.value
    }

    override fun convertToEntityAttribute(s: String): IBAN {
        return IBAN(s)
    }
}