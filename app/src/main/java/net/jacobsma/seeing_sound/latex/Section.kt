package net.jacobsma.seeing_sound.latex

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

class Section(sectionCommand : String, private var sectionNumber : Int = -1) : LatexObject(text = sectionCommand) {
    val sectionNumberEnabled = !text.contains("*{")
    init {
        text = text.substringAfter("{").substringBefore("}")
        setSectionNumber(sectionNumber)
    }

    fun setSectionNumber(sectionNumber: Int) {
        if (sectionNumberEnabled && sectionNumber > 0) {
            if (text.startsWith("${this.sectionNumber}    ")) {
                text.replaceFirst("${this.sectionNumber}", "$sectionNumber")
            } else {
                text = "$sectionNumber    $text"
            }
        }
        this.sectionNumber = sectionNumber
    }

    @Composable
    override fun View(color: Color) {
        super.View(color, MaterialTheme.typography.titleLarge.fontSize)
    }
}