package nl.maas.bankbook.client

import nl.maas.wicket.framework.viewer.Viewer
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
@Component
class BrowserManager private constructor() : ApplicationListener<ApplicationReadyEvent> {

    private val viewer = Viewer.get()

    @Inject
    lateinit var properties: PropertiesCache

    private fun startBrowser() {
        val url = properties.url
        viewer.startBrowser(url)
    }

    fun close() {
        viewer.close()
    }


    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        startBrowser()
    }
}