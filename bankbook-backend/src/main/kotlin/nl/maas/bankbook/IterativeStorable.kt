package nl.maas.bankbook

import com.google.gson.Gson
import nl.maas.filerenamer.domain.Storable
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.reflect.KClass

interface IterativeStorable<T : IterativeStorable<T>> : Storable<T> {

    companion object {
        fun path(className: String) = "${System.getProperty("user.home")}/.bankbook/${className.lowercase()}.json"

        @JvmStatic
        fun <T : IterativeStorable<T>> load(clazz: KClass<T>): List<T> {
            val className = clazz.simpleName
            val path = Paths.get(path(className!!))
            return if (path.toFile().exists()) Files.readAllLines(path)
                .map { Gson().fromJson(it, clazz.java) } else listOf<T>()
        }
    }

    fun replace(source: List<T>): List<T>


    override fun store(): T {
        val className = this::class.java.simpleName
        val path = path(className.lowercase())
        val dir = Paths.get(path.substring(0, path.lastIndexOf("/")))
        val oldList = load(this::class)
        val newList = oldList.minus(replace(oldList as List<T>)).plus(this)
        Files.setAttribute(Files.createDirectories(dir), "dos:hidden", true)
        Files.write(Paths.get("${path}"), newList.map { Gson().toJson(it) }, Charsets.UTF_8)
        return this as T
    }
}