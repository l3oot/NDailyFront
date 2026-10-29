package com.ndaily.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ndaily.mobile.data.AppStore
import com.ndaily.mobile.data.PlaceSuggestion
import com.ndaily.mobile.ui.theme.Hint2
import com.ndaily.mobile.ui.theme.Ink
import com.ndaily.mobile.ui.theme.Muted2
import com.ndaily.mobile.ui.theme.Primary
import com.ndaily.mobile.ui.theme.PromptFamily
import kotlinx.coroutines.delay

private val CardShadow = Color(0x1428508C)
private val MenuShadow = Color(0x1F28508C)

@Composable
fun rememberPlaceSuggestions(store: AppStore, query: String): List<PlaceSuggestion> {
    var items by remember { mutableStateOf<List<PlaceSuggestion>>(emptyList()) }
    LaunchedEffect(query) {
        val q = query.trim()
        if (q.length < 2) {
            items = emptyList()
            return@LaunchedEffect
        }
        delay(350)
        items = store.searchPlaces(q)
    }
    return items
}

@Composable
fun PlaceSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(22.dp), ambientColor = CardShadow, spotColor = CardShadow)
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(fontFamily = PromptFamily, fontSize = 16.sp, color = Ink),
            modifier = Modifier.weight(1f).padding(start = 12.dp),
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text(placeholder, fontFamily = PromptFamily, fontSize = 16.sp, color = Hint2, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    inner()
                }
            },
        )
        if (value.isNotEmpty()) {
            Icon(
                Icons.Outlined.Close,
                contentDescription = null,
                tint = Hint2,
                modifier = Modifier.padding(start = 8.dp).size(16.dp).clickable { onValueChange("") },
            )
        }
    }
}

@Composable
fun PlaceSuggestionMenu(
    items: List<PlaceSuggestion>,
    modifier: Modifier = Modifier,
    onPick: (PlaceSuggestion) -> Unit,
) {
    if (items.isEmpty()) return
    Box(
        modifier
            .fillMaxWidth()
            .heightIn(max = 380.dp)
            .shadow(10.dp, RoundedCornerShape(16.dp), ambientColor = MenuShadow, spotColor = MenuShadow)
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
            items.forEach { item ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onPick(item) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    Text(item.name, fontFamily = PromptFamily, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (item.detail.isNotBlank()) {
                        Text(item.detail, fontFamily = PromptFamily, fontSize = 12.sp, color = Muted2, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
        Text(
            placeProviderLabel(items.first().source),
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 12.dp, bottom = 8.dp),
            fontFamily = PromptFamily,
            fontSize = 11.sp,
            color = Color(0xFFE6B000),
        )
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
