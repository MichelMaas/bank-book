package nl.maas.bankbook.frontend.wicket.panels

import nl.maas.bankbook.frontend.services.PayDateUtility
import nl.maas.bankbook.frontend.wicket.caches.ModelCache
import nl.maas.bankbook.frontend.wicket.objects.enums.StartOfMonth
import java.time.LocalDate
import java.time.Year
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class MonthOverviewPanel : AbstractOverviewPanel(ModelCache.PERIOD.MONTH) {

    @Transient
    private val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

    override fun addToSummaryRight(): Array<Pair<*, *>> {
        val period = getPeriod()
        return arrayOf("Period" to "${period.first.format(formatter)} - ${period.second.format(formatter)}")
    }

    private fun getPeriod(): Pair<LocalDate, LocalDate> {
        val date = modelCache.date
        if (propertiesCache.properties.startOfMonth.equals(StartOfMonth.PAY_DAY)) {
            return PayDateUtility.get(modelCache).findIncomeStartEndDates(date.month, Year.of(date.year))
        } else {
            return date.withDayOfMonth(1) to date.withDayOfMonth(date.lengthOfMonth())
        }
    }
}