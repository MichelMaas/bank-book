package nl.maas.bankbook.frontend.services

import org.springframework.stereotype.Component
import java.io.File

@Component
class FxParserService {
    fun parseExportHtmFile(file: File): FXExport {
        return FXExport.readFXExport(file)
    }
}