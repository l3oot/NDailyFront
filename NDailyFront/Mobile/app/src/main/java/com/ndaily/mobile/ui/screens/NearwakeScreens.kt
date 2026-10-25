package com.ndaily.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ShoppingBag
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ndaily.mobile.data.AppStore
import com.ndaily.mobile.data.AppUiState
import com.ndaily.mobile.data.Geo
import com.ndaily.mobile.data.LatLng
import com.ndaily.mobile.data.Plans
import com.ndaily.mobile.data.SavedPlace
import com.ndaily.mobile.ui.components.PageBack
import com.ndaily.mobile.ui.components.PrimaryButton
import com.ndaily.mobile.ui.components.rememberPlaceSuggestions
import com.ndaily.mobile.ui.map.OsmMap
import com.ndaily.mobile.ui.theme.Danger
import com.ndaily.mobile.ui.theme.Green
import com.ndaily.mobile.ui.theme.Hint
import com.ndaily.mobile.ui.theme.Hint2
import com.ndaily.mobile.ui.theme.Ink
import com.ndaily.mobile.ui.theme.Muted2
import com.ndaily.mobile.ui.theme.NearwakeBg
import com.ndaily.mobile.ui.theme.Primary
import com.ndaily.mobile.ui.theme.PromptFamily
import java.util.UUID
import kotlinx.coroutines.launch

private val tones = listOf(
    Color(0xFFE8F1FF) to Color(0xFF3B82F6),
    Color(0xFFFFF3E4) to Color(0xFFF0A03A),
    Color(0xFFF1EBFF) to Color(0xFF7C6BF2),
    Color(0xFFE7F0FF) to Color(0xFF5B8DEF),
    Color(0xFFFDE8EE) to Color(0xFFF07A96),
)

