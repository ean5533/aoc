package aoc2023.day6

import lib.loadResourceMatchingPackageName

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()

fun main() {
  part1()
  part2()
}

private fun part1() {
  val times = input.lines()[0].split(" ").drop(1).filter { it.isNotBlank() }.map { it.toLong() }
  val distances = input.lines()[1].split(" ").drop(1).filter { it.isNotBlank() }.map { it.toLong() }
  val races = times.zip(distances).map { Race(it.first, it.second) }

  val winWays = races.map { it.winningHoldTimes().size }.reduce { acc, i -> acc * i }
  println(winWays)
}

private fun part2() {
  val time = input.lines()[0].split(" ").drop(1).joinToString("").toLong()
  val distance = input.lines()[1].split(" ").drop(1).joinToString("").toLong()
  val races = listOf(Race(time, distance))
  
  val winWays = races.map { it.winningHoldTimes().size }.reduce { acc, i -> acc * i }
  println(winWays)
}

private data class Race(val recordTime: Long, val distance: Long) {
  fun winningHoldTimes(): List<Long> =
    (1 until recordTime).map { timeToTravel(it, distance) }.filter { it < recordTime }

  private fun timeToTravel(timeHeld: Long, distance: Long): Long = distance.floorDiv(timeHeld) + timeHeld
}
