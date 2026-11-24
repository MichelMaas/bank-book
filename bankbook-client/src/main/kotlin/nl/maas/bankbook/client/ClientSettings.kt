package nl.maas.bankbook.client

import java.io.IOException
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.*
import java.util.function.IntPredicate

object ClientSettings {
    fun validate(value: String): String {
        val s = value.trim()
        val uri: URI?
        try {
            uri = URI.create(s)
        } catch (ex: IllegalArgumentException) {
            throw IllegalArgumentException("Ongeldig serveradres.")
        }
        require(
            !(!("http".equals(uri.getScheme(), ignoreCase = true) || "https".equals(
                uri.getScheme(),
                ignoreCase = true
            )) || uri.getHost() == null || uri.getUserInfo() != null || uri.getFragment() != null || uri.getRawQuery() != null || uri.getPort() == 0 || uri.getPort() > 65535 || s.chars()
                .anyMatch(IntPredicate { codePoint: Int -> Character.isISOControl(codePoint) }))
        ) { "Gebruik een volledig http:// of https:// adres zonder aanmeldgegevens, query of fragment." }
        return s
    }

    private fun userFile(): Path {
        val xdg = System.getenv("XDG_CONFIG_HOME")
        val root = if (xdg != null && Path.of(xdg).isAbsolute()) Path.of(xdg) else Path.of(
            System.getProperty("user.home"),
            ".config"
        )
        return root.resolve("bankbook/client.properties")
    }

    @Throws(IOException::class)
    fun load(): String {
        val override = System.getProperty("bankbook.serverUrl", System.getenv("BANKBOOK_SERVER_URL"))
        if (override != null && !override.isBlank()) return validate(override)
        val props = Properties()
        for (p in arrayOf<Path>(
            Path.of(System.getProperty("bankbook.clientConfig", "/etc/bankbook/client.properties")),
            userFile()
        )) {
            if (Files.exists(p)) Files.newBufferedReader(p).use { `in` -> props.load(`in`) }
        }
        return validate(props.getProperty("server.url", "http://127.0.0.1"))
    }

    @Throws(IOException::class)
    fun save(url: String) {
        val props = Properties()
        props.setProperty("server.url", validate(url))
        val path = userFile()
        Files.createDirectories(path.getParent())
        val tmp = Files.createTempFile(path.getParent(), "client-", ".tmp")
        try {
            Files.newBufferedWriter(tmp).use { out -> props.store(out, "Bankbook client") }
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } finally {
            Files.deleteIfExists(tmp)
        }
    }
}
