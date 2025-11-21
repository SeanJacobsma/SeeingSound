package net.jacobsma.seeingsound.acoustics

import android.util.Log
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.ndarray.data.D1
import org.jetbrains.kotlinx.multik.ndarray.data.D2
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.MultiArray
import org.jetbrains.kotlinx.multik.ndarray.data.NDArray
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.data.set
import kotlin.math.abs

fun <T : Number> rref(matrix: D2Array<T>): D2Array<T> {
    val nRows = matrix.shape[0]
    val nCols = matrix.shape[1]
    val result = matrix.deepCopy()

    var pivotRow = 0
    for (j in 0 until nCols) {
        // Find pivot
        var maxRow = pivotRow
        for (i in pivotRow + 1 until nRows) {
            if (abs(result[i, j].toDouble()) > abs(result[maxRow, j].toDouble())) {
                maxRow = i
            }
        }
//        Log.d("TAG", "rref initial pivot:[$pivotRow,$j], Matrix:\n$result")

        if (result[maxRow, j].toDouble() == 0.0) continue

        // Swap rows if necessary
        if (maxRow != pivotRow) {
            val tempRow = result[maxRow].copy()
            result[maxRow] = result[pivotRow]
            result[pivotRow] = tempRow
//            Log.d("TAG", "rref swap pivot:[$pivotRow,$j], Matrix:\n$result")
        }

        // Scale pivot row
        val pivotValue = result[pivotRow, j].toDouble()
        if (abs(pivotValue) >= 1.0E-13) {
            for (k in j until nCols) {
                result[pivotRow, k] = (result[pivotRow, k].toDouble() / pivotValue) as T
            }
        } else {
//            Log.d("TAG", "rref: clear floating point err")
            result[pivotRow, j] = 0.0 as T // Clear floating point error
        }
//        Log.d("TAG", "rref scaled pivot:[$pivotRow,$j], Matrix:\n$result")

        if (j == nCols-1) continue //Skip clearing the last rows, because that is what we need for displacements

        // Eliminate other rows
        for (i in 0 until nRows) {
            if (i != pivotRow) {
                val factor = result[i, j].toDouble()
//                Log.d("TAG", "rref reduced row $i: factor=$factor ")
                for (k in j until nCols) {
                    result[i, k] = (result[i, k].toDouble() - factor * result[pivotRow, k].toDouble()) as T
                }
            }
        }
//        Log.d("TAG", "rref reduced pivot:[$pivotRow,$j], Matrix:\n$result")
        pivotRow++
        if (pivotRow == nRows) break
    }
    return result
}

fun addColumn(originalArray:NDArray<Double, D2>, newColumnData: MultiArray<Double, D1>): NDArray<Double, D2> {
    val numRows = originalArray.shape[0]
    val numCols = originalArray.shape[1]
    val newNumCols = numCols + 1

//    Log.d("TAG", "addColumn: $newColumnData")

    // Create a new MultiArray with the increased column dimension
    val newArray = mk.zeros<Double>(numRows, newNumCols)

    // Copy existing data
    for (i in 0 until numRows) {
        for (j in 0 until numCols) {
            newArray[i, j] = originalArray[i, j]
        }
    }

    // Add the new column's data
    for (i in 0 until numRows) {
        newArray[i, numCols] = newColumnData[i]
    }

    return newArray
}