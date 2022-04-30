package nl.maas.bankbook.domain

import com.google.gson.Gson
import java.io.FileReader
import java.io.Serializable
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.reflect.KClass

interface Storable<T : Storable<T>> : Serializable {
    companion object {
        fun path(className: String) = "${System.getProperty("user.home")}/.bankbook/${className.lowercase()}.json"

        @JvmStatic
        fun <T : Storable<T>> load(clazz: KClass<T>): T? {
            val className = clazz.simpleName
            val path = Paths.get(path(className!!))
            return if (path.toFile().exists()) Gson().fromJson<T>(FileReader(path.toFile()), clazz.java) else null
        }
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