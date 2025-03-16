package dev.seokbeomkim.orgroid

import android.Manifest
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import dev.seokbeomkim.orgroid.parser.OrgParser
import dev.seokbeomkim.orgroid.calendar.CalendarHelper
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.TimeZone

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class CalendarTest {
    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.READ_CALENDAR,
        Manifest.permission.WRITE_CALENDAR
    )

    fun addEventToCalendar(
        context: Context,
        calendarId: Long,
        title: String,
        description: String,
        startTime: Long,
        endTime: Long
    ) {
        val cr: ContentResolver = context.contentResolver
        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DESCRIPTION, description)
            put(CalendarContract.Events.DTSTART, startTime)
            put(CalendarContract.Events.DTEND, endTime)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            put(CalendarContract.Events.EVENT_LOCATION, "Online")
            put(CalendarContract.Events.HAS_ALARM, 1)
        }

        val uri: Uri? = cr.insert(CalendarContract.Events.CONTENT_URI, values)

        uri?.let {
            val eventId = ContentUris.parseId(it)
            println("Event added with ID: $eventId")
        } ?: run {
            println("Failed to add event")
        }
    }

    fun deleteEvent(context: Context, eventId: Long) {
        val cr: ContentResolver = context.contentResolver
        val uri: Uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
        val rowsDeleted = cr.delete(uri, null, null)

        if (rowsDeleted > 0) {
            println("Event deleted successfully")
        } else {
            println("Failed to delete event")
        }
    }

    @Test
    fun getListOfCalendar() {
        val helper = CalendarHelper.getInstance()
        helper.updateCalendarArrayList(ApplicationProvider.getApplicationContext())
        val calendars = helper.getCalendarArrayList()

        Log.d("getListOfCalendar", "Calendar list:")
        calendars.forEach {
            Log.d("getListOfCalendar", it.toString())
        }
    }

    private fun formatEpochTime(startTime: Long): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
        return Instant.ofEpochMilli(startTime)
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    }

    private fun getEventById(context: Context, eventId: Long) {
        val uri: Uri = CalendarContract.Events.CONTENT_URI
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events.EVENT_TIMEZONE
        )

        val selection = "${CalendarContract.Events._ID} = ?"
        val selectionArgs = arrayOf(eventId.toString())

        val cursor: Cursor? =
            context.contentResolver.query(uri, projection, selection, selectionArgs, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val title = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.TITLE))
                val description =
                    it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.DESCRIPTION))
                val startTime =
                    it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART))
                val endTime = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTEND))
                val location =
                    it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.EVENT_LOCATION))
                val timezone =
                    it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.EVENT_TIMEZONE))

                println("📅 Event Info")
                println("ID: $eventId")
                println("Title: $title")
                println("Description: $description")
                println("Start Time: ${formatEpochTime(startTime)}")
                println("End Time: ${formatEpochTime(endTime)}")
                println("Location: $location")
                println("Timezone: $timezone")
            } else {
                println("❌ Event not found")
            }
        }
    }

    @Test
    fun parseOrgTest() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val stream = context.assets.open("test.org")
        val parser = OrgParser()
        parser.open(stream)
        parser.parse(mustDefineSchedule = false, mustDefineDeadline = false)

        println(parser)
    }


}