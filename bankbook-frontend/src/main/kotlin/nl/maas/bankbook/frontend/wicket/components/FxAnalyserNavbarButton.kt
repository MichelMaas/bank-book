package nl.maas.bankbook.frontend.wicket.components

import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarButton
import nl.maas.fxanalyzer.frontend.wicket.objects.enums.ButtonTypes
import nl.maas.fxanalyzer.frontend.wicket.pages.BasePage
import org.apache.wicket.model.Model

class FxAnalyserNavbarButton(val buttonType: ButtonTypes) :
    NavbarButton<Void>(buttonType.pageClass.java, Model.of(buttonType.label())) {

    init {
        setIconType(buttonType.iconType)
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun onBeforeRender() {
        super.onBeforeRender()
        isEnabled = findParent(BasePage::class.java).isButtonActive(buttonType)
    }

    fun enable(): FxAnalyserNavbarButton {
        isEnabled = true
        return this
    }

    fun disable(): FxAnalyserNavbarButton {
        isEnabled = false
        return this
    }

}