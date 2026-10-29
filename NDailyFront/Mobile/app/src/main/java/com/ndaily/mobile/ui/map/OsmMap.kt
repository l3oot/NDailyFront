package com.ndaily.mobile.ui.map

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.text.TextPaint
import android.text.TextUtils
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.PathParser
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ndaily.mobile.R
import com.ndaily.mobile.data.Geo
import com.ndaily.mobile.data.LatLng
import com.ndaily.mobile.data.ToiletLocation
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline

enum class MapPins {
    Nearwake,
    Taxi,
    Toilet,
}

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
    pathColor: Int = 0xFFC33E83.toInt(),
    pin: LatLng? = null,
    onLongPress: ((LatLng) -> Unit)? = null,
    onToiletClick: ((String) -> Unit)? = null,
    recenterToken: Int = 0,
    overviewToken: Int = 0,
    compassToken: Int = 0,
    pins: MapPins = MapPins.Nearwake,
) {
    val context = LocalContext.current
    val art = remember(context) { MapPinArt(context) }
    val mapView = remember {
        MapView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setTilesScaledToDpi(true)
            isHorizontalMapRepetitionEnabled = true
            isVerticalMapRepetitionEnabled = false
            minZoomLevel = 5.0
            maxZoomLevel = 19.0
            controller.setZoom(14.5)
            controller.setCenter(GeoPoint(Geo.Bangkok.lat, Geo.Bangkok.lng))
            setBackgroundColor(0xFFE7EEF6.toInt())
        }
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        mapView.onResume()
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
            mapView.onPause()
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
            line.outlinePaint.color = pathColor
            line.outlinePaint.strokeWidth = 8f
            line.outlinePaint.strokeCap = Paint.Cap.ROUND
            map.overlays += line
        }
        when (pins) {
            MapPins.Nearwake -> {
                origin?.let { map.overlays += art.marker(map, it, art.originDot()) }
                gps?.let {
                    val heading = if (dest != null) bearingDegrees(it, dest) else 0f
                    map.overlays += art.marker(map, it, art.gpsPuck(heading))
                }
                dest?.let { map.overlays += art.marker(map, it, art.destPin(destName)) }
                pin?.let { map.overlays += art.marker(map, it, art.draftPin()) }
            }
            MapPins.Taxi -> {
                (origin ?: gps)?.let { map.overlays += art.marker(map, it, art.taxiOrigin()) }
                dest?.let { map.overlays += art.marker(map, it, art.taxiDest()) }
            }
            MapPins.Toilet -> {
                gps?.let { map.overlays += art.marker(map, it, art.toiletGps()) }
                dest?.let { map.overlays += art.marker(map, it, art.draftPin()) }
                pin?.let { map.overlays += art.marker(map, it, art.draftPin()) }
            }
        }
        toilets.forEach { toilet ->
            val marker = art.marker(
                map,
                LatLng(toilet.latitudeToilet, toilet.longitudeToilet),
                art.toiletPin(),
            )
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

private fun bearingDegrees(from: LatLng, to: LatLng): Float {
    val lat1 = Math.toRadians(from.lat)
    val lat2 = Math.toRadians(to.lat)
    val dLng = Math.toRadians(to.lng - from.lng)
    val y = sin(dLng) * cos(lat2)
    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLng)
    return Math.toDegrees(atan2(y, x)).toFloat()
}

private data class PinBitmap(val bitmap: Bitmap, val anchorU: Float, val anchorV: Float)

private class MapPinArt(context: android.content.Context) {
    private val density = context.resources.displayMetrics.density
    private val resources = context.resources
    private val typeface = ResourcesCompat.getFont(context, R.font.prompt_bold) ?: Typeface.DEFAULT_BOLD
    private val toiletBitmap by lazy { toiletPinBitmap() }
    private val draftBitmap by lazy {
        teardrop(
            "M14 1C7 1 1.5 6.4 1.5 13.2 1.5 22.4 14 35 14 35s12.5-12.6 12.5-21.8C26.5 6.4 21 1 14 1z",
            0xFFEF4444.toInt(),
            13f,
            4.5f,
            null,
        )
    }
    private val roseBitmap by lazy {
        teardrop(
            "M14 1.5c-6.2 0-11.2 4.9-11.2 11.1 0 8.2 11.2 21.4 11.2 21.4S25.2 20.8 25.2 12.6C25.2 6.4 20.2 1.5 14 1.5Z",
            0xFFF43F5E.toInt(),
            12.2f,
            4.1f,
            null,
        )
    }
    private var labeledName: String? = null
    private var labeledPin: PinBitmap? = null
    private var puckHeading = Int.MIN_VALUE
    private var puckBitmap: PinBitmap? = null
    private val originBitmap by lazy { ringDot(0xFFC33E83.toInt(), 14f) }
    private val taxiOriginBitmap by lazy { ringDot(0xFF34C759.toInt(), 16f) }
    private val taxiDestBitmap by lazy { ringDot(0xFFFF3B30.toInt(), 16f) }
    private val toiletGpsBitmap by lazy { drawToiletGps() }

