package net.jacobsma.seeingsound.acoustics.systems

import android.util.Log
import androidx.annotation.IntRange
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.jacobsma.seeingsound.R
import net.jacobsma.seeingsound.acoustics.animate.animateTimeAsState
import net.jacobsma.seeingsound.acoustics.composables.Damper
import net.jacobsma.seeingsound.acoustics.composables.Mass
import net.jacobsma.seeingsound.acoustics.composables.Stiffness
import net.jacobsma.seeingsound.acoustics.damping.EffectiveDamping
import net.jacobsma.seeingsound.acoustics.mass.EffectiveMass
import net.jacobsma.seeingsound.acoustics.rref
import net.jacobsma.seeingsound.acoustics.stiffness.EffectiveStiffness
import net.jacobsma.seeingsound.acoustics.toAngularFrequency
import net.jacobsma.seeingsound.acoustics.toFrequency
import org.jetbrains.kotlinx.multik.api.identity
import org.jetbrains.kotlinx.multik.api.linalg.dot
import org.jetbrains.kotlinx.multik.api.linalg.eigVals
import org.jetbrains.kotlinx.multik.api.linalg.inv
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.ndarray.complex.ComplexDouble
import org.jetbrains.kotlinx.multik.ndarray.data.D2
import org.jetbrains.kotlinx.multik.ndarray.data.NDArray
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.data.set
import org.jetbrains.kotlinx.multik.ndarray.operations.append
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max

