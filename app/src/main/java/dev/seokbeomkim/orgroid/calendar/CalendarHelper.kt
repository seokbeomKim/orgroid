package dev.seokbeomkim.orgroid.calendar

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import dev.seokbeomkim.orgroid.parser.OrgParser
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.TimeZone

/**
 * A class for managing calendars in the Android CalendarContract.
 * This class will be instantiated within a singleton pattern.
 * The essential functions is to provide APIs to manage local calendars and every events associated with them.
 */
class CalendarHelper {
    private lateinit var calendars: ArrayList<CalendarItem>
    var dateType: PreferredDateType = PreferredDateType.DATE_MODE_A
    private val accountName = "orgroid"

    companion object {
        @Volatile
        private var instance: CalendarHelper? = null

        fun getInstance(): CalendarHelper {
            return instance ?: synchronized(this) {
                instance ?: CalendarHelper().also { instance = it }
            }
        }
    }

    /*
     * There are two possible use-cases of using SCHEDULED/DEADLINE in org.
     * First is using SCHEDULED as start date and DEADLINE as end date.
     * Second is using SCHEDULED and DEADLINE within the different meaning. In this case, each must
     * have its own range of date. Therefore, the return value must be pair (SCHEDULED/DEADLINE).
     *
     * TODO
     *  For now, I will make this PreferredDateType as a global parameter to make it simple and
     *  avoid the complexity. But if someone asks to change this feature to be configured in each,
     *  this needs to be reconsidered.
     */
    enum class PreferredDateType {
        DATE_MODE_A, DATE_MODE_B,
    }

    /**
     * Initialize calendar array list with the local calendars
     */
    fun updateCalendarArrayList(context: Context, orgroidOnly: Boolean = false) {
        this.calendars = ArrayList()

        getCalendarListByContentResolver(context).forEach(fun(hash: HashMap<String, Any>) {
            val newItem = CalendarItem()
            newItem.displayName = "${hash[CalendarContract.Calendars.CALENDAR_DISPLAY_NAME]}"
            newItem.accountName = "${hash[CalendarContract.Calendars.ACCOUNT_NAME]}"
            newItem.id = hash[CalendarContract.Calendars._ID] as Long

            if (orgroidOnly) {
                if (newItem.accountName == this.accountName) {
                    this.calendars.add(newItem)
                }
            } else {
                this.calendars.add(newItem)
            }
        })
    }

    private fun getCalendarIdByName(context: Context, calendarName: String): Long? {
        val uri: Uri = CalendarContract.Calendars.CONTENT_URI
        val projection = arrayOf(CalendarContract.Calendars._ID)
        val selection = "${CalendarContract.Calendars.CALENDAR_DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(calendarName)

        val cursor: Cursor? =
            context.contentResolver.query(uri, projection, selection, selectionArgs, null)

        val calendarId: Long? = if (cursor != null && cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex(CalendarContract.Calendars._ID)
            if (columnIndex != 1) {
                cursor.getLong(columnIndex)
            } else {
                null
            }
        } else {
            null // Return null if the calendar does not exist
        }

        cursor?.close()

        return calendarId
    }

