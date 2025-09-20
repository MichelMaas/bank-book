package nl.maas.bankbook.client

import nl.maas.bankbook.domain.Storable
import nl.maas.wicket.framework.services.ModelCache
import org.springframework.stereotype.Component

@Component
class PropertiesCache : ModelCache, Storable<PropertiesCache> {

    private var _Client_properties: ClientProperties = Storable.load(ClientProperties::class) ?: ClientProperties()
    protected val properties get() = _Client_properties
    val url get() = properties.serverURL

    init {
        if (isEmpty()) {
            properties.serverURL = "http://bankbook.mnw"
            properties.store()
        }
    }

    override final fun isEmpty(): Boolean {
        return properties.serverURL.isBlank()
    }

    override fun refresh() {
        _Client_properties = Storable.load(ClientProperties::class) ?: ClientProperties()
    }

}
