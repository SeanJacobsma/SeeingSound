package net.jacobsma.seeingsound.acoustics

import net.jacobsma.seeingsound.acoustics.stiffness.EffectiveStiffness
import kotlin.math.PI

fun HookesLawStiffness(displacement: Double, force: Double) : Double {
    return force/displacement
}

fun HookesLawDisplacement(stiffness: EffectiveStiffness, force: Double) : Double {
    return force/stiffness.toDouble()
}

fun HookesLawForce(stiffness: Double, displacement: Double) : Double {
    return stiffness*displacement
}

fun toFrequency(angularFrequency: Double) : Double {
    return angularFrequency / (2 * PI)
}

fun toAngularFrequency(frequency: Double) : Double {
    return frequency * 2 * PI
}

fun squared(number: Double) : Double {
    return number * number
}

