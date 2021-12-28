package day24

private val classLoader: ClassLoader = object {}.javaClass.classLoader
private val input = classLoader.getResource("text/day24")!!.readText()


// This isn't really a solution, it's just a collection of code bits I wrote to help analyze the problem.
// Credit where it's due, I wasn't able to figure this one out myself. I most cribbed this guy's homework:
// https://github.com/dphilipson/advent-of-code-2021/blob/master/src/days/day24.rs
fun main() {
    val instructions = parseInput()

    val instructionSets = instructions.windowed(18, 18, true)

    instructionSets.mapIndexed { index, instruction -> analyze(instruction, index) }.forEach { println(it) }

    //div= 1, check=11, offset=14 -- PUSH input[0] + 14
    //div= 1, check=13, offset= 8 -- PUSH input[1] + 8
    //div= 1, check=11, offset= 4 -- PUSH input[2] + 4
    //div= 1, check=10, offset=10 -- PUSH input[3] + 10
    //div=26, check=-3, offset=14 -- POP. input[4] must be popped value - 3
    //div=26, check=-4, offset=10 -- POP. input[5] must be popped value - 4
    //div= 1, check=12, offset= 4 -- PUSH input[6] + 4
    //div=26, check=-8, offset=14 -- POP. input[7] must be popped value - 8
    //div=26, check=-3, offset= 1 -- POP. input[8] must be popped value - 3
    //div=26, check=-12, offset= 6 -- POP. input[9] must be popped value - 12
    //div= 1, check=14, offset= 0 -- PUSH input[10] + 0
    //div=26, check=-6, offset= 9 -- POP. input[11] must be popped value - 6
    //div= 1, check=11, offset=13 -- PUSH input[12] + 13
    //div=26, check=-12, offset=12 -- POP. input[13] must be popped value - 12

    // 9999XX9XXX9X9X
    // 74929995999389

    println(isValidModelNumber(74929995999389, instructions))

    // 1111XX1XXX1X1X
    // 11118151637112

    println(isValidModelNumber(11118151637112, instructions))
}

private fun analyze(instructionSet: List<Instruction>, index: Int): AnalyzedInstructionSet {
    return AnalyzedInstructionSet(
        index,
        ((instructionSet[4] as Divide).parameter as Literal).value,
        ((instructionSet[5] as Add).parameter as Literal).value,
        ((instructionSet[15] as Add).parameter as Literal).value,
    )
}

private fun isValidModelNumber(modelNumber: Long, instructions: List<Instruction>): Boolean {
    val programInputs = modelNumber.toString().toList().map { it.digitToInt() }
    val programState = executeProgram(instructions, programInputs)
    return programState.variables["z"] == 0
}

private fun executeProgram(
    instructions: List<Instruction>,
    programInputs: List<Int>
): State {
    val programState = State(programInputs.iterator())
    instructions.forEach { it.execute(programState) }
    return programState
}

private fun parseInput() = input.lines().map {
    val tokens = it.split(" ")
    val (instructionName, variableName) = tokens
    val maybeParameter = tokens.drop(2).firstOrNull()?.let {
        it.toIntOrNull()?.let { Literal(it) } ?: VariableRef(it)
    }

    when (instructionName) {
        "inp" -> Input(VariableRef(variableName))
        "add" -> Add(VariableRef(variableName), maybeParameter!!)
        "mul" -> Multiply(VariableRef(variableName), maybeParameter!!)
        "div" -> Divide(VariableRef(variableName), maybeParameter!!)
        "mod" -> Modulo(VariableRef(variableName), maybeParameter!!)
        "eql" -> Equals(VariableRef(variableName), maybeParameter!!)
        else -> throw IllegalArgumentException("Don't know what to do with line '$it'")
    }
}

private sealed interface Instruction {
    fun execute(state: State)
}

private data class Input(val variableRef: VariableRef) : Instruction {
    override fun execute(state: State) {
        state.variables[variableRef.name] = state.inputs.next()
    }
}

private data class Add(val variableRef: VariableRef, val parameter: Parameter) : Instruction {
    override fun execute(state: State) {
        state.variables[variableRef.name] = state.variables[variableRef.name]!! + parameter.getValue(state)
    }
}

private data class Multiply(val variableRef: VariableRef, val parameter: Parameter) : Instruction {
    override fun execute(state: State) {
        state.variables[variableRef.name] = state.variables[variableRef.name]!! * parameter.getValue(state)
    }
}

private data class Divide(val variableRef: VariableRef, val parameter: Parameter) : Instruction {
    override fun execute(state: State) {
        state.variables[variableRef.name] =
            (state.variables[variableRef.name]!!.toDouble() / parameter.getValue(state)).toInt()
    }
}

private data class Modulo(val variableRef: VariableRef, val parameter: Parameter) : Instruction {
    override fun execute(state: State) {
        state.variables[variableRef.name] = state.variables[variableRef.name]!! % parameter.getValue(state)
    }
}

private data class Equals(val variableRef: VariableRef, val parameter: Parameter) : Instruction {
    override fun execute(state: State) {
        state.variables[variableRef.name] =
            if (state.variables[variableRef.name]!! == parameter.getValue(state)) 1 else 0
    }
}

private sealed interface Parameter {
    fun getValue(state: State): Int
}

private data class VariableRef(val name: String) : Parameter {
    override fun getValue(state: State): Int = state.variables[name]!!
}

private data class Literal(val value: Int) : Parameter {
    override fun getValue(state: State): Int = value
}

private class State(
    val inputs: Iterator<Int>,
    val variables: MutableMap<String, Int> = mutableMapOf("w" to 0, "x" to 0, "y" to 0, "z" to 0)
)

private data class AnalyzedInstructionSet(val index: Int, val div: Int, val check: Int, val offset: Int) {
    override fun toString(): String {
        val desc = if (check > 0)
            "PUSH input[$index] + $offset"
        else "POP. input[$index] must be popped value - ${check * -1}"
        return "div=${div.toString().padStart(2)}, " +
                "check=${check.toString().padStart(2)}, " +
                "offset=${offset.toString().padStart(2)} -- $desc"
    }
}