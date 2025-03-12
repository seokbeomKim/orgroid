package dev.seokbeomkim.orgroid.parser

import java.time.ZonedDateTime

class OrgDateItem {
    var start: ZonedDateTime? = null
    var end: ZonedDateTime? = null

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
}