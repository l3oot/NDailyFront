package com.ndaily.mobile.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.LocalParking
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Tag
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.ndaily.mobile.R
import com.ndaily.mobile.data.AppStore
import com.ndaily.mobile.data.AppUiState
import com.ndaily.mobile.data.ParkSpot
import com.ndaily.mobile.ui.components.DailyDialog
import com.ndaily.mobile.ui.components.PageBack
import com.ndaily.mobile.ui.components.PrimaryButton
import com.ndaily.mobile.ui.theme.Ink
import com.ndaily.mobile.ui.theme.Muted
import com.ndaily.mobile.ui.theme.Muted2
import com.ndaily.mobile.ui.theme.PrimaryAlt
import com.ndaily.mobile.ui.theme.PromptFamily
import com.ndaily.mobile.ui.theme.ToiletBg
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.launch

private val PageBg = Color(0xFFF2F6F5)
private val Teal = Color(0xFF0F766E)
private val SoftTeal = Color(0xFFD8F3EE)
private val SoftTealDeep = Color(0xFFEEF8F6)
private val Danger = Color(0xFFDC2626)
private val FieldHint = Color(0xFFB0B8C2)
private val DashStroke = Color(0xFF9BB8B2)
private val ParkCardBg = Color(0xFFF7F9FC)
private val ParkCardLine = Color(0xFFEEF2F6)
private val ParkThumbBg = Color(0xFFEEF2F7)

@Composable
fun ParkSpotListScreen(
    state: AppUiState,
    store: AppStore,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onOpen: (String) -> Unit,
) {
    val t = { key: String -> store.t(key) }
    val scope = rememberCoroutineScope()
    val spots = state.parkSpots
    var deleteId by remember { mutableStateOf<String?>(null) }
    var deleting by remember { mutableStateOf(false) }

    LaunchedEffect(state.loggedIn) {
        if (state.loggedIn) store.loadParkSpots()
    }

    Box(Modifier.fillMaxSize().background(ToiletBg)) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            Box(Modifier.fillMaxWidth()) {
                PageBack(onBack)
                Text(
                    t("parkTitle"),
                    modifier = Modifier.align(Alignment.Center),
                    fontFamily = PromptFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = Ink,
                )
                ParkPill(
                    t("parkAddAction"),
                    Icons.Outlined.Add,
                    onAdd,
                    Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
                )
            }

            if (spots.isEmpty()) {
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        Modifier.size(88.dp).clip(RoundedCornerShape(22.dp)).background(ParkThumbBg),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.LocalParking, contentDescription = null, tint = PrimaryAlt, modifier = Modifier.size(36.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(t("parkListEmptyTitle"), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Ink)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        t("parkListEmptyBody"),
                        fontFamily = PromptFamily,
                        fontSize = 14.sp,
                        color = Muted,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Text(
                            t("parkListCount").format(spots.size),
                            fontFamily = PromptFamily,
                            fontSize = 13.sp,
                            color = Muted,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                    items(spots, key = { it.id }) { spot ->
                        ParkSpotListCard(
                            spot = spot,
                            t = t,
                            onOpen = { onOpen(spot.id) },
                            onDelete = { deleteId = spot.id },
                        )
                    }
                    item { Spacer(Modifier.height(12.dp)) }
                }
            }
        }

        val pendingDelete = deleteId
        if (pendingDelete != null) {
            DailyDialog(
                title = t("parkClearTitle"),
                message = t("parkClearBody"),
                confirm = t("parkClearConfirm"),
                cancel = t("cancel"),
                confirmColor = Danger,
                onCancel = { if (!deleting) deleteId = null },
                onConfirm = {
                    if (deleting) return@DailyDialog
                    scope.launch {
                        deleting = true
                        store.deleteParkSpot(pendingDelete)
                        deleting = false
                        deleteId = null
                    }
                },
            )
        }
    }
}

