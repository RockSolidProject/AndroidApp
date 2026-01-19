package com.example.pora_projekt.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.example.pora_projekt.mqtt.MqttSender

class SensorDataService : Service() {
    private val handler = Handler()
    private var intervalMillis: Long = 60 * 1000
    private lateinit var accelerationProvider: AccelerationProvider
    private lateinit var locationProvider: LocationProvider
    private lateinit var sharedPreferences: SharedPreferences

    private val sendDataRunnable = object : Runnable {
        override fun run() {
            val intervalSeconds = sharedPreferences.getInt("update_interval", 60)
            intervalMillis = intervalSeconds * 1000L

            val (lat, lon) = locationProvider.getLocation()
            val latStr = lat?.toString()
            val lonStr = lon?.toString()

            if (latStr == null || lonStr == null) {
                android.util.Log.d("SensorDataService", "Location not available yet.")
                handler.postDelayed(this, intervalMillis)
                return
            }

            val accelData: String? = accelerationProvider.getCurrentAccelerationString()
            val timestamp : String = System.currentTimeMillis().toString()
            val username : String = MqttSender.MQTT_USERNAME ?: "Unknown"

            val payload = buildString {
                append("{\"latitude\"=$latStr")
                append(",\"longitude\"=$lonStr")
                append(",\"acceleration\"=\"${accelData ?: "N/A"}\"")
                append(",\"timestamp\"=\"$timestamp\"")
                append(",\"username\"=\"$username\"")
                append("}")
            }

            android.util.Log.d("SensorDataService", "Publishing: $payload")
            MqttSender.publish("sensors", payload)

            handler.postDelayed(this, intervalMillis)
        }
    }

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        accelerationProvider = AccelerationProvider(this)
        locationProvider = LocationProvider(this)
        locationProvider.start()

        val username = sharedPreferences.getString("username", "") ?: ""
        val password = sharedPreferences.getString("password", "") ?: ""
        if (username.isNotEmpty() && password.isNotEmpty()) {
            MqttSender.setCredentials(username, password)
            MqttSender.connect()
        }

        startForegroundService()
        handler.post(sendDataRunnable)
    }

    override fun onDestroy() {
        handler.removeCallbacks(sendDataRunnable)
        accelerationProvider.cleanup()
        locationProvider.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundService() {
        val channelId = "sensor_data_channel"
        val channel = NotificationChannel(
            channelId,
            "Sensor Data Service",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Sending sensor data")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()
        startForeground(1, notification)
    }
}
