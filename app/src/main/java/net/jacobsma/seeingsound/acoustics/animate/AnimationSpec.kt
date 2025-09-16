package net.jacobsma.seeingsound.acoustics.animate

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonSkippableComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import kotlin.math.sin

@Composable
@NonSkippableComposable
fun animateSineAsState(
    durationMillis: Int = 300,
    numCycles: Int = 1,
    offset: Double = 0.0,
    amplitude: Double = 1.0,
    phase: Double = 0.0,
    repeat: Boolean = true
): State<Float> {
    val animatedOut = remember{ Animatable(0f) }

    LaunchedEffect(durationMillis, numCycles, offset, amplitude, phase, repeat) {
        animatedOut.snapTo(0f)
        animatedOut.animateTo(
            targetValue = 1f,
            animationSpec = if (repeat) {
                infiniteRepeatable(
                    animation = sine(
                        durationMillis= durationMillis,
                        numCycles = numCycles,
                        offset = offset,
                        amplitude = if (amplitude.isNaN()) 1.0 else amplitude,
                        phase = phase),
                    repeatMode = RepeatMode.Restart
                )
            } else {
                sine(
                    durationMillis= durationMillis,
                    numCycles = numCycles,
                    offset = offset,
                    amplitude = amplitude,
                    phase = phase)
            }
        )
    }

    return if (animatedOut.value != 1f) animatedOut.asState() else remember{ mutableFloatStateOf((amplitude * sin(phase) + offset).toFloat()) }
}

@Stable
fun <T> sine(
    durationMillis: Int = 300,
    numCycles: Int = 1,
    offset: Double = 0.0,
    amplitude: Double = 1.0,
    phase: Double = 0.0,
) : TweenSpec<T> {
    val period = (durationMillis / 1000.0)
    val omega = (2 * Math.PI) / period // 2 * pi * f, f = 1/period
    val easing = Easing { x ->
        (amplitude * sin(omega * (x * period * numCycles) + phase) + offset).toFloat()
    }
    return tween(durationMillis = durationMillis * numCycles, easing = easing)
}

@Composable
fun animateTimeAsState(totalTimeMilliseconds: Float): State<Float> {
    val animatedOut = remember{ Animatable(0f) }

    LaunchedEffect(totalTimeMilliseconds) {
        animatedOut.snapTo(0f)
        animatedOut.animateTo(
            targetValue = totalTimeMilliseconds,
            animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis= totalTimeMilliseconds.toInt(),
                        easing = LinearEasing
                        ),
                    repeatMode = RepeatMode.Restart
                )
        )
    }

//    return if (animatedOut.value != totalTimeMilliseconds) animatedOut.asState() else remember{ mutableFloatStateOf(0f) }
    return animatedOut.asState()
}