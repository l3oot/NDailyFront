package com.ndaily.mobile.data

import kotlin.math.round

object TaxiFare {
    fun compute(
        distanceKm: Double,
        trafficMinutes: Int = 0,
        appBookingSurcharge: Boolean = false,
        airportSurcharge: Boolean = false,
    ): TaxiFareResult {
        val dist = distanceFare(distanceKm)
        val trafficBaht = maxOf(0, trafficMinutes) * 3.0
        val app = if (appBookingSurcharge) 20.0 else 0.0
        val airport = if (airportSurcharge) 50.0 else 0.0
        return TaxiFareResult(
            distanceLines = dist.first,
            distanceSubtotal = dist.second,
            trafficBaht = trafficBaht,
            appSurcharge = app,
            airportSurcharge = airport,
            totalBaht = dist.second + trafficBaht + app + airport,
        )
    }

    private fun distanceFare(distanceKm: Double): Pair<List<TaxiFareLine>, Double> {
        val lines = mutableListOf<TaxiFareLine>()
        if (distanceKm <= 0) return lines to 0.0
        var remaining = distanceKm
        var subtotal = 0.0
        lines += TaxiFareLine("กิโลเมตรแรก (ไม่เกิน 1 กม.)", 40.0)
        subtotal += 40
        remaining -= 1
        if (remaining <= 0) return lines to subtotal

        fun addSegment(maxKm: Double, rate: Double, label: String) {
            if (remaining <= 0) return
            val use = minOf(remaining, maxKm)
            val amt = use * rate
            lines += TaxiFareLine(label, amt)
            subtotal += amt
            remaining -= use
        }

        addSegment(9.0, 6.5, "กม.ที่ 2–10 (กม.ละ 6.50 บาท)")
        addSegment(10.0, 7.0, "กม.ที่ 11–20 (กม.ละ 7.00 บาท)")
        addSegment(20.0, 8.0, "กม.ที่ 21–40 (กม.ละ 8.00 บาท)")
        addSegment(20.0, 8.5, "กม.ที่ 41–60 (กม.ละ 8.50 บาท)")
        addSegment(20.0, 9.0, "กม.ที่ 61–80 (กม.ละ 9.00 บาท)")
        if (remaining > 0) {
            val amt = remaining * 10.5
            lines += TaxiFareLine("เกิน 80 กม. (กม.ละ 10.50 บาท)", amt)
            subtotal += amt
        }
        return lines to subtotal
    }

    fun estimateBand(distanceKm: Double): TaxiFareBand {
        val base = compute(distanceKm).distanceSubtotal
        val low = round10(base)
        val high = round10(maxOf(base * 1.35, base + 30.0))
        return TaxiFareBand(
            taxiLow = low,
            taxiHigh = high,
            otherLow = round10(low * 1.25),
            otherHigh = round10(high * 1.25),
        )
    }

    fun money(value: Double): String = "%,.2f".format(value)

    private fun round10(value: Double) = (round(value / 10.0) * 10).toInt().coerceAtLeast(0)
}

data class TaxiFareBand(
    val taxiLow: Int,
    val taxiHigh: Int,
    val otherLow: Int,
    val otherHigh: Int,
)
