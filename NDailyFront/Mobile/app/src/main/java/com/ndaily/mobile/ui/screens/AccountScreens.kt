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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ndaily.mobile.BuildConfig
import com.ndaily.mobile.data.AppStore
import com.ndaily.mobile.data.AppUiState
import com.ndaily.mobile.data.DistanceUnit
import com.ndaily.mobile.data.Geo
import com.ndaily.mobile.data.PlanCode
import com.ndaily.mobile.data.Plans
import com.ndaily.mobile.data.formatBirthdate
import com.ndaily.mobile.data.formatThaiMobile
import com.ndaily.mobile.data.isValidThaiMobile
import com.ndaily.mobile.data.nextThaiMobile
import com.ndaily.mobile.ui.components.AppFooter
import com.ndaily.mobile.ui.components.Chevron
import com.ndaily.mobile.ui.components.DailyDialog
import com.ndaily.mobile.ui.components.DailySwitch
import com.ndaily.mobile.ui.components.LanguageRowSelector
import com.ndaily.mobile.ui.components.PageBack
import com.ndaily.mobile.ui.components.PrimaryButton
import com.ndaily.mobile.ui.theme.CardDark
import com.ndaily.mobile.ui.theme.Ink
import com.ndaily.mobile.ui.theme.Line
import com.ndaily.mobile.ui.theme.Muted
import com.ndaily.mobile.ui.theme.NearwakeBg
import com.ndaily.mobile.ui.theme.PlanGreen
import com.ndaily.mobile.ui.theme.PrimaryAlt
import com.ndaily.mobile.ui.theme.PromptFamily
import com.ndaily.mobile.ui.theme.Taxi
import java.util.Calendar
import kotlinx.coroutines.launch

private val PageBg = NearwakeBg
private val SectionLabel = Color(0xFF8B95A1)
private val IconPersonBg = Color(0xFFE8F1FE)
private val IconNotifyBg = Color(0xFFFFF0E6)
private val IconLangBg = Color(0xFFF3E8FF)
private val IconDistanceBg = Color(0xFFE8F8EF)
private val IconInfoBg = Color(0xFFE8F1FE)
private val IconDocBg = Color(0xFFF1F3F5)
private val IconNotify = Color(0xFFF08A3A)
private val IconLang = Color(0xFF8B5CF6)
private val IconDistance = Color(0xFF22A06B)
private val IconDoc = Color(0xFF6B7280)
private val ProfileHeroBg = Color(0xFFEAF3FC)
private val EditBtnBg = Color(0xFFDCEBFA)
private val FieldPersonBg = Color(0xFFE8F1FE)
private val FieldBirthBg = Color(0xFFFFEBEE)
private val FieldPhoneBg = Color(0xFFE8F8EF)
private val FieldBirth = Color(0xFFE53935)
private val FieldPhone = Color(0xFF22A06B)

