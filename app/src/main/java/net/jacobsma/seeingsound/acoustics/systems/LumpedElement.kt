package net.jacobsma.seeingsound.acoustics.systems

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

//        Log.d("TAG", "getGeneralMotionEquation: sL:$sLeft, sR:$sRight, m:$m, omega:$omega")

        // TODO: damping is supposed to be an imaginary number.
        return mk.d1array(degOfFreedom) { i ->
            when (i) {
                n - 1 -> - sLeft/m  - omega * dLeft / m
                n -> - omega.pow(2) + (sLeft + sRight)/m + (dLeft + dRight)/m * omega
                n + 1 -> -sRight/m - omega * dRight/m
                else -> 0.0
            }
        }
    }

    fun getMassArray(n: Int, degOfFreedom:Int): NDArray<Double, D1> {
        return mk.d1array(degOfFreedom) { i ->
            when (i) {
                n -> mass.toDouble()
                else -> 0.0
            }
        }
    }

    fun getStiffnessArray(n: Int, degOfFreedom:Int): NDArray<Double, D1> {
        val sLeft = leftStiffness?.toDouble() ?: 0.0
        val sRight = rightStiffness?.toDouble() ?: 0.0
        val m = mass.toDouble()

        return mk.d1array(degOfFreedom) { i ->
            when (i) {
                n - 1 -> -sLeft
                n -> (sLeft + sRight)
                n + 1 -> -sRight
                else -> 0.0
            }
        }
    }

    fun getDampingArray(n: Int, degOfFreedom:Int): NDArray<Double, D1> {
        val dLeft = leftDamping?.toDouble() ?: 0.0
        val dRight = rightDamping?.toDouble() ?: 0.0
        val m = mass.toDouble()

        return mk.d1array(degOfFreedom) { i ->
            when (i) {
                n - 1 -> -dLeft
                n -> (dLeft + dRight)
                n + 1 -> -dRight
                else -> 0.0
            }
        }
    }

}