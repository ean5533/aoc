package aoc2022.day4

import lib.containsAll
import lib.containsAny
import lib.loadResourceMatchingPackageName
import lib.pair

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
private val assignments = input.lines()
    .map {
        it.split(",").map {
            it.split("-").map { it.toInt() }.pair().let { it.first..it.second }
        }.pair()
    }

fun main() {
    part1()
    part2()
}

private fun part1() {
    val count = assignments.count { it.first.containsAll(it.second) || it.second.containsAll(it.first) }
    println("count1 = $count")
}

private fun part2() {
    val count = assignments.count { it.first.containsAny(it.second) }
    println("count2 = $count")
}
