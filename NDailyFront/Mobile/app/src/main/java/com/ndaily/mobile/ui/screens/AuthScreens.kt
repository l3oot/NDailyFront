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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import com.ndaily.mobile.R
import com.ndaily.mobile.data.AppStore
import com.ndaily.mobile.data.api.ApiClient
import com.ndaily.mobile.data.AppUiState
import com.ndaily.mobile.data.isValidEmail
import com.ndaily.mobile.ui.components.Chevron
import com.ndaily.mobile.ui.components.DailyDialog
import com.ndaily.mobile.ui.components.PinMark
import com.ndaily.mobile.ui.components.PrimaryButton
import com.ndaily.mobile.ui.theme.Foreground
import com.ndaily.mobile.ui.theme.Hint
import com.ndaily.mobile.ui.theme.Hint2
import com.ndaily.mobile.ui.theme.Ink
import com.ndaily.mobile.ui.theme.Line
import com.ndaily.mobile.ui.theme.Muted
import com.ndaily.mobile.ui.theme.Muted2
import com.ndaily.mobile.ui.theme.Primary
import com.ndaily.mobile.ui.theme.PromptFamily
import com.ndaily.mobile.ui.theme.WelcomeBg
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(@Suppress("UNUSED_PARAMETER") state: AppUiState, store: AppStore, onDone: (loggedIn: Boolean) -> Unit) {
    val t = { key: String -> store.t(key) }
    LaunchedEffect(Unit) {
        val started = System.currentTimeMillis()
        store.restoreSession()
        val wait = 500 - (System.currentTimeMillis() - started)
        if (wait > 0) delay(wait)
        onDone(store.state.value.loggedIn)
    }
    Box(Modifier.fillMaxSize().background(WelcomeBg), contentAlignment = Alignment.Center) {
        Text(t("restoringSession"), fontFamily = PromptFamily, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Muted)
    }
}

