package aoc2022.day9

import lib.loadResourceAsString

private val input = loadResourceAsString("text/aoc2022/day9").trim()
private val moves = input.lines().map { it.split(" ").let { Move(it[0], it[1].toInt()) } }

fun main() {
    println(Board(2).also { it.execute(moves) }.tailVisitedCount)
    println(Board(10).also { it.execute(moves) }.tailVisitedCount)
}

private class Board(knotCount: Int) {
    private val positions: MutableList<Pair<Int, Int>> = (0 until knotCount).map { 0 to 0 }.toMutableList()
    private val tailVisitedPositions: MutableSet<Pair<Int, Int>> = mutableSetOf(0 to 0)

    val tailVisitedCount get() = tailVisitedPositions.size

    fun execute(moves: List<Move>) = moves.forEach { execute(it) }

    fun execute(move: Move) {
        (0 until move.distance).forEach() {
            positions[0] = move.applyTo(positions[0])
            (1 until positions.size).forEach { positions[it] = positions[it].moveToFollow(positions[it - 1]) }
            tailVisitedPositions.add(positions.last())
        }
    }

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

private data class Move(val direction: String, val distance: Int) {
    fun applyTo(pair: Pair<Int, Int>): Pair<Int, Int> = when (direction) {
        "R" -> pair.copy(first = pair.first + 1)
        "L" -> pair.copy(first = pair.first - 1)
        "U" -> pair.copy(second = pair.second + 1)
        "D" -> pair.copy(second = pair.second - 1)
        else -> throw IllegalStateException()
    }
}
