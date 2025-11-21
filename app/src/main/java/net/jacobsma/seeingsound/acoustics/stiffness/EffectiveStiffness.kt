package net.jacobsma.seeingsound.acoustics.stiffness

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Effective stiffness is the base class to describe different types of
 * restoring forces used in harmonic oscillation.
 */
open class EffectiveStiffness(stiffness: Number) {
    private val _mutableVal : MutableLiveData<Number> = MutableLiveData(stiffness)
    val value : LiveData<Number> = _mutableVal

    fun setStiffness(stiffness: Number) {
        _mutableVal.value = stiffness
    }

    fun toDouble() : Double {
        return _mutableVal.value?.toDouble() ?: 0.0
    }

    override fun toString() : String {
        return "%.3f".format(toDouble())
    }
}