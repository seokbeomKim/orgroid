package dev.seokbeomkim.orgtodo.calendar

import dev.seokbeomkim.orgtodo.parser.OrgItem
import java.util.Date

/**
 * EventItem: a class to represent an item in a calendar
 *
 * All contents of the instance will be used for CalendarProvider.Calendars.
 *             put(CalendarContract.Events.CALENDAR_ID, calendarId)
 *             put(CalendarContract.Events.TITLE, title)
 *             put(CalendarContract.Events.DESCRIPTION, description)
 *             put(CalendarContract.Events.DTSTART, startTime)
 *             put(CalendarContract.Events.DTEND, endTime)
 *             put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
 *             put(CalendarContract.Events.EVENT_LOCATION, "Online")
 *             put(CalendarContract.Events.HAS_ALARM, 1)
 */
class EventItem {
    private var title: String
    private var description: String
    private var startTime: Date
    private var endTime: Date
    private var rrule: String

    constructor() {
        this.title = ""
        this.description = ""
        this.startTime = Date()
        this.endTime = Date()
        this.rrule = ""
    }

    constructor(title: String, description: String, startTime: Date, endTime: Date, rrule: String) {
        this.title = title
        this.description = description
        this.startTime = startTime
        this.endTime = endTime
        this.rrule = rrule
    }

    fun getTitle(): String = this.title
    fun setTitle(title: String) {
        this.title = title
    }

    fun getDescription(): String = this.description
    fun setDescription(description: String) {
        this.description = description
    }

    fun getStartTime(): Date = this.startTime
    fun setStartTime(startTime: Date) {
        this.startTime = startTime
    }

    fun getEndTime(): Date = this.endTime
    fun setEndTime(endTime: Date) {
        this.endTime = endTime
    }

    fun getRrule(): String = this.rrule
    fun setRrule(rrule: String) {
        this.rrule = rrule
    }

    fun toOrgItem(): OrgItem {
        var rvalue = OrgItem()
        return rvalue
    }
}