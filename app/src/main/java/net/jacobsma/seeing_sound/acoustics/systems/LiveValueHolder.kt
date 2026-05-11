package net.jacobsma.seeing_sound.acoustics.systems

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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

    fun toDp() : Dp {
        return _mutableVal.value?.toDouble()?.dp ?: 0.dp
    }
}