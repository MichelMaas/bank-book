package nl.maas.bankbook.frontend.wicket.pages

import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.Navbar
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarButton
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarComponents
import nl.maas.bankbook.frontend.services.ParserService
import nl.maas.bankbook.frontend.wicket.caches.ModelCache
import nl.maas.bankbook.frontend.wicket.caches.PropertiesCache
import nl.maas.bankbook.frontend.wicket.components.FxAnalyserNavbarButton
import nl.maas.bankbook.frontend.wicket.objects.enums.ButtonTypes
import org.apache.commons.lang3.StringUtils
import org.apache.wicket.AttributeModifier
import org.apache.wicket.Component
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalDialog
import org.apache.wicket.markup.head.CssReferenceHeaderItem
import org.apache.wicket.markup.head.IHeaderResponse
import org.apache.wicket.markup.html.GenericWebPage
import org.apache.wicket.markup.html.WebMarkupContainer
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.model.Model
import org.apache.wicket.protocol.http.WebApplication
import org.apache.wicket.request.mapper.parameter.PageParameters
import java.time.Duration
import java.time.LocalTime
import javax.inject.Inject


open class BasePage : GenericWebPage<Void?> {

    constructor() : super()

    constructor(pageParameters: PageParameters) : super(pageParameters)

    lateinit var navbar: Navbar

    @Inject
    lateinit var parserService: ParserService

    @Inject
    lateinit var modelCache: ModelCache

    @Inject
    lateinit var propertiesCache: PropertiesCache

    val start: LocalTime = LocalTime.now()

    override fun onInitialize() {
        super.onInitialize()
        outputMarkupId = true
        (application as WebApplication).mountResource("/images/icon.png", propertiesCache.iconReference)
    }

    protected fun newNavbar(markupId: String): Navbar {
        navbar =
            ToolTipNavBar(
                markupId,
                propertiesCache.applicationProperties.version
            )
        navbar.position = Navbar.Position.TOP
        navbar.add(NavbarProvider())
        navbar.setBrandName(Model.of(propertiesCache.translator.translate(BasePage::class, "title")))
//        navbar.setBrandImage(propertiesCache.brandReference, Model.of(StringUtils.EMPTY))
        val navbarButtons: Array<NavbarButton<*>> = ButtonTypes.values().map { button: ButtonTypes? ->
            FxAnalyserNavbarButton(
                button!!
            )
        }.toTypedArray()
        navbar.addComponents(NavbarComponents.transform(Navbar.ComponentPosition.LEFT, *navbarButtons))
        navbar.outputMarkupId = true
        return navbar
    }

    fun findNavButton(type: ButtonTypes): FxAnalyserNavbarButton? {
        return navbar.filter { component -> component.javaClass.isAssignableFrom(FxAnalyserNavbarButton::class.java) }
            .map { it as FxAnalyserNavbarButton }.firstOrNull { type.equals(it.buttonType) }
    }


    private val modalDialog = ModalDialog("notifications")
    val loader = WebMarkupContainer("loader")

    override fun onBeforeRender() {
        super.onBeforeRender()
        loader.outputMarkupId = true
        addOrReplace(newNavbar("navbar"), modalDialog, loader)
    }

    override fun renderHead(response: IHeaderResponse) {
        super.renderHead(response)
        response.render(CssReferenceHeaderItem.forUrl("css/main.css"))
        response.render(CssReferenceHeaderItem.forUrl("http://maxcdn.bootstrapcdn.com/font-awesome/4.1.0/css/font-awesome.min.css"))
    }

    override fun onAfterRender() {
        super.onAfterRender()
        val end = LocalTime.now()
        println("Render time for page ${this::class.simpleName} is: ${Duration.between(start, end).toString()}")
    }

    @ExperimentalStdlibApi
    fun isButtonActive(type: ButtonTypes): Boolean {
        var filled = when (type) {
            ButtonTypes.YEAR_OVERVIEW, ButtonTypes.MONTH_OVERVIEW, ButtonTypes.TRANSACTIONS, ButtonTypes.FILTERS, ButtonTypes.OPTIONS, ButtonTypes.TEST -> return true
            else -> false
        }
        return filled
    }

    private inner class NavbarProvider : AttributeModifier(
        "class",
        "navbar navbar-expand navbar-dark flex-column flex-md-row bd-navbar sticky-top bg-primary fixed-bottom"
    ) {
        val childClass = AttributeModifier("class", "nav-item")
    }

    private inner class NavtabsProvider : AttributeModifier("class", "nav nav-tabs nav-tabs-dark")


    inner class ToolTipNavBar(id: String, val toolTip: String) : Navbar(id) {

        override fun newBrandNameLink(componentId: String?): Component {
            return super.newBrandNameLink(componentId).add(*tooltipVersionProvider(toolTip))
        }

        fun tooltipVersionProvider(toolTip: String) = arrayOf(
            AttributeModifier("data-toggle", "tooltip"),
            AttributeModifier("data-placement", "bottom"),
            AttributeModifier("title", toolTip)
        )
    }

    protected fun showModal(target: AjaxRequestTarget, title: String, text: String) {
        modalDialog.add(Label("modalTitle", title), Label("modalContent", text))
        modalDialog.open(target)
    }

    fun ajaxStartLoader(target: AjaxRequestTarget) {
        val classAttr = loader.markupAttributes["class"].toString()
        loader.add(AttributeModifier.replace("class", classAttr.replace("d-none", StringUtils.EMPTY).trim()))
        target.add(loader)
    }

    fun ajaxStopLoader(target: AjaxRequestTarget) {
        loader.add(AttributeModifier.append("class", "d-none"))
        target.add(loader)
    }

}