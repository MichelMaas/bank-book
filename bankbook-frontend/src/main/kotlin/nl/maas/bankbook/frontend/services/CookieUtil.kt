package nl.maas.bankbook.frontend.services

import org.apache.wicket.request.cycle.RequestCycle
import org.apache.wicket.request.http.WebRequest
import org.apache.wicket.request.http.WebResponse
import javax.servlet.http.Cookie

class CookieUtil {

    companion object {

        private val languageCookie = "bankBookLanguageCookie"

        fun languageCookieExists(): Boolean {
            var exists = false
            RequestCycle.get()?.let {
                val webRequest = it.request as WebRequest
                exists = webRequest.cookies.any { it.name.equals(languageCookie) }
            }
            return exists
        }


        fun saveLanguageCookie(language: String) {
            val webResponse = RequestCycle.get().response as WebResponse
            val cookie = Cookie(languageCookie, language)
            clearLanguageCooky(webResponse)
            webResponse.addCookie(cookie)
        }

        private fun clearLanguageCooky(webResponse: WebResponse) {
            if (languageCookieExists()) {
                val webRequest: WebRequest = RequestCycle.get().request as WebRequest
                val oldCookie: Cookie = webRequest.getCookie(languageCookie)
                webResponse.clearCookie(oldCookie)
            }
        }

        fun readLanguageCookie(): String {
            val webRequest: WebRequest = RequestCycle.get().request as WebRequest
            val cookie: Cookie = webRequest.getCookie(languageCookie)
            return cookie.value
        }

    }
}