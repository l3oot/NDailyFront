package com.ndaily.mobile.i18n

import android.content.Context
import com.ndaily.mobile.data.AppLocale
import org.json.JSONObject

class Strings(context: Context) {
    private val th = load(context, "th.json")
    private val en = load(context, "en.json")

    fun t(locale: AppLocale, key: String): String {
        val table = if (locale == AppLocale.Th) th else en
        return table[key] ?: key
    }

    private fun load(context: Context, file: String): Map<String, String> {
        val raw = context.assets.open("i18n/$file").bufferedReader().use { it.readText() }
        val obj = JSONObject(raw)
        return buildMap {
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                put(key, obj.getString(key))
            }
        }
    }
}
