package nl.maas.bankbook.client

import nl.maas.framework.io.utils.AppDirectories.dataDir
import nl.maas.wicket.framework.viewer.Viewer
import nl.maas.wicket.framework.viewer.Viewers
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

/** The same viewer and runtime preparation used by FileRenamer.  */
class BankbookClient {
    private var viewer: Viewer? = null

    @Throws(Exception::class)
    fun start() {
        val server = ClientSettings.load()
        viewer = Viewers.viewer
        val data = dataDir("Bankbook")
        Files.createDirectories(data)
        val icon = data.resolve("bankbook.png")
        BankbookClient::class.java.getResourceAsStream("/open/images/bankbook.png").use { input ->
            if (input != null) Files.copy(input, icon, StandardCopyOption.REPLACE_EXISTING)
        }
        viewer!!.icon = icon.toAbsolutePath().toString()
        viewer!!.onCloseRequest {
            System.exit(0)
            Unit
        }
        viewer!!.startBrowser(server, "Bankbook")
    }

    @Throws(Exception::class)
    fun configure() {
        SwingUtilities.invokeAndWait(Runnable {
            var current: String?
            try {
                current = ClientSettings.load()
            } catch (ex: Exception) {
                current = "http://127.0.0.1"
            }
            while (true) {
                val selected = JOptionPane.showInputDialog(
                    null,
                    "Adres van de Bankbook-server:", "Bankbook-server instellen",
                    JOptionPane.QUESTION_MESSAGE, null, null, current
                ) as? String ?: return@Runnable
                try {
                    ClientSettings.save(selected)
                    return@Runnable
                } catch (ex: Exception) {
                    JOptionPane.showMessageDialog(null, ex.message, "Ongeldig serveradres", JOptionPane.ERROR_MESSAGE)
                    current = selected
                }
            }
        })
    }

    fun getViewer(): Viewer {
        return viewer!!
    }
}
