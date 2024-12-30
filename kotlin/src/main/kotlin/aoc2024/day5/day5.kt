package aoc2024.day5

import lib.loadResourceMatchingPackageName
import lib.pair

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
private val rules = input.lines().takeWhile { it.isNotBlank() }.map { it.split("|").map { it.toInt() }.pair() }.toList()
private val updates =
  input.lines().dropWhile { it.isNotBlank() }.drop(1).map { it.split(",").map { it.toInt() } }.toList()

fun main() {
  val (correct, incorrect) = updates.partition { update ->
    rules.all { update.indexOf(it.first) < update.indexOf(it.second, update.size) }
  }
  
  part1(correct)
  part2(incorrect)
}

private fun part1(correctUpdates: List<List<Int>>) {
  println(correctUpdates.sumOf { it.middle() })
}

private fun part2(incorrectUpdates: List<List<Int>>) {
  val fixedUpdates = incorrectUpdates.map { update ->
    val relevantRules = rules.filter { update.contains(it.first) && update.contains(it.second) }
    val weightedRules = relevantRules
      .groupingBy { it.first }
      .eachCount().map { it.toPair() }
      .sortedByDescending { it.second }
      .map { it.first }
    weightedRules + (update - weightedRules).single()
  }

  println(fixedUpdates.sumOf { it.middle() })
}

/** Returns the index of [element] if it exists, else returns [default] */
private fun <T> List<T>.indexOf(element: T, default: Int) = indexOf(element).let { if (it > -1) it else default }

private fun <T> List<T>.middle(): T {
  require(this.size % 2 == 1)
  return this[size / 2]
}
