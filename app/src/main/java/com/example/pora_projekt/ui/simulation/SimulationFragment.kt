package com.example.pora_projekt.ui.simulation

import android.content.Context
import android.content.SharedPreferences
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pora_projekt.databinding.FragmentSimulationBinding
import com.example.pora_projekt.mqtt.MqttSender
import com.example.pora_projekt.prefs.SimulationPrefs
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import java.util.Locale
import kotlin.random.Random

class SimulationFragment : Fragment() {

    private var _binding: FragmentSimulationBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: SharedPreferences
    private lateinit var geocoder: Geocoder
    private var selectedLatitude = 46.55465
    private var selectedLongitude = 15.64588
    private var selectedAddress = "Maribor"
    private var isSimulationRunning = false

    companion object {
        private const val PREFS_NAME = SimulationPrefs.PREFS_NAME
        private const val KEY_IS_RUNNING = SimulationPrefs.KEY_IS_RUNNING
        private const val KEY_LATITUDE = SimulationPrefs.KEY_LATITUDE
        private const val KEY_LONGITUDE = SimulationPrefs.KEY_LONGITUDE
        private const val KEY_MIN_PEOPLE = SimulationPrefs.KEY_MIN_PEOPLE
        private const val KEY_MAX_PEOPLE = SimulationPrefs.KEY_MAX_PEOPLE
        private const val KEY_INTERVAL = SimulationPrefs.KEY_INTERVAL

        private val globalHandler = Handler(Looper.getMainLooper())
        private var globalSimulationRunnable: Runnable? = null
        var isGlobalSimulationRunning = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSimulationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        geocoder = Geocoder(requireContext(), Locale.getDefault())

        // naloži stanje
        isSimulationRunning = prefs.getBoolean(KEY_IS_RUNNING, false)
        isGlobalSimulationRunning = isSimulationRunning
        selectedLatitude = prefs.getString(KEY_LATITUDE, "46.05471")?.toDoubleOrNull() ?: 46.05471
        selectedLongitude = prefs.getString(KEY_LONGITUDE, "14.50854")?.toDoubleOrNull() ?: 14.50854
        selectedAddress = prefs.getString("address", "Ljubljana") ?: "Ljubljana"

        binding.editMinPeople.setText(prefs.getInt(KEY_MIN_PEOPLE, 1).toString())
        binding.editMaxPeople.setText(prefs.getInt(KEY_MAX_PEOPLE, 10).toString())
        binding.editInterval.setText(prefs.getLong(KEY_INTERVAL, 5L).toString())

        setupMap()
        setupAddressInput()
        setupToggleButton()
        updateUI()
    }

    private fun setupMap() {
        val mapView = binding.map
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        val defaultLocation = GeoPoint(selectedLatitude, selectedLongitude)
        mapView.controller.setZoom(8.0)
        mapView.controller.setCenter(defaultLocation)

        val mapEventsReceiver = object : org.osmdroid.events.MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                selectedLatitude = p.latitude
                selectedLongitude = p.longitude

                // odstrani stari marker
                mapView.overlays.removeAll { it is org.osmdroid.views.overlay.Marker }

                // dodaj nov marker
                val marker = org.osmdroid.views.overlay.Marker(mapView)
                marker.position = p
                marker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
                marker.title = "Izbrana lokacija"
                mapView.overlays.add(marker)
                mapView.invalidate()

                binding.textSelectedLocation.text = String.format(
                    Locale.US,
                    "Lokacija: %.6f°N, %.6f°E",
                    selectedLatitude,
                    selectedLongitude
                )

                // dobi naslov iz koordinat
                try {
                    val addresses = geocoder.getFromLocation(selectedLatitude, selectedLongitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        selectedAddress = addresses[0].getAddressLine(0) ?: "Neznan naslov"
                        binding.editAddress.setText(selectedAddress)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return true
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                return false
            }
        }

        val overlayEvents = org.osmdroid.views.overlay.MapEventsOverlay(mapEventsReceiver)
        mapView.overlays.add(overlayEvents)

        // začetni marker
        val initialMarker = org.osmdroid.views.overlay.Marker(mapView)
        initialMarker.position = defaultLocation
        initialMarker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
        initialMarker.title = "Trenutna lokacija"
        mapView.overlays.add(initialMarker)
    }

