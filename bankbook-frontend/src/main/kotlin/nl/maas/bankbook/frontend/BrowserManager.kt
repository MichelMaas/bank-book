package nl.maas.bankbook.frontend

import nl.maas.bankbook.utils.FileUtils
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriverException
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.edge.EdgeOptions
import org.openqa.selenium.io.Zip
import org.springframework.boot.ExitCodeGenerator
import org.springframework.boot.SpringApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import java.io.FileInputStream
import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
@Component
class BrowserManager private constructor() : ApplicationListener<ApplicationReadyEvent> {

    @Inject
    private lateinit var appContext: ApplicationContext
    lateinit var driver: WebDriver
    val os =
        if (System.getProperty("os.name").lowercase(Locale.getDefault()).contains("win")) "win" else System.getProperty(
            "os.name"
        ).lowercase(Locale.getDefault())
    val arch = if (System.getProperty("os.arch").lowercase(Locale.getDefault()).contains("64")) "64" else "32"
    val pathDelimiter = if (os.equals("win")) "\\" else "/"
    private fun startBrowser() {
        val url = "http://localhost:8080"

        try {
            if (os.indexOf("win") >= 0) {
                startWinBrowser(url)
            } else if (os.indexOf("mac") >= 0) {
                startMacBrowser("url")
            } else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
                startNixBrowser(url)
            }
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        }
    }

    fun close() {
        driver.close()
    }

    @Throws(IOException::class)
    private fun startNixBrowser(url: String) {
        System.setProperty(
            "webdriver.edge.driver",
            "${findWebDrivers(Paths.get("${pathDelimiter}linux${pathDelimiter}msedgedriver"))}"
        )
        startWebDriver(url)
    }

    private fun updateDriver(version: String) {
        val url = URL("https://msedgedriver.azureedge.net/$version/edgedriver_${os}${arch}.zip")
        val path = Path.of("edgedriver_${os}_${version}.zip")
        try {
            downloadDriver(url, path)
            unpackDriver(path)
        } catch (ex: Exception) {
            ex.printStackTrace()
            checkStatus(driver)
        } finally {
            cleanUpDriverUpdate(path)
            startBrowser()
        }
    }

    private fun cleanUpDriverUpdate(path: Path) {
        path.toFile().delete()
        Paths.get("${System.getProperty("webdriver.edge.driver")}").toFile().setExecutable(true)
    }

    private fun unpackDriver(path: Path) {
        Zip.unzip(
            FileInputStream(path.toFile()),
            Paths.get("${System.getProperty("webdriver.edge.driver").substringBeforeLast(pathDelimiter)}").toFile()
        )
    }

    private fun downloadDriver(url: URL, path: Path?) {
        url.openStream().use { Files.copy(it, path) }
        val webdriverLocation = Paths.get("${System.getProperty("webdriver.edge.driver")}").toRealPath().toString()
            .replaceAfterLast("/", "").replaceAfterLast(pathDelimiter, "")
        Paths.get(webdriverLocation).toFile().listFiles().forEach { it.delete() }
    }


    private fun findWebDrivers(path: Path): String {
        return FileUtils.findFile(path.toString())
    }

    @Throws(IOException::class)
    private fun startMacBrowser(url: String) {
        System.setProperty(
            "webdriver.edge.driver",
            "${findWebDrivers(Paths.get("${pathDelimiter}mac${pathDelimiter}msedgedriver"))}"
        )
        startWebDriver(url)
    }

    @Throws(IOException::class)
    private fun startWinBrowser(url: String) {
        System.setProperty(
            "webdriver.edge.driver",
            "${findWebDrivers(Paths.get("${pathDelimiter}windows${pathDelimiter}msedgedriver.exe"))}"
        )
        startWebDriver(url)
    }

    private fun startWebDriver(url: String) {
        val options = EdgeOptions()
        options.addArguments("--app=$url")
        options.setExperimentalOption("excludeSwitches", arrayOf("enable-automation"))
        try {
            driver = EdgeDriver(options)
            driver[url]
            driver.manage().window().maximize()
            checkStatus(driver)
        } catch (ex: Exception) {
            val driverVersion = ex.message?.split("\n")?.filter { it.contains("MSEdge version") }?.firstOrNull()
                ?.substringAfterLast("version ") ?: "0"
            val browserVersion =
                ex.message?.split("\n")?.filter { it.contains("Current browser version") }?.firstOrNull()
                    ?.substringAfterLast("version is ")?.substringBefore(" with") ?: "0"
            if (!browserVersion.contains(driverVersion)) {
                updateDriver(browserVersion)
            }
        }
    }

    private fun checkStatus(driver: WebDriver) {
        try {
            while (driver.manage().window().size.height > 0) {
                Thread.sleep(1000)
            }
        } catch (ex: WebDriverException) {
            val exit = SpringApplication.exit(appContext, ExitCodeGenerator { 0 })
            driver.quit()
            System.exit(exit)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        val mode = System.getProperty("mode")
        if ("headless".equals(mode?.lowercase())) {
            println("Starting in headless mode!")
        } else {
            startBrowser()
        }
    }
}