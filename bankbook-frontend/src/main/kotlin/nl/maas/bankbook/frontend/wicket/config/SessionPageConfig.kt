package nl.maas.bankbook.frontend.wicket.config

import com.giffing.wicket.spring.boot.context.extensions.ApplicationInitExtension
import com.giffing.wicket.spring.boot.context.extensions.WicketApplicationInitConfiguration
import org.apache.wicket.page.PageManager
import org.apache.wicket.pageStore.InSessionPageStore
import org.apache.wicket.pageStore.RequestPageStore
import org.apache.wicket.protocol.http.WebApplication
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order

/** Apply to the actual WicketBootStandardWebApplication created by the starter. */
@ApplicationInitExtension
@Order(Ordered.LOWEST_PRECEDENCE)
class SessionPageConfig : WicketApplicationInitConfiguration {
    override fun init(webApplication: WebApplication) {
        webApplication.setPageManagerProvider {
            PageManager(RequestPageStore(InSessionPageStore(5)))
        }
    }
}
