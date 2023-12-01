package aoc2023.day1

import lib.loadResourceMatchingPackageName

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()

fun main() {
  part1()
  part2()
}

private fun part1() {
  val numbers = input.lines()
    .map { it.filter { it.isDigit() } }
    .map { "${it.first()}${it.last()}".toInt() }

  println(numbers.sum())
}

private fun part2() {
  val numbers = input.lines()
    .map {
      it.windowed(5, partialWindows = true).mapNotNull {
        when {
          it[0].isDigit() -> it[0]
          it.startsWith("one") -> '1'
          it.startsWith("two") -> '2'
          it.startsWith("three") -> '3'
          it.startsWith("four") -> '4'
          it.startsWith("five") -> '5'
          it.startsWith("six") -> '6'
          it.startsWith("seven") -> '7'
          it.startsWith("eight") -> '8'
          it.startsWith("nine") -> '9'
          else -> null
        }
      }
    }
    .map { "${it.first()}${it.last()}".toInt() }

  println(numbers.sum())
}
