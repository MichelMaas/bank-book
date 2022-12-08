package nl.maas.bankbook.frontend.wicket.config

//@ConfigurationProperties(prefix = FilerenamerProperties.PROPERTY_PREFIX)
class BankBookProperties {
    var isEnabled = true

    companion object {
        const val PROPERTY_PREFIX = "nl.maas.bankbook.frontend.wicket.config"
    }
}