class Oscillator(
    initialMasses: ArrayList<EffectiveMass> = arrayListOf<EffectiveMass>(EffectiveMass(1.0)),
    initialStiffness: ArrayList<EffectiveStiffness> = arrayListOf<EffectiveStiffness>(EffectiveStiffness(1.0)),
    proportionalDamping: Double = 0.0,
    val maxAmplitude: Double = 50.0,
    phaseOffset: Double = 0.0,
    initialTime:Float = 0f,
    initialModeIndex:Int = 0
) : ViewModel() {

    constructor(
        nDOF: Int,
        floatingEnd: Boolean,
        baseMass:Double = 1.0,
        baseStiffness: Double = 1.0,
        baseDamping:Double = 0.0,
        maxAmplitude: Double = 50.0,
        phaseOffset: Double = 0.0,
        initialTime:Float = 0f,
        initialModeIndex: Int = 0
    ) : this(
        initialMasses = List<EffectiveMass>(nDOF) { EffectiveMass(baseMass) } as ArrayList<EffectiveMass>,
        initialStiffness = List<EffectiveStiffness>(if (floatingEnd) nDOF else (nDOF + 1)) { EffectiveStiffness(baseStiffness) } as ArrayList<EffectiveStiffness>,
        proportionalDamping= baseDamping,
        maxAmplitude = maxAmplitude,
        phaseOffset = phaseOffset,
        initialTime = initialTime,
        initialModeIndex = initialModeIndex
    )

    val masses: ArrayList<EffectiveMass> = initialMasses

    val stiffnesses: ArrayList<EffectiveStiffness> = initialStiffness
    val dampers: ArrayList<EffectiveDamping> = List(stiffnesses.size) {
        index -> EffectiveDamping(proportionalDamping * stiffnesses[index].toDouble())
    } as ArrayList<EffectiveDamping>

    private val _proportionalDamping: MutableLiveData<Double> = MutableLiveData(proportionalDamping)
    val proportionalDamping: LiveData<Double> = _proportionalDamping

    private val _dampingEnabled: MutableLiveData<Boolean> = MutableLiveData(proportionalDamping != 0.0)
    val dampingEnabled: LiveData<Boolean> = _dampingEnabled

    private val _N: MutableLiveData<Int> = MutableLiveData(masses.size)
    val N: LiveData<Int> = _N

    private val _finalStiffnessEnabled: MutableLiveData<Boolean> = MutableLiveData(stiffnesses.size == masses.size + 1)
    val finalStiffnessEnabled: LiveData<Boolean> = _finalStiffnessEnabled

    var le: ArrayList<LumpedElement> = ArrayList(List(_N.value ?: 1) {LumpedElement(leftStiffness = null, mass = EffectiveMass(0),  rightStiffness = null)})

    val modalFrequencies: ArrayList<LiveValueHolder> = ArrayList(List(_N.value ?: 1) {LiveValueHolder(0.0)})
    val modalDampingRatios: ArrayList<LiveValueHolder> = ArrayList(List(_N.value ?: 1) {LiveValueHolder(0.0)})
    val amplitudes: ArrayList<LiveValueHolder> = ArrayList(List(_N.value ?: 1) { LiveValueHolder(0.0)})

    private val _frequencyIndex: MutableLiveData<Int> = MutableLiveData(initialModeIndex)
    val selectedFrequencyIndex: LiveData<Int> = _frequencyIndex

    private val _frequency: MutableLiveData<Double> = MutableLiveData(calcNaturalFreq())
    val frequency: LiveData<Double> = _frequency

    private val _displacement: MutableLiveData<Double> = MutableLiveData(maxAmplitude)
    val displacements: ArrayList<LiveValueHolder> = ArrayList(List(_N.value ?: 1) {LiveValueHolder(0.0)})

    private val _phase: MutableLiveData<Double> = MutableLiveData(phaseOffset)
    val phase: LiveData<Double> = _phase

    private var _updatesEnabled: Boolean = true
    private var _finalStiffnessStorage: Double = stiffnesses.last().toDouble()

    init {
        buildLumpedElements()
        _frequency.value = calcNaturalFreq()
        updateDisplacement(initialTime/1000)
//        Log.d("TAG", "initialized Oscillator: ${_frequency.value}")
    }

    private fun buildLumpedElements() {
        le.clear()
        for (n in 0 until (_N.value ?: 1)) {
            val newLe: LumpedElement = when (n) {
                (_N.value ?: 1) - 1 -> {
                    if (_finalStiffnessEnabled.value == true) {
                        LumpedElement(leftStiffness = stiffnesses[n], leftDamping = dampers[n], mass = masses[n], rightStiffness = stiffnesses[n + 1], rightDamping = dampers[n + 1])
                    } else {
                        LumpedElement(leftStiffness = stiffnesses[n], leftDamping = dampers[n], mass = masses[n], rightStiffness = null, rightDamping = null)
                    }
                }

                else -> LumpedElement(leftStiffness = stiffnesses[n], leftDamping = dampers[n], mass = masses[n], rightStiffness = stiffnesses[n + 1], rightDamping = dampers[n + 1])
            }
            le.add(newLe)
        }
//        Log.d("TAG", "buildLumpedElements: $le")
    }

    fun onMassChange(newMass: Double?, index: Int = 0){
        masses[index].setMass(newMass ?: 0.0)
        le[index].mass = masses[index]
        _frequency.value = calcNaturalFreq()
    }

    fun onStiffnessChange(newStiffness: Double?, index: Int = 0) {
        stiffnesses[index].setStiffness(newStiffness ?: 0.0)
        if (_dampingEnabled.value == true) {
            dampers[index].setDamping(
                _proportionalDamping.value?.times(stiffnesses[index].toDouble()) ?: 0.0
            )
        }

        if (index != le.size) {
            le[index].leftStiffness = stiffnesses[index]
            le[index].leftDamping = dampers[index]
        }

        if (index != 0) {
            le[index - 1].rightStiffness = stiffnesses[index]
            le[index - 1].rightDamping = dampers[index]
        }

        _frequency.value = calcNaturalFreq()
    }

    fun onProportionalDampingChange(newDamping: Double?){
//        Log.d("TAG", "onProportionalDampingChange: $newDamping")
        _proportionalDamping.value = newDamping ?: 0.0
        dampingChange()
    }

    fun toggleDamping(enabled: Boolean) {
        _dampingEnabled.value = enabled
        dampingChange()
    }

    fun toggleFinalStiffness(enabled: Boolean) {
        _finalStiffnessEnabled.value = enabled
        if (enabled) {
            if (stiffnesses.size == masses.size) {
                stiffnesses.add(EffectiveStiffness(_finalStiffnessStorage))
                dampers.add(EffectiveDamping(0.0))
                le.last().rightStiffness = stiffnesses.last()
                le.last().rightDamping = dampers.last()
            }
            onStiffnessChange(_finalStiffnessStorage, stiffnesses.lastIndex)
        } else {
            _finalStiffnessStorage = stiffnesses.last().toDouble()
            onStiffnessChange(0.0, stiffnesses.lastIndex)
        }
    }

    private fun dampingChange() {
        val damping: Double? = if (_dampingEnabled.value == true) _proportionalDamping.value else 0.0

        for (i in 0 until dampers.size) {
            dampers[i].setDamping(damping?.times(stiffnesses[i].toDouble()) ?: 0.0)
        }
        _frequency.value = calcNaturalFreq()
    }

    fun onModeChange(newMode: Int?) {
        _frequencyIndex.value = newMode ?: 0
        calcAmplitude()
        _frequency.value = modalFrequencies[_frequencyIndex.value!!].toDouble()
    }

    private fun getModalMotionMatrix(omega: Double) : NDArray<Double, D2> {
        val N = masses.size
        val matrix: NDArray<Double, D2> = mk.zeros(N, N)
        for (i in 0 until le.size) {
            matrix[i] = le[i].getModalMotionEquation(i, N, omega)
        }
//        Log.d("TAG", "getMotionEquationMatrix: $matrix")
        return matrix
    }

    private fun calcModalFrequencies() {
        if (!_updatesEnabled) {
            return
        }
        val N = (N.value ?: 1)
        val ident = mk.identity<ComplexDouble>(N)
        val zeros: NDArray<ComplexDouble, D2> = mk.zeros(N, N)

        val mass: NDArray<ComplexDouble, D2> = mk.zeros(N, N)
        val damping: NDArray<ComplexDouble, D2> = mk.zeros(N, N)
        val stiffness: NDArray<ComplexDouble, D2> = mk.zeros(N, N)

        for (i in 0 until le.size) {
            mass[i] = le[i].getMassArray(i, N)
            damping[i] = le[i].getDampingArray(i, N)
            stiffness[i] = le[i].getStiffnessArray(i, N)
        }

        // Linearize the Quadratic Eigenvalue Problem (QEP)
        // a = [[Z, I];[s, d]]
        val a: NDArray<ComplexDouble, D2> = (zeros.append(ident, axis = 1)).append(
            (stiffness.append(damping, axis = 1)), axis= 0
        )

        // b = [[I, Z];[Z, m]]
        val b:NDArray<ComplexDouble, D2> = (ident.append(zeros, axis = 1)).append(
            (zeros.append( mass, axis = 1)), axis= 0
        )

        //Log.d("TAG", "calcModalFrequencies: $b")
        val bInv: NDArray<ComplexDouble, D2> = mk.linalg.inv(b)

        // Compute the standard eigenvalue matrix C = B^-1 * A
        val cMat = mk.linalg.dot(bInv, a)

        // Find the eigenvalues of C
        val eigenValues = mk.linalg.eigVals(cMat)
        // We will have double the eigenvalues as modal frequencies, because the QEP produces complex conjugates.
        if (modalFrequencies.size * 2 != eigenValues.size) {
            throw RuntimeException("Incorrect number of modal frequencies calculated.")
        }
//        Log.d("TAG", "calcModalFrequencies: eigenvalues: $eigenValues")
        val mfSize = modalFrequencies.size - 1
        var reducedEigs: ArrayList<ComplexDouble> = ArrayList()

        // This whole section probably is no longer needed, just want to make sure I don't grab the negative frequencies...
        for (i in 0 until eigenValues.size) {
            val absVal = ComplexDouble(abs(eigenValues[i].re), abs(eigenValues[i].im))
            if (reducedEigs.isEmpty()) {
                reducedEigs.add(absVal)
                continue
            }

            if (reducedEigs.any {
                (abs(it.re) - absVal.re) <= 1.0E-13  && (abs(it.im) - absVal.im) <= 1.0E-13// number is already in reducedEigs
            }) {
                continue
            }

            reducedEigs.add(absVal)
        }
        reducedEigs.reverse()
//        reducedEigs.sortWith(Comparator{ cd1, cd2 -> cd1.re.compareTo(cd2.re)}) // this doesn't work when damping gets too high
//        Log.d("TAG", "calcModalFrequencies: size mf: ${modalFrequencies.size} size reduced eig: ${reducedEigs.size}")
        while (reducedEigs.size < modalFrequencies.size) {
            reducedEigs.add(ComplexDouble(0.0))
        }
        for (i in 0..mfSize ){
            // The complex conjugates are not important for the modal frequencies because we only care about the real portion.
            modalFrequencies[i].setValue(toFrequency(reducedEigs[i].re))
            modalDampingRatios[i].setValue(reducedEigs[i].im)
//            Log.d("TAG", "calcModalFrequencies: f_$i: ${reducedEigs[i].re}")
//            Log.d("TAG", "calcModalFrequencies: beta_$i: ${reducedEigs[i].im}")
        }
    }

    private fun calcNaturalFreq() : Double {
        if (_updatesEnabled) {
            calcModalFrequencies()
            calcAmplitude()
        }
        return modalFrequencies[_frequencyIndex.value!!].toDouble()
//        return if (masses[0].toDouble() == 0.0)
//            0.0
//        else
//            sqrt((stiffnesses[0].toDouble() / masses[0].toDouble()) - squared(calcDampingRatio()))
    }

    private fun calcAmplitude(){
        if (!_updatesEnabled) {
            return
        }
        val N = masses.size
        val omega = toAngularFrequency( modalFrequencies[_frequencyIndex.value!!].toDouble())
        var matrix = getModalMotionMatrix(omega)
//        Log.d("TAG", "calcAmplitude: matrix with freq $matrix")

//        Log.d("TAG", "calcAmplitude pre rref: \n${matrix}")
        val a = rref(matrix)
        a[N-1, N-1] = -1.0 //everything is relative to the last masses amplitude
        var max = 0.0
        for (i in 0 until N) {
            max = max(abs(a[i, N-1]), max)
        }

        for (i in 0 until N) {
            amplitudes[i].setValue(-1* a[i, N-1] / max)
        }
//        Log.d("TAG", "calcAmplitude post rref: \n$a")
    }

    private fun calcDampingRatio(index: Int): Double {
//        Log.d("TAG", "calcDampingRatio: ${modalDampingRatios[index].toDouble()}")
        return modalDampingRatios[index].toDouble()
//        return if (masses[index].toDouble() == 0.0)
//            0.0
//        else
//            dampers[index].toDouble() / (2.0 * masses[index].toDouble())
    }

    fun updateDisplacement(timeSeconds: Float) : Double {
        calcAmplitude()
        for(i in 0 until displacements.size) {
            val amplitude: Double = maxAmplitude * amplitudes[i].toDouble() * exp(-1*calcDampingRatio(_frequencyIndex.value!!)*timeSeconds)
            displacements[i].setValue(amplitude * cos(toAngularFrequency(_frequency.value!!) * timeSeconds +_phase.value!!))
//        Log.d("TAG", "updateDisplacement: disp: ${displacements[0].toDouble()}, time: $timeSeconds, freq: ${_frequency.value}")
        }
        return displacements[0].toDouble()
    }

    fun incrementDOF() {
        changeDOF(masses.size + 1)
    }

    fun decrementDOF() {
        if (masses.size > 1) {
            changeDOF(masses.size - 1)
        } else {
            throw IndexOutOfBoundsException("There are too few masses to decrement")
        }
    }

    fun changeDOF(N: Int?) {
        if (N == null) {
            return
        }
//        Log.d("TAG", "changeDOF: ${masses.size} -> $N")
        _updatesEnabled = false
        while (N != masses.size) {
            if (N > masses.size) {
                stiffnesses.add(EffectiveStiffness(stiffnesses.last().toDouble()))
                dampers.add(EffectiveDamping(dampers.last().toDouble()))
                masses.add(EffectiveMass(1.0))
                modalFrequencies.add(LiveValueHolder(0.0))
                modalDampingRatios.add(LiveValueHolder(0.0))
                amplitudes.add(LiveValueHolder(0.0))
                displacements.add(LiveValueHolder(0.0))
            } else {
                stiffnesses.removeAt(stiffnesses.size - 1)
                dampers.removeAt(dampers.size - 1)
                masses.removeAt(masses.size - 1)
                modalFrequencies.removeAt(masses.size - 1)
                modalDampingRatios.removeAt(masses.size - 1)
                amplitudes.removeAt(masses.size - 1)
                displacements.removeAt(masses.size - 1)
            }
        }
        _N.value = N
        _frequencyIndex.value?.let {
            if (it >= N) {
                _frequencyIndex.value = 0
            }
        }
        buildLumpedElements()


        _updatesEnabled = true
        _frequency.value = calcNaturalFreq()
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
    oscillator.updateDisplacement(time/1000)

    Row(
        modifier = Modifier
            .fillMaxSize(),
            horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier,
            contentAlignment = Alignment.TopCenter
        ) {

            Row(modifier = Modifier) {
                Stiffness(oscillator.stiffnesses[0],0.dp, 0.dp, start.dp,oscillator.displacements[0].toDp())
                if (damping != 0.0)
                    Damper(oscillator.dampers[0], 0.dp, 0.dp, start.dp,oscillator.displacements[0].toDp())
            }
            Mass(mass = oscillator.masses[0], oscillator.displacements[0].toDp(), start.dp)

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
                onValueChange = { oscillator.onMassChange(it) },
                label = "Mass"
            )
            NumberPicker(
                value = stiffness.toDouble(),
                onValueChange = { oscillator.onStiffnessChange(it) },
                label = "Stiffness"
            )
            NumberPicker(
                value = damping.toDouble(),
                onValueChange = { oscillator.onProportionalDampingChange(it) },
                label = "Damping"
            )
        }
    }
}

