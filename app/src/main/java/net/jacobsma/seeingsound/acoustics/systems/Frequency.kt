package net.jacobsma.seeingsound.acoustics.systems

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class Frequency(frequency: Number) {
    private val _mutableVal : MutableLiveData<Number> = MutableLiveData(frequency)
    val value : LiveData<Number> = _mutableVal

    fun setFrequency(frequency: Number) {
        _mutableVal.value = frequency
    }

    fun toDouble() : Double {
        return _mutableVal.value?.toDouble() ?: 0.0
    }
}