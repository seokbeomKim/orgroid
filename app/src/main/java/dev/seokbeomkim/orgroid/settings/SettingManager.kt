package dev.seokbeomkim.orgroid.settings

import android.content.Context
import com.google.gson.Gson
import java.io.FileNotFoundException

/**
 * SettingManager class manages the settings of the application.
 *
 * The class handles store/load configuration file and manages the access
 * from the application and service.
 */
class SettingManager {
    fun saveSettings(context: Context, settingData: SettingData) {
        val gson = Gson()
        val jsonString = gson.toJson(settingData)
        context.openFileOutput("settings.json", Context.MODE_PRIVATE).use {
            it.write(jsonString.toByteArray())
        }
    }

    fun loadSettings(context: Context): SettingData? {
        return try {
            val jsonString = context.openFileInput("settings.json").bufferedReader().use {
                it.readText()
            }
            val gson = Gson()
            gson.fromJson(jsonString, SettingData::class.java)
        } catch (e: FileNotFoundException) {
            null
        }
    }

    fun getLocalOrgFiles(context: Context): List<LocalOrgFile> {
        val settings = loadSettings(context)
        return settings?.localOrgFiles ?: emptyList()
    }

    fun addLocalOrgFile(context: Context, localOrgFile: LocalOrgFile) {
        val settings = loadSettings(context) ?: SettingData(emptyList())
        val updatedSettings = settings.copy(localOrgFiles = settings.localOrgFiles + localOrgFile)
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
    val localOrgFiles: List<LocalOrgFile> = emptyList()
)