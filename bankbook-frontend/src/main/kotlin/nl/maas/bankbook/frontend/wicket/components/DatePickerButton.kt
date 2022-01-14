package nl.maas.bankbook.frontend.wicket.components

import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.datetime.DatetimePicker
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.datetime.DatetimePickerConfig
import org.apache.wicket.ajax.AjaxEventBehavior
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.model.Model
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

open class DatePickerButton(
    id: String,
    private val dateTime: LocalDate = LocalDate.now(),
    private val type: PickerTypes = PickerTypes.DAY_MONTH_YEAR,
) :
    DatetimePicker(
        id,
        Model.of(Date.from(dateTime.atStartOfDay(ZoneId.systemDefault()).toInstant())),
        config(type, dateTime)
    ) {

    companion object {

        private fun config(type: PickerTypes, date: LocalDate): DatetimePickerConfig {
            val datetimePickerConfig = DatetimePickerConfig()
            datetimePickerConfig.useView(DatetimePickerConfig.ViewModeType.YEARS)
            datetimePickerConfig.withDate(date)
            datetimePickerConfig.useMaskInput(false)
            datetimePickerConfig.withFormat(type.format)
            datetimePickerConfig.withMaxDate(LocalDate.now())
            return datetimePickerConfig
        }

        enum class PickerTypes(val format: String) {
            YEAR_ONLY("yyyy"),
            MONTH_YEAR("MMMM yyyy"),
            DAY_MONTH_YEAR("dd MMM yyyy")
        }
    }

    override fun onBeforeRender() {
        super.onBeforeRender()
        add(OnChange())
    }

    open fun onDateChanged(target: AjaxRequestTarget, date: LocalDate) {

    }

    private fun parseInput(): LocalDate {
        var output: LocalDate
        when (type) {
            PickerTypes.MONTH_YEAR -> output = LocalDate.parse(
                "${
                    input.replace(
                        input.substringBefore(" "),
                        Month.valueOf(input.substringBefore(" ").uppercase()).value.toString()
                    )
                } 01",
                DateTimeFormatter.ofPattern("${type.format.replace("MMMM", "M")} dd").withLocale(Locale.getDefault())
            )
            PickerTypes.YEAR_ONLY -> output = LocalDate.parse(
                "${input} 01 01",
                DateTimeFormatter.ofPattern("${type.format} MM dd")
            )
            PickerTypes.DAY_MONTH_YEAR -> output = LocalDate.parse(input, DateTimeFormatter.ofPattern(type.format))
            else -> output = LocalDate.now()
        }
        return output
    }

    private inner class OnChange() : AjaxEventBehavior("focusout") {
        override fun onEvent(target: AjaxRequestTarget) {
            onDateChanged(
                target,
                parseInput()
            )
        }
    }
}