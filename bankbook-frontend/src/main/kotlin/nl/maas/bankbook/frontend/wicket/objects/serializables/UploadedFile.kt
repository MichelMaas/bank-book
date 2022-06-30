package nl.maas.bankbook.frontend.wicket.objects.serializables

import org.apache.commons.lang3.StringUtils
import org.apache.wicket.markup.html.form.upload.FileUpload
import java.io.File
import java.nio.file.Files

class UploadedFile(fileUpload: FileUpload? = null) : java.io.Serializable {

    private val string: String

    val file get():File = Files.writeString(Files.createTempFile("tmp_upload", ".tmp"), string).toFile()

    init {
        var value: String = StringUtils.EMPTY
        fileUpload?.let {
            value = Files.readString(it.writeToTempFile().toPath())
        }
        this.string = value
    }


}