package net.jacobsma.seeingsound.acoustics.mass

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


/**
 * Effective mass is the base class to describe different types of
 * inertial loads used in harmonic oscillation.
 */
open class EffectiveMass(mass: Number){
    private val _mutableVal : MutableLiveData<Number> = MutableLiveData(mass)
    val value : LiveData<Number> = _mutableVal

    fun setMass(mass: Number) {
        _mutableVal.value = mass
    }

    fun toDouble() : Double {
        return _mutableVal.value?.toDouble() ?: 0.0
    }

    fun toFloat() : Float {
        return _mutableVal.value?.toFloat() ?: 0f
    }

    override fun toString() : String {
        return "%.3f".format(toDouble())
    }
}