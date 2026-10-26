package com.ndaily.mobile.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ndaily.mobile.data.AppStore
import com.ndaily.mobile.data.AppUiState
import com.ndaily.mobile.data.Geo
import com.ndaily.mobile.data.LatLng
import com.ndaily.mobile.data.Plans
import com.ndaily.mobile.data.SavedPlace
import com.ndaily.mobile.ui.components.PageBack
import com.ndaily.mobile.ui.components.rememberPlaceSuggestions
import com.ndaily.mobile.ui.map.OsmMap
import com.ndaily.mobile.ui.theme.Green
import com.ndaily.mobile.ui.theme.Hint2
import com.ndaily.mobile.ui.theme.Ink
import com.ndaily.mobile.ui.theme.Muted2
import com.ndaily.mobile.ui.theme.NearwakeBg
import com.ndaily.mobile.ui.theme.Primary
import com.ndaily.mobile.ui.theme.PromptFamily
import java.util.UUID
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
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
    val density = LocalDensity.current
    var searchHeight by remember { mutableStateOf(56.dp) }
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
            Column(
                Modifier.padding(horizontal = 88.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    t("nearAlarm"),
                    fontFamily = PromptFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(t("nearwakeLead"), fontFamily = PromptFamily, fontSize = 13.sp, color = Muted2, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
        Box(Modifier.padding(top = 16.dp).weight(1f).fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .onGloballyPositioned { searchHeight = with(density) { it.size.height.toDp() } }
                .shadow(8.dp, RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
                .padding(16.dp),
        ) {
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
                            showDivider = index < shown.lastIndex,
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
            if (suggestions.isNotEmpty()) {
                Box(
                    Modifier
                        .zIndex(2f)
                        .padding(top = searchHeight + 8.dp)
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                        .shadow(10.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White),
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 28.dp)
                            .heightIn(max = 348.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        suggestions.forEach { item ->
                            Column(
                                Modifier.fillMaxWidth().clickable {
                                    scope.launch {
                                        val place = store.pickPlace(item) ?: return@launch
                                        onOpenMap(place, false)
                                    }
                                }.padding(horizontal = 16.dp, vertical = 10.dp),
                            ) {
                                Text(item.name, fontFamily = PromptFamily, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (item.detail.isNotBlank()) Text(item.detail, fontFamily = PromptFamily, fontSize = 12.sp, color = Muted2, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                    Text(
                        placeProviderLabel(suggestions.first().source),
                        modifier = Modifier.align(Alignment.BottomEnd).padding(end = 12.dp, bottom = 8.dp),
                        fontFamily = PromptFamily,
                        fontSize = 11.sp,
                        color = Color(0xFFE6B000),
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceRow(
    item: SavedPlace,
    index: Int,
    showDivider: Boolean,
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
    Column {
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
                DropdownMenu(expanded = menuOpen, onDismissRequest = onMenu) {
                    if (canRename) {
                        DropdownMenuItem(
                            text = { Text(t("renameFavoritePlace"), fontFamily = PromptFamily, fontSize = 14.sp, color = Ink) },
                            onClick = onRename,
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(deleteLabel, fontFamily = PromptFamily, fontSize = 14.sp, color = Color(0xFFE5484D)) },
                        onClick = onDelete,
                    )
                }
            }
        }
        if (showDivider) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F4F7)))
        }
    }
}

private fun placeProviderLabel(source: String): String {
    val name = when (source.lowercase()) {
        "longdo" -> "Longdo"
        "ggmap", "google" -> "Google"
        "osm" -> "OpenStreetMap"
        else -> source.trim()
    }
    return if (name.isBlank()) "Support by" else "Support by $name"
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
    var favoriteName by remember { mutableStateOf("") }
    var namingFavorite by remember { mutableStateOf(false) }
    var starting by remember { mutableStateOf(false) }
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
    val straightLabel = straight?.let { Geo.formatStraight(it, t("taxiFareKm"), t("mapRadiusMeters")) } ?: "—"
    val subtitle = when {
        dest == null -> t("nearwakeLead")
        tracking != null -> t("mapTrackingSubtitle")
        else -> t("mapReadySubtitle")
    }

    LaunchedEffect(Unit) {
        val track = state.tracking ?: return@LaunchedEffect
        if (dest == null) {
            dest = track.dest
            fromFav = track.fromFavorite
            radius = track.radiusMeters
        }
    }
    LaunchedEffect(state.planCode) {
        radius = Plans.snapRadius(radius, state.planCode)
    }
    LaunchedEffect(dest?.id) {
        dest?.let { store.addRecent(it) }
    }
    LaunchedEffect(tracking) {
        if (tracking != null) starting = false
    }
    LaunchedEffect(starting) {
        if (!starting) return@LaunchedEffect
        delay(2500)
        starting = false
    }

    fun clearDestination() {
        store.stopTracking()
        dest = null
        query = ""
        fromFav = false
        namingFavorite = false
        favoriteName = ""
    }

    fun applyDestination(place: SavedPlace) {
        store.stopTracking()
        dest = place
        query = ""
        fromFav = false
        namingFavorite = false
        favoriteName = ""
    }

    fun chooseRadius(next: Int) {
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

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MapHeaderButton(onBack) { ChevronLeft() }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(t("nearAlarm"), fontFamily = PromptFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Ink)
                Text(subtitle, fontFamily = PromptFamily, fontSize = 13.sp, color = Muted2, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            MapHeaderButton(onSettings) {
                Icon(Icons.Outlined.Settings, contentDescription = t("mapSettings"), tint = Ink, modifier = Modifier.size(22.dp))
            }
        }

        if (state.gpsDenied) {
            Text(
                t("mapGpsDenied"),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFF1F4))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                fontFamily = PromptFamily,
                fontSize = 14.sp,
                color = Color(0xFFC33E83),
                textAlign = TextAlign.Center,
            )
        }

        val current = dest
        if (current != null) {
            Row(
                Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp)
                    .shadow(8.dp, RoundedCornerShape(22.dp), ambientColor = Color(0x1428508C), spotColor = Color(0x1428508C))
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color(0xFFFF4D6D), modifier = Modifier.size(24.dp))
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(current.name, fontFamily = PromptFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (current.detail.isNotBlank()) {
                        Text(current.detail, fontFamily = PromptFamily, fontSize = 13.sp, color = Muted2, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = t("mapClearDestination"),
                    tint = Hint2,
                    modifier = Modifier.size(20.dp).clickable { clearDestination() },
                )
            }
        } else {
            Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp).zIndex(2f)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(22.dp), ambientColor = Color(0x1428508C), spotColor = Color(0x1428508C))
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
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
                if (suggestions.isNotEmpty()) {
                    Column(
                        Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                            .heightIn(max = 220.dp)
                            .shadow(10.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x1F28508C), spotColor = Color(0x1F28508C))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        suggestions.forEach { item ->
                            Column(
                                Modifier.fillMaxWidth().clickable {
                                    scope.launch {
                                        val place = store.pickPlace(item) ?: return@launch
                                        applyDestination(place)
                                        when (item.source) {
                                            "longdo" -> store.toast(t("mapResolvedWithLongdo"))
                                            "google" -> store.toast(t("mapResolvedWithGoogle"))
                                            "nearwake" -> store.toast(t("mapResolvedFromNearwake"))
                                        }
                                    }
                                }.padding(horizontal = 16.dp, vertical = 10.dp),
                            ) {
                                Text(item.name, fontFamily = PromptFamily, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (item.detail.isNotBlank()) Text(item.detail, fontFamily = PromptFamily, fontSize = 12.sp, color = Muted2, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                } else {
                    Text(
                        t("mapLongPressHint"),
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp),
                        fontFamily = PromptFamily,
                        fontSize = 13.sp,
                        color = Color(0xFFA8B0BA),
                    )
                }
            }
        }

        Box(
            Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(22.dp)),
        ) {
            OsmMap(
                modifier = Modifier.fillMaxSize(),
                gps = state.gps,
                dest = destPoint,
                destName = dest?.name,
                radiusMeters = if (dest != null) snapped.toDouble() else null,
                onLongPress = { point ->
                    applyDestination(
                        SavedPlace(
                            UUID.randomUUID().toString(),
                            "${t("mapPickedLocation")} (${"%.5f".format(point.lat)}, ${"%.5f".format(point.lng)})",
                            "",
                            point.lat,
                            point.lng,
                        ),
                    )
                },
                recenterToken = recenter,
                overviewToken = overview,
                compassToken = compass,
            )
            if (state.gps == null && !state.gpsDenied) {
                Text(
                    t("mapWaitingGps"),
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp),
                    fontFamily = PromptFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Ink,
                )
            }
            Column(
                Modifier.align(Alignment.TopEnd).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MapTool(t("mapRouteOverview"), { overview++ }) {
                    Icon(Icons.Outlined.Navigation, contentDescription = null, tint = Color(0xFF3A4350), modifier = Modifier.size(20.dp))
                }
                MapTool(t("mapCompass"), { compass++ }) {
                    Icon(Icons.Outlined.Explore, contentDescription = null, tint = Color(0xFF3A4350), modifier = Modifier.size(20.dp))
                }
                MapTool(t("mapMyLocation"), { recenter++ }) {
                    Icon(Icons.Outlined.MyLocation, contentDescription = null, tint = Color(0xFF3A4350), modifier = Modifier.size(20.dp))
                }
            }
        }

        if (current != null) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), ambientColor = Color(0x14284050), spotColor = Color(0x14284050))
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Color.White)
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 20.dp),
            ) {
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.weight(1f).padding(end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(56.dp).clip(CircleShape).background(Color(0xFFEAF2FF)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Outlined.DirectionsCar, contentDescription = null, tint = Primary, modifier = Modifier.size(28.dp))
                        }
                        Column(Modifier.padding(start = 12.dp)) {
                            Text(t("mapArriveIn"), fontFamily = PromptFamily, fontSize = 13.sp, color = Muted2)
                            Text(
                                straightLabel,
                                fontFamily = PromptFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp,
                                lineHeight = 34.sp,
                                color = Green,
                            )
                            Text(t("mapStraightHint"), fontFamily = PromptFamily, fontSize = 11.sp, color = Hint2)
                        }
                    }
                    Box(Modifier.width(1.dp).fillMaxHeight().background(Color(0xFFE6EAF0)))
                    Column(
                        Modifier.width(132.dp).padding(start = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(t("mapNotifyWhenLeft"), fontFamily = PromptFamily, fontSize = 12.sp, color = Muted2, textAlign = TextAlign.Center)
                        Icon(Icons.Outlined.Notifications, contentDescription = null, tint = Primary, modifier = Modifier.padding(top = 6.dp).size(20.dp))
                        Text(
                            Geo.formatAlarmRadius(snapped, t("taxiFareKm"), t("mapRadiusMeters")),
                            modifier = Modifier.padding(top = 4.dp),
                            fontFamily = PromptFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Primary,
                        )
                    }
                }

                RadiusStops(snapped, maxRadius, t("taxiFareKm"), t("mapRadiusMeters"), ::chooseRadius)

                val active = tracking != null
                Row(
                    Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(
                            if (active) {
                                Brush.horizontalGradient(listOf(Color(0xFFFF8A96), Color(0xFFFF5C6C)))
                            } else {
                                Brush.horizontalGradient(listOf(Primary, Primary))
                            },
                        )
                        .clickable(enabled = !starting) {
                            if (active) {
                                store.stopTracking()
                            } else {
                                starting = true
                                store.startTracking(current, snapped, fromFav)
                            }
                        }
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (starting) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                    } else if (active) {
                        Box(Modifier.padding(end = 8.dp).size(10.dp).clip(CircleShape).background(Color.White))
                    }
                    Text(
                        when {
                            starting -> t("mapStartingTracking")
                            active -> t("mapStopTracking")
                            else -> t("mapStartTracking")
                        },
                        fontFamily = PromptFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.White,
                    )
                }

                if (!fromFav) {
                    if (namingFavorite) {
                        BasicTextField(
                            value = favoriteName,
                            onValueChange = { favoriteName = it.take(150) },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = PromptFamily, fontSize = 15.sp, color = Ink),
                            modifier = Modifier
                                .padding(top = 12.dp)
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFE6EAF0), CircleShape)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            decorationBox = { inner ->
                                Box {
                                    if (favoriteName.isEmpty()) Text(t("mapFavoriteNameHint"), fontFamily = PromptFamily, fontSize = 15.sp, color = Hint2)
                                    inner()
                                }
                            },
                        )
                    }
                    Row(
                        Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .border(1.dp, Primary, CircleShape)
                            .clickable {
                                if (!namingFavorite) {
                                    favoriteName = current.name
                                    namingFavorite = true
                                } else {
                                    val name = favoriteName.trim()
                                    if (name.isBlank()) store.toast(t("mapFavoriteNeedName"))
                                    else {
                                        store.addFavorite(current.copy(name = name))
                                        namingFavorite = false
                                        favoriteName = ""
                                    }
                                }
                            }
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                        Text(
                            t("mapSaveFavoriteAction"),
                            modifier = Modifier.padding(start = 8.dp),
                            fontFamily = PromptFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MapHeaderButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(Modifier.size(44.dp).clickable(onClick = onClick), contentAlignment = Alignment.Center) { content() }
}

@Composable
private fun ChevronLeft() {
    Canvas(Modifier.size(24.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.62f, size.height * 0.22f)
            lineTo(size.width * 0.34f, size.height * 0.5f)
            lineTo(size.width * 0.62f, size.height * 0.78f)
        }
        drawPath(path, Ink, style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
private fun MapTool(label: String, onClick: () -> Unit, icon: @Composable () -> Unit) {
    Box(
        Modifier
            .size(44.dp)
            .shadow(6.dp, CircleShape, ambientColor = Color(0x2428508C), spotColor = Color(0x2428508C))
            .clip(CircleShape)
            .background(Color.White)
            .clickable(onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { icon() }
}

@Composable
private fun RadiusStops(value: Int, maxMeters: Int, kmLabel: String, mLabel: String, onChange: (Int) -> Unit) {
    val stops = Plans.radiusStops
    val index = stops.indexOf(value).coerceAtLeast(0)
    val fraction = index / stops.lastIndex.toFloat()
    Column(Modifier.padding(top = 16.dp, start = 4.dp, end = 4.dp)) {
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .height(32.dp)
                .pointerInput(stops, maxMeters) {
                    fun pick(x: Float) {
                        val ratio = (x / size.width).coerceIn(0f, 1f)
                        val i = (ratio * stops.lastIndex).roundToInt().coerceIn(0, stops.lastIndex)
                        onChange(stops[i])
                    }
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        pick(down.position.x)
                        drag(down.id) { change ->
                            pick(change.position.x)
                            change.consume()
                        }
                    }
                },
        ) {
            Box(Modifier.align(Alignment.Center).fillMaxWidth().height(3.dp).clip(CircleShape).background(Color(0xFFE6EAF0)))
            if (fraction > 0f) {
                Box(Modifier.align(Alignment.CenterStart).fillMaxWidth(fraction).height(3.dp).clip(CircleShape).background(Primary))
            }
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = maxWidth * fraction - 12.dp)
                    .size(24.dp)
                    .shadow(4.dp, CircleShape, ambientColor = Color(0x733B82F6), spotColor = Color(0x733B82F6))
                    .clip(CircleShape)
                    .background(Primary)
                    .border(3.dp, Color.White, CircleShape),
            )
        }
        BoxWithConstraints(Modifier.fillMaxWidth().padding(top = 4.dp).height(24.dp)) {
            stops.forEachIndexed { stopIndex, stop ->
                val selected = stop == value
                val allowed = stop <= maxMeters
                Text(
                    Geo.formatAlarmRadius(stop, kmLabel, mLabel),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = maxWidth * (stopIndex / stops.lastIndex.toFloat()))
                        .graphicsLayer { translationX = -size.width / 2f }
                        .clickable { onChange(stop) },
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
