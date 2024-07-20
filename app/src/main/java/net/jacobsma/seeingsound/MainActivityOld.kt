//package net.jacobsma.seeingsound
//
//import android.os.Bundle
//import android.util.Log
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.animation.ExperimentalAnimationApi
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxHeight
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.size
//import androidx.compose.material.Button
//import androidx.compose.material.MaterialTheme
//import androidx.compose.material.Text
//import androidx.compose.material.TextField
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.saveable.rememberSaveable
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.ColorFilter
//import androidx.compose.ui.layout.onGloballyPositioned
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.IntSize
//import androidx.compose.ui.unit.dp
//import net.jacobsma.seeingsound.ui.theme.AcousticDemoComposeTheme
//import kotlin.math.sin
//
//class MainActivityOld : ComponentActivity() {
//    @OptIn(ExperimentalAnimationApi::class)
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            AcousticDemoComposeTheme {
//                // A surface container using the 'background' color from the theme
//                UI_old()
//            }
//        }
//    }
//}
//
//@Composable
//fun UI_old(){
//    Column(
//        modifier = Modifier.fillMaxSize()
//    )
//    {
//        val X_MAX_OFFSET = 250
////        val X_MAX_OFFSET = 1000
//        val X_DURATION = 1000
//        var isRunning by remember {
//            mutableStateOf(false)
//        }
////        var isRound by remember {
////            mutableStateOf(false)
////        }
//        var isForward by remember {
//            mutableStateOf(false)
//        }
//        var xState by remember {
//            mutableStateOf(0)
//        }
//
//        Button(onClick = {
//            isRunning = !isRunning
//            isForward = !isForward
//
//            xState = if (isRunning) {
//                X_MAX_OFFSET
//            } else {
//                0
//            }
//        }) {
//            Text(text = if (!isRunning) "Start" else "Stop")
//        }
//
//        var freq by rememberSaveable { mutableStateOf(1.0) }
//
//        TextField(
//            value = freq.toString(),
//            onValueChange = {
//                try {
//                    freq = it.toDouble()
//                } catch (e: Exception) {
//                    Log.d("TAG", "UI: it is not valid.")
//                }
//            },
//            label = { Text("Frequency (Hz)") }
//        )
//
//        var amplitude by rememberSaveable { mutableStateOf(1.0) }
//
//        TextField(
//            value = amplitude.toString(),
//            onValueChange = {
//                amplitude = it.toDouble()
//            },
//            label = { Text("Amplitude") }
//        )
//
////        val transition = updateTransition(
////            targetState = isRound,
////            label = null
////        )
////        val borderRadius by transition.animateInt(
////            transitionSpec = {tween(durationMillis = 1000)},
////            label = "borderRadius",
////            targetValueByState = { isRound ->
////                if(isRound) 100 else 0
////            }
////        )
////        val color by transition.animateColor(
////            transitionSpec = {tween(1000)},
////            label = "color",
////            targetValueByState = {isRound ->
////                if(isRound) Color.Green else Color.Red
////            }
////        )
////
////
////        val transition = rememberInfiniteTransition()
////        val color by transition.animateColor(
////            initialValue = Color.Red,
////            targetValue = Color.Green,
////            animationSpec = infiniteRepeatable(
////                animation = tween(2000),
////                repeatMode = RepeatMode.Reverse,
////            )
////        )
////        Box(
////            modifier = Modifier
////                .size(200.dp)
////                .background(color)
////        )
////
////
////        AnimatedVisibility(
////            visible = isVisible,
////            enter = slideInHorizontally(),
////            modifier = Modifier.fillMaxWidth().weight(1f)
////        ) {
////            Box(modifier = Modifier.background(Color.Red))
////        }
////        AnimatedContent(
////            targetState = isVisible,
////            modifier = Modifier
////                .fillMaxWidth()
////                .weight(1f),
////
////            content = {isVisible ->
////                if(isVisible) {
////                    Box(modifier = Modifier.background(Color.Green))
////                } else {
////                    Box(modifier = Modifier.background(Color.Red))
////                }
////            },
////            transitionSpec = {
////                slideInHorizontally(
////                    initialOffsetX = {
////                        if (isVisible) it else -it
////                    }
////                ) with slideOutHorizontally(
////                    targetOffsetX = {
////                        if (isVisible) -it else it
////                    }
////                )
////
////            }
////        )
//
//        val getY: (Int, Double, Double) -> Double = { x, funcAmplitude, funcPeriod ->
//            (sin(x * 2 * Math.PI / funcPeriod) * funcAmplitude)
//        }
//
//        val getRunning: () -> Boolean = {isRunning}
//
//
//        var size by remember {
//            mutableStateOf(IntSize.Zero)
//        }
//
////        if (isRunning) {
//        SineAnimate(
//            modifier = Modifier
//                .fillMaxHeight()
////                    .width(500.dp)
//                .fillMaxWidth()
//                .onGloballyPositioned { coordinates ->
//                    size = coordinates.size
//                    Log.d("TAG", "UI: ${size.width}")
//                },
////                xOffset = xOffset.value,
//            xMaxOffset = size.width,
//            amplitude = amplitude * 50.0,
//            period = 1000.0 / freq,
////            frequency = freq,
//            isForward = isForward,
//            duration = X_DURATION,
////                time = time.value,
//            getYOffset = getY,
//            run = getRunning,
//        ) {
////                if (isRunning)
//            Dot()
////                }
//        }
////        }
//    }
//}
//
////@Preview(showBackground = true)
////@Composable
////fun TestSine() {
////    val xState by remember { mutableStateOf(0) }
////    val TWO_PI = 2 * PI
////
////    val getY: (Int, Float, Float) -> Float = { x, amplitude, period ->
////        (kotlin.math.sin(x * TWO_PI / period) * amplitude).toFloat()
////    }
////    val xOffset = animateIntAsState(
////        targetValue = xState,
////        animationSpec =
////        tween(durationMillis = 4000, easing = LinearEasing, delayMillis = 500),
////    )
////    ShootArrow(
////        modifier = Modifier.fillMaxSize(fraction = .8f),
////        xOffset = xOffset.value,
////        amplitude = 1.3f,
////        getYOffset = getY,
////        period = 1000.0f,
////    ) { Arrow() }
////}
//
//@Composable
//fun Dot() {
//    Image(
//        painter = painterResource(id = R.drawable.dot),
//        contentDescription = null,
//        modifier = Modifier
//            .size(25.dp)
//            .onGloballyPositioned { coordinates ->
//
//                Log.d("TAG", "UI: ${coordinates.size.width}")
//            },
//        colorFilter = ColorFilter.tint(MaterialTheme.colors.primary)
//    )
//}
//
//fun pxToDp(px:Float, density:Float) : Dp {
//    return (px/density).dp
//}
//
//fun dpToPx(dp:Int, density:Float) : Float {
//    return (dp*density)
//}
////@Composable
////fun ShootArrow(
////    modifier: Modifier,
////    duration: Int,
//////    xOffset: Int,
////    xMaxOffset: Int,
////    amplitude: Double,
////    period: Double,
//////    frequency: Double,
//////    time: Int,
////    isForward: Boolean,
////    getYOffset: (Int, Double, Double) -> Double,
////    start: () -> Boolean,
////    item: @Composable () -> Unit
////) {
////    var xState by remember {
////        mutableStateOf(0)
////    }
////
////    val time = animateIntAsState(
//////        targetValue = if (start()) duration * xState/xMaxOffset else 0,
////        targetValue = if (start()) duration else 0,
////        animationSpec = if (start()) {
////            infiniteRepeatable(
////                animation = tween(
////                    durationMillis = duration,
////                    easing = LinearEasing
////                ),
////                repeatMode = RepeatMode.Reverse
////            )
////        } else {
////            snap(0)
////        }
////    )
////
////    val xOffset = animateIntAsState(
//////        targetValue = xState,
////        targetValue = if (start()) xMaxOffset else 0,
////        animationSpec = if (start()) {
////            infiniteRepeatable(
////                animation = tween(
////                    durationMillis =
////                    if (isForward) {
////                        duration - time.value
////                    } else {
////                        time.value
////                    },
////                    easing = LinearEasing
////                ),
////                repeatMode = RepeatMode.Reverse
////            )
////        } else {
////            snap(0)
////        }
////    )
////    // outer box
////    Box(modifier) {
////        // inner box
////        Box(
////            Modifier
////                .offset(
////                    x = xOffset.value.dp,
////                    y = -getYOffset(time.value, amplitude, period).dp
////                )
////                .align(Alignment.CenterStart)
//////                .background(Color.Green)
////        ) {
////            item()
////        }
////    }
////    xState = xMaxOffset
////}
//
////@Preview(showBackground = true)
////@Composable
////fun DefaultPreview() {
////    AcousticDemoComposeTheme {
////        TestSine()
////    }
////}
//
