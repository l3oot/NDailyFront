package com.ndaily.mobile.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ndaily.mobile.data.AppStore
import com.ndaily.mobile.data.PlaceSuggestion
import kotlinx.coroutines.delay

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
