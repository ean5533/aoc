package aoc2022.day12

import lib.*

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

private fun getNextPositions(position: Point2D, canMoveTo: (Point2D) -> Boolean): List<Point2D> =
    listOfNotNull(
        if (position.x > 0) position.copy(x = position.x - 1) else null,
        if (position.x < grid.size - 1) position.copy(x = position.x + 1) else null,
        if (position.y > 0) position.copy(y = position.y - 1) else null,
        if (position.y < grid[0].size - 1) position.copy(y = position.y + 1) else null,
    ).filter { canMoveTo(it) }

private fun height(position: Point2D): Int = grid[position.x][position.y]

private fun findPositionOf(c: Char) = input.mapIndexedNotNull { rowIndex, row ->
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

private class ForwardSearchState(current: Point2D, override val cost: Int = 0) :
    SearchState<Point2D>(current) {

    override fun getNextStates(): List<SearchState<Point2D>> =
        getNextPositions(current) { height(current) + 1 >= height(it) }
            .map { ForwardSearchState(it, cost + 1) }

    override fun isSolution(): Boolean = current == endPosition
}

private class ReverseSearchState(current: Point2D, override val cost: Int = 0) :
    SearchState<Point2D>(current) {

    override fun getNextStates(): List<SearchState<Point2D>> =
        getNextPositions(current) { height(current) - 1 <= height(it) }
            .map { ReverseSearchState(it, cost + 1) }

    override fun isSolution(): Boolean = height(current) == 'a'.code
}
