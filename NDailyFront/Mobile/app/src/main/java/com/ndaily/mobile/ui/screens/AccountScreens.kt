package com.ndaily.mobile.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ndaily.mobile.data.AppStore
import com.ndaily.mobile.data.AppUiState
import com.ndaily.mobile.data.Geo
import com.ndaily.mobile.data.PlanCode
import com.ndaily.mobile.data.Plans
import com.ndaily.mobile.data.formatBirthdate
import com.ndaily.mobile.data.isValidThaiMobile
import com.ndaily.mobile.data.nextThaiMobile
import com.ndaily.mobile.ui.components.AppFooter
import com.ndaily.mobile.ui.components.Chevron
import com.ndaily.mobile.ui.components.DailyDialog
import com.ndaily.mobile.ui.components.DailySwitch
import com.ndaily.mobile.ui.components.LanguageRowSelector
import com.ndaily.mobile.ui.components.PageBack
import com.ndaily.mobile.ui.components.PrimaryButton
import com.ndaily.mobile.ui.components.TopoBackground
import com.ndaily.mobile.ui.theme.CardDark
import com.ndaily.mobile.ui.theme.Danger
import com.ndaily.mobile.ui.theme.Ink
import com.ndaily.mobile.ui.theme.Line
import com.ndaily.mobile.ui.theme.Muted
import com.ndaily.mobile.ui.theme.Muted2
import com.ndaily.mobile.ui.theme.PlanGreen
import com.ndaily.mobile.ui.theme.Primary
import com.ndaily.mobile.ui.theme.PrimaryAlt
import com.ndaily.mobile.ui.theme.PromptFamily
import com.ndaily.mobile.ui.theme.Taxi
import java.util.Calendar
import kotlinx.coroutines.launch

