package net.jacobsma.seeingsound.acoustics.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.jacobsma.seeingsound.R
import net.jacobsma.seeingsound.acoustics.mass.EffectiveMass

@Composable
fun Mass(mass: EffectiveMass, amplitude: Dp, staticDisplacement: Dp) {
    val massObserver: Number by mass.value.observeAsState(0.0)
    Box(
        modifier = Modifier
            .offset(0.dp, staticDisplacement + amplitude),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.mass),
            contentDescription = "mass",
            modifier = Modifier
                .size(10.dp * massObserver.toFloat() + 60.dp),
            contentScale = ContentScale.FillBounds,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground, blendMode = BlendMode.SrcIn)
        )
    }
}