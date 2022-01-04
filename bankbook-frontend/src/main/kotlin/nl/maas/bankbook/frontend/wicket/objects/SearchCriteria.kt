package nl.maas.bankbook.frontend.wicket.objects

import java.io.File
import java.io.Serializable

data class SearchCriteria(var file: File? = null) : Serializable {

    companion object {
        fun default() = SearchCriteria()
    }
}