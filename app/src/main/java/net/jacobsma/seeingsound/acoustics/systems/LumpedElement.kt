package net.jacobsma.seeingsound.acoustics.systems

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import net.jacobsma.seeingsound.acoustics.mass.EffectiveMass
import net.jacobsma.seeingsound.acoustics.stiffness.EffectiveStiffness
import org.jetbrains.kotlinx.multik.api.d1array
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D1
import org.jetbrains.kotlinx.multik.ndarray.data.NDArray

class LumpedElement(
    var leftStiffness: EffectiveStiffness?,
    var mass: EffectiveMass,
    var rightStiffness: EffectiveStiffness?
) {

    fun getGeneralMotionEquation(n: Int, degOfFreedom:Int): NDArray<Double, D1> {
        val sLeft = leftStiffness?.toDouble() ?: 0.0
        val sRight = rightStiffness?.toDouble() ?: 0.0
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