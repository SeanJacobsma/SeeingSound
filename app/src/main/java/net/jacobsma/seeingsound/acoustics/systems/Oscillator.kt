package net.jacobsma.seeingsound.acoustics.systems

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import net.jacobsma.seeingsound.acoustics.addColumn
import net.jacobsma.seeingsound.acoustics.animate.animateTimeAsState
import net.jacobsma.seeingsound.acoustics.composables.Damper
import net.jacobsma.seeingsound.acoustics.composables.Mass
import net.jacobsma.seeingsound.acoustics.composables.Stiffness
import net.jacobsma.seeingsound.acoustics.damping.EffectiveDamping
import net.jacobsma.seeingsound.acoustics.mass.EffectiveMass
import net.jacobsma.seeingsound.acoustics.rref
import net.jacobsma.seeingsound.acoustics.squared
import net.jacobsma.seeingsound.acoustics.stiffness.EffectiveStiffness
import net.jacobsma.seeingsound.acoustics.toAngularFrequency
import net.jacobsma.seeingsound.acoustics.toFrequency
import org.jetbrains.kotlinx.multik.api.linalg.eigVals
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.ndarray.data.D2
import org.jetbrains.kotlinx.multik.ndarray.data.NDArray
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.data.set
import org.jetbrains.kotlinx.multik.ndarray.operations.times
import org.jetbrains.kotlinx.multik.ndarray.operations.toList
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

