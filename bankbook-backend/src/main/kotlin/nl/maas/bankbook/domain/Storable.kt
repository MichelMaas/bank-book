package nl.maas.bankbook.domain

import com.google.gson.Gson
import nl.maas.bankbook.domain.annotations.StoreAs
import java.io.FileReader
import java.io.Serializable
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

interface Storable<T : Storable<T>> : Serializable {
    companion object {
        fun path(className: String) = "${System.getProperty("user.home")}/.bankbook/${className.lowercase()}.json"

        @JvmStatic
        fun <T : Storable<T>> load(clazz: KClass<T>): T? {
            val className = if (clazz.allSuperclasses.plus(clazz)
                    .any { it.hasAnnotation<StoreAs>() }
            ) clazz.allSuperclasses.plus(clazz).find { it.hasAnnotation<StoreAs>() }!!
                .findAnnotation<StoreAs>()!!.storeAs else clazz.java.simpleName

            val path = Paths.get(path(className!!))
            return if (path.toFile().exists()) Gson().fromJson<T>(FileReader(path.toFile()), clazz.java) else null
        }
    }

    fun store(): T {
        val className = if (this::class.allSuperclasses.plus(this::class)
                .any { it.hasAnnotation<StoreAs>() }
        ) this::class.allSuperclasses.plus(this::class).find { it.hasAnnotation<StoreAs>() }!!
            .findAnnotation<StoreAs>()!!.storeAs else this::class.java.simpleName
        val path = path(className.lowercase())
        val dir = Paths.get(path.substring(0, path.lastIndexOf("/")))
        Files.setAttribute(Files.createDirectories(dir), "dos:hidden", true)
        Files.write(Paths.get("${path}"), listOf(Gson().toJson(this)), Charsets.UTF_8)
        return this as T
    }
}