package com.ndaily.mobile

import android.Manifest
import android.content.Intent
import android.os.Bundle
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

    private val permission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { granted ->
        if (granted.values.any { it }) tracker.start() else (application as DailyApp).store.setGps(null, true)
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
        if (tracker.hasPermission()) {
            tracker.start()
        } else {
            permission.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
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
        if (::tracker.isInitialized && tracker.hasPermission()) tracker.start()
    }

    override fun onPause() {
        super.onPause()
        if (::tracker.isInitialized) tracker.stop()
    }
}
