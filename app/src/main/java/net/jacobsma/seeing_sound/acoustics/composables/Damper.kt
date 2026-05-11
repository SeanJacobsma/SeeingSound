package net.jacobsma.seeing_sound.acoustics.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.jacobsma.seeing_sound.R
import net.jacobsma.seeing_sound.acoustics.damping.EffectiveDamping

@Composable
fun Damper(damping: EffectiveDamping, leftMassStart: Dp, leftMassAmp: Dp, rightMassStart:Dp, rightMassAmp:Dp) {
    Box(
        modifier = Modifier
            .offset(y=leftMassStart + leftMassAmp)
            .height((rightMassStart + rightMassAmp) - (leftMassStart + leftMassAmp)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.dashpot),
            contentDescription = "dashpot",
            modifier = Modifier
                .width(35.dp)
                .padding(start = 3.dp, end = 3.dp)
                .fillMaxHeight(),
            contentScale = ContentScale.FillBounds,
            colorFilter = ColorFilter.tint(
                MaterialTheme.colorScheme.onBackground,
                blendMode = BlendMode.SrcIn
            )
        )
    }
}