@Composable
fun MassSpring2DOF(
    start: Float = 200f,
    dur: Int = 1000,
    oscillator: Oscillator = Oscillator()
) {
    val mode: Int by oscillator.selectedFrequencyIndex.observeAsState(0)

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
                val springHeight = (availableHeight - oscillator.masses[0].toDp() - oscillator.masses[1].toDp()) /3
                val mass1Start = springHeight
                val mass2Start = springHeight*2 + oscillator.masses[0].toDp()

                Box(
                    modifier = Modifier
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Stiffness(oscillator.stiffnesses[0], 0.dp, 0.dp, mass1Start, oscillator.displacements[0].toDp())
                    Mass(mass = oscillator.masses[0], oscillator.displacements[0].toDp() , mass1Start)
                    Stiffness(oscillator.stiffnesses[1], mass1Start + oscillator.masses[0].toDp(), oscillator.displacements[0].toDp(),mass2Start,oscillator.displacements[1].toDp())
                    Mass(mass = oscillator.masses[1],oscillator.displacements[1].toDp(), mass2Start)
                    Stiffness(oscillator.stiffnesses[2], mass2Start + oscillator.masses[1].toDp(),oscillator.displacements[1].toDp(), availableHeight, 0.dp)

                }
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(2.dp)
                        .offset(y = (mass1Start + oscillator.masses[0].toDp() / 2))
                        .background(color = Color.Red)
                )
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(2.dp)
                        .offset(y = (mass2Start + oscillator.masses[1].toDp() / 2))
                        .background(color = Color.Red)
                )
            }

            Column(modifier = Modifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NumberPicker(
                    value = oscillator.stiffnesses[0].toDouble(),
                    onValueChange = { oscillator.onStiffnessChange(it, 0) },
                    label = "Stiffness 1"
                )
                NumberPicker(
                    value = oscillator.masses[0].toDouble(),
                    onValueChange = { oscillator.onMassChange(it, 0) },
                    label = "Mass 1"
                )
                NumberPicker(
                    value = oscillator.stiffnesses[1].toDouble(),
                    onValueChange = { oscillator.onStiffnessChange(it, 1) },
                    label = "Stiffness 2"
                )
                NumberPicker(
                    value = oscillator.masses[1].toDouble(),
                    onValueChange = { oscillator.onMassChange(it, 1) },
                    label = "Mass 2"
                )
                NumberPicker(
                    value = oscillator.stiffnesses[2].toDouble(),
                    onValueChange = { oscillator.onStiffnessChange(it, 2) },
                    label = "Stiffness 3"
                )
                NumberPicker(
                    value = mode,
                    onValueChange = { oscillator.onModeChange(it) },
                    label = "Mode"
                )
            }
        }
    }
}

