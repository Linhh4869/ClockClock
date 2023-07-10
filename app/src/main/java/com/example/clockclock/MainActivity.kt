package com.example.clockclock

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import java.text.DecimalFormat
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var hourEditText: EditText
    private lateinit var minuteEditText: EditText
    private var alarmPendingIntent: PendingIntent? = null
    private var alarmStartTime: Long = 0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        hourEditText = findViewById(R.id.hour)
        minuteEditText = findViewById(R.id.minute)

        val sharedPreferences = getSharedPreferences("AlarmPreferences", Context.MODE_PRIVATE)
        val alarmHour = sharedPreferences.getInt("AlarmHour", -1)
        val alarmMinute = sharedPreferences.getInt("AlarmMinute", -1)
        val alarmRepeat = sharedPreferences.getBoolean("AlarmRepeat", false)

        if (alarmHour != -1 && alarmMinute != -1) {
            setAlarm(this, alarmHour, alarmMinute, alarmRepeat)
        }

        val addBtn = findViewById<ImageButton>(R.id.addBtn)
        addBtn.setOnClickListener {
            showDialog(this)
        }
    }

    // Hiển thị dialog
    @SuppressLint("InflateParams", "MissingInflatedId")
    fun showDialog(context: Context) {
        val dialogView: View = LayoutInflater.from(context).inflate(R.layout.alarm_dialog, null)
        val builder = AlertDialog.Builder(context)
        builder.setView(dialogView)

        val dialog = builder.create()

        val cancelBtn = dialogView.findViewById<TextView>(R.id.cancel)
        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        val timePicker = dialogView.findViewById<TimePicker>(R.id.picker)
        val loopCheckbox = dialogView.findViewById<CheckBox>(R.id.loop)

        val createBtn = dialogView.findViewById<Button>(R.id.create)
        createBtn.setOnClickListener {
            val selectedHour = timePicker.hour
            val selectedMinute = timePicker.minute
            val isRepeat = loopCheckbox.isChecked

            updateSelectedTime(selectedHour, selectedMinute)

            setAlarm(context, selectedHour, selectedMinute, isRepeat)

            dialog.dismiss()
        }

        dialog.show()
    }

    @SuppressLint("CommitPrefEdits")
    private fun setAlarm(context: Context, hour: Int, minute: Int, repeat: Boolean) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MyBroadcastReceived::class.java)
        intent.action = "ACTION_ALARM_TRIGGERED"

        val sharedPreferences = context.getSharedPreferences("AlarmPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Lưu thông tin báo thức
        editor.putInt("AlarmHour", hour)
        editor.putInt("AlarmMinute", minute)
        editor.putBoolean("AlarmRepeat", repeat)
        editor.apply()

        val requestCode = if (repeat) {
            hour * 100 + minute
        } else {
            0
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)

        val timeDiff = calendar.timeInMillis - System.currentTimeMillis()
        if (repeat) {
            val interval = 5 * 60 * 1000 // milliseconds

            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                // Nếu thời gian đã qua, thì tăng thời gian lên 1 ngày
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
                interval.toLong(), pendingIntent
            )
        } else {
            if (timeDiff > 0) {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
                )
            }
        }

        // Lưu trữ PendingIntent và thời gian bắt đầu báo thức
        alarmPendingIntent = pendingIntent
        alarmStartTime = calendar.timeInMillis

    }

    override fun onResume() {
        super.onResume()

        // Kiểm tra nếu đã đặt báo thức và đến thời gian đặt báo thức
        val currentTime = System.currentTimeMillis()
        if (alarmStartTime in 1..currentTime && currentTime - alarmStartTime <= 5 * 60 * 1000) {
            val intent = Intent(this, MyNotification::class.java)
            startService(intent)
        }
    }

    // Cập nhật thời gian đã chọn lên Edit Text
    private fun updateSelectedTime(selectedHour: Int, selectedMinute: Int) {
        val formattedHour = formatTimeValue(selectedHour)
        val formattedMinute = formatTimeValue(selectedMinute)

        hourEditText.setText(formattedHour)
        minuteEditText.setText(formattedMinute)
    }

    // Forrmat định đạng cho Edit Text
    private fun formatTimeValue(value: Int): String? {
        val decimalFormat = DecimalFormat("00")
        return decimalFormat.format(value)
    }
}