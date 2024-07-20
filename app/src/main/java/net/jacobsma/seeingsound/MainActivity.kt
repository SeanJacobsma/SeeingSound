package net.jacobsma.seeingsound

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import net.jacobsma.seeingsound.latex.EquationView
import net.jacobsma.seeingsound.ui.theme.AcousticDemoComposeTheme
import net.jacobsma.seeingsound.latex.LatexView
import java.io.IOException
import java.io.InputStream

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
                HomeScreen()
            }
            composable(items[1]) {
                LearnScreen()
            }
            composable(items[2]) {
                ExploreScreen()
            }
            composable(items[3]) {
                PlaygroundScreen()
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
fun HomeScreen() {
    Text(
        text = "Home",
        color = MaterialTheme.colorScheme.onBackground)
    EquationView(latex = "\\begin{array}{cc}a & b\\c & d\\end{array}", color = MaterialTheme.colorScheme.onBackground)
}

@Composable
fun LearnScreen() {
//    LaTeXView(latex = "\\\\sin(x) \\\\cdot \\\\cos(y) \\\\cdot \\\\sin(x \\\\cdot y)", color = MaterialTheme.colorScheme.onBackground)
//    LaTeXView(latex = "\\Delta U=-\\int_{a}^{b} \\vec{F} \\cdot \\vec{ds}", color = MaterialTheme.colorScheme.onBackground)
//    EquationView(latex = "\\Delta U=-\\int_{a}^{b} \\vec{F} \\cdot \\vec{ds} \\qquad \\qquad \\qquad" +
//            "  \\vec{F} = -\\vec{\\nabla}U = - \\frac{\\delta U}{\\delta x}\\hat{x} - \\frac{\\delta U}{\\delta y}\\hat{y} - \\frac{\\delta U}{\\delta z}\\hat{z} ", color = MaterialTheme.colorScheme.onBackground)
//    LaTeXView(latex = "\\\\section*{Notes for My Paper}\\cr", color = MaterialTheme.colorScheme.onBackground)

//        var dataText by remember {
//        mutableStateOf("asd")
//    }

    val context = LocalContext.current


//    LaunchedEffect(true) {
//        kotlin.runCatching {
//            val size: Int = inputStream.available()
//            val buffer = ByteArray(size)
//            inputStream.read(buffer)
//            String(buffer)
//        }.onSuccess {
//            dataText = it
//        }.onFailure {
//            dataText = "error"
//        }
//
//    }
    var inputStream: InputStream? = null
    var fileFound by remember {
        mutableStateOf(false)
    }
    try {
        inputStream = context.assets.open("test.tex")
        fileFound = true
    } catch (e : IOException) {
        fileFound = false
    }

    Column( modifier = Modifier
        .verticalScroll(rememberScrollState())) {
        if (fileFound) {
            LatexView(inputStream = inputStream, textColor = MaterialTheme.colorScheme.onBackground)
        } else {
            Text(text = "Error 404: File Not Found", color = MaterialTheme.colorScheme.onBackground)
        }
    }
//    Text(
//        text = "Learn",
//        color = MaterialTheme.colorScheme.onBackground)
}

@Composable
fun ExploreScreen() {
//    Text(
//        text = "Explore",
//        color = MaterialTheme.colorScheme.onBackground)
}

@Composable
fun PlaygroundScreen() {
    Text(
        text = "Playground",
        color = MaterialTheme.colorScheme.onBackground)
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



