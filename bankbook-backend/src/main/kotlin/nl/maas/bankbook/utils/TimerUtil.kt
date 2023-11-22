package nl.maas.bankbook.utils

import org.apache.commons.lang3.StringUtils
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class TimerUtil {

    private val timers: MutableMap<Long, LocalTime> = mutableMapOf()

    companion object {
        private val instance = TimerUtil()

        fun start() {
            instance.startTimer()
        }

        fun stop(message: String, newline: Boolean = false) {
            instance.stopTimer(message, newline)
        }
    }

    fun startTimer() {
        timers.put(Thread.currentThread().id, LocalTime.now())
    }

    fun stopTimer(message: String, newline: Boolean) {
        val endtime = LocalTime.now()
        val startTime = timers.get(Thread.currentThread().id)
        startTime?.let { print(it, endtime, message, newline) }
    }

    fun print(startTime: LocalTime, endTime: LocalTime, message: String, newline: Boolean) {
        println(
            "$message:${if (newline) StringUtils.CR else StringUtils.SPACE}${
                Instant.ofEpochMilli(startTime.until(endTime, ChronoUnit.MILLIS)).atZone(ZoneId.systemDefault())
                    .toLocalTime().minusHours(1)
                    .format(DateTimeFormatter.ofPattern("mm'm, 'ss's and 'S'ms'"))
            }"
        )
    }
}