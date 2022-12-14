package aoc2022.day14

import lib.*

private val input = loadResourceAsString("text/aoc2022/day14").trim()

fun main() {
    println("part1: " + parseInput().let { Cave(it) }.also { it.fillWithSand() }.sandCount())
    println("part2: " + parseInput().let { Cave(it, useInfinifloor = true) }.also { it.fillWithSand() }.sandCount())
}

private fun parseInput(): Set<Pair<Int, Int>> = input.lines().flatMap {
    val coordinates = it.split(" -> ").map { it.split(",").map { it.toInt() }.pair() }
    coordinates.zipWithNext().flatMap { (start, end) ->
        (start.first smartRange end.first).cartesianProduct(start.second smartRange end.second)
    }
}.toSet()

class Cave(rocks: Set<Pair<Int, Int>>, useInfinifloor: Boolean = false) {
    private val nonEmptySpaces =
        rocks.associateWith { Cave.ROCK }.toMutableMap().also { it.put(sandSource, Cave.SOURCE) }
    private val floor = nonEmptySpaces.keys.maxOf { it.second }
    private val infinifloor = if (useInfinifloor) floor + 2 else Int.MAX_VALUE

    fun sandCount() = nonEmptySpaces.values.count { it == Companion.SAND }

    fun fillWithSand() {
        while (nonEmptySpaces[sandSource] != SAND) {
            val sandPosition = tryAddSand() ?: break
            nonEmptySpaces.put(sandPosition, Companion.SAND)
        }
    }

    private fun tryAddSand(): Pair<Int, Int>? {
        val position = generateSequence(sandSource) { tryToFall(it) }.last()
        return if (isInAbyss(position)) null else position
    }

    private fun tryToFall(current: Pair<Int, Int>): Pair<Int, Int>? =
        listOf(
            current.copy(second = current.second + 1),
            current.copy(second = current.second + 1, first = current.first - 1),
            current.copy(second = current.second + 1, first = current.first + 1),
        ).firstOrNull { !nonEmptySpaces.containsKey(it) && it.second < infinifloor && !isInAbyss(current) }

    private fun isInAbyss(space: Pair<Int, Int>): Boolean = space.second >= floor + 3

    @Suppress("unused")
    private fun draw(grid: Map<Pair<Int, Int>, String>) {
        (grid.keys.minOf { it.second }..grid.keys.maxOf { it.second }).forEach { rowNum ->
            val row = (grid.keys.minOf { it.first }..grid.keys.maxOf { it.first }).map { colNum ->
                grid[colNum to rowNum] ?: EMPTY
            }
            println(row.joinToString(""))
        }
    }

    companion object {
        const val EMPTY = " "
        const val SOURCE = "+"
        const val SAND = "O"
        const val ROCK = "#"

        private val sandSource = 500 to 0
    }
}
