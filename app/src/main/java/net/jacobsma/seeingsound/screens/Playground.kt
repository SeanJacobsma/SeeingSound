package net.jacobsma.seeingsound.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import net.jacobsma.seeingsound.acoustics.systems.MassSpring2DOF
import net.jacobsma.seeingsound.acoustics.systems.MassSpringNDOF
import net.jacobsma.seeingsound.acoustics.systems.Oscillator
import net.jacobsma.seeingsound.acoustics.systems.SingleDOF

@Composable
fun Playground(
    start: Float = 0f
) {
    var dur by remember { mutableStateOf(1000) }
    var start2 by remember { mutableStateOf(200f) }
    var clicked by remember { mutableStateOf(false)}

//    val mass = 5.0
    val mass = 1.0
//    val stiffness = 300.0
    val stiffness = 60.0
    val damping = 0.03
//    val amplitude = 50.0
    val amplitude = 30.0
    val floating = false

//    single DOF params
//    val n = 1
//    val damping = 1.0
//    val floating = true
//    val amplitude = 75.0

//    2 DOF params
//    val n = 2

//    3 DOF params
//    val n = 3

//    N DOF params
    val n = 4

    var osc by remember {mutableStateOf(
        Oscillator(
            nDOF = n,
            floatingEnd = floating,
            baseMass = mass,
            baseStiffness = stiffness,
            baseDamping = damping,
            maxAmplitude = amplitude,
        ))}
//    SingleDOF(start = start2, dur = dur, oscillator=osc)

//    MassSpring2DOF(start = start2, dur = dur, oscillator=osc)

    MassSpringNDOF(start = start2, dur = dur, oscillator = osc, n = n)


}