@Composable
fun AccountScreen(
    state: AppUiState,
    store: AppStore,
    onHome: () -> Unit,
    onWelcome: () -> Unit,
    onNotifications: () -> Unit,
) {
    val t = { key: String -> store.t(key) }
    var tab by remember { mutableIntStateOf(0) }
    var editing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(state.profile.userName) }
    var phone by remember { mutableStateOf(state.profile.userPhone) }
    var birth by remember { mutableStateOf(state.profile.userBirthdate?.take(10).orEmpty()) }
    var confirmOut by remember { mutableStateOf(false) }
    var pendingPlan by remember { mutableStateOf<PlanCode?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val displayName = state.profile.userName.ifBlank { t("accountUser") }
    val plan = Plans.of(state.planCode)

    Box(Modifier.fillMaxSize()) {
        TopoBackground()
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Box(Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
                Text(t("welcomeBrand"), fontFamily = PromptFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Ink)
            }
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 96.dp),
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .padding(20.dp),
                ) {
                    if (!state.loggedIn) {
                        PrimaryButton(t("createAccount"), onClick = onWelcome, color = PrimaryAlt)
                    } else {
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                Modifier.size(72.dp).clip(CircleShape).background(Color(0x1F4A89EA)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Outlined.Person, contentDescription = null, tint = PrimaryAlt, modifier = Modifier.size(40.dp))
                            }
                            Spacer(Modifier.size(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(displayName, fontFamily = PromptFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Ink)
                                Text(
                                    when (state.planCode) {
                                        PlanCode.Plus -> t("planNamePlus")
                                        PlanCode.Pro -> t("planNamePro")
                                        PlanCode.Free -> t("planNameFree")
                                    },
                                    fontFamily = PromptFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = PrimaryAlt,
                                )
                                if (!editing) {
                                    Text(
                                        t("accountEditProfile"),
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .clip(CircleShape)
                                            .background(PlanGreen)
                                            .clickable { editing = true }
                                            .padding(horizontal = 16.dp, vertical = 6.dp),
                                        fontFamily = PromptFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = Color.White,
                                    )
                                }
                            }
                        }
                        Box(Modifier.padding(top = 20.dp).clip(CircleShape).background(Color(0xFFECECEF)).padding(4.dp)) {
                            Row {
                                listOf(t("accountTabGeneral"), t("accountTabPlan"), t("accountTabSettings")).forEachIndexed { index, label ->
                                    Box(
                                        Modifier
                                            .weight(1f)
                                            .clip(CircleShape)
                                            .background(if (tab == index) CardDark else Color.Transparent)
                                            .clickable {
                                                tab = index
                                                if (index != 0) editing = false
                                            }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            label,
                                            fontFamily = PromptFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = if (tab == index) Color.White else Color(0xFF1A1A1A),
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        when (tab) {
                            1 -> {
                                Text(t("planPaymentSoon"), fontFamily = PromptFamily, fontSize = 12.sp, color = Muted, lineHeight = 16.sp)
                                Column(Modifier.padding(top = 12.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFF7F7F8)).padding(14.dp)) {
                                    Text(t("planUsageThisMonth"), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Muted)
                                    UsageBar(t("planFeatureNearwake"), state.nearwakeUsed, plan.nearwakeQuota, t("planTimesUnit"))
                                    UsageBar(t("planFeatureTaxi"), state.taxiUsed, plan.taxiQuota, t("planTimesUnit"))
                                }
                                Spacer(Modifier.height(12.dp))
                                Plans.all.forEach { item ->
                                    PlanCard(
                                        t = t,
                                        name = when (item.code) {
                                            PlanCode.Free -> t("planNameFree")
                                            PlanCode.Plus -> t("planNamePlus")
                                            PlanCode.Pro -> t("planNamePro")
                                        },
                                        plan = item,
                                        current = item.code == state.planCode,
                                        onSelect = { pendingPlan = item.code },
                                    )
                                    Spacer(Modifier.height(12.dp))
                                }
                            }
                            0 -> {
                                if (editing) {
                                    EditField(t("accountFieldUsername"), name, Icons.Outlined.Person) { name = it }
                                    Spacer(Modifier.height(12.dp))
                                    EditField(t("accountFieldPhone"), phone, Icons.Outlined.Phone, KeyboardOptions(keyboardType = KeyboardType.Number)) { incoming ->
                                        val next = nextThaiMobile(phone, incoming)
                                        phone = next.first
                                        if (next.second) store.toast(t("accountPhoneInvalid"))
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    Column {
                                        Text(t("accountFieldBirthdate"), fontFamily = PromptFamily, fontSize = 12.sp, color = Muted, modifier = Modifier.padding(bottom = 4.dp))
                                        Box(
                                            Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(1.dp, Line, RoundedCornerShape(12.dp))
                                                .clickable {
                                                    val cal = Calendar.getInstance()
                                                    DatePickerDialog(
                                                        context,
                                                        { _, y, m, d -> birth = "%04d-%02d-%02d".format(y, m + 1, d) },
                                                        birth.take(4).toIntOrNull() ?: cal.get(Calendar.YEAR) - 20,
                                                        (birth.drop(5).take(2).toIntOrNull() ?: 1) - 1,
                                                        birth.takeLast(2).toIntOrNull() ?: 1,
                                                    ).show()
                                                }
                                                .padding(12.dp),
                                        ) {
                                            Text(birth.ifBlank { t("accountSelectBirthdate") }, fontFamily = PromptFamily, color = Ink)
                                        }
                                    }
                                    Row(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Box(
                                            Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(1.dp, CardDark, RoundedCornerShape(12.dp))
                                                .clickable { editing = false }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center,
                                        ) { Text(t("cancel"), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold) }
                                        Box(
                                            Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(PrimaryAlt)
                                                .clickable {
                                                    if (name.isBlank()) { store.toast(t("accountNameRequired")); return@clickable }
                                                    if (birth.isBlank()) { store.toast(t("accountBirthdateRequired")); return@clickable }
                                                    if (phone.isNotBlank() && !isValidThaiMobile(phone)) { store.toast(t("accountPhoneInvalid")); return@clickable }
                                                    scope.launch {
                                                        if (store.saveProfile(name.trim(), phone, birth.ifBlank { null })) {
                                                            editing = false
                                                        }
                                                    }
                                                }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center,
                                        ) { Text(t("accountSave"), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, color = Color.White) }
                                    }
                                } else {
                                    ReadField(t("accountFieldUsername"), state.profile.userName.ifBlank { "—" }, Icons.Outlined.Person)
                                    Spacer(Modifier.height(12.dp))
                                    ReadField(t("accountFieldPhone"), state.profile.userPhone.ifBlank { "—" }, Icons.Outlined.Phone)
                                    Spacer(Modifier.height(12.dp))
                                    ReadField(t("accountFieldBirthdate"), formatBirthdate(state.profile.userBirthdate, state.locale), Icons.Outlined.Cake)
                                }
                            }
                            else -> {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(t("language"), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Muted)
                                    LanguageRowSelector(state.locale) { store.setLocale(it) }
                                }
                            }
                        }
                    }
                }
                if (state.loggedIn && tab == 2) {
                    Spacer(Modifier.height(20.dp))
                    SettingsRow(Icons.Outlined.Notifications, PrimaryAlt, Color(0x1A4A89EA), t("notifications"), Color.Unspecified, onNotifications)
                    Spacer(Modifier.height(12.dp))
                    SettingsRow(Icons.Outlined.Logout, Color(0xFFB91C1C), Color(0xFFFEF2F2), t("accountLogout"), Color(0xFFB91C1C)) { confirmOut = true }
                }
            }
        }
        Box(Modifier.align(Alignment.BottomCenter)) {
            AppFooter(t, "settings", onHome = onHome, onSettings = {})
        }
        pendingPlan?.let { code ->
            DailyDialog(t("planChangeTitle"), t("planChangeMessage"), t("planSelect"), onConfirm = {
                store.selectPlan(code)
                pendingPlan = null
            }, cancel = t("cancel"), onCancel = { pendingPlan = null })
        }
        if (confirmOut) {
            DailyDialog(
                t("logoutConfirmTitle"),
                t("logoutConfirmMessage"),
                t("accountLogout"),
                onConfirm = {
                    store.signOut()
                    confirmOut = false
                    onWelcome()
                },
                cancel = t("cancel"),
                onCancel = { confirmOut = false },
                confirmColor = Color(0xFFB91C1C),
            )
        }
    }
}

