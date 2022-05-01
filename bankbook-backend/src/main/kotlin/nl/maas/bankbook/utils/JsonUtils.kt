package nl.maas.bankbook.utils

import com.google.gson.Gson
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.InputStreamReader


class JsonUtils private constructor() {
    companion object {
        fun <T> load(path: String, clazz: Class<T>): T? {
            val jsonFile = File(FileUtils.findFile(path))
            return if (jsonFile.exists()) Gson().fromJson(
                BufferedReader(FileReader(jsonFile, Charsets.UTF_8)),
                clazz
            ) as T else null
        }

        fun <T> loadResource(path: String, clazz: Class<T>): T {
            val reader =
                BufferedReader(InputStreamReader(JsonUtils::class.java.getResourceAsStream(path), Charsets.UTF_8))
            return Gson().fromJson(
                reader,
                clazz
            ) as T
        }
    }
}