@Composable
fun SettingsScreen(
    state: AppUiState,
    store: AppStore,
    onHome: () -> Unit,
    onWelcome: () -> Unit,
    onProfile: () -> Unit,
    onNotifications: () -> Unit,
    onTerms: () -> Unit,
    onPrivacy: () -> Unit,
) {
    val t = { key: String -> store.t(key) }
    var confirmOut by remember { mutableStateOf(false) }
    var languageOpen by remember { mutableStateOf(false) }
    var distanceOpen by remember { mutableStateOf(false) }
    var aboutOpen by remember { mutableStateOf(false) }
    var legalOpen by remember { mutableStateOf(false) }

    val distanceSubtitle = when (state.distanceUnit) {
        DistanceUnit.Km -> t("settingsDistanceKm")
        DistanceUnit.Mi -> t("settingsDistanceMi")
    }

    Box(Modifier.fillMaxSize().background(PageBg)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            SettingsHeader(t("settingsTitle"), onBack = onHome)
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 108.dp),
            ) {
                SettingsSectionLabel(t("settingsSectionAccount"))
                SettingsCard {
                    SettingsNavRow(
                        icon = Icons.Outlined.Person,
                        iconTint = PrimaryAlt,
                        iconBg = IconPersonBg,
                        title = t("settingsMyProfile"),
                        subtitle = t("settingsMyProfileHint"),
                        onClick = {
                            if (state.loggedIn) onProfile() else onWelcome()
                        },
                    )
                }

                Spacer(Modifier.height(20.dp))
                SettingsSectionLabel(t("settingsSectionApp"))
                SettingsCard {
                    SettingsNavRow(
                        icon = Icons.Outlined.Notifications,
                        iconTint = IconNotify,
                        iconBg = IconNotifyBg,
                        title = t("notifications"),
                        subtitle = t("settingsNotificationsHint"),
                        onClick = onNotifications,
                    )
                    SettingsDivider()
                    SettingsNavRow(
                        icon = Icons.Outlined.Language,
                        iconTint = IconLang,
                        iconBg = IconLangBg,
                        title = t("language"),
                        subtitle = t("settingsLanguageHint"),
                        onClick = { languageOpen = true },
                    )
                    SettingsDivider()
                    SettingsNavRow(
                        icon = Icons.Outlined.NearMe,
                        iconTint = IconDistance,
                        iconBg = IconDistanceBg,
                        title = t("settingsDistanceUnits"),
                        subtitle = distanceSubtitle,
                        onClick = { distanceOpen = true },
                    )
                }

                Spacer(Modifier.height(20.dp))
                SettingsSectionLabel(t("settingsSectionAbout"))
                SettingsCard {
                    SettingsNavRow(
                        icon = Icons.Outlined.Info,
                        iconTint = PrimaryAlt,
                        iconBg = IconInfoBg,
                        title = t("settingsAboutApp"),
                        subtitle = t("settingsVersion").replace("{version}", BuildConfig.VERSION_NAME),
                        onClick = { aboutOpen = true },
                    )
                    SettingsDivider()
                    SettingsNavRow(
                        icon = Icons.Outlined.Description,
                        iconTint = IconDoc,
                        iconBg = IconDocBg,
                        title = t("settingsTermsPolicies"),
                        subtitle = t("settingsTermsPoliciesHint"),
                        onClick = { legalOpen = true },
                    )
                }

                if (state.loggedIn) {
                    Spacer(Modifier.height(20.dp))
                    SettingsCard {
                        SettingsNavRow(
                            icon = Icons.Outlined.Logout,
                            iconTint = Color(0xFFB91C1C),
                            iconBg = Color(0xFFFEF2F2),
                            title = t("accountLogout"),
                            subtitle = null,
                            titleColor = Color(0xFFB91C1C),
                            onClick = { confirmOut = true },
                        )
                    }
                }
            }
        }
        Box(Modifier.align(Alignment.BottomCenter)) {
            AppFooter(t, "settings", onHome = onHome, onSettings = {})
        }

        if (languageOpen) {
            ChoiceDialog(
                title = t("language"),
                onDismiss = { languageOpen = false },
            ) {
                LanguageRowSelector(state.locale) {
                    store.setLocale(it)
                    languageOpen = false
                }
            }
        }
        if (distanceOpen) {
            ChoiceDialog(
                title = t("settingsDistanceUnits"),
                onDismiss = { distanceOpen = false },
            ) {
                ChoiceOption(t("settingsDistanceKm"), state.distanceUnit == DistanceUnit.Km) {
                    store.setDistanceUnit(DistanceUnit.Km)
                    distanceOpen = false
                }
                Spacer(Modifier.height(10.dp))
                ChoiceOption(t("settingsDistanceMi"), state.distanceUnit == DistanceUnit.Mi) {
                    store.setDistanceUnit(DistanceUnit.Mi)
                    distanceOpen = false
                }
            }
        }
        if (aboutOpen) {
            DailyDialog(
                title = t("settingsAboutApp"),
                message = t("settingsAboutMessage").replace("{version}", BuildConfig.VERSION_NAME),
                confirm = t("ok"),
                onConfirm = { aboutOpen = false },
            )
        }
        if (legalOpen) {
            ChoiceDialog(
                title = t("settingsTermsPolicies"),
                onDismiss = { legalOpen = false },
            ) {
                ChoiceOption(t("termsOfServiceTitle"), false) {
                    legalOpen = false
                    onTerms()
                }
                Spacer(Modifier.height(10.dp))
                ChoiceOption(t("privacyTitle"), false) {
                    legalOpen = false
                    onPrivacy()
                }
            }
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
fun ProfileScreen(
    state: AppUiState,
    store: AppStore,
    onBack: () -> Unit,
    onPlan: () -> Unit,
) {
    val t = { key: String -> store.t(key) }
    var editing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(state.profile.userName) }
    var phone by remember { mutableStateOf(state.profile.userPhone) }
    var birth by remember { mutableStateOf(state.profile.userBirthdate?.take(10).orEmpty()) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val displayName = state.profile.userName.ifBlank { state.session?.userName.orEmpty().ifBlank { t("accountUser") } }
    val email = state.session?.email.orEmpty()
    val pictureUrl = state.session?.pictureUrl
    val planLabel = when (state.planCode) {
        PlanCode.Plus -> t("planNamePlus")
        PlanCode.Pro -> t("planNamePro")
        PlanCode.Free -> t("planNameFree")
    }

    Box(Modifier.fillMaxSize().background(PageBg)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            SettingsHeader(t("profileTitle"), onBack = onBack)
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(ProfileHeroBg)
                        .padding(vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            Modifier.size(96.dp).clip(CircleShape).background(Color.White),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (!pictureUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = pictureUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(96.dp).clip(CircleShape),
                                )
                            } else {
                                Icon(Icons.Outlined.Person, contentDescription = null, tint = PrimaryAlt, modifier = Modifier.size(48.dp))
                            }
                        }
                        Box(
                            Modifier
                                .offset(x = 2.dp, y = 2.dp)
                                .size(30.dp)
                                .shadow(2.dp, CircleShape)
                                .clip(CircleShape)
                                .background(PrimaryAlt)
                                .clickable { store.toast(t("profilePhotoSoon")) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(displayName, fontFamily = PromptFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Ink)
                    if (email.isNotBlank()) {
                        Text(email, fontFamily = PromptFamily, fontSize = 13.sp, color = Muted, modifier = Modifier.padding(top = 4.dp))
                    }
                    Text(
                        planLabel,
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.7f))
                            .clickable(onClick = onPlan)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        fontFamily = PromptFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = PrimaryAlt,
                    )
                }

                Spacer(Modifier.height(16.dp))

                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                ) {
                    if (editing) {
                        ProfileEditField(t("accountFieldUsername"), name, Icons.Outlined.Person, FieldPersonBg, PrimaryAlt) { name = it }
                        SettingsDivider(horizontal = 16.dp)
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircleIcon(Icons.Outlined.CalendarMonth, FieldBirth, FieldBirthBg)
                                Column(Modifier.padding(start = 14.dp).weight(1f)) {
                                    Text(t("accountFieldBirthdate"), fontFamily = PromptFamily, fontSize = 12.sp, color = Muted)
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .border(1.dp, Line, RoundedCornerShape(10.dp))
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
                                        Text(
                                            if (birth.isBlank()) t("accountSelectBirthdate") else formatBirthdate(birth, state.locale),
                                            fontFamily = PromptFamily,
                                            fontWeight = FontWeight.Medium,
                                            color = Ink,
                                        )
                                    }
                                }
                            }
                        }
                        SettingsDivider(horizontal = 16.dp)
                        ProfileEditField(
                            t("accountFieldPhone"),
                            phone,
                            Icons.Outlined.Phone,
                            FieldPhoneBg,
                            FieldPhone,
                            KeyboardOptions(keyboardType = KeyboardType.Number),
                        ) { incoming ->
                            val next = nextThaiMobile(phone, incoming)
                            phone = next.first
                            if (next.second) store.toast(t("accountPhoneInvalid"))
                        }
                        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.dp, Line, RoundedCornerShape(14.dp))
                                    .clickable {
                                        name = state.profile.userName
                                        phone = state.profile.userPhone
                                        birth = state.profile.userBirthdate?.take(10).orEmpty()
                                        editing = false
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) { Text(t("cancel"), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold) }
                            Box(
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
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
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) { Text(t("accountSave"), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, color = Color.White) }
                        }
                    } else {
                        ProfileInfoRow(
                            Icons.Outlined.Person,
                            PrimaryAlt,
                            FieldPersonBg,
                            t("accountFieldUsername"),
                            state.profile.userName.ifBlank { "—" },
                        ) { editing = true }
                        SettingsDivider(horizontal = 16.dp)
                        ProfileInfoRow(
                            Icons.Outlined.CalendarMonth,
                            FieldBirth,
                            FieldBirthBg,
                            t("accountFieldBirthdate"),
                            formatBirthdate(state.profile.userBirthdate, state.locale),
                        ) { editing = true }
                        SettingsDivider(horizontal = 16.dp)
                        ProfileInfoRow(
                            Icons.Outlined.Phone,
                            FieldPhone,
                            FieldPhoneBg,
                            t("accountFieldPhone"),
                            formatThaiMobile(state.profile.userPhone),
                        ) { editing = true }
                        Box(
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(EditBtnBg)
                                .clickable { editing = true }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Edit, contentDescription = null, tint = PrimaryAlt, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.size(8.dp))
                                Text(
                                    t("accountEditProfile"),
                                    fontFamily = PromptFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = PrimaryAlt,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlanScreen(state: AppUiState, store: AppStore, onBack: () -> Unit) {
    val t = { key: String -> store.t(key) }
    var pendingPlan by remember { mutableStateOf<PlanCode?>(null) }
    val plan = Plans.of(state.planCode)

    Box(Modifier.fillMaxSize().background(PageBg)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            SettingsHeader(t("accountTabPlan"), onBack = onBack)
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
            ) {
                Text(t("planPaymentSoon"), fontFamily = PromptFamily, fontSize = 12.sp, color = Muted, lineHeight = 16.sp)
                Column(Modifier.padding(top = 12.dp).clip(RoundedCornerShape(16.dp)).background(Color.White).padding(14.dp)) {
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
        }
        pendingPlan?.let { code ->
            DailyDialog(t("planChangeTitle"), t("planChangeMessage"), t("planSelect"), onConfirm = {
                store.selectPlan(code)
                pendingPlan = null
            }, cancel = t("cancel"), onCancel = { pendingPlan = null })
        }
    }
}

@Composable
fun NotificationScreen(state: AppUiState, store: AppStore, onBack: () -> Unit) {
    val t = { key: String -> store.t(key) }
    Box(Modifier.fillMaxSize().background(PageBg)) {
        Column(Modifier.statusBarsPadding()) {
            SettingsHeader(t("notifications"), onBack = onBack)
            Column(Modifier.padding(20.dp)) {
                SettingsCard {
                    NotifyRow(t("notifyVibration"), state.notifyVibration) { store.setNotify(vibration = !state.notifyVibration) }
                    SettingsDivider(horizontal = 16.dp)
                    NotifyRow(t("notifySound"), state.notifySound) { store.setNotify(sound = !state.notifySound) }
                }
            }
        }
    }
}

@Composable
private fun SettingsHeader(title: String, onBack: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(56.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.align(Alignment.CenterStart)) { PageBack(onBack) }
        Text(title, fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Ink)
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(
        text,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        fontFamily = PromptFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        color = SectionLabel,
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(18.dp), ambientColor = Color(0x14000000), spotColor = Color(0x14000000))
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White),
    ) { content() }
}

