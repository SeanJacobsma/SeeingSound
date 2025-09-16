package net.jacobsma.seeingsound.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import net.jacobsma.seeingsound.latex.EquationView

@Composable
fun Home() {
    Text(
        text = "Home",
        color = MaterialTheme.colorScheme.onBackground)
    EquationView(latex = "\\begin{array}{cc}a & b\\c & d\\end{array}", color = MaterialTheme.colorScheme.onBackground)
}