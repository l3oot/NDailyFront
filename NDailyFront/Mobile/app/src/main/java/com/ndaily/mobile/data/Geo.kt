package com.ndaily.mobile.data

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

object Geo {
    val Bangkok = LatLng(13.7563, 100.5018)

    fun decodePolyline(encoded: String): List<LatLng> {
        if (encoded.isBlank()) return emptyList()
        val points = mutableListOf<LatLng>()
        var index = 0
        var lat = 0
        var lng = 0
        fun read(): Int {
            var shift = 0
            var result = 0
            var byte: Int
            do {
                if (index >= encoded.length) return 0
                byte = encoded[index++].code - 63
                result = result or (byte and 0x1f shl shift)
                shift += 5
            } while (byte >= 0x20 && index <= encoded.length)
            return if (result and 1 != 0) (result shr 1).inv() else result shr 1
        }
        while (index < encoded.length) {
            lat += read()
            lng += read()
            points += LatLng(lat / 1e5, lng / 1e5)
        }
        return points
    }

    fun haversineKm(a: LatLng, b: LatLng): Double {
        val r = 6371.0
        val dLat = Math.toRadians(b.lat - a.lat)
        val dLng = Math.toRadians(b.lng - a.lng)
        val s = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(a.lat)) * cos(Math.toRadians(b.lat)) *
            sin(dLng / 2) * sin(dLng / 2)
        return 2 * r * asin(min(1.0, sqrt(s)))
    }

    fun distanceMeters(a: LatLng, b: LatLng) = haversineKm(a, b) * 1000

    fun formatDistance(meters: Double, kmLabel: String, mLabel: String): String {
        if (meters < 1000) return "${round(meters).toInt()} $mLabel"
        return "${"%.1f".format(meters / 1000)} $kmLabel"
    }

    fun formatAlarmRadius(meters: Int, kmLabel: String, mLabel: String): String {
        if (meters < 1000) return "$meters $mLabel"
        val km = meters / 1000.0
        val text = if (km == km.toInt().toDouble()) km.toInt().toString() else "%.1f".format(km)
        return "$text $kmLabel"
    }

    fun formatStraight(meters: Double, kmLabel: String, mLabel: String): String {
        if (meters < 1000) return "${maxOf(1, round(meters).toInt())} $mLabel"
        val km = meters / 1000
        val rounded = if (km >= 10) round(km) else round(km * 10) / 10
        val text = if (rounded == rounded.toInt().toDouble()) rounded.toInt().toString() else "%.1f".format(rounded)
        return "$text $kmLabel"
    }

    fun formatToiletDistance(meters: Double, locale: AppLocale): String {
        if (meters < 1000) {
            val rounded = maxOf(50, (round(meters / 50) * 50).toInt())
            return if (locale == AppLocale.Th) "$rounded ม." else "$rounded m"
        }
        val km = meters / 1000
        val label = if (km < 10) "%.1f".format(km) else round(km).toInt().toString()
        return if (locale == AppLocale.Th) "$label กม." else "$label km"
    }
}
