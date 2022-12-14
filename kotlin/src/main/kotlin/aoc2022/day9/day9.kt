package aoc2022.day9

import lib.Point2D
import lib.loadResourceAsString

private val input = loadResourceAsString("text/aoc2022/day9").trim()
private val moves = input.lines().map { it.split(" ").let { Move(it[0], it[1].toInt()) } }

/**
 * A solution using mutable data structures
 */
fun main() {
    println(MutableBoard(2).also { it.execute(moves) }.tailVisitedCount)
    println(MutableBoard(10).also { it.execute(moves) }.tailVisitedCount)
}

private class MutableBoard(knotCount: Int) {
    private val positions: MutableList<Point2D> = (0 until knotCount).map { Point2D(0, 0) }.toMutableList()
    private val tailVisitedPositions: MutableSet<Point2D> = mutableSetOf(Point2D(0, 0))

    val tailVisitedCount get() = tailVisitedPositions.size

    fun execute(moves: List<Move>) = moves.forEach { execute(it) }

    fun execute(move: Move) {
        (0 until move.distance).forEach() {
            positions[0] = move.applyTo(positions[0])
            (1 until positions.size).forEach { positions[it] = positions[it].moveToFollow(positions[it - 1]) }
            tailVisitedPositions.add(positions.last())
        }
    }

    private fun Point2D.moveToFollow(leader: Point2D): Point2D {
        if (Math.abs(leader.x - this.x) <= 1 && Math.abs(leader.y - this.y) <= 1) return this

        return this.copy(
            x = this.x + leader.x.compareTo(this.x),
            y = this.y + leader.y.compareTo(this.y),
        )
    }
}
