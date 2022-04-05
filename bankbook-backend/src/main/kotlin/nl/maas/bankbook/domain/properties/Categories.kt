package nl.maas.bankbook.domain.properties

import nl.maas.filerenamer.utils.JsonUtils

data class Categories(val category: Map<String, String>) {
    companion object {
        val UNKNOWN = "UNKNOWN"
        private val resourcePath = "/constants/categories.json"
        private val libPath = "lib/categories.json"

        fun valueOf(name: String): String {
            return load().get(name) ?: UNKNOWN
        }

        fun values(): Array<String> {
            return load().values.toTypedArray()
        }

        private fun load(): Map<String, String> {
            val loadResource =
                JsonUtils.loadResource(resourcePath, Map::class.java).map { it.key.toString() to it.value.toString() }
                    .toMap()
                    .toMutableMap()
            loadResource.putAll(
                JsonUtils.load(libPath, Map::class.java)?.map { it.key.toString() to it.value.toString() }
                    ?.toMap() ?: mapOf()
            )
            return loadResource
        }
    }
}