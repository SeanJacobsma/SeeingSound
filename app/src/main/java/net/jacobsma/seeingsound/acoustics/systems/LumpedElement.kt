package net.jacobsma.seeingsound.acoustics.systems

import android.util.Log
import net.jacobsma.seeingsound.acoustics.damping.EffectiveDamping
import net.jacobsma.seeingsound.acoustics.mass.EffectiveMass
import net.jacobsma.seeingsound.acoustics.stiffness.EffectiveStiffness
import org.jetbrains.kotlinx.multik.api.d1array
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.ndarray.data.D1
import org.jetbrains.kotlinx.multik.ndarray.data.NDArray
import kotlin.math.pow

class LumpedElement(
    var leftStiffness: EffectiveStiffness?,
    var leftDamping: EffectiveDamping? = null,
    var mass: EffectiveMass,
    var rightDamping: EffectiveDamping? = null,
    var rightStiffness: EffectiveStiffness?
) {

    fun getGeneralMotionEquation(n: Int, degOfFreedom:Int, omega:Double): NDArray<Double, D1> {
        val sLeft = leftStiffness?.toDouble() ?: 0.0
        val dLeft = leftDamping?.toDouble() ?: 0.0
        val sRight = rightStiffness?.toDouble() ?: 0.0
        val dRight = rightDamping?.toDouble() ?: 0.0
        val m = mass.toDouble()
        if (m == 0.0) {
            return mk.d1array(degOfFreedom) { i -> 0.0 }
        }

        return mk.d1array(degOfFreedom) { i ->
            when (i) {
                n - 1 -> -sLeft / m
                n -> (sLeft + sRight) / m
                n + 1 -> -sRight / m
                else -> 0.0
            }
        }
    }

}