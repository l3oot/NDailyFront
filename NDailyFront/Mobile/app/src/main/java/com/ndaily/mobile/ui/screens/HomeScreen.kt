package com.ndaily.mobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.LocalTaxi
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.LocalParking
import androidx.compose.material.icons.outlined.Wc
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ndaily.mobile.R
import com.ndaily.mobile.data.AppStore
import com.ndaily.mobile.data.AppUiState
import com.ndaily.mobile.ui.components.AppFooter
import com.ndaily.mobile.ui.components.Chevron
import com.ndaily.mobile.ui.components.DailyDialog
import com.ndaily.mobile.ui.theme.ChipAmber
import com.ndaily.mobile.ui.theme.ChipBlue
import com.ndaily.mobile.ui.theme.ChipGreen
import com.ndaily.mobile.ui.theme.Hint2
import com.ndaily.mobile.ui.theme.Ink
import com.ndaily.mobile.ui.theme.Muted2
import com.ndaily.mobile.ui.theme.Primary
import com.ndaily.mobile.ui.theme.PromptFamily

@Composable
fun HomeScreen(
    state: AppUiState,
    store: AppStore,
    onAccount: () -> Unit,
    onNearwake: () -> Unit,
    onToilet: () -> Unit,
    onTaxi: () -> Unit,
) {
    val t = { key: String -> store.t(key) }
    var soon by remember { mutableStateOf(false) }
    val name = state.profile.userName.ifBlank { state.session?.userName.orEmpty() }
    Box(Modifier.fillMaxSize().background(Color.White)) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 96.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        "${t("homeHelloWord")}${if (name.isNotBlank()) " $name" else ""} 👋",
                        fontFamily = PromptFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = Ink,
                        lineHeight = 38.sp,
                    )
                    Text(t("welcomeLead"), fontFamily = PromptFamily, fontSize = 15.sp, color = Muted2, modifier = Modifier.padding(top = 2.dp))
                }
                val pictureUrl = state.session?.pictureUrl
                Box(
                    Modifier
                        .padding(top = 4.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF2F4F7))
                        .clickable(onClick = onAccount),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!pictureUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = pictureUrl,
                            contentDescription = t("accountTitle"),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(44.dp).clip(CircleShape),
                        )
                    } else {
                        Icon(Icons.Outlined.Person, contentDescription = t("accountTitle"), tint = Color(0xFF9AA3AE), modifier = Modifier.size(24.dp))
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            FeatureCard(
                badge = t("nearAlarm"),
                title = t("homeNearTitle"),
                body = t("homeNearBody"),
                tone = ChipBlue,
                badgeBg = Color(0xFFDCEEFF),
                badgeFg = Primary,
                icon = Icons.Outlined.AccessTime,
                onClick = onNearwake,
            )
            Spacer(Modifier.height(12.dp))
            FeatureCard(
                badge = t("bentoRestroom"),
                title = t("homeToiletTitle"),
                body = t("homeToiletBody"),
                tone = ChipGreen,
                badgeBg = Color(0xFFDDF6E6),
                badgeFg = Color(0xFF2EAF62),
                icon = Icons.Outlined.Wc,
                onClick = onToilet,
            )
            Spacer(Modifier.height(12.dp))
            FeatureCard(
                badge = t("bentoTaxiCardTitle"),
                title = t("homeTaxiTitle"),
                body = t("homeTaxiBody"),
                tone = ChipAmber,
                badgeBg = Color(0xFFFFE8C4),
                badgeFg = Color(0xFFE39B2B),
                icon = Icons.Outlined.LocalTaxi,
                art = {
                    Image(painterResource(R.drawable.ic_taxi), contentDescription = null, modifier = Modifier.height(64.dp))
                },
                onClick = onTaxi,
            )
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(t("homeMoreTools"), fontFamily = PromptFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Ink)
                Row(Modifier.clickable { soon = true }, verticalAlignment = Alignment.CenterVertically) {
                    Text(t("homeSeeAll"), fontFamily = PromptFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Muted2)
                    Chevron(Muted2)
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SoonTool(Modifier.weight(1f), t("homeGas"), Color(0xFFFDE8EA), Color(0xFFE25C6A), Icons.Outlined.LocalGasStation) { soon = true }
                SoonTool(Modifier.weight(1f), t("homeFood"), Color(0xFFF0E8FB), Color(0xFF8B5CF6), Icons.Outlined.Restaurant) { soon = true }
                SoonTool(Modifier.weight(1f), t("homeParking"), Color(0xFFE7F0FB), Primary, Icons.Outlined.LocalParking) { soon = true }
                SoonTool(Modifier.weight(1f), t("homeRain"), Color(0xFFE5F6EA), Color(0xFF2EAF62), Icons.Outlined.CloudQueue) { soon = true }
            }
        }
        Box(Modifier.align(Alignment.BottomCenter)) {
            AppFooter(t, "home", onHome = {}, onSettings = onAccount)
        }
        if (soon) {
            DailyDialog(t("homeMoreTools"), t("comingPhase2"), t("ok")) { soon = false }
        }
    }
}

@Composable
private fun FeatureCard(
    badge: String,
    title: String,
    body: String,
    tone: Color,
    badgeBg: Color,
    badgeFg: Color,
    icon: ImageVector,
    art: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(tone)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 12.dp, top = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Row(
                modifier = Modifier.clip(CircleShape).background(badgeBg).padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, contentDescription = null, tint = badgeFg, modifier = Modifier.size(14.dp))
                Text(badge, modifier = Modifier.padding(start = 4.dp), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = badgeFg)
            }
            Text(title, modifier = Modifier.padding(top = 12.dp), fontFamily = PromptFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Ink, lineHeight = 26.sp)
            Text(body, modifier = Modifier.padding(top = 4.dp), fontFamily = PromptFamily, fontSize = 13.sp, color = Muted2, lineHeight = 18.sp)
        }
        art?.invoke()
        Box(
            Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center,
        ) { Chevron(Hint2) }
    }
}

@Composable
private fun SoonTool(modifier: Modifier, label: String, bg: Color, fg: Color, icon: ImageVector, onClick: () -> Unit) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.fillMaxWidth().height(64.dp).clip(RoundedCornerShape(16.dp)).background(bg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(28.dp))
        }
        Text(
            label,
            modifier = Modifier.padding(top = 8.dp),
            fontFamily = PromptFamily,
            fontSize = 12.sp,
            color = Color(0xFF5C6570),
            lineHeight = 16.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
