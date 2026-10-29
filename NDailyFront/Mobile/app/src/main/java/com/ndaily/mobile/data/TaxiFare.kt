package com.ndaily.mobile.data

object TaxiFare {
    /** อัตราค่าแท็กซี่ กรมการขนส่งทางบก — คิดตามระยะจริง ไม่ประมาณการ */
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

    /** ประมาณการค่าโดยสาร Grab ตามอัตราเริ่มต้น + ตามระยะทาง */
    fun computeGrab(distanceKm: Double): TaxiFareResult {
        val dist = grabDistanceFare(distanceKm)
        return TaxiFareResult(
            distanceLines = dist.first,
            distanceSubtotal = dist.second,
            trafficBaht = 0.0,
            appSurcharge = 0.0,
            airportSurcharge = 0.0,
            totalBaht = dist.second,
        )
    }

    private fun distanceFare(distanceKm: Double): Pair<List<TaxiFareLine>, Double> {
        val lines = mutableListOf<TaxiFareLine>()
        if (distanceKm <= 0) return lines to 0.0
        var remaining = distanceKm
        var subtotal = 0.0
        lines += TaxiFareLine("ระยะทาง 1 กิโลเมตรแรก", 40.0)
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

        addSegment(9.0, 6.5, "ระยะเกิน 1 กม. ถึง กม.ที่ 10 (กม.ละ 6.50 บาท)")
        addSegment(10.0, 7.0, "ระยะเกิน 10 กม. ถึง กม.ที่ 20 (กม.ละ 7.00 บาท)")
        addSegment(20.0, 8.0, "ระยะเกิน 20 กม. ถึง กม.ที่ 40 (กม.ละ 8.00 บาท)")
        addSegment(20.0, 8.5, "ระยะเกิน 40 กม. ถึง กม.ที่ 60 (กม.ละ 8.50 บาท)")
        addSegment(20.0, 9.0, "ระยะเกิน 60 กม. ถึง กม.ที่ 80 (กม.ละ 9.00 บาท)")
        if (remaining > 0) {
            val amt = remaining * 10.5
            lines += TaxiFareLine("ระยะเกิน 80 กม.ขึ้นไป (กม.ละ 10.50 บาท)", amt)
            subtotal += amt
        }
        return lines to subtotal
    }

    private fun grabDistanceFare(distanceKm: Double): Pair<List<TaxiFareLine>, Double> {
        val lines = mutableListOf<TaxiFareLine>()
        if (distanceKm <= 0) return lines to 0.0
        var remaining = distanceKm
        var subtotal = 0.0
        lines += TaxiFareLine("อัตราเริ่มต้น (0–2 กม.แรก)", 45.0)
        subtotal += 45
        remaining -= 2
        if (remaining <= 0) return lines to subtotal

        fun addSegment(maxKm: Double, rate: Double, label: String) {
            if (remaining <= 0) return
            val use = minOf(remaining, maxKm)
            val amt = use * rate
            lines += TaxiFareLine(label, amt)
            subtotal += amt
            remaining -= use
        }

        addSegment(4.0, 8.0, "กม.ที่ 2–6 (กม.ละ 8 บาท)")
        addSegment(34.0, 7.0, "กม.ที่ 6–40 (กม.ละ 7 บาท)")
        if (remaining > 0) {
            val amt = remaining * 10.0
            lines += TaxiFareLine("เกิน 40 กม. (กม.ละ 10 บาท)", amt)
            subtotal += amt
        }
        return lines to subtotal
    }

    fun money(value: Double): String = "%,.2f".format(value)
}
