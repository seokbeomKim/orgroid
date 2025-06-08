package dev.seokbeomkim.orgroid

import android.app.Activity
import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dev.seokbeomkim.orgroid.calendar.CalendarHelper
import dev.seokbeomkim.orgroid.databinding.ActivityMainBinding
import dev.seokbeomkim.orgroid.service.OrgSyncService
import dev.seokbeomkim.orgroid.settings.SettingManager
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.exists

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding
    private lateinit var calendarHelper: CalendarHelper
    private lateinit var settingManager: SettingManager

    private lateinit var directoryPickerLauncher: ActivityResultLauncher<Intent>

    fun requestCalendarPermission(activity: Activity) {
        val permissions = arrayOf(
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR
        )

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CALENDAR)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CALENDAR)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // 권한 요청
            ActivityCompat.requestPermissions(activity, permissions, 100)
        }
    }

    private fun startOrgSyncService() {
        val serviceIntent = Intent(this, OrgSyncService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun openDirectoryPicker(context: Context) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        directoryPickerLauncher.launch(
            intent
        )
    }

    private fun showMissingDirectoryAlert(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Directory is not configured")
            .setMessage("Please configure the directory to use Orgroid.")
            .setPositiveButton("Configure") { _, _ ->
                openDirectoryPicker(context)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAlertWhenDirectoryIsNotConfigured(context: Context) {
        val sharedPrefs =
            getSharedPreferences(R.string.shared_prefs_name.toString(), Context.MODE_PRIVATE)
        val savedUriString =
            sharedPrefs.getString(getString(R.string.prefs_local_org_dir), null)

        if (savedUriString == null) {
            showMissingDirectoryAlert(context)
        }
    }

    private fun registerHandlers(context: Context) {
        directoryPickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                val sharedPrefs = getSharedPreferences(
                    getString(R.string.shared_prefs_name),
                    Context.MODE_PRIVATE
                )
                sharedPrefs.edit()
                    .putString(getString(R.string.prefs_local_org_dir), data?.dataString)
                    .apply()
            } else {
                Log.d(TAG, "result code: ${result.resultCode}")
            }
        }
    }

    private fun createOrgroidDirectory(context: Context): File {
        val orgroidDir = File(Environment.getExternalStorageDirectory(), "orgroid")

        if (!orgroidDir.exists()) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    101 // You can define a request code
                )
            }
            val success = orgroidDir.mkdirs()
            if (!success) {
                Log.e(TAG, "Failed to create a directory at: ${orgroidDir.absolutePath}")
            }
        }

        return orgroidDir
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createOrgroidDirectory(this)

        // Initialize CalendarHelper which must be initialized before anything else (singleton)
        calendarHelper = CalendarHelper.getInstance()
        calendarHelper.updateCalendarArrayList(applicationContext)

        // Create SyncWorker service
//        startOrgSyncService()

        // Initialize UI Layout
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_calendars, R.id.navigation_setting, R.id.navigation_about
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

//        SettingManager().saveSettings(this, SettingData(true, 16, arrayListOf(1, 2, 3)))
//        Log.d("OrgroidSetting", SettingManager().loadSettings(this).toString())
    }
}