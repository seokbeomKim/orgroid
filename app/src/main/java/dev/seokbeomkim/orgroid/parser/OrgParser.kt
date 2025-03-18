package dev.seokbeomkim.orgroid.parser

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.InputStream
import java.time.ZonedDateTime

/**
 * OrgParser: Emacs org file parser
 */
class OrgParser {
    private var reader: BufferedReader? = null
    private var items: MutableList<OrgItem> = mutableListOf()

    fun open(file: File) {
        try {
            reader = BufferedReader(FileReader(file.absolutePath))
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }

    fun open(inputStream: InputStream) {
        try {
            reader = BufferedReader(inputStream.reader())
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }

    fun getItems(): MutableList<OrgItem> {
        return items
    }

    override fun toString(): String {
        var returnString = ""
        items.forEach { item -> returnString += "$item\n=============\n" }
        return returnString
    }

    private fun isHeader(line: String): Boolean = line.startsWith("*")

    private fun findStatus(line: String): String? {
        val status = line.split(" ").firstOrNull()
        return if ((status != null) && (OrgStatus.checkValidation(status))) {
            status
        } else {
            null
        }
    }

    private fun findPriority(line: String): String? {
        val regex = Regex("\\[#(.)]")
        val priority = regex.find(line)

        return priority?.groupValues?.get(priority.groupValues.lastIndex)
    }

    private fun findProgress(line: String): String? {
        val regex1 = Regex("\\[(\\d*/\\d*)]")
        val regex2 = Regex("\\[(\\d+%)]")

        val progress1 = regex1.find(line)
        val progress2 = regex2.find(line)

        return if (progress1 == null && progress2 == null) {
            null
        } else {
            progress1?.groupValues?.get(progress1.groupValues.lastIndex)
                ?: progress2?.groupValues?.get(progress2.groupValues.lastIndex)
        }
    }

    private fun sanitizeItem(item: OrgItem): OrgItem {
        var title = item.title

        title = title.replace(item.status, "")
        title = title.replace("[#" + item.priority + "]", "")
        title = title.replace("[" + item.progress + "]", "")

        item.title = title.trim()

        return item
    }

    private fun findDate(date: String, item: OrgDate): OrgDate {
        val regex = Regex("""(\d{4}+)-(\d{2}+)-(\d{2}+)""")
        val matched = regex.find(date)
        if (matched != null && matched.groupValues.lastIndex == 3) {
            item.start = ZonedDateTime.now()
                .withYear(matched.groupValues[1].toInt())
                .withMonth(matched.groupValues[2].toInt())
                .withDayOfMonth(matched.groupValues[3].toInt())
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)

            item.end = item.start
            item.isAllDay = true
        } else {
            println("No match found: $date")
        }

        return item
    }

    private fun findTime(date: String, item: OrgDate): OrgDate {
        var regex = Regex("""(\d{2}):(\d{2})-+(\d{2}):(\d{2})""")
        var matched = regex.find(date)
        if (matched != null) {
            item.start = item.start
                ?.withHour(matched.groupValues[1].toInt())
                ?.withMinute(matched.groupValues[2].toInt())
            item.end = item.end
                ?.withHour(matched.groupValues[3].toInt())
                ?.withMinute(matched.groupValues[4].toInt())
            item.isAllDay = false
        } else {
            regex = Regex("""(\d{2}):(\d{2})""")
            matched = regex.find(date)
            if (matched != null) {
                item.start = item.start
                    ?.withHour(matched.groupValues[1].toInt())
                    ?.withMinute(matched.groupValues[2].toInt())
                item.end = item.start
                item.isAllDay = false
            }
        }

        return item
    }

    private fun findRepeat(date: String, item: OrgDate): OrgDate {
        val regex = Regex("""(\.\+|\+\+|\+)([0-9]+)([a-zA-Z])""")
        val matched = regex.find(date)
        if (matched != null) {
            if (matched.groupValues[1] == ".+") {
                item.setRepeatType(OrgDate.OrgRepeatType.NEXT_FROM_NOW)
            } else if (matched.groupValues[1] == "++") {
                item.setRepeatType(OrgDate.OrgRepeatType.SKIP_UNTIL_NOW)
            } else if (matched.groupValues[1] == "+") {
                item.setRepeatType(OrgDate.OrgRepeatType.SIMPLE)
            } else {
                println("Unknown repeat type: ${matched.groupValues[1]}")
            }
            item.setRepeatCount(matched.groupValues[2].toInt())
            when (matched.groupValues[3]) {
                "y" -> item.setRepeatUnit(OrgDate.OrgRepeatUnit.YEAR)
                "m" -> item.setRepeatUnit(OrgDate.OrgRepeatUnit.MONTH)
                "d" -> item.setRepeatUnit(OrgDate.OrgRepeatUnit.DAY)
                "h" -> item.setRepeatUnit(OrgDate.OrgRepeatUnit.HOUR)
            }
        } else {
            item.setRepeatType(OrgDate.OrgRepeatType.NONE)
        }

        return item
    }

    private fun parseDate(date: String): OrgDate {
        var r = OrgDate()

        r = this.findDate(date, r)
        r = this.findTime(date, r)
        r = this.findRepeat(date, r)

        return r
    }

    private fun parseSchedule(line: String, item: OrgItem?): Boolean {
        val property = "SCHEDULED:"
        if (!line.startsWith(property)) {
            return false
        }

        val openChar = '<'
        val closeChar = '>'
        var indexOfOpenChar = 0
        var indexOfCloseChar = 0

        while (true) {
            indexOfOpenChar = line.indexOf(openChar, indexOfOpenChar + 1)
            indexOfCloseChar = line.indexOf(closeChar, indexOfCloseChar + 1)

            if (indexOfOpenChar == -1 || indexOfCloseChar == -1) {
                break
            }

            val substring = line.substring(indexOfOpenChar + 1, indexOfCloseChar)
            item?.scheduled = mergeDate(item?.scheduled, parseDate(substring))
        }

        return true
    }

    private fun mergeDate(date1: OrgDate?, date2: OrgDate?): OrgDate? {
        if (date1 == null && date2 == null) {
            return null
        } else if (date1 == null) {
            return date2
        } else if (date2 == null) {
            return date1
        } else {
            date1.merge(date2)
            return date1
        }
    }

    private fun parseDeadline(line: String, item: OrgItem?): Boolean {
        val property = "DEADLINE:"
        if (!line.startsWith(property)) {
            return false
        }

        val openChar = '<'
        val closeChar = '>'
        var indexOfOpenChar = 0
        var indexOfCloseChar = 0

        while (true) {
            indexOfOpenChar = line.indexOf(openChar, indexOfOpenChar + 1)
            indexOfCloseChar = line.indexOf(closeChar, indexOfCloseChar + 1)

            if (indexOfOpenChar == -1 || indexOfCloseChar == -1) {
                break
            }

            val substring = line.substring(indexOfOpenChar + 1, indexOfCloseChar)
            item?.deadline = mergeDate(item?.deadline, parseDate(substring))

        }

        return true
    }

    /**
     * Parse an element in the org file. If the element is properties,
     * then return null. Otherwise, return the item that is a new org item.
     */
    private fun parseLine(current: OrgItem?, line: String): OrgItem? {
        var rValue = current
        if (isHeader(line)) {
            val title = line.trimStart('*').trim()

            rValue = OrgItem()
            rValue.title = title

            title.split(" ").forEach { x ->
                findStatus(x)?.let { rValue?.status = it }
                findPriority(x)?.let { rValue?.priority = it }
                findProgress(x)?.let { rValue?.progress = it }
            }

            rValue = sanitizeItem(rValue)
            items.add(rValue)
        } else {
            if (!parseSchedule(line, rValue) && !parseDeadline(line, rValue)) {
                if (rValue?.body == null) {
                    rValue?.body = line
                } else {
                    rValue.body += "\n$line"
                }

                rValue?.body = rValue?.body?.trimStart('\n')?.trimEnd('\n').toString()
            }
        }

        return rValue
    }

    /**
     * Parse the org file. 'reader' must be initialized first.
     */
    fun parse(mustDefineSchedule: Boolean = false, mustDefineDeadline: Boolean = false) {
        var cursor: OrgItem? = null
        var newItem: OrgItem?

        reader?.use { bufferedReader ->
            bufferedReader.lines().forEach { line ->
                newItem = this.parseLine(cursor, line)
                if ((cursor != null) && (cursor != newItem)) {
                    if ((mustDefineSchedule && cursor?.scheduled == null) ||
                        (mustDefineDeadline && cursor?.deadline == null) ||
                        (cursor?.scheduled == null && cursor?.deadline == null)
                    ) {
                        items.remove(cursor)
                    }
                }
                cursor = newItem
            }
        } ?: run {
            println("Reader is not initialized")
        }

        // handle the last one
        items.lastOrNull { x ->
            (mustDefineSchedule && x.scheduled == null)
                    || (mustDefineDeadline && x.deadline == null)
                    || (cursor?.scheduled == null && cursor?.deadline == null)
        }?.let { x ->
            items.remove(x)
        }
    }
}