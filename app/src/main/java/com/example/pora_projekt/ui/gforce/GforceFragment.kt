package com.example.pora_projekt.ui.gforce

import FallDetectorLinear
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pora_projekt.databinding.FragmentGforceBinding
import com.example.pora_projekt.mqtt.MqttSender
import com.example.pora_projekt.service.LocationProvider
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

class GforceFragment : Fragment() {
    private var _binding: FragmentGforceBinding? = null
    private val binding get() = _binding!!

    private lateinit var fallDetector: FallDetectorLinear
    private val handler = Handler(Looper.getMainLooper())
    private var clearFallTextRunnable: Runnable? = null

    private var maxAcceleration = 0f
    private var minAcceleration = Float.MAX_VALUE
    private var totalAcceleration = 0f
    private var readingCount = 0
    private var currentX = 0f
    private var currentY = 0f
    private var currentZ = 0f
    private lateinit var locationProvider: LocationProvider

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGforceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationProvider = LocationProvider(requireContext())
        locationProvider.start()
        fallDetector = FallDetectorLinear(
            requireContext(),
            onFallDetected = {
                Log.d("FallDetector", "PADEC ZAZNAN!")
                binding.textFallDetected.text = "PADEC ZAZNAN!"
                val username = MqttSender.MQTT_USERNAME ?: "Unknown"
                val timestamp = System.currentTimeMillis().toString()
                val (lat, lon) = locationProvider.getLocation()
                    ?: Pair(0.0, 0.0)
                val payload = buildString {
                    append("{\"type\"=\"extreme\"")
                    append(",\"message\"=\"$username fell\"")
                    append(",\"username\"=\"$username\"")
                    append(",\"timestamp\"=\"$timestamp\"")
                    append(",\"latitude\"=$lat")
                    append(",\"longitude\"=$lon")
                    append("}")
                }

                MqttSender.publish("messages", payload)

                clearFallTextRunnable?.let { handler.removeCallbacks(it) }

                clearFallTextRunnable = Runnable {
                    binding.textFallDetected.text = ""
                }
                handler.postDelayed(clearFallTextRunnable!!, 5000L)
            },
            onAccelerationChanged = { x, y, z, magnitude ->
                updateAccelerationData(x, y, z, magnitude)
            }
        )
    }

    private fun updateAccelerationData(x: Float, y: Float, z: Float, magnitude: Float) {
        currentX = x
        currentY = y
        currentZ = z

        maxAcceleration = max(maxAcceleration, magnitude)
        minAcceleration = min(minAcceleration, magnitude)
        totalAcceleration += magnitude
        readingCount++


        binding.textCurrentAcceleration.text = String.format(Locale.US, "Trenutni pospešek: %.2f m/s²", magnitude)
        binding.textMaxAcceleration.text = String.format(Locale.US, "Maksimalni pospešek: %.2f m/s²", maxAcceleration)
        binding.textXyzValues.text = String.format(Locale.US, "X: %.2f  Y: %.2f  Z: %.2f", x, y, z)
    }

    override fun onResume() {
        super.onResume()
        fallDetector.start()
    }

    override fun onPause() {
        super.onPause()
        fallDetector.stop()
        clearFallTextRunnable?.let { handler.removeCallbacks(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearFallTextRunnable?.let { handler.removeCallbacks(it) }
        _binding = null
    }
}
