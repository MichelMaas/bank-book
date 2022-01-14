package nl.maas.bankbook.frontend

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Component

@Component
class ContextProvider : ApplicationContextAware {

    companion object {
        lateinit var ctx: ConfigurableApplicationContext
    }

    override fun setApplicationContext(context: ApplicationContext) {
        ContextProvider.Companion.ctx = context as ConfigurableApplicationContext
    }


}