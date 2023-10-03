package nl.maas.bankbook.frontend

import nl.maas.wicket.framework.viewer.Viewer
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import javax.inject.Singleton


@Singleton
@Component
class BrowserManager private constructor() : ApplicationListener<ApplicationReadyEvent> {

    private val viewer = Viewer.get()
    private fun startBrowser() {
        val url = "http://localhost:8080"
        viewer.startBrowser(url)
    }

    fun close() {
        viewer.close()
    }


    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        val mode = System.getProperty("mode")
        if (mode.isNullOrBlank() || !mode.equals("headless")) {
            startBrowser()
        }
    }
}