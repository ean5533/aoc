package aoc2024.day3

import lib.loadResourceMatchingPackageName

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
private val regex = """(mul\(([0-9]{1,3}),([0-9]{1,3})\))|(do\(\))|(don't\(\))""".toRegex()
private val instructions = regex.findAll(input).map {
  val text = it.groups[0]!!.value
  when(text.split("(")[0]) {
    "mul" -> Instruction.Mul(it.groups[2]!!.value.toLong(), it.groups[3]!!.value.toLong())
    "do" -> Instruction.Do
    "don't" -> Instruction.Dont
    else -> throw IllegalStateException()
  }
}.toList()


fun main() {
  part1()
  part2()
}

private fun part1() {
  val amounts = instructions.filterIsInstance<Instruction.Mul>().map { it.result }
  return println(amounts.sum())
}

private fun part2() {
  var enabled = true
  val sum = instructions.fold(0L) { acc, current ->
    val next = when(current) {
      is Instruction.Mul -> if(enabled) current.result else 0
      Instruction.Do -> {
        enabled = true
        0
      }
      Instruction.Dont -> {
        enabled = false
        0
      }
    }
    acc + next
  }
  return println(sum)
}

private sealed interface Instruction {
  data class Mul(val first: Long, val second: Long) : Instruction {
    val result = first * second
  }
  object Do : Instruction
  object Dont : Instruction
}
