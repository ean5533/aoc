package aoc2022.day12

import lib.Point2D
import lib.aStarSearch
import lib.loadResourceMatchingPackageName
import lib.printTimeTaken

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim().lines().map { it.toList() }
private val startPosition = findPositionOf('S')
private val endPosition = findPositionOf('E')
private val grid = parseGrid()

fun main() {
    printTimeTaken {
        println("part1: " + aStarSearch(startPosition, { getNextPositions(it, false) }, { it == endPosition })!!.cost)
    }
    printTimeTaken {
        println("part2: " + aStarSearch(endPosition, { getNextPositions(it, true) }, { height(it) == 'a'.code })!!.cost)
    }
}

private fun getNextPositions(position: Point2D, reversed: Boolean): List<Pair<Point2D, Int>> =
    listOfNotNull(
        if (position.x > 0) position.copy(x = position.x - 1) else null,
        if (position.x < grid.size - 1) position.copy(x = position.x + 1) else null,
        if (position.y > 0) position.copy(y = position.y - 1) else null,
        if (position.y < grid[0].size - 1) position.copy(y = position.y + 1) else null,
    )
        .filter { if (reversed) height(position) - 1 <= height(it) else height(position) + 1 >= height(it) }
        .map { it to 1 }

private fun height(position: Point2D): Int = grid[position.x][position.y]

private fun findPositionOf(c: Char): Point2D = input.mapIndexedNotNull { rowIndex, row ->
    row.mapIndexedNotNull { colIndex, value -> if (value == c) Point2D(rowIndex, colIndex) else null }.singleOrNull()
}.single()

private fun parseGrid(): List<List<Int>> = input.map {
    it.map {
        when (it) {
            in ('a'..'z') -> it.code
            'S' -> 'a'.code
            'E' -> 'z'.code
            else -> throw IllegalStateException()
        }
    }
}
