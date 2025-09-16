package net.jacobsma.seeingsound.acoustics.systems

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.jacobsma.seeingsound.acoustics.animate.animateTimeAsState
import net.jacobsma.seeingsound.acoustics.composables.Damper
import net.jacobsma.seeingsound.acoustics.composables.Mass
import net.jacobsma.seeingsound.acoustics.composables.Stiffness
import net.jacobsma.seeingsound.acoustics.squared
import net.jacobsma.seeingsound.acoustics.toAngularFrequency
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

class Oscillator(
    initialMass: Double = 1.0,
    initialStiffness: Double = 1.0,
    initialDamping: Double = 1.0,
    val initialDisplacement: Double = 50.0,
    phaseOffset: Double = 0.0,
) : ViewModel() {
    private val _mass: MutableLiveData<Double> = MutableLiveData(initialMass)
    val mass: LiveData<Double> = _mass

    private val _stiffness: MutableLiveData<Double> = MutableLiveData(initialStiffness)
    val stiffness: LiveData<Double> = _stiffness

    private val _damping: MutableLiveData<Double> = MutableLiveData(initialDamping)
    val damping: LiveData<Double> = _damping

    private val _frequency: MutableLiveData<Double> = MutableLiveData(calcNaturalFreq())
    val frequency: LiveData<Double> = _frequency

    private val _displacement: MutableLiveData<Double> = MutableLiveData(initialDisplacement)
    val displacement: LiveData<Double> = _displacement

    private val _phase: MutableLiveData<Double> = MutableLiveData(phaseOffset)
    val phase: LiveData<Double> = _phase

    fun onMassChange(newMass: Double?){
        _mass.value = newMass ?: 0.0
        _frequency.value = calcNaturalFreq()
    }

    fun onStiffnessChange(newStiffness: Double?){
        _stiffness.value = newStiffness ?: 0.0
        _frequency.value = calcNaturalFreq()

    }

    fun onDampingChange(newDamping: Double?){
        _damping.value = newDamping ?: 0.0
        _frequency.value = calcNaturalFreq()
    }

    private fun calcNaturalFreq() : Double {
        return if (mass.value == null || stiffness.value == null)
            0.0
        else
            sqrt((stiffness.value!! / mass.value!!) - squared(calcDampingRatio()))
    }

    private fun calcDampingRatio(): Double {
        return if (mass.value == null || damping.value == null)
            0.0
        else
            damping.value!! / (2.0 * mass.value!!)
    }

    fun updateDisplacement(timeSeconds: Float) : Double {
        val amplitude: Double = initialDisplacement * exp(-1*calcDampingRatio()*timeSeconds)
        _displacement.value = amplitude * sin(toAngularFrequency(_frequency.value!!) * timeSeconds +_phase.value!!)
        Log.d("TAG", "$timeSeconds updateDisplacement: disp: ${_displacement.value}, init disp: $initialDisplacement, freq: ${_frequency.value}")
        return _displacement.value!!
    }

}

@Composable
fun SingleDOF(
    start: Float = 200f,
    dur: Int = 1000,
    oscillator: Oscillator = Oscillator()
) {
    val mass: Double by oscillator.mass.observeAsState(0.0)
    val stiffness: Double by oscillator.stiffness.observeAsState(0.0)
    val damping: Double by oscillator.damping.observeAsState(0.0)


//    val boxHeight by animateSineAsState(
//        durationMillis = dur,
//        numCycles = 5,
//        offset = start.toDouble(),
//        amplitude = if(stiffness != 0.0) 150.0 / stiffness else 150.0,
//        phase = 0.0,
//        repeat = true
//    )

    val time:Float by animateTimeAsState(
        totalTimeMilliseconds = 10000f
    )

    Box(modifier = Modifier
        .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Row(
            modifier = Modifier,
        ) {
            Box(modifier = Modifier,
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier,

                    ) {
                        Stiffness(oscillator.updateDisplacement(time / 1000).dp + start.dp)
                        if (damping != 0.0)
                            Damper(oscillator.updateDisplacement(time/1000).dp + start.dp)
                    }
                    Mass(mass = mass)
                }
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(2.dp)
                        .offset(y = (start.dp + (10.dp * mass.toFloat() + 60.dp) / 2))
                        .background(color = Color.Red)
                )
            }

            Column(modifier = Modifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NumberPicker(
                    value = mass,
                    onValueChange = { oscillator.onMassChange(it) },
                    label = "Mass"
                )
                NumberPicker(
                    value = stiffness,
                    onValueChange = { oscillator.onStiffnessChange(it) },
                    label = "Stiffness"
                )
                NumberPicker(
                    value = damping,
                    onValueChange = { oscillator.onDampingChange(it) },
                    label = "Damping"
                )
            }
        }

    }
}

@Composable
fun NumberPicker(value:Double, onValueChange: (Double?) -> Unit, label:String) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { onValueChange(it.toDoubleOrNull())},
        label = { Text(label) }
    )
}