    fun marker(map: MapView, point: LatLng, icon: PinBitmap): Marker {
        val marker = Marker(map)
        marker.position = GeoPoint(point.lat, point.lng)
        marker.setAnchor(icon.anchorU, icon.anchorV)
        marker.icon = BitmapDrawable(resources, icon.bitmap)
        marker.setInfoWindow(null)
        marker.setPanToView(false)
        return marker
    }

    fun originDot() = originBitmap
    fun taxiOrigin() = taxiOriginBitmap
    fun taxiDest() = taxiDestBitmap
    fun toiletGps() = toiletGpsBitmap
    fun toiletPin() = toiletBitmap
    fun draftPin() = draftBitmap

    fun destPin(name: String?): PinBitmap {
        val label = name?.trim().orEmpty()
        if (label.isEmpty()) return roseBitmap
        val cached = labeledPin
        if (cached != null && labeledName == label) return cached
        val drawn = teardrop(
            "M14 1.5c-6.2 0-11.2 4.9-11.2 11.1 0 8.2 11.2 21.4 11.2 21.4S25.2 20.8 25.2 12.6C25.2 6.4 20.2 1.5 14 1.5Z",
            0xFFF43F5E.toInt(),
            12.2f,
            4.1f,
            label,
        )
        labeledName = label
        labeledPin = drawn
        return drawn
    }

    fun gpsPuck(heading: Float): PinBitmap {
        val key = heading.toInt()
        val cached = puckBitmap
        if (cached != null && puckHeading == key) return cached
        val size = px(35f).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val center = size / 2f
        canvas.drawCircle(center, center, center, paint(0x473B82F6))
        canvas.drawCircle(
            center,
            center + density,
            px(10f),
            paint(0x2E142850).apply { maskFilter = BlurMaskFilter(px(5f), BlurMaskFilter.Blur.NORMAL) },
        )
        canvas.drawCircle(center, center, px(10f), paint(AndroidColor.WHITE))
        val arrow = svgPath("M12,2.2 L20.2,20.6 L12,16.6 L3.8,20.6 Z")
        val scale = px(11f) / 24f
        arrow.transform(Matrix().apply { setScale(scale, scale) })
        val bounds = RectF()
        arrow.computeBounds(bounds, true)
        canvas.save()
        canvas.rotate(heading, center, center)
        canvas.translate(center - bounds.centerX(), center - bounds.centerY())
        canvas.drawPath(arrow, paint(0xFF3B82F6.toInt()))
        canvas.restore()
        val pin = PinBitmap(bitmap, 0.5f, 0.5f)
        puckHeading = key
        puckBitmap = pin
        return pin
    }

    private fun drawToiletGps(): PinBitmap {
        val radius = px(13f)
        val size = (radius * 2f).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val center = size / 2f
        canvas.drawCircle(center, center, radius, paint(0x474A89EA))
        canvas.drawCircle(center, center, px(9f), paint(AndroidColor.WHITE))
        canvas.drawCircle(center, center, px(7f), paint(0xFF4A89EA.toInt()))
        return PinBitmap(bitmap, 0.5f, 0.5f)
    }

    private fun ringDot(color: Int, diameterDp: Float): PinBitmap {
        val border = px(2f)
        val radius = px(diameterDp) / 2f + border
        val pad = px(6f)
        val size = ((radius + pad) * 2f).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val center = size / 2f
        canvas.drawCircle(
            center,
            center + density,
            radius,
            paint(0x59000000).apply { maskFilter = BlurMaskFilter(px(4f), BlurMaskFilter.Blur.NORMAL) },
        )
        canvas.drawCircle(center, center, radius, paint(AndroidColor.WHITE))
        canvas.drawCircle(center, center, px(diameterDp) / 2f, paint(color))
        return PinBitmap(bitmap, 0.5f, 0.5f)
    }