@Composable
private fun UsageBar(label: String, used: Int, quota: Int, unit: String) {
    val pct = if (quota <= 0) 1f else (used.toFloat() / quota).coerceIn(0f, 1f)
    val over = used >= quota && quota > 0
    Column(Modifier.padding(top = 10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontFamily = PromptFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp)
            Text("$used/$quota $unit", fontFamily = PromptFamily, fontSize = 12.sp, color = if (over) Taxi else Muted)
        }
        LinearProgressIndicator(
            progress = { pct },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(6.dp).clip(CircleShape),
            color = if (over) Taxi else PrimaryAlt,
            trackColor = Line,
        )
    }
}

@Composable
private fun PlanCard(
    t: (String) -> String,
    name: String,
    plan: com.ndaily.mobile.data.PlanInfo,
    current: Boolean,
    onSelect: () -> Unit,
) {
    val accent = when (plan.code) {
        PlanCode.Pro -> CardDark
        PlanCode.Plus -> PrimaryAlt
        PlanCode.Free -> Line
    }
    Column(
        Modifier
            .fillMaxWidth()
            .border(2.dp, accent, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(name, fontFamily = PromptFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    if (plan.priceMonth <= 0) t("planFree") else "฿${plan.priceMonth} / ${t("planPerMonth")}",
                    fontFamily = PromptFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = PrimaryAlt,
                )
            }
            if (current) {
                Text(
                    t("planCurrent"),
                    modifier = Modifier.clip(CircleShape).background(PlanGreen).padding(horizontal = 10.dp, vertical = 4.dp),
                    fontFamily = PromptFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = Color.White,
                )
            }
        }
        Text("${t("planFeatureNearwake")} ${plan.nearwakeQuota} ${t("planTimesPerMonth")}", modifier = Modifier.padding(top = 12.dp), fontFamily = PromptFamily, fontSize = 14.sp)
        Text("${t("planNearwakeRadius")} ${Geo.formatAlarmRadius(Plans.radiusMax(plan.code), t("taxiFareKm"), t("mapRadiusMeters"))}", fontFamily = PromptFamily, fontSize = 14.sp)
        Text("${t("planFeatureTaxi")} ${plan.taxiQuota} ${t("planTimesPerMonth")}", fontFamily = PromptFamily, fontSize = 14.sp)
        if (!current) {
            PrimaryButton(t("planSelect"), onClick = onSelect, modifier = Modifier.padding(top = 12.dp), color = PrimaryAlt)
        }
    }
}

@Composable
private fun ReadField(label: String, value: String, icon: ImageVector) {
    Row(
        Modifier.fillMaxWidth().border(1.dp, Line, RoundedCornerShape(12.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Muted, modifier = Modifier.size(22.dp))
        Column(Modifier.padding(start = 12.dp)) {
            Text(label, fontFamily = PromptFamily, fontSize = 11.sp, color = Muted)
            Text(value, fontFamily = PromptFamily, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun EditField(
    label: String,
    value: String,
    icon: ImageVector,
    keyboard: KeyboardOptions = KeyboardOptions.Default,
    onChange: (String) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().border(1.dp, Line, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Muted, modifier = Modifier.size(22.dp))
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Text(label, fontFamily = PromptFamily, fontSize = 11.sp, color = Muted)
            BasicTextField(
                value = value,
                onValueChange = onChange,
                textStyle = TextStyle(fontFamily = PromptFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Ink),
                keyboardOptions = keyboard,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, tint: Color, bg: Color, label: String, textColor: Color, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(bg), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint)
        }
        Text(label, modifier = Modifier.weight(1f).padding(horizontal = 12.dp), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, color = textColor)
        Chevron()
    }
}

@Composable
fun NotificationScreen(state: AppUiState, store: AppStore, onBack: () -> Unit) {
    val t = { key: String -> store.t(key) }
    Box(Modifier.fillMaxSize()) {
        TopoBackground()
        Column(Modifier.statusBarsPadding()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PageBack(onBack)
                Text(t("notifications"), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            }
            Column(Modifier.padding(20.dp)) {
                NotifyRow(t("notifyVibration"), state.notifyVibration) { store.setNotify(vibration = !state.notifyVibration) }
                Spacer(Modifier.height(16.dp))
                NotifyRow(t("notifySound"), state.notifySound) { store.setNotify(sound = !state.notifySound) }
            }
        }
    }
}

@Composable
private fun NotifyRow(label: String, value: Boolean, onToggle: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold)
        DailySwitch(value, onToggle = onToggle)
    }
}
