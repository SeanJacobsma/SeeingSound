package net.jacobsma.seeingsound.acoustics.systems

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class LiveValueHolder(value: Number) {
    private val _mutableVal : MutableLiveData<Number> = MutableLiveData(value)
    val value : LiveData<Number> = _mutableVal

    fun setValue(value: Number) {
        _mutableVal.value = value
    }

    fun toDouble() : Double {
        return _mutableVal.value?.toDouble() ?: 0.0
    }
}