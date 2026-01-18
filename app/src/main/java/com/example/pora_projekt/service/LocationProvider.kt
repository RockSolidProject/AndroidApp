package com.example.pora_projekt.service

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.*

class LocationProvider(context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private var lastLatitude: Double? = null
    private var lastLongitude: Double? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let {
                lastLatitude = it.latitude
                lastLongitude = it.longitude
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun start() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000L)
            .setMinUpdateIntervalMillis(5_000L)
            .build()
        fusedLocationClient.requestLocationUpdates(request, locationCallback, null)
    }

    fun stop() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun getLocation(): Pair<Double?, Double?> {
        return Pair(lastLatitude, lastLongitude)
    }
}
