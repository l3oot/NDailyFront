package com.ndaily.mobile.data.api

import android.content.Context

class TokenStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("ndaily.auth", Context.MODE_PRIVATE)

    var accessToken: String
        get() = prefs.getString("access", "").orEmpty()
        set(value) { prefs.edit().putString("access", value).apply() }

    var refreshToken: String
        get() = prefs.getString("refresh", "").orEmpty()
        set(value) { prefs.edit().putString("refresh", value).apply() }

    var authUuid: String
        get() = prefs.getString("authUuid", "").orEmpty()
        set(value) { prefs.edit().putString("authUuid", value).apply() }

    fun save(access: String, refresh: String, authUuid: String) {
        prefs.edit()
            .putString("access", access)
            .putString("refresh", refresh)
            .putString("authUuid", authUuid)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    val hasSession: Boolean get() = refreshToken.isNotBlank()
}