    private fun setupAddressInput() {
        binding.editAddress.setText(selectedAddress)

        binding.buttonSearchAddress.setOnClickListener {
            val address = binding.editAddress.text.toString().trim()
            if (address.isNotEmpty()) {
                searchAddress(address)
            }
        }

        binding.editAddress.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val address = binding.editAddress.text.toString().trim()
                if (address.isNotEmpty()) {
                    searchAddress(address)
                }
                true
            } else {
                false
            }
        }
    }

    private fun searchAddress(address: String) {
        try {
            val addresses = geocoder.getFromLocationName(address, 1)
            if (!addresses.isNullOrEmpty()) {
                val location = addresses[0]
                selectedLatitude = location.latitude
                selectedLongitude = location.longitude
                selectedAddress = address

                // premakni mapo na lokacijo
                val geoPoint = GeoPoint(selectedLatitude, selectedLongitude)
                binding.map.controller.setCenter(geoPoint)

                // premakne marker
                binding.map.overlays.removeAll { it is org.osmdroid.views.overlay.Marker }
                val marker = org.osmdroid.views.overlay.Marker(binding.map)
                marker.position = geoPoint
                marker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
                marker.title = "Iskana lokacija"
                binding.map.overlays.add(marker)
                binding.map.invalidate()

                binding.textSelectedLocation.text = String.format(
                    Locale.US,
                    "Lokacija: %.6f°N, %.6f°E",
                    selectedLatitude,
                    selectedLongitude
                )
            } else {
                binding.textSelectedLocation.text = "Naslov ni najden"
            }
        } catch (e: Exception) {
            binding.textSelectedLocation.text = "Napaka pri iskanju: ${e.message}"
            e.printStackTrace()
        }
    }

    private fun setupToggleButton() {
        binding.buttonToggleSimulation.setOnClickListener {
            if (isSimulationRunning) {
                stopSimulation()
            } else {
                startSimulation()
            }
        }
    }

    private fun saveState() {
        prefs.edit().apply {
            putBoolean(KEY_IS_RUNNING, isSimulationRunning)
            putString(KEY_LATITUDE, selectedLatitude.toString())
            putString(KEY_LONGITUDE, selectedLongitude.toString())
            putString("address", selectedAddress)
            putInt(KEY_MIN_PEOPLE, binding.editMinPeople.text.toString().toIntOrNull() ?: 1)
            putInt(KEY_MAX_PEOPLE, binding.editMaxPeople.text.toString().toIntOrNull() ?: 10)
            putLong(KEY_INTERVAL, binding.editInterval.text.toString().toLongOrNull() ?: 5L)
            apply()
        }
    }

    private fun updateUI() {
        if (isSimulationRunning) {
            binding.buttonToggleSimulation.text = "Ustavi simulacijo"
            binding.editMinPeople.isEnabled = false
            binding.editMaxPeople.isEnabled = false
            binding.editInterval.isEnabled = false
            binding.textStatus.text = "Simulacija je aktivna..."
        } else {
            binding.buttonToggleSimulation.text = "Zaženi simulacijo"
            binding.editMinPeople.isEnabled = true
            binding.editMaxPeople.isEnabled = true
            binding.editInterval.isEnabled = true
            binding.textStatus.text = "Simulacija je ustavljena"
        }

        binding.textSelectedLocation.text = String.format(
            Locale.US,
            "Lokacija: %.6f°N, %.6f°E",
            selectedLatitude,
            selectedLongitude
        )
    }

    private fun startSimulation() {
        val minPeople = binding.editMinPeople.text.toString().toIntOrNull() ?: 1
        val maxPeople = binding.editMaxPeople.text.toString().toIntOrNull() ?: 10
        val interval = binding.editInterval.text.toString().toLongOrNull() ?: 5L

        if (minPeople > maxPeople) {
            binding.textStatus.text = "Minimum mora biti manjši ali enak maksimalnemu"
            return
        }

        isSimulationRunning = true
        isGlobalSimulationRunning = true
        saveState()
        updateUI()

        globalSimulationRunnable = object : Runnable {
            override fun run() {
                if (isGlobalSimulationRunning) {
                    val randomPeople = (minPeople..maxPeople).random()

                    _binding?.let {
                        it.textStatus.text = String.format(
                            Locale.US,
                            "Simulacija aktivna\nLok: %.6f, %.6f\nOseb: %d\nInterval: %ds",
                            selectedLatitude,
                            selectedLongitude,
                            randomPeople,
                            interval
                        )
                    }
                    for(i in 1..randomPeople) {
                        MqttSender.publish("sensors", buildString {
                            append("{\"latitude\"=$selectedLatitude")
                            append(",\"longitude\"=$selectedLongitude")
                            append(",\"acceleration\"=\"N/A\"")
                            append(",\"timestamp\"=${System.currentTimeMillis()}")
                            append(", \"username\"=\"${Random.nextInt() }\"")
                            append("}")
                        })
                    }

                    println("Simulacija: $randomPeople oseb na lokaciji $selectedLatitude, $selectedLongitude")

                    globalHandler.postDelayed(this, interval * 1000)
                }
            }
        }

        globalHandler.post(globalSimulationRunnable!!)
    }

    private fun stopSimulation() {
        isSimulationRunning = false
        isGlobalSimulationRunning = false
        globalSimulationRunnable?.let { globalHandler.removeCallbacks(it) }

        saveState()
        updateUI()
    }

    override fun onPause() {
        super.onPause()
        if (::prefs.isInitialized) {
            saveState()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::prefs.isInitialized) {
            saveState()
        }
        _binding = null
    }
}

