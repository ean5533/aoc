package aoc2022.day3

import lib.intersectAll
import lib.loadResourceMatchingPackageName
import lib.pair

private val input = loadResourceMatchingPackageName(object {}.javaClass, "text/").trim()
private val scores = (('a'..'z') + ('A'..'Z')).mapIndexed { index, char -> char to (index + 1) }.toMap()
private val sacks = input.lines().map { it.windowed(it.length / 2, it.length / 2).pair() }

fun main() {
    part1()
    part2()
}

private fun part1() {
    val scores = sacks
        .map { it.first.toList().intersect(it.second.toList()).single() }
        .map { scores[it]!! }

    println("sum1 = ${scores.sum()}")
}

private fun part2() {
    val scores = sacks.windowed(3, 3)
        .map { it.map { it.first + it.second }.map { it.toSet() }.intersectAll().single() }
        .map { scores[it]!! }

    println("sum2 = ${scores.sum()}")
}
