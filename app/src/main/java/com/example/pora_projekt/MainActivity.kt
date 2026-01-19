package com.example.pora_projekt

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.example.pora_projekt.databinding.ActivityMainBinding
import com.example.pora_projekt.mqtt.MqttSender
import com.example.pora_projekt.service.SensorDataService
import org.osmdroid.config.Configuration

class MainActivity : AppCompatActivity() {
    private lateinit var app: PoraApp

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as PoraApp
        initializeOsmdroid()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_camera, R.id.navigation_gforce, R.id.navigation_comment, R.id.navigation_simulacija, R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        MqttSender.setCredentials(
            preferences.getString("username", "") ?: "",
            preferences.getString("password", "") ?: ""
        )
        MqttSender.connect();
    }

    private val LOCATION_PERMISSIONS = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private val LOCATION_REQUEST_CODE = 101

    private fun ensureLocationPermissions() {
        if (LOCATION_PERMISSIONS.any {
                checkSelfPermission(it) != android.content.pm.PackageManager.PERMISSION_GRANTED
            }) {
            requestPermissions(LOCATION_PERMISSIONS, LOCATION_REQUEST_CODE)
        }
    }

    override fun onStart() {
        super.onStart()
        ensureLocationPermissions()
        if (LOCATION_PERMISSIONS.all {
                checkSelfPermission(it) == android.content.pm.PackageManager.PERMISSION_GRANTED
            }) {
            startForegroundService(Intent(this, SensorDataService::class.java))
        }
    }


    // So the back button in top nav bar works
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun initializeOsmdroid() {
        try {
            Configuration.getInstance().apply {
                userAgentValue = "PORA-projekt/1.0 (Android; ${android.os.Build.VERSION.SDK_INT})"
                osmdroidBasePath = getExternalFilesDir(null)
                osmdroidTileCache = getExternalFilesDir(null)
                cacheMapTileCount = 100
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}