package com.ndaily.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalTaxi
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SwapVert
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ndaily.mobile.data.AppStore
import com.ndaily.mobile.data.AppUiState
import com.ndaily.mobile.data.DrivingRoute
import com.ndaily.mobile.data.Geo
import com.ndaily.mobile.data.LatLng
import com.ndaily.mobile.data.PlaceSuggestion
import com.ndaily.mobile.data.TaxiFare
import com.ndaily.mobile.data.TaxiFareBand
import com.ndaily.mobile.ui.components.Chevron
import com.ndaily.mobile.ui.components.DailyDialog
import com.ndaily.mobile.ui.components.PageBack
import com.ndaily.mobile.ui.components.rememberPlaceSuggestions
import com.ndaily.mobile.ui.map.OsmMap
import com.ndaily.mobile.ui.theme.Ink
import com.ndaily.mobile.ui.theme.Muted
import com.ndaily.mobile.ui.theme.Primary
import com.ndaily.mobile.ui.theme.PromptFamily
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

private val PageBg = Color(0xFFF5F7FB)
private val CardLine = Color(0xFFE6EAF0)
private val SoftBlue = Color(0xFFEAF3FF)
private val Gold = Color(0xFFF5B942)
private val PinRed = Color(0xFFFF4D6D)

@Composable
fun TaxiScreen(state: AppUiState, store: AppStore, onBack: () -> Unit) {
    val t = { key: String -> store.t(key) }
    var origin by remember { mutableStateOf<LatLng?>(null) }
    var dest by remember { mutableStateOf<LatLng?>(null) }
    var originText by remember { mutableStateOf("") }
    var destText by remember { mutableStateOf("") }
    var editOrigin by remember { mutableStateOf(false) }
    var editDest by remember { mutableStateOf(false) }
    var originLocked by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf("taxi") }
    var route by remember { mutableStateOf<DrivingRoute?>(null) }
    var fallback by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var gpsError by remember { mutableStateOf("") }
    var rateOpen by remember { mutableStateOf(false) }
    var overview by remember { mutableIntStateOf(0) }
    var recenter by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val here = t("taxiFareOriginMyLocation")
    val originSuggestions = rememberPlaceSuggestions(store, if (editOrigin) originText else "")
    val destSuggestions = rememberPlaceSuggestions(store, if (editDest) destText else "")

    LaunchedEffect(state.gps) {
        val gps = state.gps ?: return@LaunchedEffect
        if (!originLocked && origin == null) {
            origin = gps
            originText = here
        }
    }

    LaunchedEffect(origin, dest) {
        val from = origin
        val to = dest
        if (from == null || to == null) {
            route = null
            fallback = false
            loading = false
            return@LaunchedEffect
        }
        loading = true
        fallback = false
        if (!store.consumeTaxi()) {
            route = null
            loading = false
            return@LaunchedEffect
        }
        val driven = store.drivingRoute(from, to)
        if (driven == null) {
            route = DrivingRoute(Geo.haversineKm(from, to), path = listOf(from, to))
            fallback = true
        } else {
            route = driven.copy(path = driven.path.ifEmpty { listOf(from, to) })
            fallback = false
        }
        loading = false
        overview++
    }

    val km = route?.distanceKm
    val band = km?.let { TaxiFare.estimateBand(it) }
    val minutes = km?.let { timeRange(route?.durationSeconds, it) }

    Box(Modifier.fillMaxSize().background(PageBg)) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(bottom = 20.dp),
        ) {
            Row(Modifier.fillMaxWidth().padding(end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                PageBack(onBack)
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(t("bentoTaxiCardTitle"), fontFamily = PromptFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Ink)
                    Text(t("taxiFareSubtitle"), fontFamily = PromptFamily, fontSize = 13.sp, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Box(
                    Modifier.size(48.dp).clickable { rateOpen = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        Modifier.size(36.dp).shadow(4.dp, CircleShape).clip(CircleShape).background(Color.White),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.Schedule, contentDescription = t("taxiFareRateDetails"), tint = Ink, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Column(
                Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp)
                    .shadow(8.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(Primary))
                        Spacer(Modifier.height(4.dp))
                        repeat(3) {
                            Box(Modifier.padding(vertical = 2.dp).size(3.dp).clip(CircleShape).background(Color(0xFFD0D5DD)))
                        }
                        Spacer(Modifier.height(4.dp))
                        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = PinRed, modifier = Modifier.size(18.dp))
                    }
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        PlaceLine(
                            text = originText,
                            editing = editOrigin,
                            placeholder = t("taxiFareOrigin"),
                            onEdit = {
                                editOrigin = true
                                editDest = false
                                originLocked = true
                                if (originText == here) originText = ""
                            },
                            onChange = {
                                originText = it
                                origin = null
                                editOrigin = true
                                originLocked = true
                                gpsError = ""
                            },
                        )
                        Spacer(Modifier.height(14.dp))
                        PlaceLine(
                            text = destText,
                            editing = editDest,
                            placeholder = t("taxiFareDestination"),
                            onEdit = {
                                editDest = true
                                editOrigin = false
                            },
                            onChange = {
                                destText = it
                                dest = null
                                editDest = true
                            },
                            onClear = if (dest != null) {
                                {
                                    dest = null
                                    destText = ""
                                    editDest = false
                                }
                            } else {
                                null
                            },
                        )
                    }
                    Box(
                        Modifier
                            .padding(start = 8.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable {
                                val nextOrigin = dest
                                val nextOriginText = destText
                                dest = origin
                                destText = if (originText == here) here else originText
                                origin = nextOrigin
                                originText = nextOriginText
                                editOrigin = false
                                editDest = false
                                originLocked = true
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.SwapVert, contentDescription = null, tint = Color(0xFF98A2B3), modifier = Modifier.size(22.dp))
                    }
                }
            }

            SuggestionList(if (editOrigin) originSuggestions else destSuggestions) { item ->
                scope.launch {
                    val place = store.pickPlace(item) ?: return@launch
                    val point = LatLng(place.lat, place.lng)
                    if (editOrigin) {
                        origin = point
                        originText = place.name
                        editOrigin = false
                        originLocked = true
                    } else {
                        dest = point
                        destText = place.name
                        editDest = false
                    }
                    gpsError = ""
                }
            }

            if (gpsError.isNotBlank()) {
                Text(gpsError, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), fontFamily = PromptFamily, fontSize = 13.sp, color = PinRed)
            }

            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ServiceCard(t("taxiFareTaxiName"), Icons.Outlined.LocalTaxi, selected == "taxi", Modifier.weight(1f)) { selected = "taxi" }
                ServiceCard(t("taxiFareOtherShort"), Icons.Outlined.MoreHoriz, selected == "other", Modifier.weight(1f)) { selected = "other" }
            }

            if (origin != null && dest != null) {
                Box(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .height(210.dp)
                        .shadow(8.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp)),
                ) {
                    OsmMap(
                        modifier = Modifier.fillMaxSize(),
                        gps = origin,
                        dest = dest,
                        path = route?.path ?: listOfNotNull(origin, dest),
                        pathColor = 0xFF3B82F6.toInt(),
                        overviewToken = overview,
                        recenterToken = recenter,
                    )
                    Row(
                        Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Outlined.Navigation, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
                        Text(t("taxiFareRouteBadge"), modifier = Modifier.padding(start = 4.dp), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Ink)
                    }
                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .size(40.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable {
                                val gps = state.gps
                                if (gps == null) gpsError = t("taxiFareGpsError")
                                else {
                                    origin = gps
                                    originText = here
                                    editOrigin = false
                                    originLocked = true
                                    gpsError = ""
                                    recenter++
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.MyLocation, contentDescription = t("mapMyLocation"), tint = Ink, modifier = Modifier.size(18.dp))
                    }
                }

                if (loading) {
                    Text(t("taxiFareRouteLoading"), modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), fontFamily = PromptFamily, fontSize = 12.sp, color = Muted)
                }
                if (fallback) {
                    Text(t("taxiFareRouteFallback"), modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), fontFamily = PromptFamily, fontSize = 12.sp, color = Color(0xFF9A3412))
                }

                if (km != null && minutes != null) {
                    Row(
                        Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        StatCell(Icons.Outlined.LocalTaxi, t("taxiFareDistanceLabel"), "${"%.1f".format(km)} ${t("taxiFareKm")}", Modifier.weight(1f))
                        Box(Modifier.width(1.dp).height(36.dp).background(CardLine))
                        StatCell(Icons.Outlined.AccessTime, t("taxiFareTimeLabel"), "${minutes.first} - ${minutes.second} ${t("taxiFareMinuteUnit")}", Modifier.weight(1f))
                    }
                }
            }

            if (band != null) {
                Column(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    FareOption(
                        icon = Icons.Outlined.LocalTaxi,
                        iconBg = Gold,
                        iconTint = Color.White,
                        title = t("taxiFareTaxiName"),
                        subtitle = t("taxiFareMetered"),
                        subtitleColor = Primary,
                        price = bahtSpan(band.taxiLow, band.taxiHigh),
                        selected = selected == "taxi",
                        onClick = { selected = "taxi" },
                    )
                    FareOption(
                        icon = Icons.Outlined.MoreHoriz,
                        iconBg = Color(0xFFEEF2F6),
                        iconTint = Color(0xFF667085),
                        title = t("taxiFareOtherName"),
                        subtitle = t("taxiFareOtherHint"),
                        subtitleColor = Muted,
                        price = bahtSpan(band.otherLow, band.otherHigh),
                        selected = selected == "other",
                        onClick = { selected = "other" },
                    )
                }
            }

            Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = Muted, modifier = Modifier.size(16.dp))
                    Text(t("taxiFareNoteTitle"), modifier = Modifier.padding(start = 6.dp), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Ink)
                }
                NoteLine(t("taxiFareNoteApprox"))
                NoteLine(t("taxiFareNoteDepends"))
                Text(
                    "${t("planUsageRemaining")} ${state.taxiUsed}/${store.taxiQuota()} ${t("planTimesUnit")}",
                    modifier = Modifier.padding(top = 10.dp),
                    fontFamily = PromptFamily,
                    fontSize = 12.sp,
                    color = Muted,
                )
            }
        }
        if (rateOpen) {
            DailyDialog(t("taxiFareRateDetails"), t("taxiFareRateIntro"), t("ok"), onConfirm = { rateOpen = false }, confirmColor = Primary)
        }
    }
}

