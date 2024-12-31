package aoc2022.day14

import lib.*

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()

fun main() {
    println("part1: " + parseInput().let { Cave(it) }.also { it.fillWithSand() }.sandCount())
    println("part2: " + parseInput().let { Cave(it, useInfinifloor = true) }.also { it.fillWithSand() }.sandCount())
}

private fun parseInput(): Set<Line2D> = input.lines().flatMap {
    val points = it.split(" -> ").map { it.split(",").map { it.toInt() }.toPoint2D() }
    points.zipWithNext().map { (start, end) -> start..end }
}.toSet()

private class Cave(rockLines: Set<Line2D>, useInfinifloor: Boolean = false) {
    private val nonEmptySpaces = rockLines.flatMap { it.asSequence() }.associateWith { Cave.ROCK }.toMutableMap()
        .also { it.put(sandSource, Cave.SOURCE) }
    private val floor = nonEmptySpaces.keys.maxOf { it.y }
    private val infinifloor = if (useInfinifloor) floor + 2 else Int.MAX_VALUE

    fun sandCount() = nonEmptySpaces.values.count { it == Companion.SAND }

    fun fillWithSand() {
        while (nonEmptySpaces[sandSource] != SAND) {
            val sandPosition = tryAddSand() ?: break
            nonEmptySpaces.put(sandPosition, Companion.SAND)
        }
    }

    private fun tryAddSand(): Point2D? {
        val position = generateSequence(sandSource) { tryToFall(it) }.last()
        return if (isInAbyss(position)) null else position
    }

    private fun tryToFall(current: Point2D): Point2D? =
        listOf(
            current.copy(y = current.y + 1),
            current.copy(y = current.y + 1, x = current.x - 1),
            current.copy(y = current.y + 1, x = current.x + 1),
        ).firstOrNull { !nonEmptySpaces.containsKey(it) && it.y < infinifloor && !isInAbyss(current) }

    private fun isInAbyss(space: Point2D): Boolean = space.y >= floor + 3

    @Suppress("unused")
    private fun draw(grid: Map<Point2D, String>) {
        (grid.keys.minOf { it.y }..grid.keys.maxOf { it.y }).forEach { rowNum ->
            val row = (grid.keys.minOf { it.x }..grid.keys.maxOf { it.x }).map { colNum ->
                grid[Point2D(colNum, rowNum)] ?: EMPTY
            }
            println(row.joinToString(""))
        }
    }

    companion object {
        const val EMPTY = " "
        const val SOURCE = "+"
        const val SAND = "O"
        const val ROCK = "#"

        private val sandSource = Point2D(500, 0)
    }
}
