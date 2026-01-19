package com.example.pora_projekt

import android.app.Application
import android.content.SharedPreferences
import com.example.pora_projekt.prefs.MqttPrefs
import com.example.pora_projekt.prefs.SimulationPrefs
import androidx.core.content.edit

class PoraApp: Application() {
    private lateinit var mqttPrefs: SharedPreferences
    private lateinit var simulationPrefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        mqttPrefs = getSharedPreferences(MqttPrefs.PREFS_NAME, MODE_PRIVATE)
        simulationPrefs = getSharedPreferences(SimulationPrefs.PREFS_NAME, MODE_PRIVATE)
    }
    fun getMqttPrefs(): SharedPreferences = mqttPrefs
    fun getSimulationPrefs(): SharedPreferences = simulationPrefs

    fun getMqttUsername(): String? = mqttPrefs.getString(MqttPrefs.MQTT_USERNAME, null)
    fun getMqttPassword(): String? = mqttPrefs.getString(MqttPrefs.MQTT_PASSWORD, null)
    fun getDataInterval(): Int = mqttPrefs.getInt(MqttPrefs.DATA_INTERVAL,5)

    fun isSimulationRunning(): Boolean = simulationPrefs.getBoolean(SimulationPrefs.KEY_IS_RUNNING, false)

    fun getSimulationLatitude(): Double = simulationPrefs.getFloat(SimulationPrefs.KEY_LATITUDE,46.05471f).toDouble()
    fun getSimulationLongitude(): Double = simulationPrefs.getFloat(SimulationPrefs.KEY_LONGITUDE,14.50513f).toDouble()
    fun getSimulationAddress(): String = simulationPrefs.getString(SimulationPrefs.KEY_ADDRESS,"Ljubljana, Slovenia")!!
    fun getSimulationMinPeople(): Int = simulationPrefs.getInt(SimulationPrefs.KEY_MIN_PEOPLE,1)
    fun getSimulationMaxPeople(): Int = simulationPrefs.getInt(SimulationPrefs.KEY_MAX_PEOPLE, 10)
    fun getSimulationInterval(): Int = simulationPrefs.getInt(SimulationPrefs.KEY_INTERVAL,5)
    fun setSimulationRunning(running: Boolean) {
        simulationPrefs.edit {
            putBoolean(SimulationPrefs.KEY_IS_RUNNING,running)
        }
    }

    fun simulationPrefsInitialized(): Boolean = ::simulationPrefs.isInitialized





}