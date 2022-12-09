package aoc2022.day9

import lib.loadResourceAsString
import lib.replaceFirst

private val input = loadResourceAsString("text/aoc2022/day9").trim()
private val moves = input.lines().map { it.split(" ").let { Move(it[0], it[1].toInt()) } }

/**
 * A solution using immutable data structures. Performs way worse because of all the extra allocations.
 */
fun main() {
    println(Board(2).execute(moves).tailVisitedPositions.size)
    println(Board(10).execute(moves).tailVisitedPositions.size)
}

private data class Board(
    val positions: List<Pair<Int, Int>>,
    val tailVisitedPositions: Set<Pair<Int, Int>>
) {
    constructor(knotCount: Int) : this(
        positions = (0 until knotCount).map { 0 to 0 }.toMutableList(),
        tailVisitedPositions = mutableSetOf(0 to 0)
    )

    fun execute(moves: List<Move>): Board = moves.fold(this) { board, move -> board.execute(move) }

    fun execute(move: Move): Board = (0 until move.distance).fold(this) { board, _ ->
        val newPositions =  board.positions.replaceFirst { move.applyTo(it) }
            .runningReduce { leader, follower -> follower.moveToFollow(leader) }
        Board(newPositions, board.tailVisitedPositions + newPositions.last())
    }//.also { println("After ${move.direction} ${move.distance}:  ${it.positions}, ${it.tailVisitedPositions}") }

    private fun Pair<Int, Int>.moveToFollow(lead: Pair<Int, Int>): Pair<Int, Int> {
        if (Math.abs(lead.first - this.first) <= 1 && Math.abs(lead.second - this.second) <= 1) return this

        var new = this
        if (lead.first - this.first > 0) new = new.copy(first = this.first + 1)
        if (lead.first - this.first < 0) new = new.copy(first = this.first - 1)
        if (lead.second - this.second > 0) new = new.copy(second = this.second + 1)
        if (lead.second - this.second < 0) new = new.copy(second = this.second - 1)

        return new
    }
}

internal data class Move(val direction: String, val distance: Int) {
    fun applyTo(pair: Pair<Int, Int>): Pair<Int, Int> = when (direction) {
        "R" -> pair.copy(first = pair.first + 1)
        "L" -> pair.copy(first = pair.first - 1)
        "U" -> pair.copy(second = pair.second + 1)
        "D" -> pair.copy(second = pair.second - 1)
        else -> throw IllegalStateException()
    }
}
