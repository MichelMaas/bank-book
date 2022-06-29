package nl.maas.bankbook.frontend.wicket.objects

import org.apache.wicket.WicketRuntimeException
import org.apache.wicket.resource.FileSystemResource
import org.apache.wicket.resource.FileSystemResourceReference
import java.nio.file.Path

class SerializableFileResourceReference(name: String, val path: String) : FileSystemResourceReference(name),
    java.io.Serializable {
    override fun getFileSystemResource(): FileSystemResource {
        if (path == null) {
            throw WicketRuntimeException(
                "Please override #getResource() and provide a path if using a constructor which doesn't take one as argument."
            )
        }
        return FileSystemResource(Path.of(path))
    }
}