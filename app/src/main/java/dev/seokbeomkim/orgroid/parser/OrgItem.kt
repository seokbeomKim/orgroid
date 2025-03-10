package dev.seokbeomkim.orgroid.parser

import dev.seokbeomkim.orgtodo.calendar.EventItem
import java.util.Calendar
import java.util.Date

/**
 * OrgItem: a class to represent an item in an org file.
 */
class OrgItem {
    var title: String = ""
    var status: String = ""
    var body: String = ""
    var priority: String = ""
    var properties: MutableMap<String, String> = mutableMapOf()
    var progress: String = ""
    var scheduled: OrgDateItem? = null
    var deadline: OrgDateItem? = null

    fun toDateFromOrgString(string: String): Date {
        val cal = Calendar.getInstance()
        if (string.isNotEmpty()) {
            val regex = Regex("([0-9]+)-([0-9]+)-([0-9]+) +([0-9]+:[0-9]+-+[0-9]+:[0-9]+)")
            regex.find(string)?.let { x ->
                val (year, month, day, time) = x.destructured
                println("year: $year, month: $month, day: $day, time: $time")
                cal.set(Calendar.YEAR, year.toInt())
                cal.set(Calendar.MONTH, month.toInt())
                cal.set(Calendar.DAY_OF_MONTH, day.toInt())
            }
        }
        return cal.time
    }

    @Suppress("unused")
    fun toCalendarItem(): EventItem {
        val rvalue = EventItem()

        rvalue.setTitle(title)
        rvalue.setDescription(body)

// TODO implement this
//
//        var dateStr: String? = properties[OrgProperty.SCHEDULED_FROM]
//        if (dateStr != null) {
//            rvalue.setStartTime(toDateFromOrgString(dateStr))
//        }
//
//        dateStr = properties[OrgProperty.DEADLINE_FROM]
//        if (dateStr != null) {
//            rvalue.setEndTime(toDateFromOrgString(dateStr))
//        }

        return rvalue
    }

    override fun toString(): String {
        var rvalue = "Title: $title\n"
        rvalue += "Status: $status\n"
        rvalue += "Body: $body\n"
        rvalue += "Schedule: $scheduled\n"
        rvalue += "Deadline: $deadline"
        return rvalue
    }
}