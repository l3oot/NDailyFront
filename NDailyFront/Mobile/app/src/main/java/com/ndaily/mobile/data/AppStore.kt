package com.ndaily.mobile.data

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.ndaily.mobile.data.api.ApiClient
import com.ndaily.mobile.data.api.ApiResult
import com.ndaily.mobile.data.api.TokenStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class AppStore(app: Application) {
    private val prefs = app.getSharedPreferences("ndaily.frontend", Context.MODE_PRIVATE)
    private val tokens = TokenStore(app)
    private val api = ApiClient(tokens)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val handler = Handler(Looper.getMainLooper())
    private val clearToast = Runnable { update { copy(toast = "") } }
    private val _state = MutableStateFlow(loadLocal())
    val state: StateFlow<AppUiState> = _state.asStateFlow()
    private val _googleTicket = MutableStateFlow("")
    val googleTicket: StateFlow<String> = _googleTicket.asStateFlow()

    fun t(key: String) = LocalStringsHolder.t(_state.value.locale, key)

    fun setLocale(locale: AppLocale) {
        update { copy(locale = locale) }
        persistLocal()
    }

    fun markReady() {
        update { copy(ready = true) }
    }

    suspend fun restoreSession() {
        if (!tokens.hasSession) {
            markReady()
            return
        }
        val me = api.get("/api/v1/auth/me")
        val profileJson = me.obj()
        if (!me.ok || profileJson == null) {
            tokens.clear()
            update { copy(session = null, profile = UserProfile()) }
            markReady()
            return
        }
        val authUuid = profileJson.optString("authUuid").ifBlank { tokens.authUuid }
        val email = profileJson.optString("email")
        val userName = profileJson.optString("userName")
        applySession(
            UserSession(
                email = email,
                userName = userName.ifBlank { email.substringBefore("@") },
                pictureUrl = profileJson.optString("pictureUrl").ifBlank { null },
                authUuid = authUuid,
            ),
        )
        refreshAccount(authUuid)
        markReady()
    }

    data class SendOtpOutcome(val ok: Boolean, val debugOtp: String? = null, val message: String = "")

    suspend fun sendOtp(email: String): SendOtpOutcome {
        val res = api.post("/api/v1/auth/send-otp", JSONObject().put("email", email), auth = false)
        if (!res.ok) return SendOtpOutcome(false, message = res.message.ifBlank { t("sendOtpFailed") })
        return SendOtpOutcome(true, debugOtp = res.obj()?.optString("debugOtp")?.ifBlank { null })
    }

    suspend fun verifyOtp(email: String, otp: String): String? {
        val res = api.post(
            "/api/v1/auth/verify-otp",
            JSONObject().put("email", email).put("otp", otp),
            auth = false,
        )
        return acceptSession(res, email)
    }

    fun submitGoogleTicket(ticket: String) {
        if (ticket.isNotBlank()) _googleTicket.value = ticket
    }

    fun clearGoogleTicket() {
        _googleTicket.value = ""
    }

    suspend fun exchangeGoogleTicket(ticket: String): String? {
        val res = api.post(
            "/api/v1/auth/google/exchange",
            JSONObject().put("ticket", ticket),
            auth = false,
        )
        val error = acceptSession(res)
        if (error != null) toast(error)
        return error
    }

    fun signOut() {
        val refresh = tokens.refreshToken
        tokens.clear()
        update { copy(session = null, tracking = null, profile = UserProfile(), favorites = emptyList(), recents = emptyList()) }
        persistLocal()
        if (refresh.isBlank()) return
        scope.launch {
            api.post("/api/v1/auth/logout", JSONObject().put("refreshToken", refresh), auth = false)
        }
    }

    suspend fun saveProfile(name: String, phone: String, birth: String?): Boolean {
        val authUuid = _state.value.session?.authUuid.ifBlankCompat { tokens.authUuid }
        if (authUuid.isBlank()) {
            toast(t("loginFailed"))
            return false
        }
        val body = JSONObject()
            .put("userName", name)
            .put("userPhone", phone)
            .put("userBirthdate", birth.orEmpty())
        val existing = _state.value.profile.userUuid
        val res = if (existing.isBlank()) {
            api.post("/api/v1/users", body)
        } else {
            api.put("/api/v1/users", body.put("userUuid", existing))
        }
        val data = res.obj()
        if (!res.ok || data == null) {
            toast(res.message.ifBlank { t("cannotReachServer") })
            return false
        }
        update {
            copy(
                profile = UserProfile(
                    userName = data.optString("userName", name),
                    userPhone = data.optString("userPhone", phone),
                    userBirthdate = data.optString("userBirthdate").ifBlank { birth },
                    userUuid = data.optString("userUuid", existing),
                ),
            )
        }
        toast(t("accountProfileSaved"))
        return true
    }

    fun selectPlan(code: PlanCode) {
        scope.launch {
            val res = api.put("/api/v1/plans", JSONObject().put("planCode", Plans.toApi(code)))
            if (!res.ok) {
                toast(res.message.ifBlank { t("cannotReachServer") })
                return@launch
            }
            applyPlan(res.obj())
            toast(t("planChanged"))
        }
    }

    suspend fun consumeTaxi(): Boolean = consumeFeature("taxi", t("planQuotaExceededTaxi"))

    private suspend fun consumeNearwake(): Boolean = consumeFeature("nearwake", t("planQuotaExceededNearwake"))

    suspend fun searchPlaces(query: String): List<PlaceSuggestion> {
        val q = query.trim()
        if (q.length < 2) return emptyList()
        val res = api.get("/api/v1/places/search", mapOf("input" to q), auth = false)
        if (!res.ok) return emptyList()
        val rows = res.obj()?.optJSONArray("suggestions") ?: return emptyList()
        return buildList {
            for (i in 0 until rows.length()) {
                val row = rows.optJSONObject(i) ?: continue
                val lat = row.optDouble("lat", Double.NaN)
                val lon = row.optDouble("lon", Double.NaN)
                add(
                    PlaceSuggestion(
                        id = row.optString("placeId"),
                        name = row.optString("text"),
                        detail = row.optString("detail"),
                        lat = if (lat.isNaN()) 0.0 else lat,
                        lng = if (lon.isNaN()) 0.0 else lon,
                        source = row.optString("source"),
                        placeId = row.optString("placeId"),
                    ),
                )
            }
        }
    }

    suspend fun pickPlace(item: PlaceSuggestion): SavedPlace? {
        val body = JSONObject()
            .put("displayText", item.name)
            .put("placeId", item.placeId)
            .put("placeDetail", item.detail)
            .put("source", item.source)
            .put("persist", _state.value.loggedIn)
        if (item.lat != 0.0 || item.lng != 0.0) {
            body.put("knownLat", item.lat).put("knownLon", item.lng)
        }
        val res = api.post("/api/v1/places/resolve", body, auth = _state.value.loggedIn)
        val data = res.obj()
        val lat = data?.optDouble("lat") ?: item.lat
        val lng = data?.optDouble("lon") ?: item.lng
        if ((!res.ok && lat == 0.0 && lng == 0.0) || (lat == 0.0 && lng == 0.0)) {
            toast(res.message.ifBlank { t("cannotReachServer") })
            return null
        }
        var storeplaceUuid = data?.optString("storeplaceUuid").orEmpty()
        val name = data?.optString("name")?.ifBlank { item.name } ?: item.name
        if (_state.value.loggedIn && storeplaceUuid.isBlank()) {
            storeplaceUuid = persistStorePlace(item.placeId, name, item.detail, lat, lng)
        }
        val place = SavedPlace(
            id = storeplaceUuid.ifBlank { item.id },
            name = name,
            detail = item.detail,
            lat = lat,
            lng = lng,
            storeplaceUuid = storeplaceUuid,
            placeId = item.placeId,
        )
        rememberRecent(place)
        return place
    }

    fun addRecent(place: SavedPlace) {
        scope.launch { rememberRecent(place) }
    }

    fun addFavorite(place: SavedPlace) {
        scope.launch {
            val storeplaceUuid = place.storeplaceUuid.ifBlank {
                persistStorePlace(place.placeId, place.name, place.detail, place.lat, place.lng)
            }
            if (storeplaceUuid.isBlank()) {
                toast(t("cannotReachServer"))
                return@launch
            }
            val res = api.post(
                "/api/v1/nearwake/favoriteplace",
                JSONObject()
                    .put("favoritePlaceName", place.name)
                    .put("storeplaceUuid", storeplaceUuid),
            )
            if (!res.ok) {
                toast(res.message.ifBlank { t("favoritePlaceAlreadySaved") })
                return@launch
            }
            loadPlaces()
            toast(t("favoritePlaceSaved"))
        }
    }

    fun renameFavorite(id: String, name: String) {
        scope.launch {
            val res = api.put(
                "/api/v1/nearwake/favoriteplace",
                JSONObject().put("favoritePlaceUuid", id).put("favoritePlaceName", name),
            )
            if (!res.ok) {
                toast(res.message.ifBlank { t("cannotReachServer") })
                return@launch
            }
            update { copy(favorites = favorites.map { if (it.id == id) it.copy(name = name) else it }) }
            toast(t("favoritePlaceRenamed"))
        }
    }

    fun removeFavorite(id: String) {
        scope.launch {
            val res = api.delete("/api/v1/nearwake/favoriteplace/$id")
            if (!res.ok) {
                toast(res.message.ifBlank { t("cannotReachServer") })
                return@launch
            }
            update { copy(favorites = favorites.filterNot { it.id == id }) }
        }
    }

    fun removeRecent(id: String) {
        scope.launch {
            val res = api.delete("/api/v1/nearwake/recentplace/$id")
            if (!res.ok) {
                toast(res.message.ifBlank { t("cannotReachServer") })
                return@launch
            }
            update { copy(recents = recents.filterNot { it.id == id }) }
        }
    }

    suspend fun loadToilets(center: LatLng) {
        val delta = 0.25
        val res = api.get(
            "/api/v1/toilet/location",
            mapOf(
                "minLat" to (center.lat - delta).toString(),
                "maxLat" to (center.lat + delta).toString(),
                "minLng" to (center.lng - delta).toString(),
                "maxLng" to (center.lng + delta).toString(),
            ),
            auth = false,
        )
        val rows = res.arr() ?: return
        update { copy(toilets = parseToilets(rows)) }
    }

    suspend fun addToilet(toilet: ToiletLocation): Boolean {
        val res = api.post("/api/v1/toilet/location", toiletJson(toilet))
        val data = res.obj()
        if (!res.ok || data == null) {
            toast(res.message.ifBlank { t("cannotReachServer") })
            return false
        }
        val saved = parseToilet(data) ?: toilet
        update { copy(toilets = listOf(saved) + toilets.filterNot { it.toiletUuid == saved.toiletUuid }) }
        toast(t("toiletSaveSuccess"))
        return true
    }

    suspend fun drivingKm(origin: LatLng, dest: LatLng): Double? {
        val res = api.post(
            "/api/v1/ggmap/route",
            JSONObject()
                .put("originLat", origin.lat)
                .put("originLng", origin.lng)
                .put("destLat", dest.lat)
                .put("destLng", dest.lng),
            auth = false,
        )
        val km = res.obj()?.optDouble("distanceKm", Double.NaN) ?: Double.NaN
        return if (km.isNaN()) null else km
    }

    fun toggleSavedToilet(id: String) {
        val removing = id in _state.value.savedToiletIds
        update {
            copy(savedToiletIds = if (removing) savedToiletIds - id else savedToiletIds + id)
        }
        persistLocal()
        toast(if (removing) t("toiletUnbookmarked") else t("toiletBookmarked"))
    }

    fun setNotify(vibration: Boolean? = null, sound: Boolean? = null) {
        update {
            copy(
                notifyVibration = vibration ?: notifyVibration,
                notifySound = sound ?: notifySound,
            )
        }
        persistLocal()
    }

    fun setGps(point: LatLng?, denied: Boolean = false) {
        update {
            val nextTracking = tracking?.let { track ->
                val away = point?.let { Geo.distanceMeters(it, LatLng(track.dest.lat, track.dest.lng)) }
                track.copy(metersAway = away)
            }
            copy(gps = point, gpsDenied = denied, tracking = nextTracking)
        }
    }

    fun startTracking(dest: SavedPlace, radiusMeters: Int, fromFavorite: Boolean) {
        scope.launch {
            if (!consumeNearwake()) return@launch
            update {
                copy(
                    tracking = TrackingState(
                        dest = dest,
                        radiusMeters = radiusMeters,
                        fromFavorite = fromFavorite,
                        metersAway = gps?.let { Geo.distanceMeters(it, LatLng(dest.lat, dest.lng)) },
                    ),
                )
            }
        }
    }

    fun stopTracking() {
        update { copy(tracking = null) }
    }

    fun setTrackingRadius(meters: Int) {
        update { copy(tracking = tracking?.copy(radiusMeters = meters)) }
    }

    fun taxiQuota() = Plans.of(_state.value.planCode).taxiQuota

    fun nearwakeQuota() = Plans.of(_state.value.planCode).nearwakeQuota

    fun toast(message: String) {
        update { copy(toast = message) }
        handler.removeCallbacks(clearToast)
        handler.postDelayed(clearToast, 2200)
    }

    private suspend fun acceptSession(res: ApiResult, fallbackEmail: String = ""): String? {
        val data = res.obj()
        if (!res.ok || data == null) return res.message.ifBlank { t("loginFailed") }
        val access = data.optString("token")
        val refresh = data.optString("refreshToken")
        val authUuid = data.optString("authUuid")
        if (access.isBlank() || refresh.isBlank()) return t("loginFailed")
        tokens.save(access, refresh, authUuid)
        val email = fallbackEmail.ifBlank { data.optString("email") }
        applySession(
            UserSession(
                email = email,
                userName = email.substringBefore("@"),
                pictureUrl = data.optString("pictureUrl").ifBlank { null },
                authUuid = authUuid,
            ),
        )
        refreshAccount(authUuid)
        return null
    }

    private suspend fun refreshAccount(authUuid: String) {
        val user = api.get("/api/v1/users/$authUuid")
        user.obj()?.let { data ->
            update {
                copy(
                    profile = UserProfile(
                        userName = data.optString("userName"),
                        userPhone = data.optString("userPhone"),
                        userBirthdate = data.optString("userBirthdate").ifBlank { null },
                        userUuid = data.optString("userUuid"),
                    ),
                    session = session?.copy(userName = data.optString("userName").ifBlank { session.userName }),
                )
            }
        }
        val plans = api.get("/api/v1/plans")
        applyPlan(plans.obj())
        loadPlaces()
    }

    private fun applySession(session: UserSession) {
        update { copy(session = session) }
    }

    private fun applyPlan(data: JSONObject?) {
        val mine = data?.optJSONObject("mine") ?: return
        val code = Plans.fromApi(mine.optString("planCode"))
        val usage = mine.optJSONObject("usage")
        val taxi = usage?.optJSONObject("taxi")?.optInt("used") ?: 0
        val nearwake = usage?.optJSONObject("nearwake")?.optInt("used") ?: 0
        update { copy(planCode = code, taxiUsed = taxi, nearwakeUsed = nearwake) }
    }

    private suspend fun consumeFeature(feature: String, quotaMessage: String): Boolean {
        val res = api.post("/api/v1/plans/usage", JSONObject().put("feature", feature))
        if (res.errorType == 8 || res.status == 429 && res.errorType == 8) {
            toast(quotaMessage)
            return false
        }
        if (!res.ok) {
            toast(res.message.ifBlank { t("cannotReachServer") })
            return false
        }
        val used = res.obj()?.optInt("used") ?: 0
        update {
            if (feature == "taxi") copy(taxiUsed = used) else copy(nearwakeUsed = used)
        }
        return true
    }

    private suspend fun persistStorePlace(
        placeId: String,
        name: String,
        detail: String,
        lat: Double,
        lng: Double,
    ): String {
        val res = api.post(
            "/api/v1/nearwake/location",
            JSONObject()
                .put("placeId", placeId)
                .put("placeName", name)
                .put("placeDetail", detail)
                .put("placeLat", lat)
                .put("placeLon", lng),
        )
        return res.obj()?.optString("storeplaceUuid").orEmpty()
    }

    private suspend fun rememberRecent(place: SavedPlace) {
        if (!_state.value.loggedIn) return
        val storeplaceUuid = place.storeplaceUuid.ifBlank {
            persistStorePlace(place.placeId, place.name, place.detail, place.lat, place.lng)
        }
        if (storeplaceUuid.isBlank()) return
        api.post(
            "/api/v1/nearwake/recentplace",
            JSONObject()
                .put("recentPlaceName", place.name)
                .put("storeplaceUuid", storeplaceUuid),
        )
        loadPlaces()
    }

    private suspend fun loadPlaces() {
        if (!_state.value.loggedIn) return
        val fav = api.get("/api/v1/nearwake/favoriteplace")
        val rec = api.get("/api/v1/nearwake/recentplace")
        update {
            copy(
                favorites = if (fav.ok) parsePlaces(fav.arr(), favorite = true) else favorites,
                recents = if (rec.ok) parsePlaces(rec.arr(), favorite = false) else recents,
            )
        }
    }

    private fun parsePlaces(rows: JSONArray?, favorite: Boolean): List<SavedPlace> {
        if (rows == null) return emptyList()
        return buildList {
            for (i in 0 until rows.length()) {
                val row = rows.optJSONObject(i) ?: continue
                val id = if (favorite) row.optString("favoritePlaceUuid") else row.optString("recentPlaceUuid")
                val custom = if (favorite) row.optString("favoritePlaceName") else row.optString("recentPlaceName")
                add(
                    SavedPlace(
                        id = id,
                        name = custom.ifBlank { row.optString("placeName") },
                        detail = row.optString("placeDetail"),
                        lat = row.optDouble("placeLat"),
                        lng = row.optDouble("placeLon"),
                        storeplaceUuid = row.optString("storeplaceUuid"),
                        placeId = row.optString("placeId"),
                    ),
                )
            }
        }
    }

    private fun parseToilets(rows: JSONArray) = buildList {
        for (i in 0 until rows.length()) {
            parseToilet(rows.optJSONObject(i) ?: continue)?.let { add(it) }
        }
    }

    private fun parseToilet(row: JSONObject): ToiletLocation? {
        val lat = row.optDouble("latitudeToilet", Double.NaN)
        val lng = row.optDouble("longitudeToilet", Double.NaN)
        if (lat.isNaN() || lng.isNaN()) return null
        val photos = row.optJSONArray("photosToilet")
        return ToiletLocation(
            toiletUuid = row.optString("toiletUuid"),
            placeToilet = row.optString("placeToilet"),
            placeType = row.optString("placeType"),
            addressToilet = row.optString("addressToilet"),
            cleanToilet = row.optBoolean("cleanToilet"),
            freeToilet = row.optBoolean("freeToilet"),
            accessibleToilet = row.optBoolean("accessibleToilet"),
            attendantToilet = row.optBoolean("attendantToilet"),
            diaperToilet = row.optBoolean("diaperToilet"),
            genderSplitToilet = row.optBoolean("genderSplitToilet"),
            tissueToilet = row.optBoolean("tissueToilet"),
            paperToilet = row.optBoolean("paperToilet"),
            photosToilet = if (photos == null) emptyList() else buildList {
                for (i in 0 until photos.length()) add(photos.optString(i))
            },
            avgScoreToilet = row.optDouble("avgScoreToilet"),
            latitudeToilet = lat,
            longitudeToilet = lng,
            openHoursToilet = row.optString("openHoursToilet").ifBlank { "00:00-23:59" }.let {
                if (it == "-") "00:00-23:59" else it
            },
        )
    }

    private fun toiletJson(toilet: ToiletLocation) = JSONObject()
        .put("placeToilet", toilet.placeToilet)
        .put("placeType", toilet.placeType)
        .put("addressToilet", toilet.addressToilet)
        .put("cleanToilet", toilet.cleanToilet)
        .put("freeToilet", toilet.freeToilet)
        .put("accessibleToilet", toilet.accessibleToilet)
        .put("attendantToilet", toilet.attendantToilet)
        .put("diaperToilet", toilet.diaperToilet)
        .put("genderSplitToilet", toilet.genderSplitToilet)
        .put("tissueToilet", toilet.tissueToilet)
        .put("paperToilet", toilet.paperToilet)
        .put("photosToilet", JSONArray(toilet.photosToilet))
        .put("latitudeToilet", toilet.latitudeToilet)
        .put("longitudeToilet", toilet.longitudeToilet)
        .put("openHoursToilet", toilet.openHoursToilet)

    private fun update(block: AppUiState.() -> AppUiState) {
        _state.update { it.block() }
    }

    private fun persistLocal() {
        val s = _state.value
        prefs.edit()
            .putString("locale", s.locale.name)
            .putString("savedToilets", JSONArray(s.savedToiletIds.toList()).toString())
            .putBoolean("notifyVibration", s.notifyVibration)
            .putBoolean("notifySound", s.notifySound)
            .apply()
    }

    private fun loadLocal(): AppUiState {
        val locale = runCatching {
            AppLocale.valueOf(prefs.getString("locale", AppLocale.Th.name)!!)
        }.getOrDefault(AppLocale.Th)
        return AppUiState(
            locale = locale,
            toilets = emptyList(),
            savedToiletIds = readIds(prefs.getString("savedToilets", null)),
            notifyVibration = prefs.getBoolean("notifyVibration", true),
            notifySound = prefs.getBoolean("notifySound", true),
        )
    }

    private fun readIds(raw: String?): Set<String> {
        if (raw.isNullOrBlank()) return emptySet()
        val arr = JSONArray(raw)
        return buildSet {
            for (i in 0 until arr.length()) add(arr.getString(i))
        }
    }
}

private fun String?.ifBlankCompat(fallback: () -> String) = this?.ifBlank(fallback) ?: fallback()

object LocalStringsHolder {
    lateinit var strings: com.ndaily.mobile.i18n.Strings
    fun t(locale: AppLocale, key: String) = strings.t(locale, key)
}
