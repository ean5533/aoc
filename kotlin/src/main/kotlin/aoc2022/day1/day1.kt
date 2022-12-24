package aoc2022.day1

import lib.loadResourceMatchingPackageName
import lib.split

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
private val calorieLists = input.lines().split("").map { it.map { it.toInt() } }
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
