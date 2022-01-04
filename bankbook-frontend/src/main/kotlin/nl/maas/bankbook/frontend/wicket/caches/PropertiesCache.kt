package nl.maas.bankbook.frontend.wicket.caches

import nl.maas.filerenamer.utils.FileUtils
import nl.maas.filerenamer.utils.JsonUtils
import nl.maas.fxanalyzer.frontend.wicket.objects.ApplicationProperties
import nl.maas.fxanalyzer.frontend.wicket.objects.I10N
import nl.maas.fxanalyzer.frontend.wicket.objects.Options
import nl.maas.fxanalyzer.frontend.wicket.pages.BasePage
import org.apache.wicket.resource.FileSystemResourceReference
import org.springframework.stereotype.Component
import java.nio.file.Path
import kotlin.reflect.KClass

@Component
class PropertiesCache {
    protected val i10N: I10N
    val translator: Translator
    val options: Options
    val applicationProperties: ApplicationProperties
    val iconReference =
        FileSystemResourceReference("favicon", Path.of(FileUtils.findFile("icon.png")))
    val brandReference =
        FileSystemResourceReference("brand", Path.of(FileUtils.findFile("brand.png")))

    init {
        i10N = JsonUtils.load(FileUtils.findFile("I10N.json").toString(), I10N::class.java)!!
        options = Options.load()
        applicationProperties = ApplicationProperties.load()
    }

    constructor() {
        translator = Translator(this)
    }


    inner class Translator(
        val propertiesCache: PropertiesCache,
        val supportedLanguages: List<String> = propertiesCache.i10N.languages.map { it.name }
    ) {
        fun <T : KClass<out BasePage>> translate(page: T, key: String) =
            i10N.translate(page, key, currentLanguage())

        fun currentLanguage() =
            (propertiesCache.i10N.languages.find { it.name.equals(propertiesCache.options.language) }
                ?: propertiesCache.i10N.languages.first { "en".equals(it.code) }).code
    }

}
