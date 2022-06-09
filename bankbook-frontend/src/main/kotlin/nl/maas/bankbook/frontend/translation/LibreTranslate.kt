package nl.maas.bankbook.frontend.translation

import com.google.gson.Gson
import nl.maas.bankbook.frontend.wicket.objects.Language
import org.apache.commons.lang3.StringUtils
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*

class LibreTranslate {


    fun getLanguages(): List<Language> {
        val client = HttpClient.newBuilder().build();
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://libretranslate.com/languages")).GET()
            .build();

        val response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return Gson().fromJson<List<Language>>(response.body(), List::class.java)
    }

    fun detectLanguageFor(text: String): String {
        val client = HttpClient.newBuilder().build();
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://libretranslate.de/detect?${createParams("q" to text)}"))
            .POST(HttpRequest.BodyPublishers.ofString(StringUtils.EMPTY))
            .build()
        val response =
            client.send(request, HttpResponse.BodyHandlers.ofString())
        return Gson().fromJson<List<Map<String, String>>>(response.body(), List::class.java).first().get("language")
            ?: "en"
    }

    fun translate(text: String): String {
        val source = detectLanguageFor(text)
        val target = Locale.getDefault().language
        val client = HttpClient.newBuilder().build();
        val request = HttpRequest.newBuilder()
            .uri(
                URI.create(
                    "https://libretranslate.de/translate?${
                        createParams(
                            "q" to text,
                            "source" to source,
                            "target" to target
                        )
                    }"
                )
            )
            .POST(HttpRequest.BodyPublishers.ofString(StringUtils.EMPTY))
            .build()
        val response =
            client.send(request, HttpResponse.BodyHandlers.ofString())
        return Gson().fromJson<Map<String, String>>(response.body(), Map::class.java).get("translatedText") ?: text
    }

    fun String.utf8(): String = URLEncoder.encode(this, "UTF-8")

    private fun createParams(vararg params: Pair<String, String>): String {
        return params.map { (k, v) -> "${(k.utf8())}=${v.utf8()}" }
            .joinToString("&")
    }


}