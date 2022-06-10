package nl.maas.bankbook.frontend.translation

import com.google.gson.Gson
import nl.maas.bankbook.frontend.wicket.objects.Language
import java.net.Authenticator
import java.net.PasswordAuthentication
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*


class IBMTranslate {

    val baseUrl =
        "https://api.eu-de.language-translator.watson.cloud.ibm.com/instances/c123cc1c-c627-44fb-bd15-4781d078e871"

    fun getLanguages(): List<Language> {
        val client = HttpClient.newBuilder().build();
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://libretranslate.com/languages")).GET()
            .build();

        val response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return Gson().fromJson<List<Language>>(response.body(), List::class.java)
    }

    fun detectLanguageFor(text: String): String {
        if (text.isNullOrBlank()) return "en"
        val client = HttpClient.newBuilder().authenticator(ApiKey()).build();
        val postfix = "v3/identify?version=2018-05-01"
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl/$postfix"))
            .POST(HttpRequest.BodyPublishers.ofString("${text}"))
            .build()
        val response =
            client.send(request, HttpResponse.BodyHandlers.ofString())
        val fromJson = Gson().fromJson<Map<String, List<Map<String, String>>>>(
            response.body(), Map::class.java
        ).toMap()
        return fromJson["languages"]!!.asSequence().flatMap { it.asSequence() }.firstOrNull()?.value
            ?: "en"
    }

    fun translate(text: String): String {
        if (text.isNullOrBlank()) return text
        var source = "en"
        val target = Locale.getDefault().language
        return translate(text, source, target)
    }

    private fun translate(text: String, source: String, target: String): String {
        val postfix = "v3/translate?version=2018-05-01"
        val model = "$source-$target"
        val body = Gson().toJson(mapOf("text" to text, "model_id" to model))
        val client = HttpClient.newBuilder().authenticator(ApiKey()).build();
        val request = HttpRequest.newBuilder()
            .uri(
                URI.create("${baseUrl}/${postfix}")
            )
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()
        val response =
            client.send(request, HttpResponse.BodyHandlers.ofString())
        if (!response.statusCode().equals(200)) {
            val newSource = detectLanguageFor(text)
            var translated = text
            if (!source.equals(newSource)) {
                translated = translate(text, newSource, target)
            }
            return translated
        }
        return Gson().fromJson<Map<String, List<Map<String, String>>>>(
            response.body(),
            Map::class.java
        )["translations"]!!.first().values.first()
    }

    fun String.utf8(): String = URLEncoder.encode(this, "UTF-8")

    private fun createParams(vararg params: Pair<String, String>): String {
        return params.map { (k, v) -> "${(k.utf8())}=${v.utf8()}" }
            .joinToString("&")
    }

    private inner class ApiKey() : Authenticator() {
        override protected fun getPasswordAuthentication() =
            PasswordAuthentication("apikey", "Q5D5oTGgA9jniASJOT1ykbtlybleuC7PAYB4iSu2VmOT".toCharArray())
    }


}