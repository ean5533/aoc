package aoc2024.day11

import lib.*

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
private val initial = input.split(" ").map { it.toLong() }

// <<stoneValue, blinkCount>, stoneCount>
private val memo = mutableMapOf<Pair<Long, Int>, Long>()

fun main() {
  println(initial.sumOf { it.countAfterBlinks(25) })
  println(initial.sumOf { it.countAfterBlinks(75) })
}

fun Long.countAfterBlinks(blinkCount: Int): Long {
  return memo.getOrPut(this to blinkCount) {
    if (blinkCount == 1) this.blink().count().toLong()
    else this.blink().sumOf { it.countAfterBlinks(blinkCount - 1) }
  }
}

private fun Long.blink(): List<Long> {
  if (this == 0L) {
    return listOf(1L)
  }

  val toString = this.toString()
  if (toString.length % 2 == 0) {
    return listOf(
      toString.take(toString.length / 2).toLong(),
      toString.drop(toString.length / 2).toLong()
    )
  }

  return listOf(this * 2024)
}
