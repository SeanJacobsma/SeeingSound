package net.jacobsma.seeingsound.latex

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.accompanist.web.LoadingState
import com.google.accompanist.web.rememberWebViewState

/**
 * This Function creates a composable view to display a LaTeX equation. Background color of view is transparent by default.
 * @property latex LaTeX string to be formatted in math mode.
 * @param color Color of text
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun EquationView(latex: String, color: Color) {

    var webView: WebView? by remember { mutableStateOf(null) }

    val state = rememberWebViewState("file:///android_asset/latex_render.html")

    if (state.loadingState is LoadingState.Finished) {
        webView?.loadUrl("javascript:addBody('${latex.replace("\\", "\\\\")}', '#${Integer.toHexString(color.toArgb().toLong().minus(0xFF000000).toInt())}')")
    }
    com.google.accompanist.web.WebView(
        state = state,
        modifier = Modifier,
        onCreated = {
            it.settings.javaScriptEnabled = true
            webView = it
            it.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null)
            it.setBackgroundColor(0)
        }
    )
}