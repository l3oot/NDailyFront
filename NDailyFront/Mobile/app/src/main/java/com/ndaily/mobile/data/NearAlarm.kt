package com.ndaily.mobile.data

import android.app.Application

class NearAlarm(private val app: Application) {
    private var active = false
    private var allowVibration = true
    private var allowSound = true

    fun configure(vibration: Boolean, sound: Boolean) {
        allowVibration = vibration
        allowSound = sound
        if (!active) return
        NearAlarmService.configure(app, vibration, sound)
    }

    fun start(title: String, body: String) {
        active = true
        NearAlarmService.start(app, title, body, allowVibration, allowSound)
    }

    fun stop() {
        if (!active) {
            NearAlarmService.stop(app)
            return
        }
        active = false
        NearAlarmService.stop(app)
    }

    val isActive: Boolean get() = active
}