@Composable
private fun PlaceLine(
    text: String,
    editing: Boolean,
    placeholder: String,
    onEdit: () -> Unit,
    onChange: (String) -> Unit,
    onClear: (() -> Unit)? = null,
) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(editing) {
        if (!editing) return@LaunchedEffect
        yield()
        runCatching { focus.requestFocus() }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (editing) {
            BasicTextField(
                value = text,
                onValueChange = onChange,
                singleLine = true,
                textStyle = TextStyle(fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Ink),
                modifier = Modifier.weight(1f).focusRequester(focus),
                decorationBox = { inner ->
                    Box {
                        if (text.isEmpty()) Text(placeholder, fontFamily = PromptFamily, fontSize = 16.sp, color = Color(0xFFC5CDD6))
                        inner()
                    }
                },
            )
        } else {
            Text(
                text.ifBlank { placeholder },
                modifier = Modifier.weight(1f).clickable(onClick = onEdit),
                fontFamily = PromptFamily,
                fontWeight = if (text.isBlank()) FontWeight.Medium else FontWeight.SemiBold,
                fontSize = 16.sp,
                color = if (text.isBlank()) Color(0xFFC5CDD6) else Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (onClear != null) {
            Icon(
                Icons.Outlined.Close,
                contentDescription = null,
                tint = Color(0xFF98A2B3),
                modifier = Modifier.padding(start = 8.dp).size(18.dp).clickable(onClick = onClear),
            )
        }
    }
}

@Composable
private fun SuggestionList(items: List<PlaceSuggestion>, onPick: (PlaceSuggestion) -> Unit) {
    if (items.isEmpty()) return
    Column(
        Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White),
    ) {
        items.take(4).forEach { item ->
            Column(
                Modifier.fillMaxWidth().clickable { onPick(item) }.padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Text(item.name, fontFamily = PromptFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (item.detail.isNotBlank()) {
                    Text(item.detail, fontFamily = PromptFamily, fontSize = 12.sp, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun ServiceCard(label: String, icon: ImageVector, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) SoftBlue else Color.White)
            .border(1.dp, if (selected) Color(0xFFD6E6FF) else CardLine, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) Primary else Color(0xFF667085), modifier = Modifier.size(22.dp))
        Text(
            label,
            modifier = Modifier.padding(top = 6.dp, start = 8.dp, end = 8.dp),
            fontFamily = PromptFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = if (selected) Primary else Color(0xFF3C3C43),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun StatCell(icon: ImageVector, label: String, value: String, modifier: Modifier) {
    Row(modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color(0xFF98A2B3), modifier = Modifier.size(18.dp))
        Column(Modifier.padding(start = 8.dp)) {
            Text(label, fontFamily = PromptFamily, fontSize = 12.sp, color = Muted)
            Text(value, fontFamily = PromptFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun FareOption(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    subtitleColor: Color,
    price: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) SoftBlue else Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(iconBg), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(title, fontFamily = PromptFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, fontFamily = PromptFamily, fontSize = 12.sp, color = subtitleColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(price, fontFamily = PromptFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Ink)
        Chevron(Color(0xFFC5CDD6))
    }
}

@Composable
private fun NoteLine(text: String) {
    Row(Modifier.padding(top = 6.dp)) {
        Text("•", fontFamily = PromptFamily, fontSize = 13.sp, color = Muted)
        Text(text, modifier = Modifier.padding(start = 6.dp), fontFamily = PromptFamily, fontSize = 13.sp, color = Muted, lineHeight = 18.sp)
    }
}

private fun timeRange(durationSeconds: Int?, km: Double): Pair<Int, Int> {
    val base = if (durationSeconds != null && durationSeconds > 0) {
        (durationSeconds / 60.0).roundToInt().coerceAtLeast(1)
    } else {
        (km / 40.0 * 60.0).roundToInt().coerceAtLeast(1)
    }
    val high = (base * 1.4).roundToInt().coerceAtLeast(base + 1)
    return base to high
}

private fun bahtSpan(low: Int, high: Int) = "฿${"%,d".format(low)} - ${"%,d".format(high)}"
