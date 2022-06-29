package nl.maas.bankbook.frontend.wicket.objects

import nl.maas.bankbook.utils.JsonUtils

data class ApplicationProperties(
    val version: String
) : java.io.Serializable {
    companion object {
        fun load(): ApplicationProperties =
            JsonUtils.loadResource("/properties/application.json", ApplicationProperties::class.java)
    }
}