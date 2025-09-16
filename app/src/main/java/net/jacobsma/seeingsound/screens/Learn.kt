package net.jacobsma.seeingsound.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import net.jacobsma.seeingsound.latex.LatexView
import java.io.IOException
import java.io.InputStream

@Composable
fun Learn() {
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
    } catch (_ : IOException) {
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