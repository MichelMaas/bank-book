package nl.maas.bankbook.frontend.wicket.objects

import de.agilecoders.wicket.themes.markup.html.bootswatch.BootswatchTheme
import nl.maas.filerenamer.domain.Storable
import nl.maas.filerenamer.domain.Storable.Companion.path
import nl.maas.filerenamer.utils.JsonUtils
import java.time.DayOfWeek
import java.util.*

class Options private constructor(
    var theme: BootswatchTheme = BootswatchTheme.Materia,
    var firstDay: DayOfWeek = DayOfWeek.MONDAY,
    var watchedFolder: String = System.getProperty("user.home")
) : Storable<Options> {

    val language: String
        get() = Locale.getDefault().language

    companion object {
        fun load(): Options =
            JsonUtils.load("${path(Options::class.java.simpleName)}", Options::class.java) ?: Options()
    }

}
