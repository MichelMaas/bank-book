package nl.maas.bankbook.frontend.wicket.objects

import nl.maas.bankbook.frontend.ContextProvider
import nl.maas.bankbook.frontend.wicket.caches.GoogleTranslator
import nl.maas.bankbook.frontend.wicket.caches.ModelCache
import nl.maas.bankbook.utils.FileUtils
import nl.maas.wicket.framework.objects.BasePageProperties

class BankBookBasePageProperties private constructor() : BasePageProperties<ModelCache>(
    ContextProvider.ctx.getBean(ModelCache::class.java),
    ContextProvider.ctx.getBean(GoogleTranslator::class.java),
    brandName = "Bankbook",
    iconPath = FileUtils.findFile("icon.png"),
    brandPath = FileUtils.findFile("brand.png")
) {
    companion object {
        val instance = BankBookBasePageProperties()
        fun get(): BankBookBasePageProperties {
            return instance
        }
    }
}
