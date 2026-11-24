package nl.maas.bankbook

import nl.maas.bankbook.utils.CSVUtils
import java.nio.file.Files

abstract class AbstractTest {
    protected val parsedFile = run {
        val file = Files.createTempFile("bankbook-synthetic-", ".csv")
        try {
            Files.writeString(file, "NL00TEST0000000001,2026-01-01,10.00,Test\nNL00TEST0000000001,2026-01-02,20.00,Test\n")
            CSVUtils.parseFile(file.toFile())
        } finally { Files.deleteIfExists(file) }
    }
}
