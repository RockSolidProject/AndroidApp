package com.example.pora_projekt

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.pora_projekt.databinding.ActivityMainBinding
import org.osmdroid.config.Configuration

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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