package net.jacobsma.seeingsound.latex

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit

open class LatexObject(
    var text: String,
) {

//    fun setText(text: String) {
//        this.text = text
//    }

    open fun appendText(text: String) {
        if (text == "\n\n" && this.text.endsWith("\n\n")) {
            return
        }
        this.text = this.text.plus(" ").plus(text)
    }

    @Composable
    open fun View(color: Color) {
        View(color, MaterialTheme.typography.bodyMedium.fontSize)
    }

    @Composable
    fun View(color: Color, fontSize : TextUnit) {
        Text(text = text, color = color, fontSize = fontSize)
    }
}