package com.ndaily.mobile

import android.app.Application
import com.ndaily.mobile.data.AppStore
import com.ndaily.mobile.data.LocalStringsHolder
import com.ndaily.mobile.i18n.Strings
import org.osmdroid.config.Configuration

class DailyApp : Application() {
    lateinit var store: AppStore
        private set

    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = packageName
        LocalStringsHolder.strings = Strings(this)
        store = AppStore(this)
    }
}
