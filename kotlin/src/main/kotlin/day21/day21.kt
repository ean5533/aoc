package day21

import lib.*
import java.lang.Long.max

//val input = """
//    Player 1 starting position: 4
//    Player 2 starting position: 8
//""".trimIndent()
val input = """
    Player 1 starting position: 9
    Player 2 starting position: 6
""".trimIndent()

fun main() {
    val (player1Start, player2Start) = input.lines().filter { it.isNotEmpty() }.map { it.split(" ").last().toInt() }
    val initialPlayerStates = mapOf(1 to PlayerState(player1Start), 2 to PlayerState(player2Start))

    printTimeTaken { part1(initialPlayerStates) }
    printTimeTaken { part2(initialPlayerStates) }
}

private fun part1(initialPlayerStates: Map<Int, PlayerState>) {
    val die = StatefulDeterministicDie(1, 100)
    val initialBoard = BoardState(10, 1000, 1, initialPlayerStates)

    val finalBoardCounts = playBoardInAllUniverses(initialBoard, die)

    val finalBoard = finalBoardCounts.entries.single().key
    val minScore = finalBoard.playerStates.values.minOf { it.score }
    println("Part 1: ${minScore * die.rollCount}")
}

private fun part2(initialPlayerStates: Map<Int, PlayerState>) {
    val die = QuantumDie(1, 3)
    val initialBoard = BoardState(10, 21, 1, initialPlayerStates)

    val finalBoardCounts = playBoardInAllUniverses(initialBoard, die)

    val player1Wins =
        finalBoardCounts.entries.filter { it.key.playerStates[1]!!.score > it.key.playerStates[2]!!.score }
            .sumOf { it.value }
    val player2Wins = finalBoardCounts.entries.sumOf { it.value } - player1Wins

    println("Part 2: ${max(player1Wins, player2Wins)}")
}

private fun playBoardInAllUniverses(
    initialBoard: BoardState,
    die: Die
): Map<BoardState, Long> {
    return generateSequence(mapOf(initialBoard to 1L)) { boardCounts ->
        val (won, unwon) = boardCounts.entries.partition { it.key.isWon() }
        val newBoardCounts = unwon
            .flatMap { (board, count) -> die.getPossibleValues().map { board.moveNextPlayer(it) to count } }
            .plus(won.map { it.toPair() })
            .groupingBy { it.first }
            .sumCounts()
        newBoardCounts
    }
        .takeWhileInclusive { boardCounts -> boardCounts.any { !it.key.isWon() } }
        .last()
}

private data class BoardState(
    val totalSpaces: Int,
    val goalScore: Int,
    val whoseTurn: Int,
    val playerStates: Map<Int, PlayerState>
) {
    fun isWon(): Boolean = playerStates.any { it.value.score >= goalScore }

    fun moveNextPlayer(spaces: Int): BoardState {
        val state = playerStates[whoseTurn]!!
        val newSpace = (state.currentSpace + spaces).modToRange(1, totalSpaces)
        val newScore = state.score + newSpace
        val newPlayerStates = playerStates.toMutableMap().also { it[whoseTurn] = PlayerState(newSpace, newScore) }
        return this.copy(playerStates = newPlayerStates, whoseTurn = whoseTurn.incrementInsideRange(1, 2))
    }
}

private data class PlayerState(val currentSpace: Int, val score: Long = 0)

private interface Die {
    /**
     * Returns all possible values for the sum of the next 3 rolls. Values will be repeated if there are multiple
     * distinct rolls that produce that result (e.g. roll a 1 and then a 2, or roll a 2 and then a 1)
     */
    fun getPossibleValues(): List<Int>
}

private class StatefulDeterministicDie(private val minValue: Int, private val maxValue: Int) : Die {
    // Note: we can cheat and get away with making this die have mutable state *only* because it produces a single
    // value. If it produced multiple values then we'd need to make all die immutable, attaching them to the board
    // state, for subsequent rolls across multiple boards.
    private var value = maxValue
    var rollCount = 0
        private set

    private fun roll(): Int {
        rollCount += 1
        value = value.incrementInsideRange(minValue, maxValue)
        return value
    }

    override fun getPossibleValues(): List<Int> = listOf((0 until 3).sumOf { roll() })
}

private class QuantumDie(minValue: Int, maxValue: Int) : Die {
    private val possibleValues = minValue..maxValue
    private val possibleSums = possibleValues
        .cartesianProduct(possibleValues).map { (a, b) -> a + b }
        .cartesianProduct(possibleValues).map { (a, b) -> a + b }

    override fun getPossibleValues(): List<Int> = possibleSums
}
