package com.example.clockclock

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MyBroadcastReceived : BroadcastReceiver() {

    private var isAlarmPlaying = false

    @SuppressLint("ServiceCast")
    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent != null) {
            when (intent.action) {

//                "ACTION_DISMISS_ALARM" -> {
//                    if (isAlarmPlaying) {
//                        val serviceIntent = Intent(context, MyNotification::class.java)
//                        context?.stopService(serviceIntent)
//                        isAlarmPlaying = false
//
//                        // Hủy thông báo
//                        val notificationManager =
//                            context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//                        notificationManager.cancel(1)
//                    }
//                }

                "ACTION_STOP_ALARM" -> {

                    if (isAlarmPlaying) {
                        // Tắt báo thức
                        val serviceIntent = Intent(context, MyNotification::class.java)
                        context?.stopService(serviceIntent)
                        isAlarmPlaying = false

                        // Hủy thông báo
                        val notificationManager =
                            context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.cancel(1)
                    }
                }

                "ACTION_ALARM_TRIGGERED" -> {
                    // Khởi chạy service
                    val serviceIntent = Intent(context, MyNotification::class.java)
                    context?.startService(serviceIntent)

                    if (!isAlarmPlaying) {
                        isAlarmPlaying = true
                    }
                }
            }
        }
    }
}