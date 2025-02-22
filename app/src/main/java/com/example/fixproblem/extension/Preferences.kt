package com.example.fixproblem.extension

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.fixproblem.presentation.theme.CustomColor
import com.google.firebase.firestore.GeoPoint
import com.mapbox.geojson.Point
import com.mapbox.navigation.base.formatter.DistanceFormatter
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions
import com.mapbox.navigation.base.formatter.UnitType
import com.mapbox.navigation.base.internal.time.TimeFormatter
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.tripdata.progress.model.TimeRemainingFormatter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun GeoPoint.toPoint(): Point {
    return Point.fromLngLat(this.longitude, this.latitude)
}
fun Long.formatTimestamp():String{
    val now = System.currentTimeMillis()
    val diff = now - this
    return when{
        diff < 60000 ->"Vừa xong"
        diff<3600000->"${diff/60000} phút trước"
        diff<86_400_000 -> "${diff / 3_600_000} giờ trước"
        else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(this))
    }
}
//Định dạng lại tổng quãng đường di chuyển
fun RouteProgress.formatDistance(context:Context):String{
    return MapboxDistanceFormatter(
        DistanceFormatterOptions.Builder(context)
            .unitType(UnitType.METRIC)
            .build()
    ).formatDistance(
        distance = distanceRemaining.toDouble()
    ).toString()
}
//Định dạng lại thời gian di chuyển
fun RouteProgress.formatDuration(context: Context): String {
    val timeRemainingFormatter = TimeRemainingFormatter(context = context)
    return timeRemainingFormatter.format(
        this.durationRemaining
    ).toString()
}
@Composable
fun AnimatedContainer(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(initialAlpha = 0f),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        content()
    }
}
// Shimmer effect extension
fun Modifier.shimmerEffect(visible: Boolean): Modifier = composed {
    if (!visible) return@composed this

    var xShimmer by remember { mutableFloatStateOf(0f) }
    val shimmerAnimationSpec = infiniteRepeatable<Float>(
        animation = tween(
            durationMillis = 1300,
            easing = LinearEasing,
            delayMillis = 300
        ),
        repeatMode = RepeatMode.Restart
    )
    val shimmerAnimation = rememberInfiniteTransition(label = "")
    val translateAnim = shimmerAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = shimmerAnimationSpec, label = ""
    )

    xShimmer = translateAnim.value

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.LightGray.copy(alpha = 0.6f),
                Color.LightGray.copy(alpha = 0.2f),
                Color.LightGray.copy(alpha = 0.6f),
            ),
            start = Offset(xShimmer - 200, 0f),
            end = Offset(xShimmer, 0f),
        )
    )
}