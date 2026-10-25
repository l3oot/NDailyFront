package com.ndaily.mobile.ui.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ndaily.mobile.data.AppStore
import com.ndaily.mobile.data.AppUiState
import com.ndaily.mobile.data.Geo
import com.ndaily.mobile.data.SavedPlace
import com.ndaily.mobile.ui.components.ToastBar
import com.ndaily.mobile.ui.screens.AccountScreen
import com.ndaily.mobile.ui.screens.HomeScreen
import com.ndaily.mobile.ui.screens.LegalScreen
import com.ndaily.mobile.ui.screens.NearwakeMapScreen
import com.ndaily.mobile.ui.screens.NearwakeScreen
import com.ndaily.mobile.ui.screens.NotificationScreen
import com.ndaily.mobile.ui.screens.SplashScreen
import com.ndaily.mobile.ui.screens.TaxiScreen
import com.ndaily.mobile.ui.screens.ToiletFlow
import com.ndaily.mobile.ui.screens.WelcomeScreen
import com.ndaily.mobile.ui.theme.CardDark
import com.ndaily.mobile.ui.theme.Danger
import com.ndaily.mobile.ui.theme.PromptFamily

@Composable
fun AppNav(store: AppStore, state: AppUiState) {
    val nav = rememberNavController()
    val route = nav.currentBackStackEntryAsState().value?.destination?.route.orEmpty()
    val t = { key: String -> store.t(key) }

    Box(Modifier.fillMaxSize()) {
        NavHost(navController = nav, startDestination = "splash") {
            composable("splash") {
                SplashScreen(state, store) { loggedIn ->
                    nav.navigate(if (loggedIn) "home" else "welcome") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            }
            composable("welcome") {
                WelcomeScreen(
                    store = store,
                    onHome = {
                        nav.navigate("home") { popUpTo("welcome") { inclusive = true } }
                    },
                    onTerms = { nav.navigate("terms") },
                    onPrivacy = { nav.navigate("privacy") },
                )
            }
            composable("terms") {
                LegalScreen(t("termsOfServiceTitle"), t("termsBody"), t("welcomeSignIn")) { nav.popBackStack() }
            }
            composable("privacy") {
                LegalScreen(t("privacyTitle"), t("privacyBody"), t("welcomeSignIn")) { nav.popBackStack() }
            }
            composable("home") {
                HomeScreen(
                    state = state,
                    store = store,
                    onAccount = { nav.navigate("account") },
                    onNearwake = { nav.navigate("nearwake") },
                    onToilet = { nav.navigate("toilet") },
                    onTaxi = { nav.navigate("taxi") },
                )
            }
            composable("account") {
                AccountScreen(
                    state = state,
                    store = store,
                    onHome = {
                        nav.navigate("home") { popUpTo("home") { inclusive = false } }
                    },
                    onWelcome = {
                        nav.navigate("welcome") { popUpTo(0) { inclusive = true } }
                    },
                    onNotifications = { nav.navigate("notifications") },
                )
            }
            composable("notifications") {
                NotificationScreen(state, store) { nav.popBackStack() }
            }
            composable("nearwake") {
                NearwakeScreen(
                    state = state,
                    store = store,
                    onBack = { nav.popBackStack() },
                    onOpenMap = { place, fromFav ->
                        if (place == null) nav.navigate("nearwakeMap")
                        else nav.navigate(
                            "nearwakeMap?lat=${place.lat}&lng=${place.lng}&name=${android.net.Uri.encode(place.name)}&detail=${android.net.Uri.encode(place.detail)}&fromFav=$fromFav",
                        )
                    },
                )
            }
            composable(
                route = "nearwakeMap?lat={lat}&lng={lng}&name={name}&detail={detail}&fromFav={fromFav}",
                arguments = listOf(
                    navArgument("lat") { type = NavType.StringType; defaultValue = "" },
                    navArgument("lng") { type = NavType.StringType; defaultValue = "" },
                    navArgument("name") { type = NavType.StringType; defaultValue = "" },
                    navArgument("detail") { type = NavType.StringType; defaultValue = "" },
                    navArgument("fromFav") { type = NavType.BoolType; defaultValue = false },
                ),
            ) { entry ->
                val lat = entry.arguments?.getString("lat")?.toDoubleOrNull()
                val lng = entry.arguments?.getString("lng")?.toDoubleOrNull()
                val name = entry.arguments?.getString("name").orEmpty()
                val detail = entry.arguments?.getString("detail").orEmpty()
                val fromFav = entry.arguments?.getBoolean("fromFav") == true
                val place = if (lat != null && lng != null && name.isNotBlank()) {
                    SavedPlace("nav", android.net.Uri.decode(name), android.net.Uri.decode(detail), lat, lng)
                } else null
                NearwakeMapScreen(
                    state = state,
                    store = store,
                    initial = place,
                    fromFavorite = fromFav,
                    onBack = { nav.popBackStack() },
                    onSettings = { nav.navigate("notifications") },
                )
            }
            composable("nearwakeMap") {
                NearwakeMapScreen(state, store, null, false, onBack = { nav.popBackStack() }, onSettings = { nav.navigate("notifications") })
            }
            composable("toilet") { ToiletFlow(state, store) { nav.popBackStack() } }
            composable("taxi") { TaxiScreen(state, store) { nav.popBackStack() } }
        }

        val tracking = state.tracking
        val onMap = route.startsWith("nearwakeMap")
        if (tracking != null && !onMap) {
            val aboveFooter = route == "home" || route == "account"
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 12.dp, end = 12.dp, bottom = if (aboveFooter) 96.dp else 16.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardDark)
                    .padding(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        Modifier
                            .weight(1f)
                            .clickable {
                                val p = tracking.dest
                                nav.navigate(
                                    "nearwakeMap?lat=${p.lat}&lng=${p.lng}&name=${android.net.Uri.encode(p.name)}&detail=${android.net.Uri.encode(p.detail)}&fromFav=${p.let { tracking.fromFavorite }}",
                                )
                            },
                    ) {
                        Text(t("mapTrackingActive"), fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = Color(0xFF9CA3AF))
                        Text(tracking.dest.name, fontFamily = PromptFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        tracking.metersAway?.let {
                            Text("${t("trackingDistanceShort")}: ${Geo.formatDistance(it, t("taxiFareKm"), t("mapRadiusMeters"))}", fontFamily = PromptFamily, fontSize = 12.sp, color = Color(0xFFBFC3CC))
                        }
                    }
                    Text(
                        t("trackingWidgetOpenMap"),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .clickable {
                                val p = tracking.dest
                                nav.navigate("nearwakeMap?lat=${p.lat}&lng=${p.lng}&name=${android.net.Uri.encode(p.name)}&fromFav=${tracking.fromFavorite}")
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        fontFamily = PromptFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = CardDark,
                    )
                    Text(
                        t("trackingWidgetStop"),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Danger)
                            .clickable { store.stopTracking() }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        fontFamily = PromptFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Color.White,
                    )
                }
            }
        }
        if (state.toast.isNotBlank()) {
            ToastBar(state.toast, route == "home" || route == "account")
        }
    }
}