class Oscillator(
    initialMasses: ArrayList<EffectiveMass> = arrayListOf<EffectiveMass>(EffectiveMass(1.0)),
    initialStiffness: ArrayList<EffectiveStiffness> = arrayListOf<EffectiveStiffness>(EffectiveStiffness(1.0)),
    initialDamping: ArrayList<EffectiveDamping> = arrayListOf<EffectiveDamping>(EffectiveDamping(1.0)),
    val initialDisplacement: Double = 50.0,
    phaseOffset: Double = 0.0,
) : ViewModel() {

    val masses: ArrayList<EffectiveMass> = initialMasses

    val stiffnesses: ArrayList<EffectiveStiffness> = initialStiffness

    val le: ArrayList<LumpedElement> = ArrayList(List(masses.size) {LumpedElement(null, EffectiveMass(0), null)})
    init {
        for (n in 0 until le.size) {
            when (n) {
                le.size - 1 -> {
                    if (stiffnesses.size == masses.size + 1 ) {
                        le[n] = LumpedElement(stiffnesses[n], masses[n], stiffnesses[n + 1])
                    } else {
                        le[n] = LumpedElement(stiffnesses[n], masses[n], null)
                    }
                }
                else -> le[n] = LumpedElement(stiffnesses[n], masses[n], stiffnesses[n + 1])
            }
        }
        Log.d("TAG", ": $le")
    }
    val dampers: ArrayList<EffectiveDamping> = initialDamping

    val modalFrequencies: ArrayList<LiveValueHolder> = ArrayList(List(masses.size) {LiveValueHolder(0.0)})
    val amplitudes: ArrayList<LiveValueHolder> = ArrayList(List(masses.size) { LiveValueHolder(0.0)})

    private val _frequencyIndex: MutableLiveData<Int> = MutableLiveData(1)
    val selectedFrequencyIndex: LiveData<Int> = _frequencyIndex

    private val _frequency: MutableLiveData<Double> = MutableLiveData(calcNaturalFreq())
    val frequency: LiveData<Double> = _frequency

    private val _displacement: MutableLiveData<Double> = MutableLiveData(initialDisplacement)
    val displacement: LiveData<Double> = _displacement

    private val _phase: MutableLiveData<Double> = MutableLiveData(phaseOffset)
    val phase: LiveData<Double> = _phase

    fun onMassChange(newMass: Double?, index: Int){
        masses[index].setMass(newMass ?: 0.0)
        _frequency.value = calcNaturalFreq()
    }

    fun onStiffnessChange(newStiffness: Double?, index: Int) {
        stiffnesses[index].setStiffness(newStiffness ?: 0.0)
        _frequency.value = calcNaturalFreq()
    }

    fun onDampingChange(newDamping: Double?, index: Int){
        dampers[index].setDamping(newDamping ?: 0.0)
        _frequency.value = calcNaturalFreq()
    }

    private fun getMotionEquationMatrix() : NDArray<Double, D2> {
        val N = masses.size
        val matrix: NDArray<Double, D2> = mk.zeros(N, N)
        for (i in 0 until le.size) {
            matrix[i] = le[i].getGeneralMotionEquation(i, N)
        }
        Log.d("TAG", "getMotionEquationMatrix: $matrix")
        return matrix
    }

    private fun calcNaturalFreq() : Double {
        val matrix = getMotionEquationMatrix()
        val eigenValues = mk.linalg.eigVals(matrix).toList().reversed()
        if (modalFrequencies.size != eigenValues.size) {
            throw RuntimeException("Incorrect number of modal frequencies calculated.")
        }
        for (i in 0 until eigenValues.size){
            modalFrequencies[i].setValue(toFrequency(sqrt(eigenValues[i].re)))
            Log.d("TAG", "calcNaturalFreq: f_i: ${modalFrequencies[i].toDouble()}")
        }

        Log.d("TAG", "calculated eigenvalues as: ${eigenValues[0]}")
        calcAmplitude()
        return modalFrequencies[_frequencyIndex.value!!].toDouble()
//        return if (masses[0].toDouble() == 0.0)
//            0.0
//        else
//            sqrt((stiffnesses[0].toDouble() / masses[0].toDouble()) - squared(calcDampingRatio()))
    }

    private fun calcAmplitude() : Double {
        val N = masses.size
        var matrix = getMotionEquationMatrix()
        for (n in 0 until N) {
            val omegaSquared = squared(toAngularFrequency( modalFrequencies[_frequencyIndex.value!!].toDouble()))
            Log.d("TAG", "calcAmplitude w^2: $omegaSquared")
            matrix[n,n] -= omegaSquared
        }
        Log.d("TAG", "calcAmplitude: matrix with freq $matrix")

        val B = matrix[0 until N, 0].deepCopy() * -1.0
        Log.d("TAG", "calcAmplitude: B: ${B[0]}")
        for (i in 0 until N) {
            matrix[i,0] = 0.0
        }
        Log.d("TAG", "calcAmplitude: matrix:$matrix")
        val C = addColumn(matrix, B)
        var D: NDArray<Double, D2> = mk.zeros(1,N+1)
        Log.d("TAG", "calcAmplitude: $D")
        D[0,0] = 1.0
        D[0,N] = 1.0

        val a = rref(C.cat(D))
        Log.d("TAG", "calcAmplitude: $a")
        return 0.0
    }

    private fun calcDampingRatio(): Double {
        return if (masses[0].toDouble() == 0.0)
            0.0
        else
            dampers[0].toDouble() / (2.0 * masses[0].toDouble())
    }

    fun updateDisplacement(timeSeconds: Float) : Double {
        val amplitude: Double = initialDisplacement * exp(-1*calcDampingRatio()*timeSeconds)
        _displacement.value = amplitude * sin(toAngularFrequency(_frequency.value!!) * timeSeconds +_phase.value!!)
//        Log.d("TAG", "$timeSeconds updateDisplacement: disp: ${_displacement.value}, init disp: $initialDisplacement, freq: ${_frequency.value}")
        return _displacement.value!!
    }

}

@Composable
fun SingleDOF(
    start: Float = 200f,
    dur: Int = 1000,
    oscillator: Oscillator = Oscillator()
) {
    val mass: Number by oscillator.masses[0].value.observeAsState(0.0)
    val stiffness: Number by oscillator.stiffnesses[0].value.observeAsState(0.0)
    val damping: Number by oscillator.dampers[0].value.observeAsState(0.0)

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
            Box(
                modifier = Modifier,
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
                    Mass(mass = mass.toDouble(), oscillator.updateDisplacement(time/1000).dp, start.dp)
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
                    value = mass.toDouble(),
                    onValueChange = { oscillator.onMassChange(it, 0) },
                    label = "Mass"
                )
                NumberPicker(
                    value = stiffness.toDouble(),
                    onValueChange = { oscillator.onStiffnessChange(it, 0) },
                    label = "Stiffness"
                )
                NumberPicker(
                    value = damping.toDouble(),
                    onValueChange = { oscillator.onDampingChange(it, 0) },
                    label = "Damping"
                )
            }
        }
    }
}

