package com.ndaily.mobile.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ndaily.mobile.R
import com.ndaily.mobile.data.AppStore
import com.ndaily.mobile.data.AppUiState
import com.ndaily.mobile.data.Geo
import com.ndaily.mobile.data.LatLng
import com.ndaily.mobile.data.PlaceSuggestion
import com.ndaily.mobile.data.TaxiFare
import com.ndaily.mobile.data.TaxiFareResult
import com.ndaily.mobile.ui.components.DailyDialog
import com.ndaily.mobile.ui.components.DailySwitch
import com.ndaily.mobile.ui.components.PageBack
import com.ndaily.mobile.ui.components.PrimaryButton
import com.ndaily.mobile.ui.components.TopoBackground
import com.ndaily.mobile.ui.components.rememberPlaceSuggestions
import com.ndaily.mobile.ui.map.OsmMap
import com.ndaily.mobile.ui.theme.Background
import com.ndaily.mobile.ui.theme.Ink
import com.ndaily.mobile.ui.theme.Line
import com.ndaily.mobile.ui.theme.Muted
import com.ndaily.mobile.ui.theme.PromptFamily
import com.ndaily.mobile.ui.theme.Taxi
import kotlinx.coroutines.launch

@Composable
fun TaxiScreen(state: AppUiState, store: AppStore, onBack: () -> Unit) {
    val t = { key: String -> store.t(key) }
    var originLabel by remember { mutableStateOf("") }
    var destLabel by remember { mutableStateOf("") }
    var origin by remember { mutableStateOf<LatLng?>(null) }
    var dest by remember { mutableStateOf<LatLng?>(null) }
    var originQuery by remember { mutableStateOf(true) }
    var destQuery by remember { mutableStateOf(true) }
    var traffic by remember { mutableStateOf("") }
    var optApp by remember { mutableStateOf(false) }
    var optAirport by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<TaxiFareResult?>(null) }
    var need by remember { mutableStateOf("") }
    var rateOpen by remember { mutableStateOf(false) }
    var gpsError by remember { mutableStateOf("") }
    var routeKm by remember { mutableStateOf<Double?>(null) }
    val scope = rememberCoroutineScope()
    val originSuggestions = rememberPlaceSuggestions(store, if (originQuery) originLabel else "")
    val destSuggestions = rememberPlaceSuggestions(store, if (destQuery) destLabel else "")
    val canCalculate = origin != null && dest != null

    Box(Modifier.fillMaxSize()) {
        TopoBackground()
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            PageBack(onBack)
            Box(
                Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Taxi)
                    .padding(20.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(t("bentoTaxiCardTitle"), fontFamily = PromptFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.White)
                        Text(t("taxiFareSubtitle"), modifier = Modifier.padding(top = 4.dp), fontFamily = PromptFamily, fontSize = 13.sp, color = Color(0xFFF2D5E5), lineHeight = 18.sp)
                    }
                    Image(painterResource(R.drawable.ic_taxi), contentDescription = null, modifier = Modifier.width(78.dp))
                }
            }
            Box(Modifier.fillMaxWidth().padding(end = 20.dp, top = 4.dp), contentAlignment = Alignment.CenterEnd) {
                Text(
                    t("taxiFareRateDetails"),
                    modifier = Modifier.clickable { rateOpen = true },
                    fontFamily = PromptFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Taxi,
                )
            }
            Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp).clip(RoundedCornerShape(20.dp)).background(Background).padding(bottom = 8.dp)) {
                Row(Modifier.clip(RoundedCornerShape(12.dp)).border(1.dp, Line, RoundedCornerShape(12.dp)).background(Color.White)) {
                    PlaceBox(
                        modifier = Modifier.weight(1f),
                        label = t("taxiFareOrigin"),
                        value = originLabel,
                        onChange = {
                            originLabel = it
                            origin = null
                            originQuery = true
                            result = null
                            routeKm = null
                            need = ""
                        },
                        onPick = { item ->
                            scope.launch {
                                val place = store.pickPlace(item) ?: return@launch
                                origin = LatLng(place.lat, place.lng)
                                originLabel = place.name
                                originQuery = false
                                result = null
                                dest?.let { d ->
                                    routeKm = store.drivingKm(LatLng(place.lat, place.lng), d) ?: Geo.haversineKm(LatLng(place.lat, place.lng), d)
                                }
                            }
                        },
                        suggestions = originSuggestions,
                        enabled = originQuery || origin == null,
                        placeholder = t("taxiFareSearchHint"),
                    )
                    Box(Modifier.width(1.dp).height(72.dp).background(Line).align(Alignment.CenterVertically))
                    PlaceBox(
                        modifier = Modifier.weight(1f),
                        label = t("taxiFareDestination"),
                        value = destLabel,
                        onChange = {
                            destLabel = it
                            dest = null
                            destQuery = true
                            result = null
                            routeKm = null
                            need = ""
                        },
                        onPick = { item ->
                            scope.launch {
                                val place = store.pickPlace(item) ?: return@launch
                                dest = LatLng(place.lat, place.lng)
                                destLabel = place.name
                                destQuery = false
                                result = null
                                origin?.let { o ->
                                    routeKm = store.drivingKm(o, LatLng(place.lat, place.lng)) ?: Geo.haversineKm(o, LatLng(place.lat, place.lng))
                                }
                            }
                        },
                        suggestions = destSuggestions,
                        enabled = destQuery || dest == null,
                        placeholder = t("taxiFareSearchHint"),
                    )
                }
                Row(
                    Modifier.padding(top = 4.dp, start = 4.dp).clickable {
                        val gps = state.gps
                        if (gps == null) gpsError = t("taxiFareGpsError")
                        else {
                            origin = gps
                            originLabel = t("taxiFareOriginMyLocation")
                            originQuery = false
                            result = null
                            gpsError = ""
                            dest?.let { d ->
                                scope.launch {
                                    routeKm = store.drivingKm(gps, d) ?: Geo.haversineKm(gps, d)
                                }
                            }
                        }
                    },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.MyLocation, contentDescription = null, tint = Taxi, modifier = Modifier.padding(end = 6.dp))
                    Text(t("taxiFareOriginMyLocation"), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Taxi)
                }
                Text(t("taxiFareTraffic"), modifier = Modifier.padding(top = 12.dp), fontFamily = PromptFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Muted)
                Box(Modifier.padding(top = 8.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).border(1.dp, Line, RoundedCornerShape(12.dp)).background(Color.White).padding(14.dp)) {
                    BasicTextField(
                        value = traffic,
                        onValueChange = { traffic = it.filter { ch -> ch.isDigit() }; result = null },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(fontFamily = PromptFamily, fontSize = 16.sp, color = Ink),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Text(t("taxiFareTrafficHelp"), modifier = Modifier.padding(top = 4.dp), fontFamily = PromptFamily, fontSize = 12.sp, color = Muted)
                SwitchRow(t("taxiFareOptApp"), optApp) { optApp = !optApp; result = null }
                Box(Modifier.fillMaxWidth().height(1.dp).background(Line))
                SwitchRow(t("taxiFareOptAirport"), optAirport) { optAirport = !optAirport; result = null }
                routeKm?.let { km ->
                    Column(Modifier.padding(top = 14.dp).border(width = 0.dp, color = Color.Transparent, shape = RoundedCornerShape(0.dp))) {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(Line))
                        Text(
                            "${t("taxiFareDistanceRoute")}: ${TaxiFare.money(km)} ${t("taxiFareKm")}",
                            modifier = Modifier.padding(top = 12.dp),
                            fontFamily = PromptFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Taxi,
                        )
                        Text(t("taxiFareRouteFallback"), modifier = Modifier.padding(top = 6.dp), fontFamily = PromptFamily, fontSize = 12.sp, color = Color(0xFF9A3412), lineHeight = 16.sp)
                        Box(Modifier.padding(top = 12.dp).fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp))) {
                            OsmMap(
                                modifier = Modifier.fillMaxSize(),
                                gps = null,
                                origin = origin,
                                dest = dest,
                                path = listOfNotNull(origin, dest),
                                overviewToken = 1,
                            )
                        }
                    }
                }
            }
            if (gpsError.isNotBlank()) Text(gpsError, modifier = Modifier.padding(horizontal = 20.dp), fontFamily = PromptFamily, fontSize = 14.sp, color = Taxi)
            PrimaryButton(
                t("taxiFareCalculate"),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                color = Taxi,
                enabled = canCalculate,
                onClick = {
                    val from = origin
                    val to = dest
                    if (from == null) { need = t("taxiFareSelectOrigin"); return@PrimaryButton }
                    if (to == null) { need = t("taxiFareSelectPlace"); return@PrimaryButton }
                    scope.launch {
                        val km = routeKm ?: store.drivingKm(from, to) ?: Geo.haversineKm(from, to)
                        routeKm = km
                        if (!store.consumeTaxi()) {
                            need = t("planQuotaExceededTaxi")
                            result = null
                            return@launch
                        }
                        need = ""
                        result = TaxiFare.compute(km, traffic.toIntOrNull() ?: 0, optApp, optAirport)
                    }
                },
            )
            Text(
                "${t("planUsageRemaining")} ${state.taxiUsed}/${store.taxiQuota()} ${t("planTimesUnit")}",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                fontFamily = PromptFamily,
                fontSize = 12.sp,
                color = Muted,
            )
            if (need.isNotBlank()) Text(need, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), fontFamily = PromptFamily, fontSize = 14.sp, color = Taxi)
            result?.let { fare ->
                Column(
                    Modifier
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, Taxi.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                        .background(Background)
                        .padding(20.dp),
                ) {
                    Text(t("taxiFareBreakdown"), fontFamily = PromptFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Box(Modifier.padding(vertical = 12.dp).fillMaxWidth().height(1.dp).background(Line))
                    fare.distanceLines.forEach { line ->
                        FareLine(line.label, "${TaxiFare.money(line.amountBaht)} ${t("taxiFareBaht")}")
                    }
                    if (fare.trafficBaht > 0) FareLine(t("taxiFareTrafficCharge"), "${TaxiFare.money(fare.trafficBaht)} ${t("taxiFareBaht")}")
                    if (fare.appSurcharge > 0) FareLine(t("taxiFareSurchargeApp"), "${TaxiFare.money(fare.appSurcharge)} ${t("taxiFareBaht")}")
                    if (fare.airportSurcharge > 0) FareLine(t("taxiFareSurchargeAirport"), "${TaxiFare.money(fare.airportSurcharge)} ${t("taxiFareBaht")}")
                    Box(Modifier.padding(vertical = 16.dp).fillMaxWidth().height(1.dp).background(Line))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(t("taxiFareTotal"), fontFamily = PromptFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("${TaxiFare.money(fare.totalBaht)} ${t("taxiFareBaht")}", fontFamily = PromptFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Taxi)
                    }
                }
            }
            Text(t("taxiFareDisclaimer"), modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), fontFamily = PromptFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Muted, lineHeight = 18.sp)
        }
        if (rateOpen) {
            DailyDialog(t("taxiFareRateDetails"), t("taxiFareRateIntro"), t("ok"), onConfirm = { rateOpen = false }, confirmColor = Taxi)
        }
    }
}

