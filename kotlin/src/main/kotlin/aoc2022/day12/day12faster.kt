package aoc2022.day12

import lib.SearchState
import lib.aStarSearch
import lib.loadResourceAsString
import lib.printTimeTaken

private val input = loadResourceAsString("text/aoc2022/day12").trim().lines().map { it.toList() }
private val startPosition = findPositionOf('S')
private val endPosition = findPositionOf('E')
private val grid = parseGrid()

/**
 * A slightly faster solution due to fewer allocations during the search.
 */
fun main() {
    printTimeTaken {
        println("part1: " + aStarSearch(ForwardSearchState(startPosition))!!.cost)
    }
    printTimeTaken {
        println("part2: " + aStarSearch(ReverseSearchState(endPosition))!!.cost)
    }
}

private fun getNextPositions(position: Pair<Int, Int>, canMoveTo: (Pair<Int, Int>) -> Boolean): List<Pair<Int, Int>> =
    listOfNotNull(
        if (position.first > 0) position.copy(first = position.first - 1) else null,
        if (position.first < grid.size - 1) position.copy(first = position.first + 1) else null,
        if (position.second > 0) position.copy(second = position.second - 1) else null,
        if (position.second < grid[0].size - 1) position.copy(second = position.second + 1) else null,
    ).filter { canMoveTo(it) }

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

private class ForwardSearchState(current: Pair<Int, Int>, override val cost: Int = 0) :
    SearchState<Pair<Int, Int>>(current) {

    override fun getNextStates(): List<SearchState<Pair<Int, Int>>> =
        getNextPositions(current) { height(current) + 1 >= height(it) }
            .map { ForwardSearchState(it, cost + 1) }

    override fun isSolution(): Boolean = current == endPosition
}

private class ReverseSearchState(current: Pair<Int, Int>, override val cost: Int = 0) :
    SearchState<Pair<Int, Int>>(current) {

    override fun getNextStates(): List<SearchState<Pair<Int, Int>>> =
        getNextPositions(current) { height(current) - 1 <= height(it) }
            .map { ReverseSearchState(it, cost + 1) }

    override fun isSolution(): Boolean = height(current) == 'a'.code
}
