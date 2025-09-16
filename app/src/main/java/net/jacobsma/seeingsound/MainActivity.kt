package net.jacobsma.seeingsound

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import net.jacobsma.seeingsound.acoustics.animate.animateSineAsState
import net.jacobsma.seeingsound.screens.Explore
import net.jacobsma.seeingsound.ui.theme.AcousticDemoComposeTheme
import net.jacobsma.seeingsound.screens.Learn
import net.jacobsma.seeingsound.screens.Playground
import net.jacobsma.seeingsound.screens.Home

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AcousticDemoComposeTheme {
                // A surface container using the 'background' color from the theme
                UI()
            }
        }
    }
}

@Composable
fun UI() {
    val navController = rememberNavController()
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Home", "Learn", "Explore", "Playground")
    val icons = listOf(R.drawable.baseline_home_24, R.drawable.baseline_school_24, R.drawable.baseline_architecture_24, R.drawable.baseline_attractions_24)
    var startTheAnimation by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally) {
        NavHost(
            navController = navController, startDestination = items[0], modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .align(Alignment.CenterHorizontally)
        ) {
            composable(items[0]) {
                Home()
                startTheAnimation = 0f
            }
            composable(items[1]) {
                Learn()
                startTheAnimation = 20f
            }
            composable(items[2]) {
                Explore()
                startTheAnimation = 30f

            }
            composable(items[3]) {
                Playground(start = startTheAnimation)
                startTheAnimation = 1f
            }
        }
        NavigationBar(
            modifier = Modifier,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer) {
            items.forEachIndexed { index, item ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(id = icons[index]),
                            contentDescription = item
                        )
                    },
                    label = { Text(item) },
                    selected = selectedItem == index,
                    onClick = {
                        selectedItem = index
                        navController.navigate(items[index])
                    },
                    colors = NavigationBarItemColors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedIndicatorColor = MaterialTheme.colorScheme.inversePrimary,
                        unselectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unselectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        disabledIconColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledTextColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }
    }



}

@Composable
fun Test(
    start: Float = 0f,
    dur: Int = 1000
) {
    val boxHeight by animateSineAsState(
        durationMillis = dur,
        numCycles = 5,
        offset = start.toDouble(),
        amplitude = 75.0,
        phase = 90.0,
        repeat = true
    )

    Box(modifier = Modifier
        .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .background(color = Color.Green)
                    .width(50.dp)
                    .height(boxHeight.dp)
            )
            Box(
                modifier = Modifier
                    .background(color = Color.Blue)
                    .size(100.dp)
            )
        }

        Box(modifier = Modifier
            .width(150.dp)
            .height(2.dp)
            .offset(y = (start.dp + 50.dp))
            .background(color = Color.Red))
    }
}

@Composable
fun Dot() {
    Image(
        painter = painterResource(id = R.drawable.dot),
        contentDescription = null,
        modifier = Modifier
            .size(25.dp)
            .onGloballyPositioned { coordinates ->

                Log.d("TAG", "UI: ${coordinates.size.width}")
            },
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
    )
}

fun pxToDp(px:Float, density:Float) : Dp {
    return (px/density).dp
}

fun dpToPx(dp:Int, density:Float) : Float {
    return (dp*density)
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
fun DefaultPreview() {
    AcousticDemoComposeTheme {
        UI()
    }
}



