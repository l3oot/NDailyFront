package com.ndaily.mobile.ui.map

import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ndaily.mobile.data.Geo
import com.ndaily.mobile.data.LatLng
import com.ndaily.mobile.data.ToiletLocation
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline

@Composable
fun OsmMap(
    modifier: Modifier = Modifier,
    gps: LatLng?,
    dest: LatLng? = null,
    destName: String? = null,
    radiusMeters: Double? = null,
    toilets: List<ToiletLocation> = emptyList(),
    origin: LatLng? = null,
    path: List<LatLng> = emptyList(),
    pin: LatLng? = null,
    onLongPress: ((LatLng) -> Unit)? = null,
    onToiletClick: ((String) -> Unit)? = null,
    recenterToken: Int = 0,
    overviewToken: Int = 0,
    compassToken: Int = 0,
) {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            minZoomLevel = 5.0
            maxZoomLevel = 20.0
            controller.setZoom(14.5)
            controller.setCenter(GeoPoint(Geo.Bangkok.lat, Geo.Bangkok.lng))
        }
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    AndroidView(factory = { mapView }, modifier = modifier, update = { map ->
        map.overlays.removeAll { true }
        if (onLongPress != null) {
            map.overlays += MapEventsOverlay(object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint?) = false
                override fun longPressHelper(p: GeoPoint?): Boolean {
                    if (p != null) onLongPress(LatLng(p.latitude, p.longitude))
                    return true
                }
            })
        }
        radiusMeters?.takeIf { dest != null && it > 0 }?.let { radius ->
            val circle = Polygon(map)
            circle.points = Polygon.pointsAsCircle(GeoPoint(dest!!.lat, dest.lng), radius)
            circle.fillPaint.color = 0x333B82F6
            circle.outlinePaint.color = 0xFF3B82F6.toInt()
            circle.outlinePaint.strokeWidth = 4f
            map.overlays += circle
        }
        if (path.size >= 2) {
            val line = Polyline(map)
            line.setPoints(path.map { GeoPoint(it.lat, it.lng) })
            line.outlinePaint.color = 0xFFC33E83.toInt()
            line.outlinePaint.strokeWidth = 8f
            line.outlinePaint.strokeCap = Paint.Cap.ROUND
            map.overlays += line
        }
        origin?.let { map.overlays += dotMarker(map, it, 0xFFC33E83.toInt(), "A") }
        dest?.let { map.overlays += pinMarker(map, it, 0xFFFF4D6D.toInt(), destName) }
        pin?.let { map.overlays += pinMarker(map, it, 0xFF3B82F6.toInt(), null) }
        gps?.let { map.overlays += gpsMarker(map, it) }
        toilets.forEach { toilet ->
            val marker = pinMarker(map, LatLng(toilet.latitudeToilet, toilet.longitudeToilet), 0xFF4A89EA.toInt(), toilet.placeToilet)
            marker.setOnMarkerClickListener { _, _ ->
                onToiletClick?.invoke(toilet.toiletUuid)
                true
            }
            map.overlays += marker
        }
        map.invalidate()
    })

    DisposableEffect(recenterToken) {
        if (recenterToken > 0) {
            val target = gps ?: dest ?: pin ?: Geo.Bangkok
            mapView.controller.animateTo(GeoPoint(target.lat, target.lng))
        }
        onDispose { }
    }
    DisposableEffect(overviewToken, dest, gps, origin) {
        if (overviewToken > 0) {
            val points = listOfNotNull(gps, dest, origin, pin).map { GeoPoint(it.lat, it.lng) }
            if (points.size >= 2) {
                mapView.zoomToBoundingBox(BoundingBox.fromGeoPoints(points), true, 80)
            }
        }
        onDispose { }
    }
    DisposableEffect(compassToken) {
        if (compassToken > 0) mapView.mapOrientation = 0f
        onDispose { }
    }
}

private fun gpsMarker(map: MapView, point: LatLng): Marker {
    val marker = Marker(map)
    marker.position = GeoPoint(point.lat, point.lng)
    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
    marker.icon = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(AndroidColor.WHITE)
        setStroke(8, 0xFF3B82F6.toInt())
        setSize(28, 28)
    }
    marker.title = "GPS"
    return marker
}

private fun pinMarker(map: MapView, point: LatLng, color: Int, title: String?): Marker {
    val marker = Marker(map)
    marker.position = GeoPoint(point.lat, point.lng)
    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
    marker.icon = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
        setSize(22, 22)
    }
    marker.title = title
    return marker
}

private fun dotMarker(map: MapView, point: LatLng, color: Int, title: String): Marker {
    val marker = Marker(map)
    marker.position = GeoPoint(point.lat, point.lng)
    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
    marker.icon = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
        setSize(20, 20)
    }
    marker.title = title
    return marker
}
