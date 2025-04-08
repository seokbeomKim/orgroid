package dev.seokbeomkim.orgroid.ui.calendars

import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.seokbeomkim.orgroid.calendar.CalendarHelper
import dev.seokbeomkim.orgroid.parser.OrgParser
import java.io.File

class CalendarsViewModel : ViewModel() {

    val parser: OrgParser = OrgParser()

    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }

    fun getFileNameAndExtension(context: Context, uri: Uri): Pair<String, String?> {
        var fileName = ""
        var extension: String? = null

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex)
                    extension = fileName.substringAfterLast('.', "")
                }
            }
        }

        return Pair(fileName, extension)
    }

    fun getRealPathFromURI(context: Context, uri: Uri): String? {
        var filePath: String? = null
        val projection = arrayOf(MediaStore.MediaColumns.DATA)

        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                filePath = cursor.getString(columnIndex)
            }
        }
        return filePath
    }

    /**
     * Try to parse the org file and show the result in a dialog.
     * @param context The context of the activity.
     * @param fullPath The path of org file
     * @return True if the org file is parsed successfully, false otherwise.
     */
    fun tryToParseOrgFile(context: Context, fullPath: String?): Boolean {
        parser.flush()

        if (fullPath != null) {
            val file = File(fullPath)
            if (file.exists()) {
                parser.open(file)
                parser.parse()
            } else {
                Log.e(TAG, "tryToParseOrgFile: the file ($fullPath) does not exist!")
            }
        } else {
            Log.e(TAG, "tryToParseOrgFile: fullPath is null")
        }

        return parser.getItems().isNotEmpty()
    }

    fun createCalendar(context: Context, calendarName: String, calendarColor: Int) {
        Log.d(TAG, "createCalendar: $calendarName, $calendarColor")

        val helper = CalendarHelper.getInstance()
        val scheduleId = helper.createCalendar(context, calendarName, calendarColor = calendarColor)

        helper.updateCalendarArrayList(context, true)

        helper.dateType = CalendarHelper.PreferredDateType.DATE_MODE_B
        helper.addEventsByParser(
            parser,
            context,
            scheduleCalendar = scheduleId,
            deadlineCalendar = null
        )
    }

    fun editCalendar(
        requireContext: Context,
        calendarId: Long,
        calendarName: String,
        calendarColor: Int
    ) {
        Log.d("orgroid", "editCalendar: $calendarId, $calendarName, $calendarColor")

        val helper = CalendarHelper.getInstance()
        helper.editCalendar(requireContext, calendarId, calendarName, calendarColor)
        helper.updateCalendarArrayList(requireContext, true)
    }
}