package nl.maas.bankbook.frontend.javafx

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.scene.web.WebView
import javafx.stage.Stage

class JavaFXApplication : Application() {


    override fun start(stage: Stage) {
        stage.title = "Filerenamer"
        stage.isMaximized = true
        val webView = WebView()
        webView.isContextMenuEnabled = true
        webView.engine.load("http://localhost:8080")
        val stackPane = StackPane(webView)
        val scene = Scene(stackPane)
        stage.scene = scene
        stage.show()
    }

    override fun stop() {
        super.stop()
        val ctx = nl.maas.fxanalyzer.frontend.ContextProvider.ctx
        ctx.close()
    }

}