package dev.seokbeomkim.orgroid.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import dev.seokbeomkim.orgroid.R

class OrgSyncService : Service() {
    private lateinit var runnable: Runnable
    private lateinit var handler: Handler
    private val channelId: String = "OrgSyncServiceChannel"

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        handler = Handler(Looper.getMainLooper())

        runnable = object : Runnable {
            override fun run() {
                Log.d("MyForegroundService", "Logging message every 3 seconds")
                handler.postDelayed(this, 3000) // Repeat every 3 seconds
            }
        }
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            channelId,
            "Foreground Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(serviceChannel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationIntent = Intent(this, OrgSyncService::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification: Notification = Notification.Builder(this, channelId)
            .setContentTitle("Foreground Service")
            .setContentText("Service is running")
            .setSmallIcon(R.drawable.ic_about_black_24dp)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
        handler.post(runnable)

        Log.d("OrgSyncService", "Service started")

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable) // Stop the logging when the service is destroyed
    }

}