package nl.maas.bankbook.frontend.wicket.panels

import nl.maas.bankbook.frontend.wicket.caches.PropertiesCache
import nl.maas.bankbook.frontend.wicket.tools.TranslatedDayOfWeek
import org.apache.commons.lang3.StringUtils
import org.apache.wicket.AttributeModifier
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.ajax.markup.html.AjaxLink
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.markup.html.list.ListItem
import org.apache.wicket.markup.html.list.ListView
import org.apache.wicket.model.Model
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.*
import javax.inject.Inject

abstract class CalendarPanel(
    id: String,
    private var currentMonth: LocalDate,
    value: Map<LocalDate, String>
) :
    AbstractPanel(id) {
    private val DAYS_PER_ROW = 6
    private var hiddenValue: Map<LocalDate, String>

    @Inject
    lateinit var propertiesCache: PropertiesCache

    init {
        hiddenValue = value
        fillMonthValues(value)
        outputMarkupId = true
        add(BackLink(), ForwardLink())
    }

    private fun fillMonthValues(value: Map<LocalDate, String>) {
        val tmp = (1..currentMonth.month.length(currentMonth.isLeapYear)).map {
            currentMonth.withDayOfMonth(it)
        }.filterNot { value.containsKey(it) }.map { it to StringUtils.EMPTY }.toMap().toMutableMap()
        tmp.putAll(value)
        hiddenValue = tmp.toSortedMap()
    }

    fun replaceValue(newValue: Map<LocalDate, String>) {
        fillMonthValues(newValue)
    }

    override fun onBeforeRender() {
        super.onBeforeRender()
        setComponents()
    }

    private fun setComponents() {
        addOrReplace(
            Label(
                "month",
                propertiesCache.translator.translate(
                    "${
                        propertiesCache.translator.translate(
                            currentMonth.month.name
                        )
                    } ${currentMonth.year}${propertiesCache.translator.translate("yearIndicator")}"
                )
            ),
            DayRepeater(),
            RowRepeater(createRowRanges(currentMonth.month.length(isLeapYear()), DAYS_PER_ROW))
        )
    }

    private fun isLeapYear() =
        hiddenValue.keys.find { true }?.isLeapYear ?: LocalDate.now().isLeapYear

    private fun createRowRanges(listSize: Int, maxRangeSize: Int): List<List<LocalDate>> {
        return hiddenValue.keys.groupBy {
            it.get(
                WeekFields.of(Locale.UK).weekOfWeekBasedYear()
            )
        }.map { it.key to fillWeek(it.value) }.toMap().values.toList()
    }

    private fun fillWeek(week: List<LocalDate>): List<LocalDate> {
        if (week.size == 7) return week

        val absentDays = DayOfWeek.values().filterNot { week.map { it.dayOfWeek }.contains(it) }
        val newWeek = mutableListOf<LocalDate>()
        if (absentDays.last() < week.first().dayOfWeek) {
            for (diff in (1..absentDays.size).reversed()) {
                newWeek.add(week.first().minusDays(diff.toLong()))
            }
            newWeek.addAll(week)
        } else {
            newWeek.addAll(week)
            for (diff in 1..absentDays.size) {
                newWeek.add(week.last().plusDays(diff.toLong()))
            }
        }
        return newWeek
    }

    private inner class DayRepeater :
        ListView<String>("days", TranslatedDayOfWeek.translatedDays(containingPage(), propertiesCache).toList()) {
        override fun populateItem(day: ListItem<String>) {
            day.addOrReplace(Label("day", day.modelObject))
        }
    }

    private inner class RowRepeater(ranges: List<List<LocalDate>>) :
        ListView<List<LocalDate>>("rowRepeater", ranges) {
        override fun populateItem(row: ListItem<List<LocalDate>>) {
            row.addOrReplace(ColumnRepeater(row.modelObject))
        }
    }

    private inner class ColumnRepeater(val days: List<LocalDate>) :
        ListView<LocalDate>("columnRepeater", days) {
        override fun populateItem(column: ListItem<LocalDate>) {
            val amountLabel = Label("amountLabel", hiddenValue.get(column.modelObject)?.toString() ?: StringUtils.EMPTY)
            if (amountLabel.defaultModelObject.toString()
                    .contains("-")
            ) amountLabel.add(
                AttributeModifier.append(
                    "class",
                    "text-danger"
                )
            ) else amountLabel.add(AttributeModifier.append("class", "text-info"))
            column.addOrReplace(
                Label("dateLabel", column.modelObject.dayOfMonth.toString()),
                amountLabel
            )
            if (!currentMonth.month.equals(column.modelObject.month)) {
                column.add(AttributeModifier.replace("class", "text-muted"))
            } else if (listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).contains(column.modelObject.dayOfWeek)) {
                column.add(AttributeModifier.replace("class", "text-danger"))
            }
        }
    }

    abstract fun onBackClicked(newMonth: LocalDate)

    abstract fun onForwardClicked(newMonth: LocalDate)

    private inner class BackLink : AjaxLink<String>("back", Model.of("")) {

        override fun onBeforeRender() {
            super.onBeforeRender()
            addOrReplace(
                Label(
                    "backLabel",
                    propertiesCache.translator.translate(currentMonth.month.minus(1L).name).take(3)
                )
            )
            add(AttributeModifier.replace("class", "text-left border-right border-secondary btn btn-primary btn-block"))
            isEnabled = true

        }

        override fun onClick(target: AjaxRequestTarget) {
            currentMonth = currentMonth.minusMonths(1)
            onBackClicked(currentMonth)
            setComponents()
            target.add(this@CalendarPanel)
        }

    }

    private inner class ForwardLink : AjaxLink<String>("forward", Model.of("")) {

        override fun onBeforeRender() {
            super.onBeforeRender()
            addOrReplace(
                Label(
                    "forwardLabel",
                    propertiesCache.translator.translate(currentMonth.month.plus(1L).name).take(3)
                )
            )
            addClass()
        }

        private fun addClass() {
            val now = LocalDate.now()
            if (now.month.equals(currentMonth.month) && now.year.equals(currentMonth.year)) {
                add(
                    AttributeModifier.replace(
                        "class",
                        "text-right border-left border-secondary btn btn-primary btn-block disabled"
                    )
                )
                add(AttributeModifier.replace("disabled", ""))
                isEnabled = false
            } else {
                add(
                    AttributeModifier.replace(
                        "class",
                        "text-right border-left border-secondary btn btn-primary btn-block"
                    )
                )
                add(AttributeModifier.remove("disabled"))
                isEnabled = true
            }
        }

        override fun onClick(target: AjaxRequestTarget) {
            currentMonth = currentMonth.plusMonths(1)
            onForwardClicked(currentMonth)
            setComponents()
            target.add(this@CalendarPanel)
        }

    }
}