package nl.maas.bankbook.utils

import java.util.*

class UUIDUtil {
    companion object {
        fun createUUID(existing: Array<Long>): Long {
            var new = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE
            while (existing.contains(new)) {
                new = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE
            }
            return new
        }
    }
}