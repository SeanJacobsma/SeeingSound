package net.jacobsma.seeingsound.acoustics.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Stiffness(leftMassStart: Dp, leftMassAmp: Dp, rightMassStart:Dp, rightMassAmp:Dp) {
    Box(
        modifier = Modifier
            .width(25.dp)
            .offset(0.dp, leftMassStart + leftMassAmp)
            .height((rightMassStart + rightMassAmp) - (leftMassStart + leftMassAmp))
            .background(color = Color.Green) // This must come after setting offset, otherwise offset is not applied

    )
}