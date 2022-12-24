package aoc2022.day7

import lib.loadResourceMatchingPackageName

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
private val allDirs = parseInput()

/**
 * A simple, hacky solution that makes a lot of assumptions but sufficiently solves both parts.
 * 
 * See day7tree.kt for a more flexible, exensible solution that builds a tree structure.
 */
fun main() {
    part1()
    part2()
}

private fun part1() {
    val smallDirs = allDirs.filter { it.size <= 100000 }
    println("part1 = ${smallDirs.sumOf { it.size }}")
}

private fun part2() {
    val spaceNeeded = 30000000 - (70000000 - allDirs.first().size)
    val smallestDeletionToSatify = allDirs
        .map { it.size }
        .filter { it > spaceNeeded }
        .minByOrNull { it }!!
    println("part2 = $smallestDeletionToSatify")
}

private fun parseInput(): List<Dir> {
    val allDirs = mutableListOf<Dir>()
    val stack = mutableListOf<Dir>()
    input.lines().forEach {
        when {
            it == "$ cd .." ->
                stack.removeLast()

            it.startsWith("$ cd") -> {
                val dir = Dir()
                stack.add(dir)
                allDirs.add(dir)
            }

            it[0].digitToIntOrNull() != null -> {
                val size = it.split(" ")[0].toLong()
                stack.forEach { it.size += size }
            }
        }
    }
    
    return allDirs
}

class Dir(var size: Long = 0)
