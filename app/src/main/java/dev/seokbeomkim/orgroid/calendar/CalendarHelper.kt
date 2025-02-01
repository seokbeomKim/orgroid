package dev.seokbeomkim.orgtodo.calendar

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.widget.Toast
import java.util.TimeZone

/**
 * A class for managing calendars in the Android CalendarContract.
 * This class will be instantiated within a singleton pattern.
 * The essential functions is to provide APIs to manage local calendars and every events associated with them.
 */
class CalendarHelper {
    val CALENDAR_NAME: String = "orgroid"

    var calendars: ArrayList<CalendarItem>
    lateinit var appMainContext: Context

    companion object {
        @Volatile
        private var instance: CalendarHelper? = null

        fun getInstace(): CalendarHelper {
            return instance ?: synchronized(this) {
                instance ?: CalendarHelper().also { instance = it }
            }
        }
    }

    constructor() {
        this.calendars = ArrayList()
    }

    fun setMainAppContext(context: Context) {
        instance?.appMainContext = context
    }

    fun getCalendarArrayList(): ArrayList<CalendarItem> {
        return calendars
    }

    /**
     * Initialize calendar array list with the local calendars
     */
    fun initCalendarArrayList() {
        getCalendarListByContentResolver(this.appMainContext).forEach(
            fun(hash: HashMap<String, Any>) {
                println("account name: ${hash[CalendarContract.Calendars.ACCOUNT_NAME]}")
                println("display name: ${hash[CalendarContract.Calendars.CALENDAR_DISPLAY_NAME]}")
                println("calendar id: ${hash[CalendarContract.Calendars._ID]}")

                var newItem = CalendarItem()
                newItem.setTitle("${hash[CalendarContract.Calendars.CALENDAR_DISPLAY_NAME]}")
                newItem.setDescription("${hash[CalendarContract.Calendars.ACCOUNT_NAME]}")
                this.calendars.add(newItem)
            }
        )
    }

    fun createCalendar(context: Context, accountName: String, calendarName: String): Long {
        val cr: ContentResolver = context.contentResolver
        val values = ContentValues().apply {
            put(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
            put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
            put(CalendarContract.Calendars.NAME, CALENDAR_NAME)
            put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, calendarName)
            put(CalendarContract.Calendars.CALENDAR_COLOR, 0xFF00FF00.toInt())
            put(
                CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
                CalendarContract.Calendars.CAL_ACCESS_OWNER
            )
            put(CalendarContract.Calendars.OWNER_ACCOUNT, accountName)
            put(CalendarContract.Calendars.SYNC_EVENTS, 1)
            put(CalendarContract.Calendars.VISIBLE, 1)
        }

        var uri: Uri = CalendarContract.Calendars.CONTENT_URI.buildUpon()
            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
            .appendQueryParameter(
                CalendarContract.Calendars.ACCOUNT_TYPE,
                CalendarContract.ACCOUNT_TYPE_LOCAL
            )
            .build()

        val newUri: Uri? = cr.insert(uri, values)
        return newUri?.let { ContentUris.parseId(it) } ?: -1
    }

    fun addEventToCalendar(
        context: Context,
        title: String?,
        description: String?,
        startTime: Long,
        endTime: Long
    ) {
        val cr = context.contentResolver
        val values = ContentValues()

        val calendarId = 1
        values.put(CalendarContract.Events.CALENDAR_ID, calendarId)
        values.put(CalendarContract.Events.TITLE, title)
        values.put(CalendarContract.Events.DESCRIPTION, description)
        values.put(CalendarContract.Events.DTSTART, startTime)
        values.put(CalendarContract.Events.DTEND, endTime)
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID())
        values.put(CalendarContract.Events.EVENT_LOCATION, "Online")
        values.put(CalendarContract.Events.HAS_ALARM, 1) // 알람 설정

        val uri = cr.insert(CalendarContract.Events.CONTENT_URI, values)

        if (uri != null) {
            val eventId = ContentUris.parseId(uri)
            Toast.makeText(context, "Event added with ID: $eventId", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Failed to add event", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteEvent(context: Context, eventId: Long) {
        val cr: ContentResolver = context.contentResolver
        val uri: Uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
        val rowsDeleted = cr.delete(uri, null, null)

        if (rowsDeleted > 0) {
            Toast.makeText(context, "Event deleted successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Failed to delete event", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteCalendar(context: Context, calendarId: Long) {
        val cr: ContentResolver = context.contentResolver
        val uri: Uri =
            ContentUris.withAppendedId(CalendarContract.Calendars.CONTENT_URI, calendarId)
        val rowsDeleted = cr.delete(uri, null, null)

        if (rowsDeleted > 0) {
            println("Calendar deleted successfully")
        } else {
            println("Failed to delete calendar")
        }
    }

    fun getCalendarListByContentResolver(context: Context): List<HashMap<String, Any>> {
        val cr: ContentResolver = context.contentResolver
        val uri: Uri = CalendarContract.Calendars.CONTENT_URI
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.CALENDAR_COLOR,
        )
        val cursor: Cursor? = cr.query(uri, projection, null, null, null)

        val calendars = mutableListOf<HashMap<String, Any>>()
        cursor?.use {
            while (it.moveToNext()) {
                var newItem = HashMap<String, Any>()
                newItem[CalendarContract.Calendars._ID] = it.getLong(0)
                newItem[CalendarContract.Calendars.CALENDAR_DISPLAY_NAME] = it.getString(1)
                newItem[CalendarContract.Calendars.ACCOUNT_NAME] = it.getString(2)
                newItem[CalendarContract.Calendars.CALENDAR_COLOR] = it.getInt(3)

                calendars.add(newItem)
            }
        }
        return calendars
    }
}