@Composable
private fun FareLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, modifier = Modifier.weight(1f).padding(end = 12.dp), fontFamily = PromptFamily, fontSize = 13.sp, color = Muted)
        Text(value, fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onToggle: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f).padding(end = 12.dp), fontFamily = PromptFamily, fontSize = 14.sp)
        DailySwitch(checked, color = Taxi, onToggle = onToggle)
    }
}

@Composable
private fun PlaceBox(
    modifier: Modifier,
    label: String,
    value: String,
    onChange: (String) -> Unit,
    onPick: (PlaceSuggestion) -> Unit,
    suggestions: List<PlaceSuggestion>,
    enabled: Boolean,
    placeholder: String,
) {
    val shown = if (enabled) suggestions else emptyList()
    Column(modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
        Text(label, fontFamily = PromptFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Muted)
        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            textStyle = TextStyle(fontFamily = PromptFamily, fontSize = 15.sp, color = Ink),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) Text(placeholder, fontFamily = PromptFamily, fontSize = 14.sp, color = Muted)
                    inner()
                }
            },
        )
        shown.take(4).forEach { item ->
            Text(
                item.name,
                modifier = Modifier.fillMaxWidth().clickable { onPick(item) }.padding(vertical = 4.dp),
                fontFamily = PromptFamily,
                fontSize = 13.sp,
                color = Ink,
            )
        }
    }
}
