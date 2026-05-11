package net.jacobsma.seeing_sound.latex

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

class Subsection(sectionTitle : String) : LatexObject(text = sectionTitle) {
    @Composable
    override fun View(color: Color) {
        super.View(color, MaterialTheme.typography.titleMedium.fontSize)
    }
}