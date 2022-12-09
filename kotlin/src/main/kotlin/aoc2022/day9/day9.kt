package aoc2022.day9

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
