package nl.maas.bankbook.frontend.wicket.objects

import nl.maas.bankbook.IterativeStorable
import nl.maas.bankbook.domain.Transaction
import nl.maas.bankbook.domain.properties.Categories.Companion.UNKNOWN
import org.apache.commons.lang3.StringUtils
import org.apache.wicket.markup.html.form.upload.FileUpload

class Filter() : IterativeStorable<Filter> {
    var filter = StringUtils.EMPTY
    var category = UNKNOWN
    var file: FileUpload? = null

    @Transient
    var foundTransactions: List<Transaction> = listOf()

    @Transient
    var saveFilter: Boolean = true
    override fun replace(source: List<Filter>): List<Filter> {
        return source.filter { it.filter.equals(this.filter) }
    }

}