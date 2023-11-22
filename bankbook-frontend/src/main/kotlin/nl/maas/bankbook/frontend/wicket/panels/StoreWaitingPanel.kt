package nl.maas.bankbook.frontend.wicket.panels

import nl.maas.bankbook.frontend.wicket.caches.ModelCache
import nl.maas.wicket.framework.panels.RIAPanel
import org.apache.wicket.spring.injection.annot.SpringBean

abstract class StoreWaitingPanel : RIAPanel() {

    @SpringBean
    protected lateinit var modelCache: ModelCache
    override fun holdRenderWhile(): Boolean {
        return modelCache.shouldHoldRender
    }
}