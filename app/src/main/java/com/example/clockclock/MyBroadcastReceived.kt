package com.example.clockclock

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService

class MyBroadcastReceived : BroadcastReceiver() {
    @SuppressLint("ServiceCast")
    override fun onReceive(context: Context?, intent: Intent?) {

        val sharedPreferences = context?.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE)
        var isAlarmPlaying = sharedPreferences?.getBoolean("isAlarmPlaying", false) ?: false

        if (intent != null) {
            when (intent.action) {
                "ACTION_ALARM_TRIGGERED" -> {
                    val notification = createNotification(context)
                    if (notification != null) {
                        showNotification(context, notification)
                    }

                    if (context != null) {
                        startAlarmSound(context)
                    }

                    if (!isAlarmPlaying) {
                        isAlarmPlaying = true
                    }
                }

                "ACTION_STOP_ALARM" -> {
                    if (isAlarmPlaying) {
                        if (context != null) {
                            stopAlarm(context)
                        }
                    }

                    val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(1)

                    isAlarmPlaying = false
                }

            }

        }

        val editor = sharedPreferences?.edit()
        editor?.putBoolean("isAlarmPlaying", isAlarmPlaying)
        editor?.apply()
    }

    private fun createNotification(context: Context?): Notification? {
        val channelId = "myNotify"
        val channelName = "AlarmNotify"

        val stopActionIntent = Intent(context, MyBroadcastReceived::class.java)
        stopActionIntent.action = "ACTION_STOP_ALARM"
        val stopActionPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            stopActionIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return context?.let {
            NotificationCompat.Builder(it, channelId)
                .setContentTitle("Báo thức")
                .setContentText("Dậy đi em ơi, không ai cứu được em nữa đâu")
                .setSmallIcon(R.drawable.app_ic)
                .addAction(R.drawable.stop_noti, "Stop", stopActionPendingIntent)
                .build()
        }
    }

    private fun showNotification(context: Context?, noti : Notification) {
        val notiId = 1

        val channel = NotificationChannel(
            "myNotify",
            "AlarmNotify",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        notificationManager.notify(notiId, noti)
    }

    private fun startAlarmSound(context: Context) {
        val player = MediaPlayer.create(context, R.raw.sound_noti)
        player.isLooping = true
        player.start()
    }

    private fun stopAlarm(context: Context) {
        val player = MediaPlayer.create(context, R.raw.sound_noti)
        player.stop()
        player.reset()
        player.release()
    }
}