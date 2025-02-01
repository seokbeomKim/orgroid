package dev.seokbeomkim.orgtodo.calendar

import dev.seokbeomkim.orgtodo.parser.OrgItem
import java.util.Date

class CalendarItem {
    private var title: String
    private var description: String
    private var startTime: Date
    private var endTime: Date

    constructor() {
        this.title = ""
        this.description = ""
        this.startTime = Date()
        this.endTime = Date()
    }

    constructor(title: String, description: String, startTime: Date, endTime: Date) {
        this.title = title
        this.description = description
        this.startTime = startTime
        this.endTime = endTime
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

    fun toOrgItem(): OrgItem {
        var rvalue = OrgItem()
        return rvalue
    }
}