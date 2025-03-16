package dev.seokbeomkim.orgroid.calendar

/**
 * CalendarItem: A class to show an item in the calendar list
 *
 * Note that the item represents "Calendar", not the "Event".
 */
class CalendarItem {

    var id: Long = 0

    /**
     * @title: Calendar name
     */
    lateinit var displayName: String

    /**
     * @body: Calendar description
     */
    lateinit var accountName: String

    override fun toString(): String {
        return "id: $id, displayName: $displayName, accountName: $accountName"
    }
}