package dev.seokbeomkim.orgroid.parser

import java.time.ZonedDateTime

class OrgDate {
    var start: ZonedDateTime? = null
    var end: ZonedDateTime? = null
    var isAllDay: Boolean = false

    /**
     * Actually, we do not need to consider how to handle repeat type.
     * This is just an information to display.
     */
    enum class OrgRepeatType {
        NONE,
        SIMPLE, // +
        SKIP_UNTIL_NOW, // ++
        NEXT_FROM_NOW, // .+
    }

    enum class OrgRepeatUnit {
        YEAR,
        MONTH,
        DAY,
        HOUR,
    }

    private var repeatUnit: OrgRepeatUnit = OrgRepeatUnit.DAY

    @Suppress("UNUSED")
    fun getRepeatUnit(): OrgRepeatUnit = repeatUnit
    fun setRepeatUnit(value: OrgRepeatUnit) {
        repeatUnit = value
    }

    private var repeatType: OrgRepeatType = OrgRepeatType.NONE

    @Suppress("UNUSED")
    fun getRepeatType(): OrgRepeatType = repeatType
    fun setRepeatType(value: OrgRepeatType) {
        repeatType = value
    }

    private var repeatCount: Int = 0

    @Suppress("UNUSED")
    fun getRepeatCount(): Int = repeatCount
    fun setRepeatCount(value: Int) {
        repeatCount = value
    }

    override fun toString(): String {
        return "OrgDateItem(start=$start, end=$end, repeatType=$repeatType, repeatCount=$repeatCount, repeatUnit=$repeatUnit)"
    }

    fun merge(date: OrgDate) {
        if (start?.isAfter(date.start) == true) {
            start = date.start
        }
        if (end?.isBefore(date.end) == true) {
            end = date.end
        }
    }

    fun early(): ZonedDateTime? {
        return if (start == null && end == null) {
            null
        } else if (start == null) {
            end
        } else if (end == null) {
            start
        } else {
            if (start!!.isBefore(end)) {
                start
            } else {
                end
            }
        }
    }

    fun late(): ZonedDateTime? {
        return if (start == null && end == null) {
            null
        } else if (start == null) {
            end
        } else if (end == null) {
            start
        } else {
            if (start!!.isBefore(end)) {
                end
            } else {
                start
            }
        }
    }

    operator fun compareTo(target: OrgDate): Int {
        return this.early()!!.compareTo(target.early())
    }
}