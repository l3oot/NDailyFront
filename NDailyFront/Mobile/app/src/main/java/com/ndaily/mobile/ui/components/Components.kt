package com.ndaily.mobile.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ndaily.mobile.R
import com.ndaily.mobile.data.AppLocale
import com.ndaily.mobile.ui.theme.CardDark
import com.ndaily.mobile.ui.theme.FooterInactive
import com.ndaily.mobile.ui.theme.Foreground
import com.ndaily.mobile.ui.theme.Hint2
import com.ndaily.mobile.ui.theme.Ink
import com.ndaily.mobile.ui.theme.Muted
import com.ndaily.mobile.ui.theme.Muted2
import com.ndaily.mobile.ui.theme.Overlay
import com.ndaily.mobile.ui.theme.Primary
import com.ndaily.mobile.ui.theme.PrimaryAlt
import com.ndaily.mobile.ui.theme.PromptFamily
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DailyDialog(
    title: String,
    message: String,
    confirm: String,
    cancel: String? = null,
    onCancel: (() -> Unit)? = null,
    confirmColor: Color = Primary,
    onConfirm: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Overlay)
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(20.dp),
        ) {
            Text(title, fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Ink)
            Spacer(Modifier.height(8.dp))
            Text(message, fontFamily = PromptFamily, fontSize = 14.sp, color = Muted2, lineHeight = 20.sp)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (cancel != null && onCancel != null) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF2F2F7))
                            .clickable(onClick = onCancel)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(cancel, fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, color = Foreground)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(confirmColor)
                        .clickable(onClick = onConfirm)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(confirm, fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun AppFooter(
    t: (String) -> String,
    active: String,
    onHome: () -> Unit,
    onSettings: () -> Unit,
) {
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White),
    ) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEEF1F4)))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp + bottomInset),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FooterTab(
                label = t("footerHome"),
                selected = active == "home",
                onClick = onHome,
                icon = { color -> Icon(Icons.Outlined.Home, contentDescription = null, tint = color, modifier = Modifier.size(24.dp)) },
            )
            FooterTab(
                label = t("footerSettings"),
                selected = active == "settings",
                onClick = onSettings,
                icon = { color -> Icon(Icons.Outlined.Settings, contentDescription = null, tint = color, modifier = Modifier.size(24.dp)) },
            )
        }
    }
}

@Composable
private fun RowScope.FooterTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit,
) {
    val color = if (selected) Primary else FooterInactive
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        icon(color)
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            fontFamily = PromptFamily,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 12.sp,
            color = color,
        )
    }
}

@Composable
fun PageBack(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(16.dp)) {
                val path = Path().apply {
                    moveTo(size.width * 0.62f, size.height * 0.18f)
                    lineTo(size.width * 0.32f, size.height * 0.5f)
                    lineTo(size.width * 0.62f, size.height * 0.82f)
                }
                drawPath(path, CardDark, style = Stroke(width = 5f, cap = StrokeCap.Round))
            }
        }
    }
}

@Composable
fun LanguageSwitcher(locale: AppLocale, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.7f))
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(if (locale == AppLocale.Th) R.drawable.flag_th else R.drawable.flag_en),
            contentDescription = null,
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
fun LanguageRowSelector(locale: AppLocale, onSelect: (AppLocale) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        FlagCircle(selected = locale == AppLocale.Th, res = R.drawable.flag_th) { onSelect(AppLocale.Th) }
        FlagCircle(selected = locale == AppLocale.En, res = R.drawable.flag_en) { onSelect(AppLocale.En) }
    }
}

@Composable
private fun FlagCircle(selected: Boolean, res: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(0xFFFFF8F0))
            .then(
                if (selected) Modifier.border(2.dp, PrimaryAlt, CircleShape)
                else Modifier.border(1.dp, Color(0x0D000000), CircleShape),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(painterResource(res), contentDescription = null, modifier = Modifier.size(26.dp))
    }
}

@Composable
fun ToastBar(text: String, aboveFooter: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = if (aboveFooter) 112.dp else 24.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Text(
            text,
            modifier = Modifier
                .clip(CircleShape)
                .background(CardDark)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            color = Color.White,
            fontFamily = PromptFamily,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun TopoBackground(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        fun wave(i: Int, p1: Float, a1: Float, p2: Float, a2: Float, base: Float, blue: Boolean) {
            val path = Path()
            path.moveTo(0f, base)
            var x = 0f
            val step = if (blue) 8f else 6f
            while (x <= w + 20) {
                val y = if (blue) {
                    base + cos(x / p1 + i) * a1
                } else {
                    base + sin(x / p1 + i * 0.7f) * a1 + cos(x / p2) * a2
                }
                path.lineTo(x, y)
                x += step
            }
            drawPath(
                path,
                color = if (blue) Color(0x0A4A89EA) else Color(0x09000000),
                style = Stroke(width = if (blue) 1.6f else 2f),
            )
        }
        repeat(14) { i -> wave(i, 22f, 12f, 41f, 6f, -40f + i * 70f, false) }
        repeat(10) { i -> wave(i, 28f, 16f, 1f, 0f, -20f + i * 88f, true) }
    }
}

@Composable
fun PinMark() {
    Box(
        modifier = Modifier
            .size(56.dp)
            .shadow(10.dp, RoundedCornerShape(18.dp), ambientColor = Color(0x243B82F6))
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(38.dp)) {
            val path = Path().apply {
                moveTo(size.width * 0.48f, size.height * 0.11f)
                cubicTo(
                    size.width * 0.20f, size.height * 0.11f,
                    size.width * 0.10f, size.height * 0.36f,
                    size.width * 0.10f, size.height * 0.36f,
                )
                cubicTo(
                    size.width * 0.10f, size.height * 0.62f,
                    size.width * 0.48f, size.height * 0.90f,
                    size.width * 0.48f, size.height * 0.90f,
                )
                cubicTo(
                    size.width * 0.48f, size.height * 0.90f,
                    size.width * 0.86f, size.height * 0.62f,
                    size.width * 0.86f, size.height * 0.36f,
                )
                cubicTo(
                    size.width * 0.86f, size.height * 0.22f,
                    size.width * 0.70f, size.height * 0.11f,
                    size.width * 0.48f, size.height * 0.11f,
                )
                close()
            }
            drawPath(path, Primary)
            drawCircle(Color.White, radius = size.minDimension * 0.12f, center = Offset(size.width * 0.48f, size.height * 0.34f))
            drawCircle(Color(0xFFF5B942), radius = size.minDimension * 0.11f, center = Offset(size.width * 0.72f, size.height * 0.67f))
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Primary,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = if (enabled) 1f else 0.5f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White)
    }
}

@Composable
fun DailySwitch(checked: Boolean, color: Color = Primary, onToggle: () -> Unit) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 22.dp else 2.dp,
        animationSpec = tween(180),
        label = "dailySwitch",
    )
    Box(
        modifier = Modifier
            .width(48.dp)
            .height(28.dp)
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(percent = 50))
                .background(if (checked) color else Color(0xFFD1D1D6)),
        )
        Box(
            Modifier
                .offset(x = thumbOffset)
                .size(24.dp)
                .shadow(2.dp, CircleShape, clip = false)
                .background(Color.White, CircleShape),
        )
    }
}

@Composable
fun Chevron(color: Color = Hint2) {
    Canvas(Modifier.size(20.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.38f, size.height * 0.27f)
            lineTo(size.width * 0.62f, size.height * 0.5f)
            lineTo(size.width * 0.38f, size.height * 0.73f)
        }
        drawPath(path, color, style = Stroke(width = 4.5f, cap = StrokeCap.Round))
    }
}
