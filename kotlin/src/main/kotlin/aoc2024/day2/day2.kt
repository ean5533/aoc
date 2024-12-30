package aoc2024.day2

import lib.loadResourceMatchingPackageName

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
private val reports = input.lines().map { it.split(" ").map { it.toInt() } }


fun main() {
  part1()
  part2()
}

private fun part1() {
  val safe = reports.filter { it.isSafe() }
  println(safe.size)
}

private fun part2() {
  val (safe, maybeSafe) = reports.partition { it.isSafe() }
  val safeAfterRemoval = maybeSafe.filter { report ->
    // Try again by removing elements one at a time to see if any valid reports are generated
    (0..report.size-1).any { index -> report.toMutableList().also { it.removeAt(index) }.isSafe() }
  }

  println(safe.size + safeAfterRemoval.size)
}

private fun List<Int>.isSafe(): Boolean {
  val deltas = this.windowed(2).map { it[0] - it[1] }
  return deltas.all { it in 1..3 } || deltas.all { it in -3..-1 }
}
