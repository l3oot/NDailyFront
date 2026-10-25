package com.ndaily.mobile.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Accessible
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Wc
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ndaily.mobile.R
import com.ndaily.mobile.data.AppStore
import com.ndaily.mobile.data.AppUiState
import com.ndaily.mobile.data.Geo
import com.ndaily.mobile.data.LatLng
import com.ndaily.mobile.data.ToiletDraft
import com.ndaily.mobile.data.ToiletLocation
import com.ndaily.mobile.data.displayHours
import com.ndaily.mobile.data.hoursForPayload
import com.ndaily.mobile.data.isOpenAllDay
import com.ndaily.mobile.data.nextOpenHours
import com.ndaily.mobile.data.starCount
import com.ndaily.mobile.ui.components.PageBack
import com.ndaily.mobile.ui.components.PrimaryButton
import com.ndaily.mobile.ui.components.rememberPlaceSuggestions
import com.ndaily.mobile.ui.map.OsmMap
import com.ndaily.mobile.ui.theme.Hint2
import com.ndaily.mobile.ui.theme.Ink
import com.ndaily.mobile.ui.theme.Muted
import com.ndaily.mobile.ui.theme.Muted2
import com.ndaily.mobile.ui.theme.Primary
import com.ndaily.mobile.ui.theme.PrimaryAlt
import com.ndaily.mobile.ui.theme.PromptFamily
import com.ndaily.mobile.ui.theme.Taxi
import com.ndaily.mobile.ui.theme.ToiletBg
import java.util.UUID
import kotlinx.coroutines.launch

private data class PlaceFilter(val id: String, val label: String, val icon: ImageVector)
private data class Amenity(val key: String, val label: String, val icon: ImageVector)

@Composable
fun ToiletFlow(state: AppUiState, store: AppStore, onBack: () -> Unit) {
    var view by remember { mutableStateOf("browse") }
    var selected by remember { mutableStateOf<ToiletLocation?>(null) }
    var draft by remember { mutableStateOf(ToiletDraft()) }
    var pin by remember { mutableStateOf(state.gps ?: Geo.Bangkok) }
    var formError by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var placeFilter by remember { mutableStateOf("all") }
    var amenities by remember { mutableStateOf(setOf<String>()) }
    var recenter by remember { mutableIntStateOf(0) }
    var focus by remember { mutableStateOf<LatLng?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        store.loadToilets(state.gps ?: Geo.Bangkok)
    }

    val visible = state.toilets.filter { row ->
        val q = query.trim().lowercase()
        val matchQ = q.isEmpty() || row.placeToilet.lowercase().contains(q) || row.addressToilet.lowercase().contains(q)
        val matchType = placeFilter == "all" || row.placeType == placeFilter
        val matchAmenity = listOf("clean" to row.cleanToilet, "free" to row.freeToilet, "accessible" to row.accessibleToilet, "attendant" to row.attendantToilet)
            .all { (key, value) -> key !in amenities || value }
        matchQ && matchType && matchAmenity
    }.let { rows ->
        val gps = state.gps
        if (gps == null) rows else rows.sortedBy { Geo.distanceMeters(gps, LatLng(it.latitudeToilet, it.longitudeToilet)) }
    }

    when (view) {
        "detail" -> selected?.let {
            ToiletDetail(
                state = state,
                store = store,
                toilet = it,
                onBack = { view = "browse" },
            )
        } ?: run { view = "browse" }
        "form" -> ToiletFormScreen(
            store = store,
            draft = draft,
            pin = pin,
            error = formError,
            onChange = { draft = it },
            onPin = { pin = it },
            onClose = { view = "browse" },
            onSave = {
                if (draft.placeToilet.isBlank()) { formError = store.t("toiletPlaceRequired"); return@ToiletFormScreen }
                if (draft.placeType.isBlank()) { formError = store.t("toiletTypeRequired"); return@ToiletFormScreen }
                if (draft.addressToilet.isBlank()) { formError = store.t("toiletAddressRequired"); return@ToiletFormScreen }
                val hours = hoursForPayload(draft.openHoursToilet)
                if (hours == null) { formError = store.t("toiletHoursInvalid"); return@ToiletFormScreen }
                scope.launch {
                    val ok = store.addToilet(
                        ToiletLocation(
                            toiletUuid = UUID.randomUUID().toString(),
                            placeToilet = draft.placeToilet.trim(),
                            placeType = draft.placeType,
                            addressToilet = draft.addressToilet.trim(),
                            cleanToilet = draft.cleanToilet,
                            freeToilet = draft.freeToilet,
                            accessibleToilet = draft.accessibleToilet,
                            attendantToilet = draft.attendantToilet,
                            diaperToilet = draft.diaperToilet,
                            genderSplitToilet = draft.genderSplitToilet,
                            tissueToilet = draft.tissueToilet,
                            paperToilet = draft.paperToilet,
                            latitudeToilet = pin.lat,
                            longitudeToilet = pin.lng,
                            openHoursToilet = hours.ifBlank { "00:00-23:59" },
                        ),
                    )
                    if (ok) view = "browse"
                }
            },
        )
        else -> ToiletBrowse(
            state = state,
            store = store,
            query = query,
            onQuery = { query = it },
            placeFilter = placeFilter,
            onPlaceFilter = { placeFilter = it },
            amenities = amenities,
            onToggleAmenity = { key -> amenities = if (key in amenities) amenities - key else amenities + key },
            toilets = visible,
            recenterToken = recenter,
            onRecenter = { recenter++ },
            onOpen = { id ->
                selected = state.toilets.firstOrNull { it.toiletUuid == id }
                view = "detail"
            },
            onAdd = {
                draft = ToiletDraft()
                pin = state.gps ?: Geo.Bangkok
                formError = ""
                view = "form"
            },
            onBack = onBack,
            onFocus = { focus = it },
            focus = focus,
        )
    }
}

