package dev.seokbeomkim.orgroid

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.CalendarContract
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import dev.seokbeomkim.orgtodo.calendar.CalendarHelper

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule
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
        android.Manifest.permission.READ_CALENDAR,
        android.Manifest.permission.WRITE_CALENDAR
    )

    @Test
    fun createCalendar() {
        // Context of the app under test.
        val helper = CalendarHelper()
        helper.createCalendar(
            ApplicationProvider.getApplicationContext(),
            CalendarContract.Calendars.OWNER_ACCOUNT, "TEST"
        )
        helper.createCalendar(
            ApplicationProvider.getApplicationContext(),
            CalendarContract.Calendars.OWNER_ACCOUNT, "TEST2"
        )
    }

    @Test
    fun removeCalendar() {
        val helper = CalendarHelper()
        helper.deleteCalendar(ApplicationProvider.getApplicationContext(), 13)
    }

    @Test
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

    @Test
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
        val helper = CalendarHelper()
        val calendars = helper.getCalendarArrayList()

        println("Calendar list:")
        calendars.forEach {
            println(it)
        }
    }
}