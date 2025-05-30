package dev.seokbeomkim.orgroid.service

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.CalendarContract
import android.util.Log

class CalendarEventsObserver(handler: Handler) : ContentObserver(handler) {
    lateinit var contentResolver: ContentResolver
    lateinit var uri: Uri
    internal val TAG = "CalendarEventsObserver"

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)

        uri?.let {
            fetchEventDetails()
        }
    }

    private fun updateOrgDocument(uri: Uri) {
        Log.d(TAG, "update org document with " + uri.toString())
    }

    private fun fetchEventDetails() {
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.LAST_SYNCED
        )

        val selection =
            "${CalendarContract.Calendars.ACCOUNT_NAME} = ? AND ${CalendarContract.Events.DELETED} = 0"
        val selectionArgs = arrayOf("orgroid")

        val cursor = contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )

        cursor?.use {
            it.moveToLast()
            Log.d(
                TAG,
                "Event ID: ${it.getLong(0)}, Title: ${it.getString(1)} Last Synced: ${it.getLong(2)}"
            )
        }
    }
}