@Composable
fun MassSpring2DOF(
    start: Float = 200f,
    dur: Int = 1000,
    oscillator: Oscillator = Oscillator()
) {
    val mass1: Number by oscillator.masses[0].value.observeAsState(0.0)
    val mass2: Number by oscillator.masses[1].value.observeAsState(0.0)
    val stiffness1: Number by oscillator.stiffnesses[0].value.observeAsState(0.0)
    val stiffness2: Number by oscillator.stiffnesses[1].value.observeAsState(0.0)
    val stiffness3: Number by oscillator.stiffnesses[2].value.observeAsState(0.0)
    val damping: Number by oscillator.dampers[0].value.observeAsState(0.0)

    val time:Float by animateTimeAsState(
        totalTimeMilliseconds = 10000f
    )
    oscillator.updateDisplacement(time / 1000)

    Box(modifier = Modifier
        .fillMaxSize(),
        contentAlignment = Alignment.TopStart
    ) {
        Row(
            modifier = Modifier,
        ) {
            BoxWithConstraints(modifier = Modifier,
                contentAlignment = Alignment.TopCenter
            ) {
                val availableHeight = maxHeight
                val springHeight = (availableHeight - (10.dp * mass1.toFloat() + 60.dp) - (10.dp * mass2.toFloat() + 60.dp)) /3
//                val mass1Start = availableHeight/3 - (10.dp * mass1.toFloat() + 60.dp) /2
                val mass1Start = springHeight
//                val mass2Start = availableHeight*2/3 - (10.dp * mass2.toFloat() + 60.dp) /2
                val mass2Start = springHeight*2 + (10.dp * mass1.toFloat() + 60.dp)

                Box(
                    modifier = Modifier
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
//                    horizontalAlignment = Alignment.CenterHorizontally
                ) {


//                    Stiffness(oscillator.updateDisplacement(time / 1000).dp + start.dp)
                    Stiffness(0.dp, 0.dp, mass1Start, oscillator.displacement.value?.dp ?: 0.dp)
                    Mass(mass = mass1.toDouble(), oscillator.displacement.value?.dp ?: 0.dp, mass1Start)
//                    Mass(mass = mass1.toDouble(), 0.dp, 0.dp)
//                    Stiffness(oscillator.updateDisplacement(time / 1000).dp + start.dp)
                    Stiffness(mass1Start + (10.dp * mass1.toFloat() + 60.dp), oscillator.displacement.value?.dp ?: 0.dp,mass2Start,oscillator.displacement.value?.dp ?: 0.dp)
                    Mass(mass = mass2.toDouble(),oscillator.displacement.value?.dp ?: 0.dp, mass2Start)
//                    Stiffness(oscillator.updateDisplacement(time / 1000).dp + start.dp)
                    Stiffness(mass2Start + (10.dp * mass2.toFloat() + 60.dp),oscillator.displacement.value?.dp ?: 0.dp, availableHeight, 0.dp)

                }
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(2.dp)
                        .offset(y = (mass1Start + (10.dp * mass1.toFloat() + 60.dp) / 2))
                        .background(color = Color.Red)
                )
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(2.dp)
                        .offset(y = (mass2Start + (10.dp * mass2.toFloat() + 60.dp) / 2))
                        .background(color = Color.Red)
                )
            }

            Column(modifier = Modifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NumberPicker(
                    value = stiffness1.toDouble(),
                    onValueChange = { oscillator.onStiffnessChange(it, 0) },
                    label = "Stiffness 1"
                )
                NumberPicker(
                    value = mass1.toDouble(),
                    onValueChange = { oscillator.onMassChange(it, 0) },
                    label = "Mass 1"
                )
                NumberPicker(
                    value = stiffness2.toDouble(),
                    onValueChange = { oscillator.onStiffnessChange(it, 1) },
                    label = "Stiffness 2"
                )
                NumberPicker(
                    value = mass2.toDouble(),
                    onValueChange = { oscillator.onMassChange(it, 1) },
                    label = "Mass 2"
                )
                NumberPicker(
                    value = stiffness3.toDouble(),
                    onValueChange = { oscillator.onStiffnessChange(it, 2) },
                    label = "Stiffness 3"
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

