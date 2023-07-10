package com.example.clockclock

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.NotificationCompat

class MyNotification : Service() {
    private lateinit var player: MediaPlayer
    private var isAlarmPlaying = false

    override fun onCreate() {
        super.onCreate()
        player = MediaPlayer.create(this, R.raw.sound_noti)
        player.isLooping
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isAlarmPlaying) {
//            startForegroundService(Intent(this, MyNotification::class.java))
            showNotification()
            startAlarmSound()
            isAlarmPlaying = true
            if (intent?.action == "ACTION_STOP_ALARM") {
                stopSelf()
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        player.stop()
        stopAlarmSound()
    }

    private fun showNotification() {
        val channelId = "myNotify"
        val channelName = "AlarmNotify"
        val notificationId = 1

        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val stopActionIntent = Intent(this, MyNotification::class.java)
        stopActionIntent.action = "ACTION_STOP_ALARM"
        val stopActionPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            stopActionIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

//        val dismissIntent = Intent(this, MyBroadcastReceived::class.java)
//        dismissIntent.action = "ACTION_DISMISS_ALARM"
//        val dismissPendingIntent = PendingIntent.getBroadcast(
//            this,
//            0,
//            dismissIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT
//        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Giờ lành đã điểm!")
            .setContentText("Dậy đi em ơi, không ai cứu được em đâu")
            .setSmallIcon(R.drawable.app_ic)
            .addAction(R.drawable.stop_noti, "Stop", stopActionPendingIntent)
//            .addAction(R.drawable.stop_noti, "Dismiss", dismissPendingIntent)
            .build()

        val notificationIntent = Intent(this, MyNotification::class.java)
        val pendingIntent = PendingIntent.getService(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

//        startForegroundService(notificationIntent)

        startForeground(notificationId, notification)
    }

    private fun startAlarmSound() {
        player.start()
    }

    private fun stopAlarmSound() {
        player.stop()
        player.reset()
        player.release()
        isAlarmPlaying = false
    }
}