    private fun toiletPinBitmap(): PinBitmap {
        val pad = px(8f)
        val width = (px(40f) + pad * 2f).toInt().coerceAtLeast(1)
        val height = (px(48f) + pad * 2f).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val blue = paint(0xFF2F80ED.toInt())
        val shadow = paint(0x47142850).apply { maskFilter = BlurMaskFilter(px(3f), BlurMaskFilter.Blur.NORMAL) }
        val cx = pad + px(20f)
        val bowl = pad + px(18f) + px(2f)
        canvas.drawCircle(cx, pad + px(44.5f) + px(2f), px(3.2f), shadow)
        canvas.drawCircle(cx, bowl, px(16f), shadow)
        canvas.drawCircle(cx, pad + px(44.5f), px(3.2f), blue)
        canvas.drawCircle(cx, pad + px(18f), px(16f), blue)
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.6f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            color = AndroidColor.WHITE
        }
        val bowlPath = svgPath("M7,12 h13 a1,1,0,0,1,1,1 a5,5,0,0,1,-5,5 h-0.598 a0.5,0.5,0,0,0,-0.424,0.765 l1.544,2.47 a0.5,0.5,0,0,1,-0.424,0.765 H5.402 a0.5,0.5,0,0,1,-0.424,-0.765 L7,18")
        val tankPath = svgPath("M8,18 a5,5,0,0,1,-5,-5 V4 a2,2,0,0,1,2,-2 h8 a2,2,0,0,1,2,2 v8")
        canvas.save()
        canvas.translate(pad + px(11f), pad + px(9f))
        canvas.scale(0.75f * density, 0.75f * density)
        canvas.drawPath(bowlPath, stroke)
        canvas.drawPath(tankPath, stroke)
        canvas.restore()
        return PinBitmap(bitmap, (pad + px(20f)) / width, (pad + px(47f)) / height)
    }

    private fun teardrop(data: String, color: Int, holeY: Float, holeR: Float, label: String?): PinBitmap {
        val pad = px(16f)
        val pinW = px(28f)
        val pinH = px(36f)
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = 0xFF1B2430.toInt()
            textSize = px(13f)
            typeface = this@MapPinArt.typeface
        }
        val labelText = label?.let {
            TextUtils.ellipsize(it, textPaint, px(150f), TextUtils.TruncateAt.END).toString()
        }
        val labelW = if (labelText.isNullOrEmpty()) 0f else textPaint.measureText(labelText) + px(20f)
        val labelH = if (labelText.isNullOrEmpty()) 0f else textPaint.fontMetrics.descent - textPaint.fontMetrics.ascent + px(12f)
        val gap = if (labelW > 0f) px(6f) else 0f
        val width = (pad * 2f + labelW + gap + pinW).toInt().coerceAtLeast(1)
        val height = (pad * 2f + maxOf(pinH, labelH + px(1f))).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val pinLeft = pad + labelW + gap
        val pinTop = pad
        if (!labelText.isNullOrEmpty()) {
            val rect = RectF(pad, pad + px(1f), pad + labelW, pad + px(1f) + labelH)
            canvas.drawRoundRect(
                RectF(rect.left, rect.top + px(4f), rect.right, rect.bottom + px(4f)),
                px(10f),
                px(10f),
                paint(0x29142850).apply { maskFilter = BlurMaskFilter(px(7f), BlurMaskFilter.Blur.NORMAL) },
            )
            canvas.drawRoundRect(rect, px(10f), px(10f), paint(AndroidColor.WHITE))
            val baseline = rect.top + px(6f) - textPaint.fontMetrics.ascent
            canvas.drawText(labelText, rect.left + px(10f), baseline, textPaint)
        }
        val path = svgPath(data)
        canvas.save()
        canvas.translate(pinLeft, pinTop)
        canvas.scale(density, density)
        canvas.drawPath(path, paint(color))
        canvas.drawCircle(14f, holeY, holeR, paint(AndroidColor.WHITE))
        canvas.restore()
        val anchorX = pinLeft + px(14f)
        val anchorY = pinTop + px(34f)
        return PinBitmap(bitmap, anchorX / width, anchorY / height)
    }

    private fun svgPath(data: String): Path {
        return try {
            PathParser.createPathFromPathData(data)
        } catch (_: RuntimeException) {
            Path()
        }
    }

    private fun paint(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }

    private fun px(dp: Float) = dp * density
}
