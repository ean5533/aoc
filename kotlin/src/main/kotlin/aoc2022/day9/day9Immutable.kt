package aoc2022.day9

import lib.Point2D
import lib.loadResourceMatchingPackageName
import lib.replaceFirst

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
private val moves = input.lines().map { it.split(" ").let { Move(it[0], it[1].toInt()) } }

/**
 * A solution using immutable data structures. Performs way worse because of all the extra allocations.
 */
fun main() {
    println(Board(2).execute(moves).tailVisitedPositions.size)
    println(Board(10).execute(moves).tailVisitedPositions.size)
}

private data class Board(
    val positions: List<Point2D>,
    val tailVisitedPositions: Set<Point2D>
) {
    constructor(knotCount: Int) : this(
        positions = (0 until knotCount).map { Point2D(0, 0) }.toMutableList(),
        tailVisitedPositions = mutableSetOf(Point2D(0, 0))
    )

    fun execute(moves: List<Move>): Board = moves.fold(this) { board, move -> board.execute(move) }

    fun execute(move: Move): Board = (0 until move.distance).fold(this) { board, _ ->
        val newPositions =  board.positions.replaceFirst { move.applyTo(it) }
            .runningReduce { leader, follower -> follower.moveToFollow(leader) }
        Board(newPositions, board.tailVisitedPositions + newPositions.last())
    }//.also { println("After ${move.direction} ${move.distance}:  ${it.positions}, ${it.tailVisitedPositions}") }

    private fun Point2D.moveToFollow(leader: Point2D): Point2D {
        if (Math.abs(leader.x - this.x) <= 1 && Math.abs(leader.y - this.y) <= 1) return this

        return this.copy(
            x = this.x + leader.x.compareTo(this.x),
            y = this.y + leader.y.compareTo(this.y),
        )
    }
}

internal data class Move(val direction: String, val distance: Int) {
    fun applyTo(pair: Point2D): Point2D = when (direction) {
        "R" -> pair.copy(x = pair.x + 1)
        "L" -> pair.copy(x = pair.x - 1)
        "U" -> pair.copy(y = pair.y + 1)
        "D" -> pair.copy(y = pair.y - 1)
        else -> throw IllegalStateException()
    }
}
