package aoc2022.day1

import lib.loadResourceAsString

private val input = loadResourceAsString("text/aoc2022/day1")
private val calorieLists = input.split("\n\n").map { it.split("\n").mapNotNull { it.toIntOrNull() } }
private val sortedSums = calorieLists.map { it.sum() }.toList().sortedDescending()

fun main() {
    part1()
    part2()
}

private fun part1() {
    println("max = ${sortedSums.first()}")
}

private fun part2() {
    println("max3 = ${sortedSums.take(3).sum()}")
}
