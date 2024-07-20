package net.jacobsma.seeingsound.latex

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.IllegalArgumentException
import java.util.Stack

@Composable
fun LatexView(inputStream: InputStream?, textColor: Color = MaterialTheme.colorScheme.onBackground) {
//    Text(text = File(filePath).readText())
    val latexObjects = remember {
        mutableStateListOf<LatexObject>()
    }

    LaunchedEffect(true) {
        latexObjects.addAll(parse(inputStream))
    }

    Column {
        for(obj in latexObjects) {
            obj.View(textColor)
        }
    }
}

private fun parse(inputStream: InputStream?) : Collection<LatexObject> {
    val latexObjects : ArrayList<LatexObject> = ArrayList()
    val stack : Stack<LatexObject> = Stack()
    BufferedReader(InputStreamReader(inputStream)).use { reader ->
        reader.whileReadLines { line ->
            /* Remove all leading whitespace */
            cleanupLatexLine(line)?.let {

                var needNew = stack.safePeek() == null
                var env: LatexEnvironment = LatexEnvironment.TEXT
                if (it.startsWith("\\")) {
                    env = classifyCommand(it)
                    when (env) {
                        LatexEnvironment.CLOSER -> {}
                        LatexEnvironment.UNKNOWN -> {
                            if (stack.safePeek() is Equation) {
                                needNew = false
                            }
                        }
                        else -> needNew = true
                    }
                }

                if (needNew) {
                    if (stack.safePeek() is Text) {
                        val pop = stack.pop();
                        stack.amend(
                            success = { obj : LatexObject ->
                                if (obj is LatexContainer) {
                                    obj.addChild(pop)
                                }
                            },
                            failure = {
                                latexObjects.add(pop)
                            }
                        )
                    }

                    when (env) {
                        LatexEnvironment.TEXT -> stack.push(Text(it))
                        LatexEnvironment.DOCUMENT -> stack.push(LatexContainer())
                        LatexEnvironment.EQUATION -> stack.push(Equation(""))
                        LatexEnvironment.SECTION -> {
                            stack.amend(
                                success = { obj : LatexObject ->
                                    if (obj is LatexContainer) {
                                        obj.addChild(Section(it))
                                    }
                                },
                                failure = {
                                    latexObjects.add(Section(it))
                                }
                            )

                        }
                        LatexEnvironment.SUBSECTION -> Subsection(it)
                        else -> stack.push(Text(it))
                    }
                } else {
                    if (env == LatexEnvironment.CLOSER) {
                        val pop = stack.pop();
                        // FIXME: make this function not suck
                        if (stack.safePeek() != null) {
                            val parent = stack.pop()
                            if (parent  is LatexContainer) {
                                parent.addChild(pop)
                            }
                            stack.push(parent)
                        } else {
                            latexObjects.add(pop)
                        }
                    } else {
                        stack.peek().appendText(it)
                    }
                }
            }
        }
        while(stack.isNotEmpty()) {
            latexObjects.add(stack.pop())
        }
        return latexObjects
    }
}

private fun classifyCommand(command : String) : LatexEnvironment {
    if (!command.startsWith("\\")) {
        throw IllegalArgumentException("Provided String is not a LaTeX command")
    }

    // TODO: handle commands and environments
    if (command.startsWith("\\begin")) {
        val environmentType = command.substringAfter("{").substringBefore("}")
        when(environmentType) {
            "equation" -> return LatexEnvironment.EQUATION
            "document" -> return LatexEnvironment.DOCUMENT
        }
    }
    if (command.startsWith("\\end")) {
        return LatexEnvironment.CLOSER
    }
    if (command.startsWith("\\section")) {
        return LatexEnvironment.SECTION
    }

    return LatexEnvironment.UNKNOWN
}

inline fun BufferedReader.whileReadLines(handle: (String) -> Unit) {
    while (true) {
        val line = readLine() ?: break
        handle(line)
    }
}

fun <E> Stack<E>.safePeek() : E? {
    return if (isEmpty()) {
        null
    } else {
        peek();
    }
}

private inline fun <E> Stack<E>.amend(success: (e : E) -> Unit, failure: () -> Any?) {
    if (safePeek() != null) {
        val parent = pop()
        success(parent)
        push(parent)
    } else {
        failure()
    }
}

fun cleanupLatexLine(lineIn: String) : String? {
    var line = lineIn.trimStart()
    if (line.startsWith("%")) {
        return null
    }
    if (line == "") {
        line = "\n\n"
    }
    return line
}

enum class LatexEnvironment {
    UNKNOWN,
    CLOSER,
    DOCUMENT,
    TEXT,
    EQUATION,
    SECTION,
    SUBSECTION
}