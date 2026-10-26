package com.ndaily.mobile.data

data class LatLng(
    val lat: Double,
    val lng: Double,
)

data class UserSession(
    val email: String,
    val userName: String,
    val pictureUrl: String? = null,
    val authUuid: String = "",
)

data class UserProfile(
    val userName: String = "",
    val userPhone: String = "",
    val userBirthdate: String? = null,
    val userUuid: String = "",
)

enum class AppLocale { Th, En }

enum class PlanCode { Free, Plus, Pro }

data class PlanInfo(
    val code: PlanCode,
    val priceMonth: Int,
    val taxiQuota: Int,
    val nearwakeQuota: Int,
)

data class SavedPlace(
    val id: String,
    val name: String,
    val detail: String,
    val lat: Double,
    val lng: Double,
    val storeplaceUuid: String = "",
    val placeId: String = "",
)

data class PlaceSuggestion(
    val id: String,
    val name: String,
    val detail: String,
    val lat: Double,
    val lng: Double,
    val source: String = "",
    val placeId: String = id,
)

data class ToiletLocation(
    val toiletUuid: String,
    val placeToilet: String,
    val placeType: String,
    val addressToilet: String,
    val cleanToilet: Boolean = false,
    val freeToilet: Boolean = false,
    val accessibleToilet: Boolean = false,
    val attendantToilet: Boolean = false,
    val diaperToilet: Boolean = false,
    val genderSplitToilet: Boolean = false,
    val tissueToilet: Boolean = false,
    val paperToilet: Boolean = false,
    val photosToilet: List<String> = emptyList(),
    val avgScoreToilet: Double = 0.0,
    val latitudeToilet: Double,
    val longitudeToilet: Double,
    val openHoursToilet: String = "00:00-23:59",
)

data class ToiletDraft(
    val placeToilet: String = "",
    val placeType: String = "",
    val addressToilet: String = "",
    val openHoursToilet: String = "__:__-__:__",
    val cleanToilet: Boolean = false,
    val freeToilet: Boolean = false,
    val accessibleToilet: Boolean = false,
    val attendantToilet: Boolean = false,
    val diaperToilet: Boolean = false,
    val genderSplitToilet: Boolean = false,
    val tissueToilet: Boolean = false,
    val paperToilet: Boolean = false,
)

data class TrackingState(
    val dest: SavedPlace,
    val radiusMeters: Int,
    val fromFavorite: Boolean,
    val metersAway: Double? = null,
)

data class TaxiFareLine(
    val label: String,
    val amountBaht: Double,
)

data class DrivingRoute(
    val distanceKm: Double,
    val durationSeconds: Int? = null,
    val path: List<LatLng> = emptyList(),
)

data class TaxiFareResult(
    val distanceLines: List<TaxiFareLine>,
    val distanceSubtotal: Double,
    val trafficBaht: Double,
    val appSurcharge: Double,
    val airportSurcharge: Double,
    val totalBaht: Double,
)

data class AppUiState(
    val locale: AppLocale = AppLocale.Th,
    val ready: Boolean = false,
    val session: UserSession? = null,
    val profile: UserProfile = UserProfile(),
    val planCode: PlanCode = PlanCode.Free,
    val taxiUsed: Int = 0,
    val nearwakeUsed: Int = 0,
    val favorites: List<SavedPlace> = emptyList(),
    val recents: List<SavedPlace> = emptyList(),
    val toilets: List<ToiletLocation> = emptyList(),
    val savedToiletIds: Set<String> = emptySet(),
    val notifyVibration: Boolean = true,
    val notifySound: Boolean = true,
    val tracking: TrackingState? = null,
    val gps: LatLng? = null,
    val gpsDenied: Boolean = false,
    val toast: String = "",
) {
    val loggedIn: Boolean get() = session != null
}

object Plans {
    val all = listOf(
        PlanInfo(PlanCode.Free, 0, 30, 30),
        PlanInfo(PlanCode.Plus, 10, 100, 100),
        PlanInfo(PlanCode.Pro, 30, 500, 500),
    )

    fun of(code: PlanCode) = all.first { it.code == code }

    fun fromApi(value: String) = when (value.lowercase()) {
        "plus" -> PlanCode.Plus
        "pro" -> PlanCode.Pro
        else -> PlanCode.Free
    }

    fun toApi(code: PlanCode) = code.name.lowercase()

    fun radiusMax(code: PlanCode) = when (code) {
        PlanCode.Free -> 500
        PlanCode.Plus -> 3_000
        PlanCode.Pro -> 5_000
    }

    val radiusStops = listOf(500, 1_000, 2_000, 5_000)

    fun snapRadius(meters: Int, code: PlanCode): Int {
        val max = radiusMax(code)
        val allowed = radiusStops.filter { it <= max }.ifEmpty { listOf(radiusStops.first()) }
        return allowed.minBy { kotlin.math.abs(it - meters) }
    }
}
