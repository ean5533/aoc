package aoc2023.day9

import lib.loadResourceMatchingPackageName

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
private val histories = input.lines().map { History(it.split(" ").map { it.toInt() }) }

fun main() {
  println(histories.sumOf { it.next() })
  println(histories.sumOf { it.previous() })
}

private data class History(val values: List<Int>) {
  fun next(): Int = values.last() + (derivative()?.next() ?: 0)
  fun previous(): Int = values.first() - (derivative()?.previous() ?: 0)

  private fun derivative(): History? =
    if (values.toSet() == setOf(0)) null
    else History(values.zipWithNext { a, b -> b - a })
}
