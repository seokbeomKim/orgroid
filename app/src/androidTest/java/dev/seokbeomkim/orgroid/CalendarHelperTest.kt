package dev.seokbeomkim.orgroid

import android.Manifest
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import dev.seokbeomkim.orgroid.calendar.CalendarHelper
import dev.seokbeomkim.orgroid.parser.OrgParser
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalendarHelperTest {

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.READ_CALENDAR,
        Manifest.permission.WRITE_CALENDAR
    )

    @Test
    fun calendarHelperListUpTest() {
        val helper = CalendarHelper.getInstance()
        helper.updateCalendarArrayList(ApplicationProvider.getApplicationContext())
        helper.getCalendarArrayList().forEach { x ->
            println(x.toString())
        }
    }

    @Test
    fun calendarHelperCreationTest() {
        val helper = CalendarHelper.getInstance()
        helper.updateCalendarArrayList(ApplicationProvider.getApplicationContext())

        helper.dateType = CalendarHelper.PreferredDateType.DATE_MODE_B
        if (helper.createCalendar(
                ApplicationProvider.getApplicationContext(),
                "orgroid",
            ) < 0
        ) {
            Log.e("CalendarHelperTest", "Calendar creation failed")
        }
    }

    @Test
    fun calendarHelperDeleteTest() {
        val helper = CalendarHelper.getInstance()
        helper.updateCalendarArrayList(
            ApplicationProvider.getApplicationContext(),
            true
        )

        helper.getCalendarArrayList().forEach { x ->
            helper.deleteCalendar(ApplicationProvider.getApplicationContext(), x.id)
        }
    }

    @Test
    fun calendarHelperEventCreationModeATest() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val stream = context.assets.open("test.org")
        val parser = OrgParser()
        parser.open(stream)
        parser.parse(mustDefineSchedule = false, mustDefineDeadline = false)
        Log.d("CalendarHelperTest", "Events: $parser")

        val helper = CalendarHelper.getInstance()

        val scheduleId = helper.createCalendar(context, "Orgroid (Scheduled)")
        val deadlineId = helper.createCalendar(context, "Orgroid (deadline)")

        helper.updateCalendarArrayList(context, true)

        helper.dateType = CalendarHelper.PreferredDateType.DATE_MODE_A
        helper.addEventsByParser(parser, context, scheduleId, deadlineId)
    }

    @Test
    fun calendarHelperEventCreationModeBTest() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val stream = context.assets.open("test.org")
        val parser = OrgParser()
        parser.open(stream)
        parser.parse(mustDefineSchedule = false, mustDefineDeadline = false)
        Log.d("CalendarHelperTest", "Events: $parser")

        val helper = CalendarHelper.getInstance()

        val scheduleId = helper.createCalendar(context, "Orgroid (Scheduled)")
        val deadlineId = helper.createCalendar(context, "Orgroid (deadline)")

        helper.updateCalendarArrayList(context, true)

        helper.dateType = CalendarHelper.PreferredDateType.DATE_MODE_B
        helper.addEventsByParser(parser, context, scheduleId, deadlineId)
    }
}