    fun createCalendar(
        context: Context,
        calendarName: String,
        accountName: String = this.accountName,
    ): Long {

        val calendarId = this.getCalendarIdByName(context, calendarName)
        if (calendarId != null) {
            return calendarId
        }

        val cr: ContentResolver = context.contentResolver
        val values = ContentValues().apply {
            put(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
            put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
            put(CalendarContract.Calendars.NAME, calendarName)
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

        val uri: Uri = CalendarContract.Calendars.CONTENT_URI.buildUpon()
            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
            .appendQueryParameter(
                CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL
            ).build()

        val newUri: Uri? = cr.insert(uri, values)
        return newUri?.let { ContentUris.parseId(it) } ?: -1
    }

    private fun addEventToCalendar(
        context: Context,
        title: String?,
        description: String?,
        startTime: Long?,
        endTime: Long?,
        calendarId: Long? = null
    ) {
        val cr = context.contentResolver
        val values = ContentValues()
        var shadowedTitle = title

        if (calendarId == null) {
            return
        }

        if (shadowedTitle == null) {
            shadowedTitle = "No title"
        }

        if (startTime == null && endTime == null) {
            return
        }

        if (endTime == startTime) {
            var zonedStart =
                ZonedDateTime.ofInstant(Instant.ofEpochMilli(startTime!!), ZoneId.systemDefault())
                    .withHour(0).withMinute(0).withSecond(0).withNano(0)

            zonedStart = zonedStart.withZoneSameLocal(ZoneId.of("UTC"))
            val zonedEnd = zonedStart.plusDays(1)
            println(zonedStart.toString())
            println(zonedEnd.toString())

            values.put(CalendarContract.Events.ALL_DAY, true)
            values.put(CalendarContract.Events.DTSTART, zonedStart.toInstant().toEpochMilli())
            values.put(CalendarContract.Events.DTEND, zonedEnd.toInstant().toEpochMilli())
        } else {
            var zonedStart =
                ZonedDateTime.ofInstant(Instant.ofEpochMilli(startTime!!), ZoneId.systemDefault())
            var zoneEnd = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(endTime!!),
                ZoneId.systemDefault()
            )

            if (zonedStart.hour == 0 && zonedStart.minute == 0 && zonedStart.second == 0 && zonedStart.nano == 0 &&
                zoneEnd.hour == 0 && zoneEnd.minute == 0 && zoneEnd.second == 0 && zoneEnd.nano == 0
            ) {
                zonedStart = zonedStart.withZoneSameLocal(ZoneId.of("UTC"))
                zoneEnd = zoneEnd.withZoneSameLocal(ZoneId.of("UTC"))

                values.put(CalendarContract.Events.ALL_DAY, true)
                values.put(CalendarContract.Events.DTSTART, zonedStart.toInstant().toEpochMilli())
                values.put(
                    CalendarContract.Events.DTEND,
                    zoneEnd.toInstant().toEpochMilli() + 24 * 60 * 60 * 1000
                )
            } else {
                values.put(CalendarContract.Events.DTSTART, startTime)
                values.put(CalendarContract.Events.DTEND, endTime)
            }
        }

        values.put(CalendarContract.Events.CALENDAR_ID, calendarId)
        values.put(CalendarContract.Events.TITLE, shadowedTitle)
        values.put(CalendarContract.Events.DESCRIPTION, description)
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        values.put(CalendarContract.Events.EVENT_LOCATION, "Online")
        values.put(CalendarContract.Events.HAS_ALARM, 1)

        val uri = cr.insert(CalendarContract.Events.CONTENT_URI, values)

        if (uri != null) {
            val eventId = ContentUris.parseId(uri)
            // Toast.makeText(context, "Event added with ID: $eventId", Toast.LENGTH_SHORT).show()
            Log.d("CalendarHelper", "Event added with ID: $eventId")
        } else {
            // Toast.makeText(context, "Failed to add event", Toast.LENGTH_SHORT).show()
            Log.d("CalendarHelper", "Failed to add event")
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

    private fun getCalendarListByContentResolver(context: Context): List<HashMap<String, Any>> {
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
                val newItem = HashMap<String, Any>()
                newItem[CalendarContract.Calendars._ID] = it.getLong(0)
                newItem[CalendarContract.Calendars.CALENDAR_DISPLAY_NAME] = it.getString(1)
                newItem[CalendarContract.Calendars.ACCOUNT_NAME] = it.getString(2)
                newItem[CalendarContract.Calendars.CALENDAR_COLOR] = it.getInt(3)

                calendars.add(newItem)
            }
        }
        return calendars
    }

    fun getCalendarArrayList(): ArrayList<CalendarItem> {
        return this.calendars
    }

    fun addEventsByParser(
        parser: OrgParser, context: Context, scheduleCalendar: Long?, deadlineCalendar: Long?
    ) {
        when (this.dateType) {
            PreferredDateType.DATE_MODE_A -> {
                parser.getItems().forEach { x ->

                    val early = x.getStartDate()
                    val late = x.getEndDate()

                    this.addEventToCalendar(
                        context,
                        title = x.title,
                        description = x.body,
                        startTime = early?.early()?.toInstant()?.toEpochMilli(),
                        endTime = late?.late()?.toInstant()?.toEpochMilli(),
                        scheduleCalendar,
                    )
                }
            }

            PreferredDateType.DATE_MODE_B -> {
                parser.getItems().forEach { x ->
                    this.addEventToCalendar(
                        context,
                        title = x.title,
                        description = x.body,
                        startTime = x.scheduled?.start?.toInstant()?.toEpochMilli(),
                        endTime = x.scheduled?.end?.toInstant()?.toEpochMilli(),
                        scheduleCalendar
                    )
                    this.addEventToCalendar(
                        context,
                        title = x.title,
                        description = x.body,
                        startTime = x.deadline?.start?.toInstant()?.toEpochMilli(),
                        endTime = x.deadline?.end?.toInstant()?.toEpochMilli(),
                        deadlineCalendar
                    )
                }
            }
        }
    }
}