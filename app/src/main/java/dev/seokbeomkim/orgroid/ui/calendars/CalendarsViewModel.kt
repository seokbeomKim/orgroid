package dev.seokbeomkim.orgroid.ui.calendars

import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.seokbeomkim.orgtodo.parser.OrgParser
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

    fun tryToParseOrgFile(fullPath: String?) {
        if (fullPath != null) {
            val file = File(fullPath)
            if (file.exists()) {
                parser.open(file)
                parser.parse()
                parser.dumpItems()
            } else {
                Log.e(TAG, "tryToParseOrgFile: the file ($fullPath) does not exist!")
            }
        } else {
            Log.e(TAG, "tryToParseOrgFile: fullPath is null")
        }
    }
}