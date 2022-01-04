package nl.maas.filerenamer.domain

import com.google.gson.Gson
import java.io.Serializable
import java.nio.file.Files
import java.nio.file.Paths

interface Storable<T : Storable<T>> : Serializable {
    companion object {
        fun path(className: String) = "${System.getProperty("user.home")}/.fxanalyzer/${className.lowercase()}.json"
    }

    fun store(): T {
        val className = this::class.java.simpleName
        val path = path(className.lowercase())
        val dir = Paths.get(path.substring(0, path.lastIndexOf("/")))
        Files.setAttribute(Files.createDirectories(dir), "dos:hidden", true)
        Files.write(Paths.get("${path}"), listOf(Gson().toJson(this)), Charsets.UTF_8)
        return this as T
    }
}