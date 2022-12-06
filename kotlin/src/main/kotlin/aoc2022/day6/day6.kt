package aoc2022.day6

import lib.loadResourceAsString

private val input = loadResourceAsString("text/aoc2022/day6").trim().lines().first()

fun main() {
    part1()
    part2()
}

private fun part1() {
    println(findUniqueSequence(4))
}

private fun part2() {
    println(findUniqueSequence(14))
}

private fun findUniqueSequence(length: Int) =
    input.windowed(length).indexOfFirst { it.toSet().size == length } + length
