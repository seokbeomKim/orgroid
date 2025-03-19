package dev.seokbeomkim.orgroid

import android.app.Activity
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var calendarHelper: CalendarHelper

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestCalendarPermission(this)

        // Initialize CalendarHelper which must be initialized before anything else (singleton)
        calendarHelper = CalendarHelper.getInstance()
        calendarHelper.updateCalendarArrayList(applicationContext)

        // Create SyncWorker service
        startOrgSyncService()

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
    }
}