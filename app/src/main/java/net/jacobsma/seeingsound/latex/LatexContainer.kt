package net.jacobsma.seeingsound.latex

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

class LatexContainer() : LatexObject(text = "") {
    val children : MutableList<LatexObject> = mutableListOf()
    constructor(children: MutableList<LatexObject>) : this() {
        children.addAll(children)
    }

    override fun appendText(text: String) {
        if (children.isNotEmpty() && children.last() is Text) {
            children.last().appendText(text)
        } else {
            addChild(Text(text))
        }
    }

    fun addChild(child: LatexObject) {
        children.add(child)
    }

    @Composable
    override fun View(color: Color) {
        Column {
            super.View(color)
            for (child in children) {
                child.View(color)
            }

        }
    }

}