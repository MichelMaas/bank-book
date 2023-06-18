package nl.maas.bankbook.frontend.persistence.entities

import nl.maas.bankbook.domain.enums.Categories
import nl.maas.jpa.framework.entity.AbstractFullSearchEntity
import org.hibernate.search.annotations.Indexed
import javax.persistence.*

@Entity
@Indexed
@Table(name = "FILTERS")
class Filter : AbstractFullSearchEntity() {
    @Column(name = "filterString")
    var filterString: String? = null

    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    var category: Categories? = null
}