@Composable
fun WelcomeScreen(
    store: AppStore,
    onHome: () -> Unit,
    onTerms: () -> Unit,
    onPrivacy: () -> Unit,
) {
    val t = { key: String -> store.t(key) }
    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var otpStep by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var dialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(Modifier.fillMaxSize().background(WelcomeBg).imePadding()) {
        Image(
            painter = painterResource(R.drawable.login_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.CenterEnd,
            modifier = Modifier.fillMaxWidth().height(280.dp),
        )
        Box(
            Modifier.fillMaxWidth().height(280.dp).background(
                Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, WelcomeBg)),
            ),
        )
        Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())) {
            Column(Modifier.padding(horizontal = 24.dp).padding(top = 32.dp)) {
                PinMark()
                Spacer(Modifier.height(20.dp))
                Text(t("welcomeTitle"), fontFamily = PromptFamily, fontWeight = FontWeight.Bold, fontSize = 30.sp, color = Ink, lineHeight = 34.sp)
                Text(t("welcomeLead"), fontFamily = PromptFamily, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Muted2, modifier = Modifier.padding(top = 6.dp))
                Text(t("welcomeFeatures"), fontFamily = PromptFamily, fontSize = 13.sp, color = Hint, modifier = Modifier.padding(top = 4.dp))
            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .shadow(12.dp, RoundedCornerShape(28.dp), ambientColor = Color(0x0F28508C))
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White)
                    .padding(20.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFE8F1FF)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.Email, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.size(12.dp))
                    Column {
                        Text(t("welcomeEmailTitle"), fontFamily = PromptFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink)
                        Text(
                            if (otpStep) t("loginOtpSent") else t("welcomeEmailHint"),
                            fontFamily = PromptFamily,
                            fontSize = 13.sp,
                            color = Muted2,
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
                FieldBox(icon = true) {
                    BasicTextField(
                        value = email,
                        onValueChange = { email = it },
                        enabled = !otpStep && !busy,
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = PromptFamily, fontSize = 15.sp, color = Ink),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            Box {
                                if (email.isEmpty()) {
                                    Text(t("welcomeEmailPlaceholder"), fontFamily = PromptFamily, fontSize = 15.sp, color = Hint2)
                                }
                                inner()
                            }
                        },
                    )
                }
                if (otpStep) {
                    Spacer(Modifier.height(12.dp))
                    FieldBox(icon = false) {
                        BasicTextField(
                            value = otp,
                            onValueChange = { otp = it.filter { ch -> ch.isDigit() }.take(8) },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontFamily = PromptFamily,
                                fontSize = 20.sp,
                                color = Ink,
                                textAlign = TextAlign.Center,
                                letterSpacing = 6.sp,
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { inner ->
                                Box(contentAlignment = Alignment.Center) {
                                    if (otp.isEmpty()) {
                                        Text(t("loginOtpLabel"), fontFamily = PromptFamily, fontSize = 15.sp, color = Hint2)
                                    }
                                    inner()
                                }
                            },
                        )
                    }
                    Text(
                        t("loginChangeEmail"),
                        modifier = Modifier.padding(top = 8.dp).clickable {
                            otpStep = false
                            otp = ""
                        },
                        fontFamily = PromptFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Primary,
                    )
                }
                Spacer(Modifier.height(16.dp))
                PrimaryButton(
                    text = when {
                        busy && otpStep -> t("loggingInShort")
                        busy -> t("sendingOtp")
                        otpStep -> t("welcomeSignIn")
                        else -> t("welcomeSendOtp")
                    },
                    enabled = !busy,
                    onClick = {
                        val target = email.trim()
                        if (target.isEmpty()) {
                            dialog = t("dialogIncomplete") to t("dialogFillEmail")
                            return@PrimaryButton
                        }
                        if (!isValidEmail(target)) {
                            dialog = t("dialogIncomplete") to t("loginInvalidEmail")
                            return@PrimaryButton
                        }
                        if (!otpStep) {
                            busy = true
                            scope.launch {
                                val result = store.sendOtp(target)
                                busy = false
                                if (!result.ok) {
                                    dialog = t("cannotConnect") to result.message.ifBlank { t("sendOtpFailed") }
                                    return@launch
                                }
                                otpStep = true
                                dialog = if (result.debugOtp.isNullOrBlank()) {
                                    t("loginOtpSent") to t("loginOtpSent")
                                } else {
                                    t("loginOtpSent") to "${t("loginDevOtp")} ${result.debugOtp}"
                                }
                            }
                        } else {
                            if (otp.length < 4) {
                                dialog = t("dialogIncomplete") to t("dialogFillOtp")
                                return@PrimaryButton
                            }
                            busy = true
                            scope.launch {
                                val error = store.verifyOtp(target, otp)
                                busy = false
                                if (error != null) {
                                    dialog = t("wrongOtp") to error
                                    return@launch
                                }
                                onHome()
                            }
                        }
                    },
                )
                Row(
                    modifier = Modifier.padding(top = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.weight(1f).height(1.dp).background(Color(0xFFE8EDF3)))
                    Text(t("welcomeOrContinue"), modifier = Modifier.padding(horizontal = 12.dp), fontFamily = PromptFamily, fontSize = 13.sp, color = Hint)
                    Box(Modifier.weight(1f).height(1.dp).background(Color(0xFFE8EDF3)))
                }
                Spacer(Modifier.height(16.dp))
                SocialRow(R.drawable.ic_google, t("loginWithGoogle")) {
                    val url = "${ApiClient.BASE_URL}/api/v1/auth/google/start?client=android"
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
                Spacer(Modifier.height(12.dp))
                SocialRow(null, t("welcomeLine"), line = true) {
                    dialog = t("welcomeLine") to t("welcomeLineSoon")
                }
                Text(
                    t("welcomeConsent"),
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    textAlign = TextAlign.Center,
                    fontFamily = PromptFamily,
                    fontSize = 12.sp,
                    color = Hint,
                    lineHeight = 18.sp,
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text(
                        t("welcomeTermsLink"),
                        modifier = Modifier.clickable(onClick = onTerms),
                        fontFamily = PromptFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Primary,
                    )
                    Text(" ${t("welcomeAnd")} ", fontFamily = PromptFamily, fontSize = 12.sp, color = Hint)
                    Text(
                        t("welcomePrivacyLink"),
                        modifier = Modifier.clickable(onClick = onPrivacy),
                        fontFamily = PromptFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Primary,
                    )
                }
            }
        }
        dialog?.let { (title, message) ->
            DailyDialog(title, message, t("ok")) { dialog = null }
        }
    }
}

@Composable
private fun FieldBox(icon: Boolean, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFE6EBF1), RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon) {
            Icon(Icons.Outlined.Email, contentDescription = null, tint = Hint2, modifier = Modifier.size(18.dp).padding(end = 0.dp))
            Spacer(Modifier.size(12.dp))
        }
        Box(Modifier.weight(1f)) { content() }
    }
}

@Composable
private fun SocialRow(icon: Int?, label: String, line: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFE7EDF4), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (line) {
            Box(Modifier.size(28.dp).clip(RoundedCornerShape(7.dp)).background(Color(0xFF06C755)), contentAlignment = Alignment.Center) {
                Text("L", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = PromptFamily)
            }
        } else if (icon != null) {
            Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(22.dp))
        }
        Text(label, modifier = Modifier.weight(1f).padding(horizontal = 12.dp), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Ink)
        Chevron()
    }
}

@Composable
fun LegalScreen(title: String, body: String, backLabel: String, onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Text("← $backLabel", modifier = Modifier.clickable(onClick = onBack), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Primary)
        Text(title, modifier = Modifier.padding(top = 16.dp), fontFamily = PromptFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Foreground)
        Text(body, modifier = Modifier.padding(top = 16.dp), fontFamily = PromptFamily, fontSize = 14.sp, color = Foreground, lineHeight = 22.sp)
    }
}
