package com.ndaily.mobile

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ndaily.mobile.data.AppStore
import com.ndaily.mobile.data.LocationTracker
import com.ndaily.mobile.ui.nav.AppNav
import com.ndaily.mobile.ui.theme.DailyTheme

class MainActivity : ComponentActivity() {
    private lateinit var tracker: LocationTracker
    private var askedPermission = false
    private var promptedLocationSettings = false

    private val permission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { granted ->
        if (granted.values.any { it }) beginTracking() else (application as DailyApp).store.setGps(null, true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = (application as DailyApp).store
        handleGoogleIntent(intent, store)
        tracker = LocationTracker(this) { point, denied -> store.setGps(point, denied) }
        enableEdgeToEdge()
        setContent {
            val state by store.state.collectAsState()
            DailyTheme {
                AppNav(store = store, state = state)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleGoogleIntent(intent, (application as DailyApp).store)
    }

    private fun handleGoogleIntent(intent: Intent?, store: AppStore) {
        val data = intent?.data ?: return
        if (data.scheme != "ndaily" || data.host != "auth" || data.path != "/google") return
        val ticket = data.getQueryParameter("ticket").orEmpty()
        val error = data.getQueryParameter("error").orEmpty()
        if (ticket.isNotBlank()) store.submitGoogleTicket(ticket)
        else if (error == "missing") store.toast(store.t("googleNotConfigured"))
        else if (error.isNotBlank()) store.toast(store.t("googleSignInFailed"))
        intent.data = null
    }

    override fun onResume() {
        super.onResume()
        if (!::tracker.isInitialized) return
        if (tracker.hasPermission()) {
            beginTracking()
            return
        }
        if (askedPermission) return
        askedPermission = true
        window.decorView.post {
            if (isFinishing || isDestroyed) return@post
            if (tracker.hasPermission()) {
                beginTracking()
                return@post
            }
            val prefs = getSharedPreferences("ndaily.frontend", MODE_PRIVATE)
            val askedBefore = prefs.getBoolean("asked_location", false)
            val canAskAgain = shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (askedBefore && !canAskAgain) {
                startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", packageName, null)
                    },
                )
                return@post
            }
            prefs.edit().putBoolean("asked_location", true).apply()
            permission.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    override fun onPause() {
        super.onPause()
        if (::tracker.isInitialized) tracker.stop()
    }

    private fun beginTracking() {
        if (!tracker.locationServicesEnabled()) {
            (application as DailyApp).store.setGps(null, false)
            if (!promptedLocationSettings) {
                promptedLocationSettings = true
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            return
        }
        tracker.start()
    }
}
