package nl.maas.bankbook.frontend.wicket.objects

import nl.maas.filerenamer.utils.JsonUtils

data class ApplicationProperties(
    val version: String
) {
    companion object {
        fun load(): ApplicationProperties =
            JsonUtils.loadResource("/properties/application.json", ApplicationProperties::class.java)
    }
}