@Composable
fun ParkSpotFormScreen(
    state: AppUiState,
    store: AppStore,
    spotId: String?,
    onBack: () -> Unit,
) {
    val t = { key: String -> store.t(key) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val editing = spotId?.let { id -> state.parkSpots.firstOrNull { it.id == id } }
    val formId = remember(spotId) { spotId ?: UUID.randomUUID().toString() }

    var label by remember(editing?.id, editing?.savedAt) { mutableStateOf(editing?.label.orEmpty()) }
    var place by remember(editing?.id, editing?.savedAt) { mutableStateOf(editing?.place.orEmpty()) }
    var zone by remember(editing?.id, editing?.savedAt) { mutableStateOf(editing?.zone.orEmpty()) }
    var floor by remember(editing?.id, editing?.savedAt) { mutableStateOf(editing?.floor.orEmpty()) }
    var pillar by remember(editing?.id, editing?.savedAt) { mutableStateOf(editing?.pillar.orEmpty()) }
    var photoPath by remember(editing?.id, editing?.savedAt) { mutableStateOf(editing?.photoPath.orEmpty()) }
    var pendingPhoto by remember { mutableStateOf<File?>(null) }
    var error by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val file = pendingPhoto
        pendingPhoto = null
        if (ok && file != null && file.exists()) {
            if (photoPath.isNotBlank() && photoPath != file.absolutePath && photoPath != editing?.photoPath) {
                runCatching { File(photoPath).takeIf { it.exists() }?.delete() }
            }
            photoPath = file.absolutePath
            error = ""
        } else {
            file?.delete()
        }
    }

    fun launchCamera() {
        val dir = File(context.cacheDir, "park_photos").apply { mkdirs() }
        val file = File(dir, "park_${System.currentTimeMillis()}.jpg")
        pendingPhoto = file
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        takePicture.launch(uri)
    }

    fun submit() {
        if (saving) return
        val pl = place.trim()
        val z = zone.trim()
        val f = floor.trim()
        val p = pillar.trim()
        if (pl.isBlank() || z.isBlank() || f.isBlank() || p.isBlank()) {
            error = t("parkFormRequired")
            return
        }
        val gps = state.gps
        if (gps == null) {
            error = t("parkGpsError")
            return
        }
        error = ""
        scope.launch {
            saving = true
            val ok = store.saveParkSpot(
                ParkSpot(
                    id = formId,
                    label = label.trim(),
                    place = pl,
                    zone = z,
                    floor = f,
                    pillar = p,
                    lat = gps.lat,
                    lng = gps.lng,
                    photoPath = photoPath,
                    savedAt = System.currentTimeMillis(),
                ),
            )
            saving = false
            if (ok) onBack()
        }
    }

    Box(Modifier.fillMaxSize().background(PageBg)) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            Box(Modifier.fillMaxWidth()) {
                PageBack(onBack)
                Text(
                    if (editing == null) t("parkAddNew") else t("parkEditTitle"),
                    modifier = Modifier.align(Alignment.Center),
                    fontFamily = PromptFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = Ink,
                )
                if (editing != null) {
                    Row(
                        Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE6EAF0), CircleShape)
                            .clickable {
                                val maps = "https://www.google.com/maps/dir/?api=1&destination=${editing.lat},${editing.lng}"
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(maps)))
                            }
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_google_maps),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            t("parkOpenMaps"),
                            fontFamily = PromptFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Ink,
                        )
                    }
                }
            }

            Column(
                Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                PhotoDropzone(
                    photoPath = photoPath,
                    takeLabel = if (photoPath.isBlank()) t("parkTakePhoto") else t("parkRetakePhoto"),
                    hint = t("parkPhotoOptional"),
                    onTake = { launchCamera() },
                    onClear = {
                        if (photoPath.isNotBlank() && photoPath != editing?.photoPath) {
                            runCatching { File(photoPath).delete() }
                        }
                        photoPath = ""
                    },
                )

                Column(
                    Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(24.dp), clip = false)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(t("parkDetailsTitle"), fontFamily = PromptFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink)
                    Text(t("parkFormLead"), fontFamily = PromptFamily, fontSize = 13.sp, color = Muted2, lineHeight = 18.sp)

                    ParkField(
                        label = t("parkLabel"),
                        value = label,
                        placeholder = t("parkLabelHint"),
                        icon = Icons.Outlined.DirectionsCar,
                        onChange = { label = it },
                    )
                    ParkField(
                        label = t("parkPlace"),
                        value = place,
                        placeholder = t("parkPlaceHint"),
                        icon = Icons.Outlined.LocationOn,
                        onChange = { place = it },
                    )

                    Text(t("parkSpotSection"), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Muted2)

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ParkField(
                            label = t("parkZone"),
                            value = zone,
                            placeholder = t("parkZoneHint"),
                            icon = Icons.Outlined.LocalParking,
                            modifier = Modifier.weight(1f),
                            onChange = { zone = it },
                        )
                        ParkField(
                            label = t("parkFloor"),
                            value = floor,
                            placeholder = t("parkFloorHint"),
                            icon = Icons.Outlined.Layers,
                            modifier = Modifier.weight(1f),
                            onChange = { floor = it },
                        )
                    }
                    ParkField(
                        label = t("parkPillar"),
                        value = pillar,
                        placeholder = t("parkPillarHint"),
                        icon = Icons.Outlined.Tag,
                        onChange = { pillar = it },
                    )
                }

                if (error.isNotBlank()) {
                    Text(error, fontFamily = PromptFamily, fontSize = 14.sp, color = Danger)
                }

                PrimaryButton(
                    if (saving) t("parkSaving") else t("parkSave"),
                    onClick = { submit() },
                    color = Teal,
                    enabled = !saving,
                )
            }
        }
    }
}

@Composable
private fun ParkPill(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(CircleShape)
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF3C3C43), modifier = Modifier.size(15.dp))
        Text(
            label,
            modifier = Modifier.padding(start = 6.dp),
            fontFamily = PromptFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = Color(0xFF3C3C43),
        )
    }
}

