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

    private fun Pair<Int, Int>.moveToFollow(leader: Pair<Int, Int>): Pair<Int, Int> {
        if (Math.abs(leader.first - this.first) <= 1 && Math.abs(leader.second - this.second) <= 1) return this

        return this.copy(
            first = this.first + leader.first.compareTo(this.first),
            second = this.second + leader.second.compareTo(this.second),
        )
    }
}
