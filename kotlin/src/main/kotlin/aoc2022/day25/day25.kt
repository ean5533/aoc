package aoc2022.day25

import lib.loadResourceMatchingPackageName

private val snafuCharsToValues = mapOf(
  '0' to 0L,
  '1' to 1L,
  '2' to 2L,
  '-' to -1L,
  '=' to -2L,
)

fun main() {
  val fuelValues = loadResourceMatchingPackageName(object {}.javaClass).trim().lines().map { it.snafuToLong() }
  println("part1: ${fuelValues.sum().toSnafu()}")
}

private fun String.snafuToLong(): Long =
  mapIndexed { index, char -> Math.pow(5.0, length - index - 1.0).toLong() * snafuCharsToValues[char]!! }.sum()

private fun Long.toSnafu(): String = if (this == 0L) "" else {
  snafuCharsToValues.entries
    .first { (_, value) -> (value + 5).mod(5) == mod(5) }
    .let { (char, value) -> ((this - value).floorDiv(5)).toSnafu() + char }
}
