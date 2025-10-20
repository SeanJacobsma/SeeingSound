package net.jacobsma.seeingsound.acoustics.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.jacobsma.seeingsound.acoustics.mass.EffectiveMass

@Composable
fun Mass(mass: EffectiveMass, amplitude: Dp, staticDisplacement: Dp) {
    val massObserver: Number by mass.value.observeAsState(0.0)
    Box(
        modifier = Modifier
            .size(10.dp * massObserver.toFloat() + 60.dp)
            .offset(0.dp, staticDisplacement + amplitude)
            .background(color = Color.Blue) // This must come after setting offset, otherwise offset is not applied
    )
}