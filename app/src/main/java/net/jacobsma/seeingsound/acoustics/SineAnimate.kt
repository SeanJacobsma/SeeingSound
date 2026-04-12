package net.jacobsma.seeingsound.acoustics

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import net.jacobsma.seeingsound.dpToPx
import net.jacobsma.seeingsound.pxToDp

@Composable
fun SineAnimate(
    modifier: Modifier,
    duration: Int,
//    xOffset: Int,
    xMaxOffset: Int,
    amplitude: Double,
    period: Double,
//    frequency: Double,
//    time: Int,
    isForward: Boolean,
    getYOffset: (Int, Double, Double) -> Double,
    run: () -> Boolean,
    item: @Composable () -> Unit
) {
    var xState by remember {
        mutableStateOf(0)
    }

    val dpDensity = LocalContext.current.resources.displayMetrics.density
    val LineColor = MaterialTheme.colorScheme.primary

    val time = animateIntAsState(
        targetValue = if (run()) duration else 0,
//        targetValue = if (run()) 1000 else 0,
        animationSpec = if (run()) {
            infiniteRepeatable(
                animation = tween(
                    durationMillis = duration,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            snap(0)
        }
    )

    val xOffset = animateIntAsState(
//        targetValue = xState,
        targetValue = if (run()) (xMaxOffset - dpToPx(25/2, dpDensity).toInt()) else 0,
        animationSpec = if (run()) {
            infiniteRepeatable(
                animation = tween(
//                    durationMillis =
//                    if (isForward) {
//                        duration - time.value
//                    } else {
//                        time.value
//                    },
                    durationMillis = duration,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            snap(0)
        }
    )

    // outer box
    Box(modifier = modifier
        .drawBehind {
        val height = size.height
        val width = size.width
//        Log.d("TAG", "SineAnimate: w:${width.toInt()} h:${height.toInt()}")
        val points = mutableListOf<Offset>()
        val path = Path()

        path.moveTo(dpToPx(25/2, dpDensity), (height + dpToPx(50, dpDensity))/2 )
        for (x in dpToPx(25/2, dpDensity).toInt()..width.toInt() - dpToPx(25/2, dpDensity).toInt()) {
//            Log.d("TAG", "SineAnimate: x:${x.toFloat()}")
            val y = (-getYOffset((x/width*1000).toInt(), amplitude*2, period) + (height/2 )).toFloat()
//            val y = (sin(x * (2f * PI /   (width/period)))
//                    * (height / 2) + (height / 2)).toFloat()
            val point = Offset(x.toFloat(), y)
//            Log.d("TAG", "SineAnimate: point:${point}")
            points.add(point)
            path.lineTo(x.toFloat(), y)
        }
        path.close()
//        Log.d("TAG", "SineAnimate: $points")
        drawPoints(
            points = points,
            strokeWidth = 5f,
            pointMode = PointMode.Polygon,
            color = LineColor
        )
//        drawPath(
//            path = path,
//            color = Color.Blue,
//            style = Stroke(3.0f))
    }) {
        // inner box
        Box(
            Modifier
                .offset(
                    x = pxToDp(xOffset.value.toFloat(), dpDensity),
                    y = -getYOffset(time.value, amplitude, period).dp
                )
//                .offset(
//                    x = (xMaxOffset/dpDensity).dp,
//                    y = 0.dp
//                )
                .align(Alignment.CenterStart)
//                .background(Color.Green)
        ) {
            item()
        }
    }
    xState = xMaxOffset
}