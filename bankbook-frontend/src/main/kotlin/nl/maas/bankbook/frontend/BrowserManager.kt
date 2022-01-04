package nl.maas.bankbook.frontend

import nl.maas.filerenamer.utils.FileUtils
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriverException
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.edge.EdgeOptions
import org.springframework.boot.ExitCodeGenerator
import org.springframework.boot.SpringApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import java.io.IOException
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

    private fun startBrowser() {
        val url = "http://localhost:8080"
        val os = System.getProperty("os.name").lowercase(Locale.getDefault())
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
        System.setProperty("webdriver.edge.driver", "${findWebDrivers(Paths.get("/linux/msedgedriver"))}")
        startWebDriver(url)
    }

    private fun findWebDrivers(path: Path): String {
        return FileUtils.findFile(path.toString())
    }

    @Throws(IOException::class)
    private fun startMacBrowser(url: String) {
        System.setProperty("webdriver.edge.driver", "${findWebDrivers(Paths.get("/mac/msedgedriver"))}")
        startWebDriver(url)
    }

    @Throws(IOException::class)
    private fun startWinBrowser(url: String) {
        System.setProperty("webdriver.edge.driver", "${findWebDrivers(Paths.get("/windows/msedgedriver.exe"))}")
        startWebDriver(url)
    }

    private fun startWebDriver(url: String) {
        val options = EdgeOptions()
        options.addArguments("--app=$url")
        driver = EdgeDriver(options)
        driver[url]
        driver.manage().window().maximize()
        checkStatus(driver)
    }

    private fun checkStatus(driver: WebDriver) {
        try {
            while (driver.manage().window().size.height > 0) {
                Thread.sleep(1000)
            }
        } catch (ex: WebDriverException) {
            SpringApplication.exit(appContext, ExitCodeGenerator { 0 })
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        startBrowser()
    }
}