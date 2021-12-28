package day4

import lib.loadResourceAsString
import java.lang.IllegalArgumentException

private val input = loadResourceAsString("text/day4")

fun main() {
    val (numbers, boards) = parseInput()

    part1(numbers, boards)
    part2(numbers, boards)
}

private fun parseInput(): Pair<List<Int>, List<Board>> {
    val lines = input.lines()
    val numbers = lines[0].split(",").map(String::toInt)

    // Take six lines at a time (1 blank line, 5 board lines)
    val boards = lines.drop(1).windowed(6, 6)
        .map { sixLines -> sixLines.drop(1).map { it.split(" ").filterNot { it.isEmpty() }.map { Cell(it.toInt()) } } }
        .map(::Board)

    return Pair(numbers, boards)
}

private fun part1(numbers: List<Int>, boards: List<Board>) {
    // Draw numbers until some board is solved
    val numberThatSolved = numbers.first { number ->
        boards.forEach { it.mark(number) }
        boards.any { it.isSolved() }
    }

    // Hypothetically multiple boards may be solved simultaneously
    val solvedBoardScores = boards.filter { it.isSolved() }.map { it.getScore(numberThatSolved) }

    println("Solved board scores $solvedBoardScores")
}

private fun part2(numbers: List<Int>, boards: List<Board>) {
    // First, draw numbers until there are one or fewer unsolved boards left
    val calledNumbers = numbers.takeWhile { number ->
        boards.filter { !it.isSolved() }.forEach { it.mark(number) }
        boards.filter { !it.isSolved() }.size > 1
    }

    // Hypothetically the last 2+ boards could be solved simultaneously
    val lastBoard = boards.singleOrNull { !it.isSolved() }
        ?: throw IllegalArgumentException("Impossible to let the giant squid win")

    // Continue drawing numbers until last board is solved
    val numberThatSolved = numbers.minus(calledNumbers).first { number ->
        lastBoard.mark(number)
        lastBoard.isSolved()
    }

    println("Final board score ${lastBoard.getScore(numberThatSolved)}")

}

private data class Board(val numbers: List<List<Cell>>) {
    private var isSolved = false

    fun mark(number: Int): Board {
        val maybeCoordinates = find(number)?.also { (row, col) -> numbers[row][col].mark() }
        if (wasSolvedBy(maybeCoordinates)) isSolved = true
        return this
    }

    fun isSolved() = isSolved

    private fun wasSolvedBy(maybeCoordinates: Pair<Int, Int>?): Boolean {
        if (maybeCoordinates == null) return false

        val (row, col) = maybeCoordinates

        val rowComplete = numbers[row].all { it.isMarked() }
        val colComplete = numbers.map { it[col] }.all { it.isMarked() }

        return rowComplete || colComplete
    }

    fun getScore(multiplier: Int): Int {
        val unmarkedNumbers = numbers.sumOf { it.filter { !it.isMarked() }.sumOf { it.number } }
        return unmarkedNumbers * multiplier
    }

    private fun find(number: Int): Pair<Int, Int>? {
        return numbers.mapIndexed { rowIndex, row ->
            val colIndex = row.indexOfFirst { it.number == number }
            if (colIndex > -1) {
                Pair(rowIndex, colIndex)
            } else {
                null
            }
        }.filterNotNull().firstOrNull()
    }
}

private data class Cell(val number: Int) {
    private var isMarked: Boolean = false

    fun mark() {
        isMarked = true
    }

    fun isMarked() = isMarked
}
