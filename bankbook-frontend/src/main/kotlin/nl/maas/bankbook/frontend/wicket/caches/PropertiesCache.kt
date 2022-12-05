package nl.maas.bankbook.frontend.wicket.caches

import nl.maas.bankbook.frontend.wicket.objects.ApplicationProperties
import nl.maas.bankbook.frontend.wicket.objects.I10N
import nl.maas.bankbook.frontend.wicket.objects.Options
import nl.maas.bankbook.frontend.wicket.objects.serializables.SerializableFileResourceReference
import nl.maas.bankbook.providers.Translator
import nl.maas.bankbook.utils.FileUtils
import nl.maas.bankbook.utils.JsonUtils
import org.springframework.stereotype.Component
import javax.inject.Inject

@Component
class PropertiesCache(@Inject private val translatorProvider: TranslatorProvider) : java.io.Serializable {

    val supportedLanguages get() = TranslatorProvider().translators.keys.toList()
    protected val i10N: I10N
    val translator: Translator get() = translatorProvider.getTranslatorFor(options.language)

    val options: Options
    val applicationProperties: ApplicationProperties
    val iconReference =
        SerializableFileResourceReference("favicon", FileUtils.findFile("icon.png"))

    val brandReference =
        SerializableFileResourceReference("brand", FileUtils.findFile("brand.png"))

    init {
        i10N = JsonUtils.load(FileUtils.findFile("I10N.json"), I10N::class.java)!!
        options = Options.load()
        applicationProperties = ApplicationProperties.load()
    }


}
