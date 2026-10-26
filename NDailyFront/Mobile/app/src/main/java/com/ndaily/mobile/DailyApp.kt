package com.ndaily.mobile

import android.app.Application
import com.ndaily.mobile.data.AppStore
import com.ndaily.mobile.data.LocalStringsHolder
import com.ndaily.mobile.i18n.Strings
import java.io.File
import org.osmdroid.config.Configuration

class DailyApp : Application() {
    lateinit var store: AppStore
        private set

    override fun onCreate() {
        super.onCreate()
        val osm = Configuration.getInstance()
        osm.load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        val base = File(cacheDir, "osmdroid").apply { mkdirs() }
        val tiles = File(base, "tiles").apply { mkdirs() }
        osm.osmdroidBasePath = base
        osm.osmdroidTileCache = tiles
        osm.userAgentValue = "NDaily/0.1.0 ($packageName)"
        LocalStringsHolder.strings = Strings(this)
        store = AppStore(this)
    }
}
