package dev.seokbeomkim.orgtodo.parser

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.InputStream

/**
 * OrgParser: Emacs org file parser
 * Properties:
 * - SCHEDULED
 * - DEADLINE
 *
 * Status (customizable):
 * - TODO
 * - IN-PROGRESS
 * - DONE
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

    fun dumpItems() {
        items.forEach { item ->
            println("Title: ${item.getTitle()}")
            println("Status: ${item.getStatus()}")
            println("Body: ${item.getBody()}")
            println("Properties: ${item.getProperties()}")
            println("Priority: ${item.getPriority()}")
            println("Progress: ${item.getProgress()}")
            println()
        }
    }

    fun isHeader(line: String): Boolean = line.startsWith("*")

    fun findStatus(line: String): String? {
        val status = line.split(" ")?.firstOrNull()
        if ((status != null) && (OrgConfigurator.checkStatusExist(status))) {
            return status
        } else {
            return null
        }
    }

    fun findPriority(line: String): String? {
        var regex = Regex("\\[#(.)\\]")
        val priority = regex.find(line)

        if (priority == null) {
            return null
        } else {
            return priority?.groupValues?.get(priority.groupValues.lastIndex)
        }
    }

    fun findProgress(line: String): String? {
        var regex1 = Regex("\\[(\\d*\\/\\d*)\\]")
        var regex2 = Regex("\\[(\\d+%)\\]")

        val progress1 = regex1.find(line)
        val progress2 = regex2.find(line)

        if (progress1 == null && progress2 == null) {
            return null
        } else {
            return progress1?.groupValues?.get(progress1.groupValues.lastIndex)
                ?: progress2?.groupValues?.get(progress2.groupValues.lastIndex)
        }
    }

    fun sanitizeTitle(item: OrgItem): OrgItem {
        var title = item.getTitle()

        if (title != null) {
            title = title.replace(item.getStatus(), "")
            title = title.replace("[#" + item.getPriority() + "]", "")
            title = title.replace("[" + item.getProgress() + "]", "")

            item.setTitle(title.trim())
        }

        return item
    }

    fun findSchedule(line: String, item: OrgItem?): Boolean {
        val regex = Regex("SCHEDULED: <([0-9]+-[0-9]+-[0-9]+ [A-Za-z]+)>")
        val matched = regex.find(line)
        item?.setProperty(
            OrgProperty.SCHEDULED, matched?.groupValues?.get(
                matched.groupValues.lastIndex
            )
        )
        return matched != null
    }

    fun findDeadline(line: String, item: OrgItem?): Boolean {
        val regex = Regex("DEADLINE: <([0-9]+-[0-9]+-[0-9]+ [A-Za-z]+)>")
        val matched = regex.find(line)
        item?.setProperty(
            OrgProperty.DEADLINE, matched?.groupValues?.get(
                matched.groupValues.lastIndex
            )
        )
        return matched != null
    }

    /**
     * Parse an element in the org file. If the element is properties, then return null. Otherwise,
     * return the item that is a new org item.
     */
    fun parseLine(current: OrgItem?, line: String): OrgItem? {
        var rValue = current
        if (isHeader(line)) {
            val title = line.trimStart('*').trim()

            rValue = OrgItem()
            rValue.setTitle(title)

            title.split(" ").forEach({ x ->
                findStatus(x)?.let { rValue?.setStatus(it) }
                findPriority(x)?.let { rValue?.setPriority(it) }
                findProgress(x)?.let { rValue?.setProgress(it) }
            })

            rValue = sanitizeTitle(rValue)
            items.add(rValue)
        } else {
            if ((findSchedule(line, rValue) == false) && (findDeadline(line, rValue) == false)) {
                rValue?.addToBody(line)
            }
        }

        return rValue
    }

    /**
     * Parse the org file. 'reader' must be initialized first.
     */
    fun parse() {
        var cursor: OrgItem? = null

        reader?.use { bufferedReader ->
            bufferedReader.lines().forEach { line ->
                var newItem = parseLine(cursor, line)
                if (newItem != cursor) {
                    cursor = newItem
                } else if (newItem != null) {
                    if (items.last() != null) {
                        items.removeAt(items.lastIndex)
                    }
                    items.add(newItem)
                }
            }
        } ?: run {
            println("Reader is not initialized")
        }
    }
}