@Composable
fun MassSpringNDOF(
    start: Float = 200f,
    time: Float = 0f,
    n: Int,
    oscillator: Oscillator = Oscillator(nDOF = n, floatingEnd=false)
) {


//    val time:Float = 0f
    oscillator.updateDisplacement(time / 1000)

    Box(modifier = Modifier
        .fillMaxSize(),
//        contentAlignment = Alignment.TopCenter,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly

        ) {
            BoxWithConstraints(modifier = Modifier
                .fillMaxWidth(0.3f),
                contentAlignment = Alignment.TopCenter
            ) {
                val N = oscillator.N.value ?: 1
                val availableHeight = maxHeight
                var springHeight = availableHeight
                for (mass in oscillator.masses) {
                    springHeight -= mass.toDp()
                }
                springHeight /= N + 1

                val massStarts: ArrayList<Dp> = ArrayList()
                for (i in 1..oscillator.masses.size) {
                    var start = springHeight*i
                    for (m in 0 until i-1) {
                        start += oscillator.masses[m].toDp()
                    }
                    massStarts.add(start)
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
//                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (i in 0 until N){
                        Row(modifier=Modifier) {
                            Stiffness(
                                oscillator.stiffnesses[i],
                                if (i - 1 < 0) 0.dp else massStarts[i - 1] + oscillator.masses[i - 1].toDp(),
                                if (i - 1 < 0) 0.dp else oscillator.displacements[i - 1].toDp(),
                                massStarts[i],
                                oscillator.displacements[i].toDp()
                            )
                            if (oscillator.dampers[i].toDouble() != 0.0) {
                                Damper(
                                    oscillator.dampers[i],
                                    if (i - 1 < 0) 0.dp else massStarts[i - 1] + oscillator.masses[i - 1].toDp(),
                                    if (i - 1 < 0) 0.dp else oscillator.displacements[i - 1].toDp(),
                                    massStarts[i],
                                    oscillator.displacements[i].toDp()
                                )
                            }
                        }
                        Mass(mass = oscillator.masses[i], oscillator.displacements[i].toDp() , massStarts[i])
                    }
                    if (oscillator.finalStiffnessEnabled.value == true) {
                        Row(modifier=Modifier) {
                            Stiffness(
                                oscillator.stiffnesses[N],
                                massStarts[N - 1] + oscillator.masses[N - 1].toDp(),
                                oscillator.displacements[N - 1].toDp(),
                                availableHeight,
                                0.dp
                            )
                            if (oscillator.dampers[N].toDouble() != 0.0)
                                Damper(
                                    oscillator.dampers[N],
                                    massStarts[N - 1] + oscillator.masses[N - 1].toDp(),
                                    oscillator.displacements[N - 1].toDp(),
                                    availableHeight,
                                    0.dp
                                )
                        }
                    }
                }
                for (i in 0 until N) {
                    Row (
                        modifier = Modifier
                            .width(oscillator.masses[i].toDp() + 30.dp)
                            .offset(y = (massStarts[i] + oscillator.masses[i].toDp() / 2))
                                ,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
//                                .width(150.dp)
                                .width(10.dp)
                                .height(2.dp)
//                                .offset(y = (massStarts[i] + oscillator.masses[i].toDp() / 2))
                                .background(color = MaterialTheme.colorScheme.onBackground)
                        )
//                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .width(10.dp)
                                .height(2.dp)
//                                .offset(y = (massStarts[i] + oscillator.masses[i].toDp() / 2))
                                .background(color = MaterialTheme.colorScheme.onBackground)
                        )
                    }
                }
            }

            MassSpringNDOFMenu(n, oscillator)
        }

        Text(
            color = Color.Red,
            text= "Time: ${"%.1f".format(time/1000)} s"
        )
    }
}

