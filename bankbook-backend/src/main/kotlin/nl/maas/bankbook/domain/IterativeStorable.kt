package nl.maas.bankbook.domain

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import nl.maas.bankbook.domain.annotations.StoreAs
import org.apache.commons.lang3.NotImplementedException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

interface IterativeStorable<T : IterativeStorable<T>> : Storable<T> {

    companion object {
        fun path(className: String) = "${System.getProperty("user.home")}/.bankbook/${className.lowercase()}.json"


        @JvmStatic
        fun <T : IterativeStorable<T>> load(clazz: KClass<T>): List<T> {

            val className = if (clazz.allSuperclasses.plus(clazz)
                    .any { it.hasAnnotation<StoreAs>() }
            ) clazz.allSuperclasses.plus(clazz).find { it.hasAnnotation<StoreAs>() }!!
                .findAnnotation<StoreAs>()!!.storeAs else clazz.java.simpleName
            val path = Paths.get(path(className!!))

            return runBlocking {
                if (path.toFile().exists()) Files.readAllLines(path).map { async { decode(it) }.await() }
                    .map { async { Gson().fromJson(it, JsonObject::class.java) as JsonObject }.await() }
                    .map {
                        async {
                            Gson().fromJson(
                                it,
                                if (it.has("type"))
                                    Class.forName(
                                        it.get("type").asString
                                    ) as Class<T> else
                                    clazz.java
                            )
                        }.await()
                    } else listOf<T>()
            }
        }

        fun <T : IterativeStorable<T>> storeAll(storables: List<T>): List<T> {
            if (storables.size > 0) {
                val storablesClass = storables[0]::class
                val hasAltName = storablesClass.allSuperclasses.plus(storablesClass)
                    .any { it.hasAnnotation<StoreAs>() }
                val className =
                    if (hasAltName) storablesClass.allSuperclasses.plus(storablesClass)
                        .find { it.hasAnnotation<StoreAs>() }!!
                        .findAnnotation<StoreAs>()!!.storeAs else storablesClass.java.simpleName
                val path = path(className.lowercase())
                val dir = Paths.get(path.substring(0, path.lastIndexOf("/")))
                val oldList = load(storablesClass)
                val toRemove = storables.flatMap { runBlocking { async { it.replace(oldList) }.await() } }
                val newList = oldList.minus(toRemove).plus(storables)
                Files.setAttribute(Files.createDirectories(dir), "dos:hidden", true)
                Files.write(Paths.get("${path}"), newList.map { encode(serialize(it)) }, Charsets.UTF_8)
                return load(storablesClass)
            } else {
                return listOf()
            }
        }

        private fun encode(json: String): String {
            return String(Base64.getEncoder().encode(json.toByteArray()), Charsets.UTF_8)
        }

        private fun decode(json: String): String {
            return String(Base64.getDecoder().decode(json.toByteArray()))
        }

        private fun <T : IterativeStorable<T>> serialize(it: IterativeStorable<T>): String {
            val jsonTree = Gson().toJsonTree(it) as JsonObject
            jsonTree.addProperty(
                "type",
                it::class.qualifiedName
            )
            return Gson().toJson(jsonTree)
        }
    }


    fun replace(source: List<T>): List<T>


    override fun store(): T {
        val hasAltName = this::class.allSuperclasses.plus(this::class)
            .any { it.hasAnnotation<StoreAs>() }
        val className =
            if (hasAltName) this::class.allSuperclasses.plus(this::class).find { it.hasAnnotation<StoreAs>() }!!
                .findAnnotation<StoreAs>()!!.storeAs else this::class.java.simpleName
        val path = path(className.lowercase())
        val dir = Paths.get(path.substring(0, path.lastIndexOf("/")))
        val oldList = load(this::class)
        val newList = oldList.minus(replace(oldList as List<T>)).plus(this)
        Files.setAttribute(Files.createDirectories(dir), "dos:hidden", true)
        Files.write(Paths.get("${path}"), newList.map { encode(serialize(it)) }, Charsets.UTF_8)
        return this as T
    }


    fun reloadAll(): List<T> {
        return load(this::class) as List<T>
    }

    override fun reload(): T {
        throw NotImplementedException("Use #reloadAll() to reload!")
    }

}
