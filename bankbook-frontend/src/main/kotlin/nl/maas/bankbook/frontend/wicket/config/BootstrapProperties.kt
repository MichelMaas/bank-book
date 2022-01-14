package nl.maas.bankbook.frontend.wicket.config

import de.agilecoders.wicket.core.settings.BootstrapSettings
import de.agilecoders.wicket.themes.markup.html.bootswatch.BootswatchTheme
import nl.maas.bankbook.frontend.wicket.objects.Options
import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties(prefix = BootstrapProperties.PROPERTY_PREFIX)
class BootstrapProperties : BootstrapSettings() {
    var isEnabled = true
    var theme: BootswatchTheme = Options.load().theme ?: BootswatchTheme.Pulse

    companion object {
        const val PROPERTY_PREFIX = "nl.maas.bankbook.frontend.wicket.config"
    }
}