package aoc2022.day10

import lib.loadResourceMatchingPackageName
import lib.pop
import lib.push

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
private val instructions = input.lines().map {
    when {
        it == "noop" -> NoOp
        it.startsWith("addx") -> AddX(it.split(" ")[1].toInt())
        else -> throw IllegalStateException()
    }
}

fun main() {
    part1()
    part2()
}

private fun part1() {
    val computer = Computer(instructions)
    val xVals = (1..220).map { computer.x.also { computer.tick() } }.toList()
    val majorTicks = (20..220 step 40).map { it * xVals[it - 1] }
    println(majorTicks.sum())
}

private fun part2() {
    val computer = Computer(instructions)
    val crt = (1..240).windowed(40, 40).map {
        it.mapIndexed { colIndex, _ ->
            val xVal = computer.x.also { computer.tick() }
            if ((xVal - 1..xVal + 1).contains(colIndex)) "#" else " "
        }
    }
    println(crt.joinToString("\n") { it.joinToString("") })
}

class Computer(instructions: List<Instruction>) {
    private var instructions = ArrayDeque(instructions.reversed())
    var x = 1
        private set

    fun tick() {
        when (val instruction = instructions.pop()!!) {
            NoOp -> {}
            is AddX -> {
                if (instruction.ticksLeft <= 1) x += instruction.amount
                else instructions.push(instruction.copy(ticksLeft = instruction.ticksLeft - 1))
            }
        }
    }
}

sealed interface Instruction
object NoOp : Instruction
data class AddX(val amount: Int, val ticksLeft: Int = 2) : Instruction
