package com.ndaily.mobile.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Looper
import androidx.core.content.ContextCompat

class LocationTracker(
    private val context: Context,
    private val onUpdate: (LatLng?, denied: Boolean) -> Unit,
) : LocationListener {
    private val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var started = false
    private var currentSignal: CancellationSignal? = null

    fun hasPermission(): Boolean {
        val fine = granted(Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = granted(Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine || coarse
    }

    fun locationServicesEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            manager.isLocationEnabled
        } else {
            providerEnabled(LocationManager.GPS_PROVIDER) || providerEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }

    @SuppressLint("MissingPermission")
    fun start() {
        if (started) return
        if (!hasPermission()) {
            onUpdate(null, true)
            return
        }
        if (!locationServicesEnabled()) {
            onUpdate(null, false)
            return
        }
        val providers = activeProviders()
        if (providers.isEmpty()) {
            onUpdate(null, false)
            return
        }
        var listening = false
        providers.forEach { provider ->
            try {
                manager.requestLocationUpdates(provider, 1000L, 0f, this, Looper.getMainLooper())
                listening = true
            } catch (_: Exception) {
            }
        }
        if (requestCurrent(providers.first())) listening = true
        if (!listening) {
            onUpdate(null, false)
            return
        }
        started = true
        readLastKnown()?.let { onUpdate(it, false) }
    }

    fun stop() {
        if (!started) {
            currentSignal?.cancel()
            currentSignal = null
            return
        }
        started = false
        currentSignal?.cancel()
        currentSignal = null
        manager.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) {
        onUpdate(LatLng(location.latitude, location.longitude), false)
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

    @SuppressLint("MissingPermission")
    private fun requestCurrent(provider: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return false
        currentSignal?.cancel()
        val signal = CancellationSignal()
        currentSignal = signal
        return try {
            manager.getCurrentLocation(provider, signal, ContextCompat.getMainExecutor(context)) { location ->
                location?.let { onUpdate(LatLng(it.latitude, it.longitude), false) }
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun activeProviders(): List<String> {
        val names = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) add(LocationManager.FUSED_PROVIDER)
            if (granted(Manifest.permission.ACCESS_FINE_LOCATION)) add(LocationManager.GPS_PROVIDER)
            add(LocationManager.NETWORK_PROVIDER)
        }
        return names.distinct().filter(::providerEnabled)
    }

    @SuppressLint("MissingPermission")
    private fun readLastKnown(): LatLng? {
        val names = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) add(LocationManager.FUSED_PROVIDER)
            if (granted(Manifest.permission.ACCESS_FINE_LOCATION)) add(LocationManager.GPS_PROVIDER)
            add(LocationManager.NETWORK_PROVIDER)
            add(LocationManager.PASSIVE_PROVIDER)
        }
        var best: Location? = null
        for (name in names.distinct()) {
            val location = try {
                manager.getLastKnownLocation(name)
            } catch (_: Exception) {
                null
            } ?: continue
            if (best == null || location.time > best.time) best = location
        }
        return best?.let { LatLng(it.latitude, it.longitude) }
    }

    private fun providerEnabled(name: String): Boolean =
        try {
            manager.isProviderEnabled(name)
        } catch (_: Exception) {
            false
        }

    private fun granted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}
