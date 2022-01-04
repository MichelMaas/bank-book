package nl.maas.bankbook.frontend.wicket.objects

import de.agilecoders.wicket.themes.markup.html.bootswatch.BootswatchTheme
import nl.maas.filerenamer.domain.Storable
import nl.maas.filerenamer.domain.Storable.Companion.path
import nl.maas.filerenamer.utils.JsonUtils
import java.time.DayOfWeek

class Options private constructor(
    var language: String = "English",
    var theme: BootswatchTheme? = BootswatchTheme.Pulse,
    var firstDay: DayOfWeek = DayOfWeek.MONDAY
) : Storable<Options> {


    companion object {
        fun load(): Options =
            JsonUtils.load("${path(Options::class.java.simpleName)}", Options::class.java) ?: Options()
    }

}
