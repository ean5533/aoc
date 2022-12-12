package aoc2022.day12

import lib.aStarSearch
import lib.loadResourceAsString
import lib.printTimeTaken

private val input = loadResourceAsString("text/aoc2022/day12").trim().lines().map { it.toList() }
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

private fun getNextPositions(position: Pair<Int, Int>, reversed: Boolean): List<Pair<Pair<Int, Int>, Int>> =
    listOfNotNull(
        if (position.first > 0) position.copy(first = position.first - 1) else null,
        if (position.first < grid.size - 1) position.copy(first = position.first + 1) else null,
        if (position.second > 0) position.copy(second = position.second - 1) else null,
        if (position.second < grid[0].size - 1) position.copy(second = position.second + 1) else null,
    )
        .filter { if (reversed) height(position) - 1 <= height(it) else height(position) + 1 >= height(it) }
        .map { it to 1 }

private fun height(position: Pair<Int, Int>): Int = grid[position.first][position.second]

private fun findPositionOf(c: Char) = input.mapIndexedNotNull { rowIndex, row ->
    row.mapIndexedNotNull { colIndex, value -> if (value == c) rowIndex to colIndex else null }.singleOrNull()
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
