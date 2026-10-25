package com.ndaily.mobile.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat

class LocationTracker(
    private val context: Context,
    private val onUpdate: (LatLng?, denied: Boolean) -> Unit,
) : LocationListener {
    private val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var started = false

    fun hasPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun start() {
        if (started) return
        if (!hasPermission()) {
            onUpdate(null, true)
            return
        }
        started = true
        val last = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        last?.let { onUpdate(LatLng(it.latitude, it.longitude), false) }
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { manager.isProviderEnabled(it) }
        if (providers.isEmpty()) {
            onUpdate(null, true)
            return
        }
        providers.forEach { provider ->
            manager.requestLocationUpdates(provider, 2500L, 8f, this, Looper.getMainLooper())
        }
    }

    fun stop() {
        if (!started) return
        started = false
        manager.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) {
        onUpdate(LatLng(location.latitude, location.longitude), false)
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
}
