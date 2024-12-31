package aoc2024.day7

import lib.*

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
private val equations = input.lines().map {
  val (first, last) = it.split(":")
  val numbers = last.trim().split(" ")
  Equation(first.toLong(), numbers.map { it.toLong() })
}

fun main() {
  part1()
  part2()
}

fun part1() {
  val satisfiable = equations.filter { it.canSatisfy(false) }
  println(satisfiable.sumOf { it.testValue })
}

fun part2() {
  val satisfiable = equations.filter { it.canSatisfy(true) }
  println(satisfiable.sumOf { it.testValue })
}

data class Equation(val testValue: Long, val components: List<Long>) {
  fun canSatisfy(extraOperator: Boolean): Boolean {
    if (components.size == 1) return testValue == components.single()
    if (components[0] > testValue) return false

    val (first, second) = components

    return listOfNotNull(
      reducedTo(first + second),
      reducedTo(first * second),
      if (extraOperator) reducedTo((first.toString() + second.toString()).toLong()) else null,
    ).any { it.canSatisfy(extraOperator) }
  }

  fun reducedTo(newFirst: Long): Equation {
    return copy(components = listOf(newFirst) + components.drop(2))
  }
}
