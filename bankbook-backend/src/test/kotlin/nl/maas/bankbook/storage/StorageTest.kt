package nl.maas.bankbook.storage

import nl.maas.bankbook.domain.Storable
import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Files

class StorageTest {
    data class SyntheticRecord(var label: String = "") : Storable<SyntheticRecord>
    @Test fun dataDirectoryCanBeConfiguredAndReloaded() {
        val root = Files.createTempDirectory("bankbook-storage-test-")
        val previous = System.getProperty("bankbook.dataDir")
        try {
            System.setProperty("bankbook.dataDir", root.toString())
            SyntheticRecord("synthetic fixture").store()
            assertEquals("synthetic fixture", Storable.load(SyntheticRecord::class)?.label)
            assertTrue(Files.exists(root.resolve("syntheticrecord.json")))
            val payment = nl.maas.bankbook.domain.Payment(1L, java.time.LocalDate.of(2026, 1, 2),
                nl.maas.bankbook.domain.IBAN("NL00TEST0000000001"), java.util.Currency.getInstance("EUR"),
                nl.maas.bankbook.domain.Amount(java.math.BigDecimal("12.34"), "EUR"),
                nl.maas.bankbook.domain.enums.MutationTypes.MAN, "Synthetic payment")
            val restored = nl.maas.bankbook.domain.IterativeStorable.storeAll<nl.maas.bankbook.domain.Transaction>(listOf(payment)).single()
            assertEquals(payment.date, restored.date)
            assertEquals(payment.mutation.value, restored.mutation.value)
            assertEquals(payment.id, restored.id)
        } finally {
            if (previous == null) System.clearProperty("bankbook.dataDir") else System.setProperty("bankbook.dataDir", previous)
            Files.walk(root).use { stream -> stream.sorted(Comparator.reverseOrder()).forEach { Files.delete(it) } }
        }
    }
}
