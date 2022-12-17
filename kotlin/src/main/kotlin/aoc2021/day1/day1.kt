package aoc2021.day1

import lib.loadResourceMatchingPackageName

private val input = loadResourceMatchingPackageName(object {}.javaClass, "text/")
private val numbers: List<Int> = input.lines().map { it.trim().toInt() }

fun main() {
    part1()
    part2()
}

private fun part1() {
    val increases = numbers
        .zipWithNext { a, b -> if (b > a) 1 else 0 }
        .sum()
    println(increases)
}

private fun part2() {
    val increases = numbers
        .windowed(3, 1)
        .map { it.sum() }
        .zipWithNext { a, b -> if (b > a) 1 else 0 }
        .sum()
    println(increases)
}