@Composable
fun NearwakeScreen(
    state: AppUiState,
    store: AppStore,
    onBack: () -> Unit,
    onOpenMap: (SavedPlace?, Boolean) -> Unit,
) {
    val t = { key: String -> store.t(key) }
    var query by remember { mutableStateOf("") }
    var tab by remember { mutableStateOf("recent") }
    var menuId by remember { mutableStateOf<String?>(null) }
    var editingId by remember { mutableStateOf<String?>(null) }
    var editName by remember { mutableStateOf("") }
    val suggestions = rememberPlaceSuggestions(store, query)
    val scope = rememberCoroutineScope()
    val shown = if (tab == "favorites") state.favorites else state.recents

    Column(
        Modifier
            .fillMaxSize()
            .background(NearwakeBg)
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(56.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.align(Alignment.CenterStart)) { PageBack(onBack) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(t("nearAlarm"), fontFamily = PromptFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Ink)
                Text(t("nearwakeLead"), fontFamily = PromptFamily, fontSize = 13.sp, color = Muted2)
            }
            Row(
                Modifier
                    .align(Alignment.CenterEnd)
                    .clip(CircleShape)
                    .background(Color(0xFFE7F1FF))
                    .clickable { onOpenMap(null, false) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Map, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                Text(t("nearwakePickMap"), modifier = Modifier.padding(start = 4.dp), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Primary)
            }
        }
        Box(Modifier.padding(top = 16.dp).shadow(8.dp, RoundedCornerShape(22.dp)).clip(RoundedCornerShape(22.dp)).background(Color.White).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Primary)
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = PromptFamily, fontSize = 16.sp, color = Ink),
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                    decorationBox = { inner ->
                        Box {
                            if (query.isEmpty()) Text(t("nearwakeSearchPlaceholder"), fontFamily = PromptFamily, fontSize = 16.sp, color = Hint2)
                            inner()
                        }
                    },
                )
                if (query.isNotEmpty()) {
                    Icon(Icons.Outlined.Close, contentDescription = null, tint = Hint2, modifier = Modifier.clickable { query = "" }.size(16.dp))
                }
            }
        }
        Text(t("nearwakeSearchHint"), modifier = Modifier.padding(start = 16.dp, top = 8.dp), fontFamily = PromptFamily, fontSize = 13.sp, color = Color(0xFFA8B0BA))
        if (suggestions.isNotEmpty()) {
            Column(Modifier.padding(top = 8.dp).shadow(10.dp, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp)).background(Color.White)) {
                suggestions.forEach { item ->
                    Column(
                        Modifier.fillMaxWidth().clickable {
                            scope.launch {
                                val place = store.pickPlace(item) ?: return@launch
                                onOpenMap(place, false)
                            }
                        }.padding(horizontal = 16.dp, vertical = 10.dp),
                    ) {
                        Text(item.name, fontFamily = PromptFamily, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Ink)
                        if (item.detail.isNotBlank()) Text(item.detail, fontFamily = PromptFamily, fontSize = 12.sp, color = Muted2, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
        Row(Modifier.padding(top = 24.dp, start = 4.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(
                t("nearwakeRecent"),
                modifier = Modifier.clickable { tab = "recent"; menuId = null },
                fontFamily = PromptFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (tab == "recent") Ink else Hint2,
            )
            Text(
                t("nearwakeFavorites"),
                modifier = Modifier.clickable { tab = "favorites"; menuId = null },
                fontFamily = PromptFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (tab == "favorites") Ink else Hint2,
            )
        }
        Column(
            Modifier
                .padding(top = 12.dp, bottom = 16.dp)
                .height(320.dp)
                .shadow(8.dp, RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White),
        ) {
            when {
                !state.loggedIn -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(t("favoritePlacesLoginPrompt"), fontFamily = PromptFamily, fontSize = 15.sp, color = Color(0xFF5C6570), modifier = Modifier.padding(16.dp))
                }
                shown.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (tab == "favorites") t("nearwakeFavoritesEmpty") else t("nearwakeRecentEmpty"),
                        fontFamily = PromptFamily,
                        fontSize = 15.sp,
                        color = Muted2,
                    )
                }
                else -> LazyColumn {
                    itemsIndexed(shown, key = { _, item -> item.id }) { index, item ->
                        PlaceRow(
                            item = item,
                            index = index,
                            editing = editingId == item.id,
                            editName = editName,
                            onEditName = { editName = it },
                            menuOpen = menuId == item.id,
                            canRename = tab == "favorites",
                            t = t,
                            onOpen = { onOpenMap(item, tab == "favorites") },
                            onMenu = { menuId = if (menuId == item.id) null else item.id },
                            onRename = {
                                editingId = item.id
                                editName = item.name
                                menuId = null
                            },
                            onSaveRename = {
                                if (editName.isBlank()) store.toast(t("favoritePlaceNameRequired"))
                                else {
                                    store.renameFavorite(item.id, editName.trim())
                                    editingId = null
                                }
                            },
                            onDelete = {
                                if (tab == "favorites") store.removeFavorite(item.id) else store.removeRecent(item.id)
                                menuId = null
                            },
                            deleteLabel = if (tab == "favorites") t("deleteFavoritePlace") else t("deleteRecentPlace"),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceRow(
    item: SavedPlace,
    index: Int,
    editing: Boolean,
    editName: String,
    onEditName: (String) -> Unit,
    menuOpen: Boolean,
    canRename: Boolean,
    t: (String) -> String,
    onOpen: () -> Unit,
    onMenu: () -> Unit,
    onRename: () -> Unit,
    onSaveRename: () -> Unit,
    onDelete: () -> Unit,
    deleteLabel: String,
) {
    val tone = tones[index % tones.size]
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(tone.first), contentAlignment = Alignment.Center) {
            Icon(placeIcon(item.name), contentDescription = null, tint = tone.second, modifier = Modifier.size(20.dp))
        }
        if (editing) {
            BasicTextField(
                value = editName,
                onValueChange = onEditName,
                textStyle = TextStyle(fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Ink),
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
            )
            Icon(Icons.Outlined.Check, contentDescription = t("accountSave"), tint = Primary, modifier = Modifier.clickable(onClick = onSaveRename))
        } else {
            Column(Modifier.weight(1f).padding(horizontal = 12.dp).clickable(onClick = onOpen)) {
                Text(item.name, fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (item.detail.isNotBlank()) Text(item.detail, fontFamily = PromptFamily, fontSize = 13.sp, color = Muted2, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Box {
            Icon(Icons.Outlined.MoreVert, contentDescription = null, tint = Hint2, modifier = Modifier.clickable(onClick = onMenu).size(20.dp))
            if (menuOpen) {
                Column(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 24.dp)
                        .shadow(10.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White),
                ) {
                    if (canRename) Text(t("renameFavoritePlace"), modifier = Modifier.clickable(onClick = onRename).padding(horizontal = 16.dp, vertical = 10.dp), fontFamily = PromptFamily, fontSize = 14.sp, color = Ink)
                    Text(deleteLabel, modifier = Modifier.clickable(onClick = onDelete).padding(horizontal = 16.dp, vertical = 10.dp), fontFamily = PromptFamily, fontSize = 14.sp, color = Color(0xFFE5484D))
                }
            }
        }
    }
}

private fun placeIcon(name: String): ImageVector {
    val text = name.lowercase()
    return when {
        "บ้าน" in text || "home" in text || "house" in text -> Icons.Outlined.Home
        "ทำงาน" in text || "ออฟฟิศ" in text || "work" in text || "office" in text -> Icons.Outlined.Work
        "ห้าง" in text || "ร้าน" in text || "mall" in text || "shop" in text -> Icons.Outlined.ShoppingBag
        "สนามบิน" in text || "airport" in text -> Icons.Outlined.Flight
        "มหาวิทยาลัย" in text || "โรงเรียน" in text || "university" in text || "school" in text -> Icons.Outlined.School
        else -> Icons.Outlined.LocationOn
    }
}

@Composable
fun NearwakeMapScreen(
    state: AppUiState,
    store: AppStore,
    initial: SavedPlace?,
    fromFavorite: Boolean,
    onBack: () -> Unit,
    onSettings: () -> Unit,
) {
    val t = { key: String -> store.t(key) }
    var query by remember { mutableStateOf("") }
    var dest by remember { mutableStateOf(initial) }
    var fromFav by remember { mutableStateOf(fromFavorite) }
    var radius by remember { mutableIntStateOf(state.tracking?.radiusMeters ?: 500) }
    var naming by remember { mutableStateOf(false) }
    var favoriteName by remember { mutableStateOf("") }
    var recenter by remember { mutableIntStateOf(0) }
    var overview by remember { mutableIntStateOf(0) }
    var compass by remember { mutableIntStateOf(0) }
    val suggestions = rememberPlaceSuggestions(store, query)
    val scope = rememberCoroutineScope()
    val tracking = state.tracking
    val maxRadius = Plans.radiusMax(state.planCode)
    val snapped = Plans.snapRadius(radius, state.planCode)
    val destPoint = dest?.let { LatLng(it.lat, it.lng) }
    val straight = if (state.gps != null && destPoint != null) Geo.distanceMeters(state.gps, destPoint) else null

    LaunchedEffect(dest) {
        dest?.let { store.addRecent(it) }
    }

    Column(Modifier.fillMaxSize().background(Color.White).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            PageBack(onBack)
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(t("nearAlarm"), fontFamily = PromptFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Ink)
                Text(
                    when {
                        dest == null -> t("nearwakeLead")
                        tracking != null -> t("mapTrackingSubtitle")
                        else -> t("mapReadySubtitle")
                    },
                    fontFamily = PromptFamily,
                    fontSize = 13.sp,
                    color = Muted2,
                )
            }
            Icon(Icons.Outlined.Settings, contentDescription = t("mapSettings"), modifier = Modifier.size(22.dp).clickable(onClick = onSettings), tint = Ink)
        }
        if (state.gpsDenied) {
            Text(t("mapGpsDenied"), modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFF1F4)).padding(12.dp).fillMaxWidth(), fontFamily = PromptFamily, fontSize = 14.sp, color = Color(0xFFC33E83))
        }
        if (dest != null) {
            Row(
                Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .shadow(8.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Color(0xFFFF4D6D), modifier = Modifier.size(24.dp))
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(dest!!.name, fontFamily = PromptFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (dest!!.detail.isNotBlank()) Text(dest!!.detail, fontFamily = PromptFamily, fontSize = 13.sp, color = Muted2, maxLines = 1)
                }
                Icon(Icons.Outlined.Close, contentDescription = t("mapClearDestination"), tint = Hint2, modifier = Modifier.clickable {
                    dest = null
                    naming = false
                    store.stopTracking()
                })
            }
        } else {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    Modifier.shadow(8.dp, RoundedCornerShape(22.dp)).clip(RoundedCornerShape(22.dp)).background(Color.White).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Primary)
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = PromptFamily, fontSize = 16.sp, color = Ink),
                        modifier = Modifier.weight(1f).padding(start = 12.dp),
                        decorationBox = { inner ->
                            Box {
                                if (query.isEmpty()) Text(t("mapSearchHint"), fontFamily = PromptFamily, fontSize = 16.sp, color = Hint2)
                                inner()
                            }
                        },
                    )
                }
                Text(t("mapLongPressHint"), modifier = Modifier.padding(start = 4.dp, top = 8.dp), fontFamily = PromptFamily, fontSize = 13.sp, color = Color(0xFFA8B0BA))
                suggestions.forEach { item ->
                    Text(
                        item.name,
                        modifier = Modifier.fillMaxWidth().clickable {
                            scope.launch {
                                val place = store.pickPlace(item) ?: return@launch
                                dest = place
                                query = ""
                                fromFav = false
                            }
                        }.padding(vertical = 8.dp),
                        fontFamily = PromptFamily,
                        fontSize = 15.sp,
                        color = Ink,
                    )
                }
            }
        }
        Box(Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 8.dp).clip(RoundedCornerShape(22.dp))) {
            OsmMap(
                modifier = Modifier.fillMaxSize(),
                gps = state.gps,
                dest = destPoint,
                destName = dest?.name,
                radiusMeters = snapped.toDouble(),
                onLongPress = { point ->
                    dest = SavedPlace(UUID.randomUUID().toString(), "${t("mapPickedLocation")} (${"%.5f".format(point.lat)}, ${"%.5f".format(point.lng)})", "", point.lat, point.lng)
                    fromFav = false
                    store.stopTracking()
                },
                recenterToken = recenter,
                overviewToken = overview,
                compassToken = compass,
            )
            if (state.gps == null && !state.gpsDenied) {
                Text(t("mapWaitingGps"), modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp), fontFamily = PromptFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Ink)
            }
            Column(Modifier.align(Alignment.TopEnd).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MapTool(onClick = { overview++ }) { Icon(Icons.Outlined.Navigation, null, tint = Color(0xFF3A4350)) }
                MapTool(onClick = { compass++ }) { Icon(Icons.Outlined.Explore, null, tint = Color(0xFF3A4350)) }
                MapTool(onClick = { recenter++ }) { Icon(Icons.Outlined.MyLocation, null, tint = Color(0xFF3A4350)) }
            }
        }
        if (dest != null) {
            Column(
                Modifier
                    .shadow(10.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                Row(Modifier.fillMaxWidth()) {
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(56.dp).clip(CircleShape).background(Color(0xFFEAF2FF)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.DirectionsCar, contentDescription = null, tint = Primary)
                        }
                        Column(Modifier.padding(start = 12.dp)) {
                            Text(t("mapArriveIn"), fontFamily = PromptFamily, fontSize = 13.sp, color = Muted2)
                            Text(
                                straight?.let { Geo.formatStraight(it, t("taxiFareKm"), t("mapRadiusMeters")) } ?: "—",
                                fontFamily = PromptFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp,
                                color = Green,
                            )
                            Text(t("mapStraightHint"), fontFamily = PromptFamily, fontSize = 11.sp, color = Hint2)
                        }
                    }
                    Box(Modifier.width(1.dp).fillMaxHeight().background(Color(0xFFE6EAF0)))
                    Column(Modifier.width(132.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(t("mapNotifyWhenLeft"), fontFamily = PromptFamily, fontSize = 12.sp, color = Muted2)
                        Icon(Icons.Outlined.Notifications, contentDescription = null, tint = Primary, modifier = Modifier.padding(top = 6.dp))
                        Text(Geo.formatAlarmRadius(snapped, t("taxiFareKm"), t("mapRadiusMeters")), fontFamily = PromptFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Primary)
                    }
                }
                RadiusStops(snapped, maxRadius, t("taxiFareKm"), t("mapRadiusMeters")) { next ->
                    if (next > maxRadius) {
                        val planLabel = when (state.planCode) {
                            com.ndaily.mobile.data.PlanCode.Plus -> t("planNamePlus")
                            com.ndaily.mobile.data.PlanCode.Pro -> t("planNamePro")
                            else -> t("planNameFree")
                        }
                        store.toast("$planLabel ${Geo.formatAlarmRadius(maxRadius, t("taxiFareKm"), t("mapRadiusMeters"))}")
                    } else {
                        radius = next
                        if (tracking != null) store.setTrackingRadius(next)
                    }
                }
                Spacer(Modifier.height(16.dp))
                val active = tracking != null
                PrimaryButton(
                    text = if (active) t("mapStopTracking") else t("mapStartTracking"),
                    color = if (active) Color(0xFFFF5C6C) else Primary,
                    onClick = {
                        if (active) store.stopTracking()
                        else dest?.let { store.startTracking(it, snapped, fromFav) }
                    },
                )
                if (!fromFav) {
                    Spacer(Modifier.height(12.dp))
                    if (naming) {
                        Box(Modifier.fillMaxWidth().border(1.dp, Color(0xFFE6EAF0), CircleShape).padding(horizontal = 16.dp, vertical = 12.dp)) {
                            BasicTextField(
                                value = favoriteName,
                                onValueChange = { favoriteName = it.take(150) },
                                textStyle = TextStyle(fontFamily = PromptFamily, fontSize = 15.sp, color = Ink),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { inner ->
                                    Box {
                                        if (favoriteName.isEmpty()) Text(t("mapFavoriteNameHint"), fontFamily = PromptFamily, color = Hint2)
                                        inner()
                                    }
                                },
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .border(1.dp, Primary, CircleShape)
                            .clickable {
                                val current = dest ?: return@clickable
                                if (!naming) {
                                    favoriteName = current.name
                                    naming = true
                                } else {
                                    if (favoriteName.isBlank()) store.toast(t("mapFavoriteNeedName"))
                                    else {
                                        store.addFavorite(current.copy(name = favoriteName.trim()))
                                        naming = false
                                    }
                                }
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Star, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                            Text(t("mapSaveFavoriteAction"), modifier = Modifier.padding(start = 8.dp), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MapTool(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier.size(44.dp).shadow(6.dp, CircleShape).clip(CircleShape).background(Color.White).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun RadiusStops(value: Int, maxMeters: Int, kmLabel: String, mLabel: String, onChange: (Int) -> Unit) {
    val stops = Plans.radiusStops
    val index = stops.indexOf(value).coerceAtLeast(0)
    Column(Modifier.padding(top = 16.dp, start = 4.dp, end = 4.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(32.dp)
                .pointerInput(maxMeters) {
                    detectTapGestures { offset ->
                        val ratio = (offset.x / size.width).coerceIn(0f, 1f)
                        val i = (ratio * (stops.lastIndex)).toInt().coerceIn(0, stops.lastIndex)
                        onChange(stops[i])
                    }
                },
        ) {
            Box(Modifier.align(Alignment.CenterStart).fillMaxWidth().height(3.dp).clip(CircleShape).background(Color(0xFFE6EAF0)))
            Box(Modifier.align(Alignment.CenterStart).fillMaxWidth(index / stops.lastIndex.toFloat()).height(3.dp).clip(CircleShape).background(Primary))
            Box(Modifier.align(Alignment.CenterStart).padding(start = ((index / stops.lastIndex.toFloat()) * 280).dp).size(24.dp).clip(CircleShape).background(Primary).border(3.dp, Color.White, CircleShape))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            stops.forEach { stop ->
                val selected = stop == value
                val allowed = stop <= maxMeters
                Text(
                    Geo.formatAlarmRadius(stop, kmLabel, mLabel),
                    modifier = Modifier.clickable { onChange(stop) },
                    fontFamily = PromptFamily,
                    fontSize = 12.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        selected -> Primary
                        allowed -> Muted2
                        else -> Color(0xFFD5DBE3)
                    },
                )
            }
        }
    }
}
