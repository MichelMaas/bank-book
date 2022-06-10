package nl.maas.bankbook.domain.enums

import org.apache.commons.lang3.StringUtils

enum class Categories {
    GROCERIES,
    SPORTS,
    CLOTHING,
    INSURANCE,
    CAR,
    PUBLIC_TRANSPORT,
    UTILITIES,
    TAX,
    TAX_RETURNS,
    TELECOM,
    VACATION,
    HOME,
    LOANS,
    SCHOOL,
    WORK,
    INCOME,
    EATING_OUT_TAKE_OUT,
    CHILD_BENEFITS,
    SAVING,
    INVESTMENTS,
    HEALTH,
    SHOPPING,
    OTHER;

    override fun toString(): String {
        return name.lowercase().replaceFirstChar { name[0] }.replace("_", StringUtils.SPACE)
    }
}