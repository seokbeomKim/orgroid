package dev.seokbeomkim.orgroid.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory.Companion.instance
import com.google.gson.Gson
import dev.seokbeomkim.orgroid.calendar.CalendarHelper
import java.io.FileNotFoundException
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists

/**
 * SettingManager class manages the settings of the application.
 *
 * The class handles store/load configuration file and manages the access
 * from the application and service.
 */
class SettingManager {
    private val REQUEST_CODE_OPEN_DIRECTORY = 1001
    private val TAG = "SettingManager"
    private lateinit var data: SettingData

    companion object {
        @Volatile
        private var instance: SettingManager? = null

        fun getInstance(): SettingManager {
            return instance ?: synchronized(this) {
                instance ?: SettingManager().also {
                    instance = it
                    it.data = SettingData("", emptyList())
                }
            }
        }
    }

    fun saveSettings(context: Context, settingData: SettingData) {
        val gson = Gson()
        val jsonString = gson.toJson(settingData)
        context.openFileOutput("settings.json", Context.MODE_PRIVATE).use {
            it.write(jsonString.toByteArray())
        }
    }

    fun loadSettings(context: Context): SettingData? {
        if (!context.getFileStreamPath("settings.json").exists()) {
            return null
        }

        return try {
            val jsonString = context.openFileInput("settings.json").bufferedReader().use {
                it.readText()
            }
            val gson = Gson()
            data = gson.fromJson(jsonString, SettingData::class.java)
            data
        } catch (e: FileNotFoundException) {
            data = SettingData("", emptyList())
            null
        }
    }

    fun getLocalOrgFiles(context: Context): List<LocalOrgFile> {
        val settings = loadSettings(context)
        return settings?.localOrgFiles ?: emptyList()
    }

    fun addLocalOrgFile(context: Context, localOrgFile: LocalOrgFile) {
        val settings = loadSettings(context) ?: SettingData("", emptyList())
        val updatedSettings = settings.copy(localOrgFiles = settings.localOrgFiles + localOrgFile)
        saveSettings(context, updatedSettings)
    }

    fun checkDefaultDirectory(): Boolean {
        return data.localOrgDirectory.isNotBlank() && Path(data.localOrgDirectory).exists()
    }

    fun updateLocalOrgDirectory(context: Context, newDirectory: String) {
        val settings = loadSettings(context) ?: SettingData("", emptyList())
        val updatedSettings = settings.copy(localOrgDirectory = newDirectory)
        saveSettings(context, updatedSettings)
    }
}

data class LocalOrgFile(
    val calendarId: Long,
    val calendarPath: String,
    val lastSyncTime: Long
)

/**
 * SettingData class represents the data for local org files.
 */
data class SettingData(
    var localOrgDirectory: String,
    val localOrgFiles: List<LocalOrgFile> = emptyList()
)