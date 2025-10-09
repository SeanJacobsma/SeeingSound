package net.jacobsma.seeingsound.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import net.jacobsma.seeingsound.acoustics.damping.EffectiveDamping
import net.jacobsma.seeingsound.acoustics.mass.EffectiveMass
import net.jacobsma.seeingsound.acoustics.stiffness.EffectiveStiffness
import net.jacobsma.seeingsound.acoustics.systems.MassSpring2DOF
import net.jacobsma.seeingsound.acoustics.systems.Oscillator
import net.jacobsma.seeingsound.acoustics.systems.SingleDOF

@Composable
fun Playground(
    start: Float = 0f
) {
    var dur by remember { mutableStateOf(1000) }
    var start2 by remember { mutableStateOf(200f) }
    var clicked by remember { mutableStateOf(false)}
//    var osc by remember {mutableStateOf(Oscillator(initialMasses = arrayListOf(EffectiveMass(1.0)), initialDisplacement = -75.0))}
//    SingleDOF(start = start2, dur = dur, oscillator=osc)

    var osc by remember {mutableStateOf(
        Oscillator(
            initialMasses = arrayListOf(EffectiveMass(5.0), EffectiveMass(5.0)),
            initialStiffness = arrayListOf(EffectiveStiffness(3.0), EffectiveStiffness(3.0), EffectiveStiffness(3.0)),
            initialDamping = arrayListOf(EffectiveDamping(0.0)),
            initialDisplacement = 75.0
        ))}
    MassSpring2DOF(start = start2, dur = dur, oscillator=osc)

}