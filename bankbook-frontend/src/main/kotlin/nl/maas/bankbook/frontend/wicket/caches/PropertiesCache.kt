package nl.maas.bankbook.frontend.wicket.caches

import nl.maas.bankbook.domain.Storable
import nl.maas.bankbook.domain.properties.Categories
import nl.maas.bankbook.frontend.wicket.objects.Properties
import nl.maas.wicket.framework.services.ModelCache
import org.springframework.stereotype.Component

@Component
class PropertiesCache : ModelCache, Storable<PropertiesCache> {

    private var _properties: Properties = Storable.load(Properties::class) ?: Properties()
    val categories: Array<String> = Categories.values()
    val properties get() = _properties

    override fun isEmpty(): Boolean {
        return false
    }

    override fun refresh() {
        _properties = Storable.load(Properties::class) ?: Properties()
    }

}
