package nl.maas.bankbook.frontend.wicket.objects

import nl.maas.bankbook.domain.Amount
import nl.maas.wicket.framework.objects.behavior.FormValueTransformer
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils

class AmountTransformer : FormValueTransformer<Amount> {
    override fun fromString(value: String): Amount {
        val parts = value.split(StringUtils.SPACE)
        if (parts.size != 2 && !NumberUtils.isParsable(parts[1])) {
            throw IllegalStateException("$value is not convertable to Amount")
        }
        return Amount(parts[1].replace(",", "."), parts[0])
    }

    override fun toString(value: Amount): String {
        return value.toString()
    }
}