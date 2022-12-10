package nl.maas.bankbook.frontend.wicket.components

import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.datetime.DatetimePicker
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.datetime.DatetimePickerConfig
import nl.maas.bankbook.parsers.LocalDateParser
import org.apache.wicket.ajax.AjaxEventBehavior
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.model.Model
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

open class DatePickerButton(
    id: String,
    private val dateTime: LocalDate = LocalDate.now(),
    private val type: PickerTypes = PickerTypes.DAY_MONTH_YEAR,
    private val locale: String = Locale.getDefault().variant
) :
    DatetimePicker(
        id,
        Model.of(Date.from(dateTime.atStartOfDay(ZoneId.systemDefault()).toInstant())),
        config(type, dateTime, locale)
    ) {

    val japaneseNumerals: Map<String, String> =
        listOf(
            "一" to "1",
            "二" to "2",
            "三" to "3",
            "四" to "4",
            "五" to "5",
            "六" to "6",
            "七" to "7",
            "八" to "8",
            "九" to "9",
            "十" to "10",
            "十一" to "11",
            "十二" to "12"
        ).toMap()

    val parsableInput
        get() = japaneseNumerals.map { input.replace(it.key, it.value) }.findLast { !input.equals(it) } ?: input

    companion object {

        private fun config(
            type: PickerTypes,
            date: LocalDate,
            locale: String = Locale.getDefault().variant
        ): DatetimePickerConfig {
            val datetimePickerConfig = DatetimePickerConfig()
            datetimePickerConfig.useView(DatetimePickerConfig.ViewModeType.YEARS)
            datetimePickerConfig.withDate(date)
            datetimePickerConfig.useMaskInput(false)
            datetimePickerConfig.withFormat(type.format)
            datetimePickerConfig.withMaxDate(LocalDate.now())
            datetimePickerConfig.useLocale(locale)
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
        println(date.month)
    }

    private fun parseInput(): LocalDate {
        var output: LocalDate

        when (type) {
            PickerTypes.MONTH_YEAR -> {
                output = LocalDateParser.parseStringTo(
                    "1 $parsableInput",
                    Locale.forLanguageTag(locale)
                )
            }
            PickerTypes.YEAR_ONLY -> output = LocalDateParser.parseStringTo(
                "${parsableInput} 01 01",
                Locale.forLanguageTag(locale)
            )
            PickerTypes.DAY_MONTH_YEAR -> output =
                LocalDateParser.parseStringTo(parsableInput, Locale.forLanguageTag(locale))
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