package aoc2024.day1

import lib.loadResourceMatchingPackageName
import kotlin.math.abs

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
private val pairs = input.lines().map { it.split(" ").let { it.first().toInt() to it.last().toInt() } }
private val firstList = pairs.map { it.first }
private val secondList = pairs.map { it.second }

fun main() {
  part1()
  part2()
}

private fun part1() {
  val deltas = firstList.sorted()
    .zip(secondList.sorted())
    .map { abs(it.first - it.second) }
  println(deltas.sum())
}

private fun part2() {
  val scores = firstList.map { first -> first * secondList.count { it == first} }
  println(scores.sum())
}
