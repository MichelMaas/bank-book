package nl.maas.bankbook.frontend

import org.springframework.stereotype.Component

/** Kept for the existing restart action; the server owns no desktop window. */
@Component
class BrowserManager {
    fun close() {}
}