@Composable
private fun ParkSpotListCard(
    spot: ParkSpot,
    t: (String) -> String,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    Box {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, ParkCardLine, RoundedCornerShape(16.dp))
                .background(ParkCardBg)
                .clickable(onClick = onOpen)
                .padding(12.dp),
        ) {
            if (spot.photoPath.isNotBlank() && File(spot.photoPath).exists()) {
                AsyncImage(
                    model = File(spot.photoPath),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(88.dp).clip(RoundedCornerShape(12.dp)),
                )
            } else {
                Box(
                    Modifier.size(88.dp).clip(RoundedCornerShape(12.dp)).background(ParkThumbBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.DirectionsCar, contentDescription = null, tint = PrimaryAlt, modifier = Modifier.size(32.dp))
                }
            }
            Column(Modifier.weight(1f).padding(start = 12.dp, end = 28.dp)) {
                Text(
                    spotTitle(spot, t),
                    fontFamily = PromptFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (spot.place.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = PrimaryAlt, modifier = Modifier.size(13.dp))
                        Text(
                            spot.place,
                            fontFamily = PromptFamily,
                            fontSize = 12.sp,
                            color = Muted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
                Text(
                    t("parkListMeta").format(spot.zone, spot.floor, spot.pillar),
                    fontFamily = PromptFamily,
                    fontSize = 12.sp,
                    color = Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Text(
                    formatSavedAt(spot.savedAt, t),
                    fontFamily = PromptFamily,
                    fontSize = 12.sp,
                    color = Color(0xFFA0A8B3),
                    modifier = Modifier.padding(top = 4.dp),
                )
                Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (spot.place.isNotBlank()) {
                        ParkChip(spot.place, Color(0xFFE8F1FC), PrimaryAlt)
                    }
                    ParkChip(t("parkZone") + " " + spot.zone, Color(0xFFDBEAFE), Color(0xFF1D4ED8))
                    ParkChip(t("parkFloor") + " " + spot.floor, Color(0xFFDCFCE7), Color(0xFF15803D))
                }
            }
        }
        Icon(
            Icons.Outlined.Delete,
            contentDescription = t("parkClearConfirm"),
            tint = Danger,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .size(18.dp)
                .clickable(onClick = onDelete),
        )
    }
}

@Composable
private fun ParkChip(text: String, bg: Color, fg: Color) {
    Text(
        text,
        modifier = Modifier.clip(CircleShape).background(bg).padding(horizontal = 8.dp, vertical = 2.dp),
        fontFamily = PromptFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        color = fg,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun PhotoDropzone(
    photoPath: String,
    takeLabel: String,
    hint: String,
    onTake: () -> Unit,
    onClear: () -> Unit,
) {
    val hasPhoto = photoPath.isNotBlank() && File(photoPath).exists()
    val shape = RoundedCornerShape(24.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .height(if (hasPhoto) 220.dp else 188.dp)
            .clip(shape)
            .background(if (hasPhoto) Color.Black else SoftTealDeep)
            .then(
                if (hasPhoto) Modifier
                else Modifier.drawBehind {
                    val stroke = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 12f), 0f),
                    )
                    val inset = 1.dp.toPx()
                    val path = Path().apply {
                        addRoundRect(
                            RoundRect(
                                left = inset,
                                top = inset,
                                right = size.width - inset,
                                bottom = size.height - inset,
                                cornerRadius = CornerRadius(24.dp.toPx()),
                            ),
                        )
                    }
                    drawPath(path, color = DashStroke, style = stroke)
                },
            )
            .clickable(onClick = onTake),
        contentAlignment = Alignment.Center,
    ) {
        if (hasPhoto) {
            AsyncImage(
                model = File(photoPath),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.18f)),
            )
            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(14.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.92f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = Teal, modifier = Modifier.size(16.dp))
                Text(takeLabel, fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Ink)
            }
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(onClick = onClear),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 24.dp)) {
                Box(
                    Modifier.size(58.dp).clip(CircleShape).background(SoftTeal),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = Teal, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    takeLabel,
                    fontFamily = PromptFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Ink,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    hint,
                    fontFamily = PromptFamily,
                    fontSize = 13.sp,
                    color = Muted2,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                )
            }
        }
    }
}

@Composable
private fun ParkField(
    label: String,
    value: String,
    placeholder: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onChange: (String) -> Unit,
) {
    Column(modifier) {
        Text(
            label,
            fontFamily = PromptFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = Color(0xFF4B5563),
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SoftTealDeep)
                .border(1.dp, Color(0xFFDCE8E5), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(SoftTeal),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = Teal, modifier = Modifier.size(17.dp))
            }
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                textStyle = TextStyle(fontFamily = PromptFamily, fontSize = 15.sp, color = Ink, fontWeight = FontWeight.Medium),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    Box {
                        if (value.isEmpty()) {
                            Text(placeholder, fontFamily = PromptFamily, fontSize = 14.sp, color = FieldHint, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        inner()
                    }
                },
            )
        }
    }
}

private fun spotTitle(spot: ParkSpot, t: (String) -> String): String =
    spot.label.ifBlank { t("parkUntitled").format(spot.zone, spot.floor) }

private fun formatSavedAt(millis: Long, t: (String) -> String): String {
    val fmt = SimpleDateFormat("d MMM yyyy · HH:mm", Locale.getDefault())
    return t("parkSavedAt").format(fmt.format(Date(millis)))
}
