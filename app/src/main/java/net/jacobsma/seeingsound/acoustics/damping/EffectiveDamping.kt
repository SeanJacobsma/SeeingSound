package net.jacobsma.seeingsound.acoustics.damping

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Effective damping is the base class to describe different types of
 * dissipative forces used in harmonic oscillation.
 */
open class EffectiveDamping(damping: Number) {
    private val _mutableVal : MutableLiveData<Number> = MutableLiveData(damping)
    val value : LiveData<Number> = _mutableVal

    fun setDamping(damping: Number) {
        _mutableVal.value = damping
    }

    fun toDouble() : Double {
        return _mutableVal.value?.toDouble() ?: 0.0
    }
}