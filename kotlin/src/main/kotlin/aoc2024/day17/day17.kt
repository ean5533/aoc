package aoc2024.day17

import lib.loadResourceMatchingPackageName
import kotlin.math.pow

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()

fun main() {
  val computer = Computer(
    input.lines()[0].split(" ").last().toInt(),
    input.lines()[1].split(" ").last().toInt(),
    input.lines()[2].split(" ").last().toInt(),
  )
  val instructions = input.lines().drop(4).first().split(" ")[1].split(",").map { it.toInt() }
  
  val output = mutableListOf<Int>()
  val final = generateSequence(computer) { it.doNext(instructions, output) }.last()
  
  println(output.joinToString(","))
}

data class Computer(val a: Int, val b: Int, val c: Int, val instructionPointer: Int = 0) {
  fun doNext(instructions: List<Int>, output: MutableList<Int>): Computer? {
    if (instructionPointer >= instructions.size - 1)
      return null
    
    val operand = instructions[instructionPointer + 1]
    return when(instructions[instructionPointer]) {
      0 -> copy(a = a / 2.0.pow(comboValueOf(operand)).toInt())
      1 -> copy(b = b.xor(operand))
      2 -> copy(b = comboValueOf(operand) % 8)
      3 -> if(a == 0 || operand == instructionPointer) this else copy(instructionPointer = operand - 2)
      4 -> copy(b = b.xor(c))
      5 -> this.also { output.add(comboValueOf(operand) % 8) }
      6 -> copy(b = a / 2.0.pow(comboValueOf(operand)).toInt())
      7 -> copy(c = a / 2.0.pow(comboValueOf(operand)).toInt())
      else -> throw IllegalStateException()
    }.let { it.copy(instructionPointer = it.instructionPointer + 2)}
  }

  private fun comboValueOf(operand: Int): Int {
    return when (operand) {
      in (0..3) -> operand
      4 -> a
      5 -> b
      6 -> c
      else -> throw IllegalStateException()
    }
  }
}