@Composable
private fun SettingsDivider(horizontal: Dp = 68.dp) {
    HorizontalDivider(Modifier.padding(start = horizontal), color = Line.copy(alpha = 0.7f), thickness = 0.5.dp)
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String?,
    titleColor: Color = Ink,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircleIcon(icon, iconTint, iconBg)
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(title, fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = titleColor)
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, fontFamily = PromptFamily, fontSize = 12.sp, color = Muted, modifier = Modifier.padding(top = 2.dp), lineHeight = 16.sp)
            }
        }
        Chevron()
    }
}

@Composable
private fun CircleIcon(icon: ImageVector, tint: Color, bg: Color) {
    Box(Modifier.size(40.dp).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    tint: Color,
    bg: Color,
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircleIcon(icon, tint, bg)
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(label, fontFamily = PromptFamily, fontSize = 12.sp, color = Muted)
            Text(value, fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Ink, modifier = Modifier.padding(top = 2.dp))
        }
        Chevron()
    }
}

@Composable
private fun ProfileEditField(
    label: String,
    value: String,
    icon: ImageVector,
    bg: Color,
    tint: Color,
    keyboard: KeyboardOptions = KeyboardOptions.Default,
    onChange: (String) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircleIcon(icon, tint, bg)
        Column(Modifier.padding(start = 14.dp).weight(1f)) {
            Text(label, fontFamily = PromptFamily, fontSize = 12.sp, color = Muted)
            BasicTextField(
                value = value,
                onValueChange = onChange,
                textStyle = TextStyle(fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Ink),
                keyboardOptions = keyboard,
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun ChoiceDialog(title: String, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0x4D000000))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .clickable(enabled = false) {}
                .padding(20.dp),
        ) {
            Text(title, fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Ink)
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun ChoiceOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color(0xFFEAF3FC) else Color(0xFFF7F7F8))
            .border(1.dp, if (selected) PrimaryAlt else Color.Transparent, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Ink)
        if (selected) {
            Text("✓", fontFamily = PromptFamily, fontWeight = FontWeight.Bold, color = PrimaryAlt)
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
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
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
private fun NotifyRow(label: String, value: Boolean, onToggle: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold)
        DailySwitch(value, onToggle = onToggle)
    }
}
