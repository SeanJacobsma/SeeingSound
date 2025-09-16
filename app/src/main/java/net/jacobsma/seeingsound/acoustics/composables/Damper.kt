package net.jacobsma.seeingsound.acoustics.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Damper(height: Dp) {
    Box(
        modifier = Modifier
            .background(color = Color.Yellow)
            .width(25.dp)
            .height(height)
    )
}