@Composable
private fun ToiletBrowse(
    state: AppUiState,
    store: AppStore,
    query: String,
    onQuery: (String) -> Unit,
    placeFilter: String,
    onPlaceFilter: (String) -> Unit,
    amenities: Set<String>,
    onToggleAmenity: (String) -> Unit,
    toilets: List<ToiletLocation>,
    recenterToken: Int,
    onRecenter: () -> Unit,
    onOpen: (String) -> Unit,
    onAdd: () -> Unit,
    onBack: () -> Unit,
    onFocus: (LatLng) -> Unit,
    focus: LatLng?,
) {
    val t = { key: String -> store.t(key) }
    val placeFilters = listOf(
        PlaceFilter("all", t("toiletFilterAll"), Icons.Outlined.GridView),
        PlaceFilter("mall", t("toiletTypeMall"), Icons.Outlined.Storefront),
        PlaceFilter("gas", t("toiletTypeGas"), Icons.Outlined.LocalGasStation),
        PlaceFilter("food", t("toiletTypeFood"), Icons.Outlined.Restaurant),
    )
    val amenityChips = listOf(
        Amenity("clean", t("toiletAmenityClean"), Icons.Outlined.Star),
        Amenity("free", t("toiletAmenityFree"), Icons.Outlined.Star),
        Amenity("accessible", t("toiletAmenityAccessible"), Icons.Outlined.Accessible),
        Amenity("attendant", t("toiletAmenityAttendant"), Icons.Outlined.Person),
    )
    val suggestions = rememberPlaceSuggestions(store, query)
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().background(ToiletBg).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            PageBack(onBack)
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(t("toiletMapTitle"), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(t("bentoRestroomSubtitle"), fontFamily = PromptFamily, fontSize = 12.sp, color = Muted)
            }
            Row(
                Modifier.clip(CircleShape).background(Color.White).shadow(2.dp, CircleShape).clickable(onClick = onAdd).padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, tint = PrimaryAlt, modifier = Modifier.size(16.dp))
                Text(t("toiletAddAction"), modifier = Modifier.padding(start = 4.dp), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = PrimaryAlt)
            }
        }
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp).height(44.dp).clip(RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE6EAF0), RoundedCornerShape(16.dp)).background(Color.White).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Search, contentDescription = null, tint = Muted, modifier = Modifier.size(16.dp))
            BasicTextField(
                value = query,
                onValueChange = onQuery,
                singleLine = true,
                textStyle = TextStyle(fontFamily = PromptFamily, fontSize = 15.sp, color = Ink),
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                decorationBox = { inner ->
                    Box {
                        if (query.isEmpty()) Text(t("toiletSearchHint"), fontFamily = PromptFamily, fontSize = 14.sp, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        inner()
                    }
                },
            )
            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = PrimaryAlt, modifier = Modifier.size(16.dp))
        }
        if (suggestions.isNotEmpty() && query.length >= 2) {
            suggestions.take(4).forEach { item ->
                Text(
                    item.name,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp).clickable {
                        scope.launch {
                            val place = store.pickPlace(item)
                            val point = if (place != null) LatLng(place.lat, place.lng) else LatLng(item.lat, item.lng)
                            if (point.lat != 0.0 || point.lng != 0.0) onFocus(point)
                            onQuery(place?.name ?: item.name)
                        }
                    },
                    fontFamily = PromptFamily,
                    fontSize = 14.sp,
                    color = Ink,
                )
            }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            placeFilters.forEach { item ->
                val selected = placeFilter == item.id
                Row(
                    Modifier.clip(CircleShape).background(if (selected) PrimaryAlt else Color.White).clickable { onPlaceFilter(item.id) }.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(item.icon, contentDescription = null, tint = if (selected) Color.White else Color(0xFF3C3C43), modifier = Modifier.size(15.dp))
                    Text(item.label, modifier = Modifier.padding(start = 6.dp), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = if (selected) Color.White else Color(0xFF3C3C43))
                }
            }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            amenityChips.forEach { item ->
                val selected = item.key in amenities
                Row(
                    Modifier.clip(CircleShape).border(1.dp, if (selected) PrimaryAlt else Color(0xFFE6EAF0), CircleShape).background(Color.White).clickable { onToggleAmenity(item.key) }.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(item.icon, contentDescription = null, tint = if (selected) PrimaryAlt else Color(0xFF6B7280), modifier = Modifier.size(14.dp))
                    Text(item.label, modifier = Modifier.padding(start = 6.dp), fontFamily = PromptFamily, fontSize = 14.sp, color = if (selected) PrimaryAlt else Color(0xFF6B7280))
                }
            }
        }
        Box(Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 8.dp).clip(RoundedCornerShape(22.dp))) {
            OsmMap(
                modifier = Modifier.fillMaxSize(),
                gps = state.gps,
                toilets = toilets,
                dest = focus,
                recenterToken = recenterToken,
                onToiletClick = onOpen,
            )
            Row(
                Modifier.align(Alignment.TopCenter).padding(top = 12.dp).clip(CircleShape).background(Color.White).shadow(4.dp, CircleShape).clickable(onClick = onRecenter).padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Refresh, contentDescription = null, tint = PrimaryAlt, modifier = Modifier.size(13.dp))
                Text(t("toiletSearchArea"), modifier = Modifier.padding(start = 6.dp), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = PrimaryAlt)
            }
            Box(
                Modifier.align(Alignment.BottomEnd).padding(12.dp).size(40.dp).shadow(4.dp, CircleShape).clip(CircleShape).background(Color.White).clickable(onClick = onRecenter),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.MyLocation, contentDescription = t("mapMyLocation"), modifier = Modifier.size(18.dp)) }
        }
        Column(
            Modifier
                .height(280.dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
        ) {
            Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.width(44.dp).height(6.dp).clip(CircleShape).background(Color(0xFFE5E7EB)))
            }
            if (toilets.isEmpty()) {
                Text(t("toiletEmptyList"), modifier = Modifier.padding(24.dp).fillMaxWidth(), fontFamily = PromptFamily, fontSize = 14.sp, color = Muted)
            } else {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    toilets.forEach { toilet ->
                        ToiletCard(state, store, toilet) { onOpen(toilet.toiletUuid) }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun ToiletCard(state: AppUiState, store: AppStore, toilet: ToiletLocation, onOpen: () -> Unit) {
    val t = { key: String -> store.t(key) }
    val saved = toilet.toiletUuid in state.savedToiletIds
    val gps = state.gps
    val distance = gps?.let { Geo.formatToiletDistance(Geo.distanceMeters(it, LatLng(toilet.latitudeToilet, toilet.longitudeToilet)), state.locale) }
    val badge = when {
        isOpenAllDay(toilet.openHoursToilet) -> t("toiletOpen24")
        toilet.cleanToilet -> t("toiletAmenityClean")
        else -> ""
    }
    val score = starCount(toilet.avgScoreToilet)
    Box {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFEEF2F6), RoundedCornerShape(16.dp)).background(Color(0xFFF7F9FC)).clickable(onClick = onOpen).padding(12.dp),
        ) {
            Box(Modifier.size(88.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFEEF2F7)), contentAlignment = Alignment.Center) {
                Image(painterResource(R.drawable.ic_toilet), contentDescription = null, modifier = Modifier.size(32.dp))
                if (badge.isNotBlank()) {
                    Text(badge, modifier = Modifier.align(Alignment.TopStart).padding(6.dp).clip(RoundedCornerShape(6.dp)).background(Color.White).padding(horizontal = 6.dp, vertical = 2.dp), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, color = PrimaryAlt)
                }
            }
            Column(Modifier.weight(1f).padding(start = 12.dp, end = 24.dp)) {
                Text(toilet.placeToilet, fontFamily = PromptFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (score > 0) Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(13.dp))
                    Text("%.1f".format(score), fontFamily = PromptFamily, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                }
                if (distance != null) Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = PrimaryAlt, modifier = Modifier.size(13.dp))
                    Text(distance, fontFamily = PromptFamily, fontSize = 12.sp, color = Muted, modifier = Modifier.padding(start = 4.dp))
                }
                Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (toilet.cleanToilet) Chip(t("toiletAmenityClean"), Color(0xFFDCFCE7), Color(0xFF15803D))
                    if (toilet.freeToilet) Chip(t("toiletAmenityFree"), Color(0xFFDBEAFE), Color(0xFF1D4ED8))
                }
            }
        }
        Icon(
            if (saved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
            contentDescription = t("toiletBookmark"),
            tint = PrimaryAlt,
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).clickable { store.toggleSavedToilet(toilet.toiletUuid) }.size(18.dp),
        )
    }
}

