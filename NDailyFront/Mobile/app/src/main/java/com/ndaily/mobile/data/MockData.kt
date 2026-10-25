package com.ndaily.mobile.data

object MockData {
    val places = listOf(
        PlaceSuggestion("1", "สยามพารากอน", "ถนนพระรามที่ 1, ปทุมวัน, กรุงเทพฯ", 13.7466, 100.5347),
        PlaceSuggestion("2", "เซ็นทรัลเวิลด์", "ถนนราชดำริ, ปทุมวัน, กรุงเทพฯ", 13.7467, 100.5395),
        PlaceSuggestion("3", "สนามบินสุวรรณภูมิ", "บางพลี, สมุทรปราการ", 13.6900, 100.7501),
        PlaceSuggestion("4", "สนามบินดอนเมือง", "ถนนวิภาวดีรังสิต, กรุงเทพฯ", 13.9126, 100.6068),
        PlaceSuggestion("5", "จตุจักร", "เขตจตุจักร, กรุงเทพฯ", 13.7996, 100.5504),
        PlaceSuggestion("6", "ไอคอนสยาม", "ถนนเจริญนคร, คลองสาน, กรุงเทพฯ", 13.7265, 100.5104),
        PlaceSuggestion("7", "สวนลุมพินี", "เขตปทุมวัน, กรุงเทพฯ", 13.7308, 100.5418),
        PlaceSuggestion("8", "มหาวิทยาลัยธรรมศาสตร์ ท่าพระจันทร์", "พระนคร, กรุงเทพฯ", 13.7563, 100.4900),
        PlaceSuggestion("9", "BTS สยาม", "ปทุมวัน, กรุงเทพฯ", 13.7456, 100.5341),
        PlaceSuggestion("10", "บ้าน", "ตำแหน่งตัวอย่าง", 13.7563, 100.5018),
        PlaceSuggestion("11", "Siam Paragon", "Rama I Rd, Pathum Wan, Bangkok", 13.7466, 100.5347),
        PlaceSuggestion("12", "Suvarnabhumi Airport", "Bang Phli, Samut Prakan", 13.6900, 100.7501),
        PlaceSuggestion("13", "Chatuchak Weekend Market", "Chatuchak, Bangkok", 13.7996, 100.5504),
        PlaceSuggestion("14", "CentralWorld", "Ratchadamri Rd, Bangkok", 13.7467, 100.5395),
        PlaceSuggestion("15", "Home", "Sample location", 13.7563, 100.5018),
    )

    val toilets = listOf(
        ToiletLocation(
            toiletUuid = "t1",
            placeToilet = "สยามพารากอน ชั้น 2",
            placeType = "mall",
            addressToilet = "991 ถนนพระรามที่ 1 ปทุมวัน กรุงเทพฯ",
            cleanToilet = true,
            freeToilet = true,
            accessibleToilet = true,
            tissueToilet = true,
            paperToilet = true,
            genderSplitToilet = true,
            avgScoreToilet = 4.6,
            latitudeToilet = 13.7468,
            longitudeToilet = 100.5349,
            openHoursToilet = "10:00-22:00",
        ),
        ToiletLocation(
            toiletUuid = "t2",
            placeToilet = "เซ็นทรัลเวิลด์ โซนอาหาร",
            placeType = "mall",
            addressToilet = "999/9 ถนนพระรามที่ 1 ปทุมวัน กรุงเทพฯ",
            cleanToilet = true,
            freeToilet = true,
            attendantToilet = true,
            diaperToilet = true,
            tissueToilet = true,
            avgScoreToilet = 4.2,
            latitudeToilet = 13.7469,
            longitudeToilet = 100.5392,
            openHoursToilet = "10:00-22:00",
        ),
        ToiletLocation(
            toiletUuid = "t3",
            placeToilet = "ปั๊มปตท. วิภาวดี",
            placeType = "gas",
            addressToilet = "ถนนวิภาวดีรังสิต จตุจักร กรุงเทพฯ",
            cleanToilet = true,
            freeToilet = false,
            tissueToilet = true,
            avgScoreToilet = 3.8,
            latitudeToilet = 13.7802,
            longitudeToilet = 100.5471,
            openHoursToilet = "00:00-23:59",
        ),
        ToiletLocation(
            toiletUuid = "t4",
            placeToilet = "ร้านอาหารตามสั่ง สยาม",
            placeType = "food",
            addressToilet = "ซอยสยาม ปทุมวัน กรุงเทพฯ",
            freeToilet = true,
            genderSplitToilet = true,
            avgScoreToilet = 3.1,
            latitudeToilet = 13.7449,
            longitudeToilet = 100.5330,
            openHoursToilet = "09:00-21:00",
        ),
    )

    val sampleRecents = listOf(
        SavedPlace("r1", "สยามพารากอน", "ถนนพระรามที่ 1, ปทุมวัน", 13.7466, 100.5347),
        SavedPlace("r2", "สนามบินสุวรรณภูมิ", "บางพลี, สมุทรปราการ", 13.6900, 100.7501),
        SavedPlace("r3", "จตุจักร", "เขตจตุจักร, กรุงเทพฯ", 13.7996, 100.5504),
    )

    val sampleFavorites = listOf(
        SavedPlace("f1", "บ้าน", "ตำแหน่งตัวอย่าง", 13.7563, 100.5018),
        SavedPlace("f2", "ออฟฟิศ", "เซ็นทรัลเวิลด์", 13.7467, 100.5395),
    )

    fun suggest(query: String): List<PlaceSuggestion> {
        val q = query.trim().lowercase()
        if (q.length < 2) return emptyList()
        return places.filter {
            it.name.lowercase().contains(q) || it.detail.lowercase().contains(q)
        }.distinctBy { "${it.name}|${it.lat}|${it.lng}" }.take(8)
    }
}
