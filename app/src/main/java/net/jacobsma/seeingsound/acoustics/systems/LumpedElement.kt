package net.jacobsma.seeingsound.acoustics.systems

import net.jacobsma.seeingsound.acoustics.damping.EffectiveDamping
import net.jacobsma.seeingsound.acoustics.mass.EffectiveMass
import net.jacobsma.seeingsound.acoustics.stiffness.EffectiveStiffness
import org.jetbrains.kotlinx.multik.api.d1array
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.ndarray.complex.ComplexDouble
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

    /**
     * Modal motion does not require damping, as the mode shapes are not dependant on damping. Modal
     * motion is the relative amplitude based on the motion of the last mass. Damping will affect the
     * natural frequency. Damping will change the amplitude over time, but relative amplitude will remain the same.
     */
    fun getModalMotionEquation(n: Int, degOfFreedom:Int, omega:Double): NDArray<Double, D1> {
        val sLeft = leftStiffness?.toDouble() ?: 0.0
//        val dLeft = leftDamping?.toDouble() ?: 0.0
        val sRight = rightStiffness?.toDouble() ?: 0.0
//        val dRight = rightDamping?.toDouble() ?: 0.0
        val m = mass.toDouble()
        if (m == 0.0) {
            return mk.d1array(degOfFreedom) { i -> 0.0 }
        }

//        Log.d("TAG", "getGeneralMotionEquation: sL:$sLeft, sR:$sRight, m:$m, omega:$omega")

        return mk.d1array(degOfFreedom) { i ->
            when (i) {
//                n - 1 -> - sLeft/m  - omega * dLeft / m
//                n -> - omega.pow(2) + (sLeft + sRight)/m + (dLeft + dRight)/m * omega
//                n + 1 -> -sRight/m - omega * dRight/m
                n - 1 -> - sLeft/m
                n -> - omega.pow(2) + (sLeft + sRight)/m
                n + 1 -> -sRight/m
                else -> 0.0
            }
        }
    }

    fun getMassArray(n: Int, degOfFreedom:Int): NDArray<ComplexDouble, D1> {
        return mk.d1array(degOfFreedom) { i ->
            when (i) {
                n -> ComplexDouble(mass.toDouble())
                else -> ComplexDouble(0.0)
            }
        }
    }

    fun getStiffnessArray(n: Int, degOfFreedom:Int): NDArray<ComplexDouble, D1> {
        val sLeft = leftStiffness?.toDouble() ?: 0.0
        val sRight = rightStiffness?.toDouble() ?: 0.0
        val m = mass.toDouble()

        return mk.d1array(degOfFreedom) { i ->
            when (i) {
                n - 1 -> ComplexDouble(-sLeft, 0.0)
                n -> ComplexDouble(sLeft + sRight, 0.0)
                n + 1 -> ComplexDouble(-sRight, 0.0)
                else -> ComplexDouble(0.0)
            }
        }
    }

    fun getDampingArray(n: Int, degOfFreedom:Int): NDArray<ComplexDouble, D1> {
        val dLeft = leftDamping?.toDouble() ?: 0.0
        val dRight = rightDamping?.toDouble() ?: 0.0
        val m = mass.toDouble()

        return mk.d1array(degOfFreedom) { i ->
            when (i) {
                n - 1 -> ComplexDouble(0.0, -dLeft)
                n -> ComplexDouble(0.0, dLeft + dRight)
                n + 1 -> ComplexDouble(0.0, -dRight)
                else -> ComplexDouble(0.0)
            }
        }
    }

    override fun toString() : String {
        return "(s:${leftStiffness?.toDouble()} d: ${leftDamping?.toDouble()} m:${mass.toDouble()} d:${rightDamping?.toDouble()} s:${rightStiffness?.toDouble()})"
    }
}