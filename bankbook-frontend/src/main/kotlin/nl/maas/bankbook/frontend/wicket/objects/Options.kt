package nl.maas.bankbook.frontend.wicket.objects

import de.agilecoders.wicket.themes.markup.html.bootswatch.BootswatchTheme
import nl.maas.bankbook.frontend.services.CookieUtil
import nl.maas.filerenamer.domain.Storable
import nl.maas.filerenamer.domain.Storable.Companion.path
import nl.maas.filerenamer.utils.JsonUtils
import java.time.DayOfWeek

class Options private constructor(
    var theme: BootswatchTheme = BootswatchTheme.Materia,
    var firstDay: DayOfWeek = DayOfWeek.MONDAY,
    var watchedFolder: String = System.getProperty("user.home")
) : Storable<Options> {

    var language: String
        get() = if (CookieUtil.languageCookieExists()) CookieUtil.readLanguageCookie() else "English"
        set(language: String) = CookieUtil.saveLanguageCookie(language)

    companion object {
        fun load(): Options =
            JsonUtils.load("${path(Options::class.java.simpleName)}", Options::class.java) ?: Options()
    }

}