@Composable
fun MassSpringNDOFMenu(
    n: Int,
    oscillator: Oscillator = Oscillator(nDOF = n, floatingEnd=false)
) {
    val mode: Int by oscillator.selectedFrequencyIndex.observeAsState(0)
    val dampingEnabled: Boolean by oscillator.dampingEnabled.observeAsState(false)
    val floatingEnd: Boolean by oscillator.finalStiffnessEnabled.observeAsState(!oscillator.finalStiffnessEnabled.value!!)
    var modeMenuExpanded by remember { mutableStateOf(false)}


    Column(modifier = Modifier
        .verticalScroll(rememberScrollState())
        .fillMaxWidth(0.7f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable {
                    modeMenuExpanded = true
                }
                .padding(
                    top = 16.dp,
                    bottom = 16.dp
                )
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Mode ${mode + 1} (${"%.3f".format(oscillator.modalFrequencies[mode].toDouble())} Hz)",
                    color = MaterialTheme.colorScheme.onBackground
                )
                Icon(
                    painter = painterResource(
                        id =
                            if (modeMenuExpanded) {
                                R.drawable.baseline_arrow_drop_up_24
                            } else {
                                R.drawable.baseline_arrow_drop_down_24
                            }
                    ),
                    contentDescription = "Mode expansion drop down",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                )
            }
//                    Image(
//                        painter = painterResource(id = R.drawable.drop_down_ic),
//                        contentDescription = "DropDown Icon"
//                    )
            DropdownMenu(
                expanded = modeMenuExpanded,
                onDismissRequest = { modeMenuExpanded = false }
            ) {
                oscillator.modalFrequencies.forEachIndexed { index, freq ->
                    DropdownMenuItem(
                        text = { Text("Mode ${index + 1} (${"%.3f".format(freq.toDouble())} Hz)", color = MaterialTheme.colorScheme.onBackground) },
                        onClick = {
                            oscillator.onModeChange(index)
                        }
                    )
                }
            }
        }

        val N by oscillator.N.observeAsState()
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
        ) {
            var decrementEnabled by remember { mutableStateOf(false) }
            // Arrow left clickable
            IconButton(
                onClick = {
//                    try {
                        oscillator.decrementDOF()
                        if (N == 1) {
                            decrementEnabled = false
                        }
//                    } catch (e: IndexOutOfBoundsException) {
//                        Log.d("Oscillator", "MassSpringNDOFMenu: ${e.message}")
//                    }
                },
                enabled = decrementEnabled,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary, // Color when enabled
                    disabledContentColor = Color.Gray // Color when disabled
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_arrow_left_24),
                    contentDescription = "Left Arrow",
                    modifier = Modifier.fillMaxSize(.85f))
            }

            NumberPicker(
                value = N ?: 1,
                onValueChange = {
                    oscillator.changeDOF(it)
                    if (it != null) {
                        decrementEnabled = it > 1
                    }
                },
//                label = "Degrees Of Freedom",
                label = "DOF",
                modifier = Modifier.width(60.dp)
            )
            // Arrow right clickable
            IconButton(
                onClick = {
                    try {
                        oscillator.incrementDOF()
                        N?.let {
                            if (it > 1) {
                                decrementEnabled = true
                            }
                        }
                    } catch (e: IndexOutOfBoundsException) {
                        Log.d("Oscillator", "MassSpringNDOFMenu: ${e.message}")
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary, // Color when enabled
                    disabledContentColor = MaterialTheme.colorScheme.scrim // Color when disabled
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_arrow_right_24),
                    contentDescription = "Right Arrow",
                    modifier = Modifier.fillMaxSize(.85f)
                )
            }
        }

        LabeledCheckBox(
            text = "Damping",
            textColor = MaterialTheme.colorScheme.onBackground,
            checked = dampingEnabled,
            onCheckedChanged = { isChecked ->
                oscillator.toggleDamping(isChecked)
            }
        )

        if (dampingEnabled) {
            val propotionalDamping by oscillator.proportionalDamping.observeAsState()
            LabeledSlider(
                text = "Proportional Damping: ${"%.3f".format(propotionalDamping ?: 0.0)}",
                value = (propotionalDamping?.toFloat() ?: 0f),
                valueRange = 0f..0.05f,
                onValueChange = { oscillator.onProportionalDampingChange(it.toDouble()) },
                steps = 0,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Damping Ratio: ${"%.3f".format(oscillator.modalDampingRatios[mode].toDouble())}",
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        LabeledCheckBox(
            text = "Floating End",
            textColor = MaterialTheme.colorScheme.onBackground,
            checked = !floatingEnd,
            onCheckedChanged = { isChecked ->
                oscillator.toggleFinalStiffness(!isChecked)
            }
        )

        N?.let { N ->
            for (i in 0 until N) {
                val stiffness by oscillator.stiffnesses[i].value.observeAsState()
                LabeledSlider(
                    text = "Stiffness ${i + 1}",
                    value = stiffness?.toFloat() ?: 0f,
                    valueRange = 6f..600f,
                    onValueChange = { oscillator.onStiffnessChange(it.toDouble(), i) },
                    steps = 10,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                val mass by oscillator.masses[i].value.observeAsState()
                LabeledSlider(
                    text = "Mass ${i + 1}",
                    value = mass?.toFloat() ?: 0f,
                    valueRange = 1f..3f,
                    onValueChange = { oscillator.onMassChange(it.toDouble(), i) },
                    steps = 9,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary,
                        activeTrackColor = MaterialTheme.colorScheme.secondary,
                        inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }
        if (floatingEnd) {
            val i = oscillator.stiffnesses.size - 1
            val stiffness by oscillator.stiffnesses[i].value.observeAsState()
            LabeledSlider(
                text = "Stiffness ${i + 1}",
                value = stiffness?.toFloat() ?: 0f,
                valueRange = 6f..600f,
                onValueChange = { oscillator.onStiffnessChange(it.toDouble(), i) },
                steps = 10,
                modifier = Modifier
                    .fillMaxWidth()
            )
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

@Composable
fun NumberPicker(value:Double, onValueChange: (Double?) -> Unit, label:String, modifier: Modifier) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { onValueChange(it.toDoubleOrNull())},
        label = { Text(label) },
        modifier = modifier
    )
}

@Composable
fun NumberPicker(value:Int, onValueChange: (Int?) -> Unit, label:String) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { onValueChange(it.toIntOrNull())},
        label = { Text(label) }
    )
}

@Composable
fun NumberPicker(value:Int, onValueChange: (Int?) -> Unit, label:String, modifier: Modifier) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { onValueChange(it.toIntOrNull())},
        label = { Text(label) },
        modifier = modifier
    )
}

@Composable
fun LabeledSlider(
    text: String,
    value:Float,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChange: (Float) -> Unit,
    colors: SliderColors = SliderDefaults.colors(),
    @IntRange steps: Int = 0,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = textColor
        )
    }

    Slider(
        value = value,
        valueRange = valueRange,
        onValueChange = onValueChange,
        colors = colors,
        steps = steps,
        modifier = modifier
    )
}

@Composable
fun LabeledCheckBox(
    text:String,
    textColor:Color=Color.Unspecified,
    checked:Boolean,
    onCheckedChanged: ((Boolean) -> Unit)?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = textColor
        )
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChanged
        )
    }
}