@Composable
private fun Chip(text: String, bg: Color, fg: Color) {
    Text(text, modifier = Modifier.clip(CircleShape).background(bg).padding(horizontal = 8.dp, vertical = 2.dp), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = fg)
}

@Composable
private fun ToiletDetail(state: AppUiState, store: AppStore, toilet: ToiletLocation, onBack: () -> Unit) {
    val t = { key: String -> store.t(key) }
    val context = LocalContext.current
    val saved = toilet.toiletUuid in state.savedToiletIds
    val hours = displayHours(toilet.openHoursToilet)
    val gps = state.gps
    val distance = gps?.let { Geo.formatToiletDistance(Geo.distanceMeters(it, LatLng(toilet.latitudeToilet, toilet.longitudeToilet)), state.locale) }
    val score = starCount(toilet.avgScoreToilet)
    val maps = "https://www.google.com/maps/dir/?api=1&destination=${toilet.latitudeToilet},${toilet.longitudeToilet}"
    fun openMaps() {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(maps)))
    }
    val active = buildList {
        if (toilet.cleanToilet) add(t("toiletAmenityClean") to Icons.Outlined.Star)
        if (toilet.freeToilet) add(t("toiletAmenityFree") to Icons.Outlined.Star)
        if (toilet.accessibleToilet) add(t("toiletAmenityAccessible") to Icons.Outlined.Accessible)
        if (toilet.attendantToilet) add(t("toiletAmenityAttendant") to Icons.Outlined.Person)
        if (toilet.diaperToilet) add(t("toiletAmenityDiaper") to Icons.Outlined.ChildCare)
        if (toilet.genderSplitToilet) add(t("toiletAmenityGender") to Icons.Outlined.Wc)
        if (toilet.tissueToilet) add(t("toiletAmenityTissue") to Icons.Outlined.Star)
        if (toilet.paperToilet) add(t("toiletAmenityPaper") to Icons.Outlined.Star)
    }
    Column(Modifier.fillMaxSize().background(ToiletBg).statusBarsPadding()) {
        Box(Modifier.fillMaxWidth()) {
            PageBack(onBack)
            Text(t("toiletDetailTitle"), modifier = Modifier.align(Alignment.Center), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = Ink)
            Row(Modifier.align(Alignment.CenterEnd).padding(end = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconBubble(onClick = {
                    context.startActivity(Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "${toilet.placeToilet}\n${toilet.addressToilet}")
                    })
                    store.toast(t("toiletShareCopied"))
                }) { Icon(Icons.Outlined.Share, null, modifier = Modifier.size(16.dp)) }
                IconBubble(onClick = { store.toggleSavedToilet(toilet.toiletUuid) }) {
                    Icon(if (saved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder, null, tint = if (saved) PrimaryAlt else Ink, modifier = Modifier.size(16.dp))
                }
            }
        }
        Box(Modifier.padding(16.dp).fillMaxWidth().height(180.dp).clip(RoundedCornerShape(28.dp)).background(Color(0xFFE7EDF5)), contentAlignment = Alignment.Center) {
            Image(painterResource(R.drawable.ic_toilet), contentDescription = null, modifier = Modifier.size(48.dp))
        }
        Column(Modifier.padding(horizontal = 16.dp).clip(RoundedCornerShape(24.dp)).background(Color.White).padding(16.dp)) {
            Row {
                Column(Modifier.weight(1f)) {
                    Text(toilet.placeToilet, fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = Ink)
                    Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (score > 0) Text("★ %.1f".format(score), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        if (distance != null) Text(distance, fontFamily = PromptFamily, fontSize = 14.sp, color = Color(0xFF6B7280))
                        if (hours.isNotBlank()) Text("${t("toiletStatusOpen")} $hours", fontFamily = PromptFamily, fontSize = 14.sp, color = Color(0xFF6B7280))
                    }
                    if (toilet.addressToilet.isNotBlank()) {
                        Row(Modifier.padding(top = 8.dp)) {
                            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = PrimaryAlt, modifier = Modifier.size(15.dp).padding(top = 2.dp))
                            Text(toilet.addressToilet, fontFamily = PromptFamily, fontSize = 14.sp, color = Color(0xFF6B7280), modifier = Modifier.padding(start = 6.dp))
                        }
                    }
                }
                Box(Modifier.size(44.dp).clip(CircleShape).background(PrimaryAlt).clickable { openMaps() }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Navigation, contentDescription = t("toiletNavigate"), tint = Color.White)
                }
            }
        }
        if (active.isNotEmpty()) {
            Row(Modifier.padding(16.dp).clip(RoundedCornerShape(24.dp)).background(Color.White).padding(16.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                active.forEach { (label, icon) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp)) {
                        Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFE8F1FC)), contentAlignment = Alignment.Center) {
                            Icon(icon, contentDescription = null, tint = PrimaryAlt)
                        }
                        Text(label, modifier = Modifier.padding(top = 6.dp), fontFamily = PromptFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, color = Color(0xFF374151))
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Row(Modifier.background(ToiletBg).padding(16.dp).navigationBarsPadding(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFD7DEE8), RoundedCornerShape(16.dp)).background(Color.White).clickable { store.toggleSavedToilet(toilet.toiletUuid) }.padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(if (saved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder, contentDescription = null, tint = if (saved) PrimaryAlt else Ink, modifier = Modifier.size(16.dp))
                Text(t("toiletBookmark"), modifier = Modifier.padding(start = 8.dp), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
            Row(
                Modifier.weight(1.4f).clip(RoundedCornerShape(16.dp)).background(PrimaryAlt).clickable { openMaps() }.padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Navigation, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Text(t("toiletNavigate"), modifier = Modifier.padding(start = 8.dp), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun IconBubble(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(Modifier.size(36.dp).shadow(4.dp, CircleShape).clip(CircleShape).background(Color.White).clickable(onClick = onClick), contentAlignment = Alignment.Center) { content() }
}

@Composable
private fun ToiletFormScreen(
    store: AppStore,
    draft: ToiletDraft,
    pin: LatLng,
    error: String,
    onChange: (ToiletDraft) -> Unit,
    onPin: (LatLng) -> Unit,
    onClose: () -> Unit,
    onSave: () -> Unit,
) {
    val t = { key: String -> store.t(key) }
    val types = listOf("mall" to t("toiletTypeMall"), "gas" to t("toiletTypeGas"), "food" to t("toiletTypeFood"))
    val amenities = listOf(
        "clean" to t("toiletAmenityClean"),
        "free" to t("toiletAmenityFree"),
        "accessible" to t("toiletAmenityAccessible"),
        "attendant" to t("toiletAmenityAttendant"),
        "diaper" to t("toiletAmenityDiaper"),
        "gender" to t("toiletAmenityGender"),
        "tissue" to t("toiletAmenityTissue"),
        "paper" to t("toiletAmenityPaper"),
    )
    Column(Modifier.fillMaxSize().background(ToiletBg).statusBarsPadding().verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
        Box(Modifier.fillMaxWidth()) {
            PageBack(onClose)
            Text(t("toiletAddNew"), modifier = Modifier.align(Alignment.Center), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = Ink)
        }
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(t("toiletBasicInfo"), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Ink)
            FormField(t("toiletPlaceName"), draft.placeToilet, required = true) { onChange(draft.copy(placeToilet = it)) }
            Text(t("toiletPlaceType") + " *", fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1F2937))
            types.forEach { (id, label) ->
                val selected = draft.placeType == id
                Text(
                    label,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).border(1.dp, if (selected) PrimaryAlt else Color(0xFFE6EAF0), RoundedCornerShape(16.dp)).background(if (selected) Color(0xFFE8F1FC) else Color.White).clickable { onChange(draft.copy(placeType = id)) }.padding(14.dp),
                    fontFamily = PromptFamily,
                    color = if (selected) PrimaryAlt else Ink,
                )
            }
            FormField(t("toiletAddress"), draft.addressToilet, required = true, placeholder = t("toiletAddressPlaceholder")) { onChange(draft.copy(addressToilet = it)) }
            Text(t("toiletMapPin") + " *", fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Box(Modifier.fillMaxWidth().height(176.dp).clip(RoundedCornerShape(24.dp)).border(1.dp, Color(0xFFE6EAF0), RoundedCornerShape(24.dp))) {
                OsmMap(modifier = Modifier.fillMaxSize(), gps = null, pin = pin, onLongPress = onPin)
            }
            Text("${t("toiletCoords")}: ${"%.4f".format(pin.lat)}, ${"%.4f".format(pin.lng)} (${t("toiletPinHint")})", fontFamily = PromptFamily, fontSize = 12.sp, color = Muted)
            FormField(t("toiletOpenHours"), draft.openHoursToilet) { onChange(draft.copy(openHoursToilet = nextOpenHours(draft.openHoursToilet, it))) }
            Text(t("toiletDetails"), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                amenities.forEach { (key, label) ->
                    val selected = amenityOn(draft, key)
                    Text(
                        label,
                        modifier = Modifier.clip(RoundedCornerShape(16.dp)).border(1.dp, if (selected) PrimaryAlt else Color(0xFFE6EAF0), RoundedCornerShape(16.dp)).background(if (selected) Color(0xFFE8F1FC) else Color.White).clickable { onChange(toggleAmenity(draft, key)) }.padding(horizontal = 12.dp, vertical = 8.dp),
                        fontFamily = PromptFamily,
                        fontSize = 14.sp,
                        color = if (selected) PrimaryAlt else Color(0xFF6B7280),
                    )
                }
            }
            if (error.isNotBlank()) Text(error, fontFamily = PromptFamily, fontSize = 14.sp, color = Taxi)
            PrimaryButton(t("toiletAddAction"), onClick = onSave, color = PrimaryAlt)
        }
    }
}

@Composable
private fun FormField(label: String, value: String, required: Boolean = false, placeholder: String = "", onChange: (String) -> Unit) {
    Column {
        Text(if (required) "$label *" else label, fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1F2937), modifier = Modifier.padding(bottom = 6.dp))
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE6EAF0), RoundedCornerShape(16.dp)).background(Color.White).padding(16.dp)) {
            BasicTextField(
                value = value,
                onValueChange = onChange,
                textStyle = TextStyle(fontFamily = PromptFamily, fontSize = 15.sp, color = Color(0xFF1F2937)),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    Box {
                        if (value.isEmpty() && placeholder.isNotBlank()) Text(placeholder, fontFamily = PromptFamily, color = Hint2)
                        inner()
                    }
                },
            )
        }
    }
}

private fun amenityOn(draft: ToiletDraft, key: String) = when (key) {
    "clean" -> draft.cleanToilet
    "free" -> draft.freeToilet
    "accessible" -> draft.accessibleToilet
    "attendant" -> draft.attendantToilet
    "diaper" -> draft.diaperToilet
    "gender" -> draft.genderSplitToilet
    "tissue" -> draft.tissueToilet
    "paper" -> draft.paperToilet
    else -> false
}

private fun toggleAmenity(draft: ToiletDraft, key: String) = when (key) {
    "clean" -> draft.copy(cleanToilet = !draft.cleanToilet)
    "free" -> draft.copy(freeToilet = !draft.freeToilet)
    "accessible" -> draft.copy(accessibleToilet = !draft.accessibleToilet)
    "attendant" -> draft.copy(attendantToilet = !draft.attendantToilet)
    "diaper" -> draft.copy(diaperToilet = !draft.diaperToilet)
    "gender" -> draft.copy(genderSplitToilet = !draft.genderSplitToilet)
    "tissue" -> draft.copy(tissueToilet = !draft.tissueToilet)
    "paper" -> draft.copy(paperToilet = !draft.paperToilet)
    else -> draft
}
