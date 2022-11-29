package aoc2021.day13

import lib.Point2D
import lib.loadResourceAsString

private val input = loadResourceAsString("text/aoc2021/day13")


fun main() {
    val (paper, foldInstructions) = parseInput()

    part1(paper, foldInstructions)
    part2(paper, foldInstructions)
}

private fun parseInput(): Pair<Paper, List<FoldInstruction>> {
    val dots = input.lines()
        .takeWhile { it.isNotEmpty() }
        .map { it.split(",").map { it.toInt() } }
        .map { (x, y) -> Point2D(x, y) }
        .toSet()
    val foldInstructions = input.lines()
        .dropWhile { it.isNotEmpty() }
        .drop(1)
        .map { it.removePrefix("fold along ").split("=") }
        .map { (direction, location) -> FoldInstruction(FoldDirection.from(direction), location.toInt()) }
    return Pair(Paper(dots), foldInstructions)
}

private fun part1(paper: Paper, foldInstructions: List<FoldInstruction>) {
    val foldedPaper = foldPaper(paper, foldInstructions.take(1))
    println("Part 1: ${foldedPaper.dots.size} dots visible")
}

private fun part2(paper: Paper, foldInstructions: List<FoldInstruction>) {
    val foldedPaper = foldPaper(paper, foldInstructions)
    println("Part 2: Final folded paper looks like:\n${foldedPaper.toStringGrid()}")
}

private fun foldPaper(originalPaper: Paper, foldInstructions: List<FoldInstruction>): Paper {
    val foldedPaper = foldInstructions.fold(originalPaper) { (dots), (foldDirection, foldLocation) ->
        // Surely there is a way to deduplicate this logic, but I can't find a way that looks clean
        val newDots = if (foldDirection == FoldDirection.UP) {
            val (topPoints, bottomPoints) = dots.partition { it.y < foldLocation }
            topPoints + bottomPoints.map { it.copy(y = reflect(it.y, foldLocation)) }
        } else {
            val (leftPoints, rightPoints) = dots.partition { it.x < foldLocation }
            leftPoints + rightPoints.map { it.copy(x = reflect(it.x, foldLocation)) }
        }
        Paper(newDots.toSet())
    }
    return foldedPaper
}

private fun reflect(value: Int, over: Int): Int = over * 2 - value

private data class Paper(val dots: Set<Point2D>) {
    fun toStringGrid(): String {
        val height = dots.maxOf { it.y } + 1
        val width = dots.maxOf { it.x } + 1

        val board = Array(height) { Array(width) { " " } }
        dots.forEach { board[it.y][it.x] = "#" }
        return board.joinToString("\n") { it.joinToString("") }
    }
}

private data class FoldInstruction(val direction: FoldDirection, val location: Int)

private enum class FoldDirection {
    UP, LEFT;

    companion object {
        fun from(direction: String): FoldDirection {
            return if (direction == "y") FoldDirection.UP else FoldDirection.LEFT
        }
    }
}
