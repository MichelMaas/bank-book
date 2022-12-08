package nl.maas.bankbook.frontend.wicket.config

import com.giffing.wicket.spring.boot.context.extensions.WicketApplicationInitConfiguration
import org.apache.wicket.protocol.http.WebApplication
import org.springframework.beans.factory.annotation.Autowired

//
//@ApplicationInitExtension
//@ConditionalOnProperty(
//    prefix = nl.maas.bankbook.frontend.wicket.config.FilerenamerProperties.PROPERTY_PREFIX,
//    value = ["enabled"],
//    matchIfMissing = true
//)
//@ConditionalOnClass(
//    FilerenamerConfig::class
//)
//@EnableConfigurationProperties(nl.maas.bankbook.frontend.wicket.config.FilerenamerProperties::class)
class BankBookConfig : WicketApplicationInitConfiguration {
    @Autowired
    private val prop: nl.maas.bankbook.frontend.wicket.config.BankBookProperties? = null
    override fun init(webApplication: WebApplication) {
        webApplication.cspSettings.blocking().disabled()
    }
}