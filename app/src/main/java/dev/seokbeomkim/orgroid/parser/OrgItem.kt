package dev.seokbeomkim.orgtodo.parser

import dev.seokbeomkim.orgtodo.calendar.CalendarItem
import java.util.Date

/**
 * OrgItem: a class to represent an item in an org file.
 */
class OrgItem {
    private var title: String
    private var status: String
    private var body: String
    private var priority: String
    private var properties: MutableMap<String, String>
    private var progress: String

    constructor() {
        this.title = ""
        this.body = ""
        this.status = ""
        this.priority = ""
        this.progress = ""
        this.properties = mutableMapOf()
    }

    constructor(
        title: String, body: String, status: String, priority: String,
        progress: String, properties: MutableMap<String, String>
    ) {
        this.title = title
        this.body = body
        this.status = status
        this.priority = priority
        this.progress = progress
        this.properties = properties
    }

    fun getTitle(): String = title
    fun setTitle(newTitle: String) {
        title = newTitle
    }

    fun getProgress(): String = progress
    fun setProgress(newProgress: String) {
        progress = newProgress
    }

    fun getBody(): String = body
    fun setBody(newBody: String) {
        body = newBody
    }

    fun addToBody(line: String) {
        if (!line.isEmpty()) {
            if (body.isEmpty()) {
                body = line
            } else {
                body += "\n" + line
            }
        }
    }

    fun getStatus(): String = status
    fun setStatus(newStatus: String) {
        status = newStatus
    }

    fun getProperties(): Map<String, String> = properties
    fun hasProperty(key: String): Boolean {
        return properties.containsKey(key)
    }

    fun getProperty(key: String): String? = properties[key]
    fun getProperty(key: String, defaultValue: String): String {
        return properties.getOrDefault(key, defaultValue)
    }

    fun setProperty(key: String, newValue: String?) {
        if (newValue != null) {
            properties[key] = newValue
        }
    }

    fun removeProperty(key: String) {
        properties.remove(key)
    }

    fun getPriority(): String = priority
    fun setPriority(newPriority: String) {
        priority = newPriority
    }

    /**
     * Convert org string (SCHEDULED, DEADLINE) to Date
     */
    fun toDateFromOrgString(string: String): Date {
        var rValue = Date()
        if (string.length > 0) {
            val regex = Regex("([0-9]+)-([0-9]+)-([0-9]+)")
            regex.find(string)?.let { x ->
                val (year, month, day) = x.destructured
                println("year: $year, month: $month, day: $day")
                rValue = Date(year.toInt(), month.toInt(), day.toInt())
            }
        }
        return rValue
    }

    fun toCalendarItem(): CalendarItem {
        val rvalue = CalendarItem()
        var dateStr: String?

        rvalue.setTitle(title)
        rvalue.setDescription(body)

        dateStr = properties[OrgProperty.SCHEDULED]
        if (dateStr != null) {
            rvalue.setStartTime(toDateFromOrgString(dateStr))
        }

        dateStr = properties[OrgProperty.DEADLINE]
        if (dateStr != null) {
            rvalue.setEndTime(toDateFromOrgString(dateStr))
        }

        return rvalue
    }
}