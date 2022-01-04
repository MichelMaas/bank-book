package nl.maas.bankbook.frontend.wicket.caches

import nl.maas.fxanalyzer.frontend.wicket.objects.SearchCriteria
import org.springframework.stereotype.Component

@Component
class ModelCache {
    var fxDataSet = FXDataSet.load()
    var searchCriteria: SearchCriteria = SearchCriteria.default()
}