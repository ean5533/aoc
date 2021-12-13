package day13

private val classLoader: ClassLoader = object {}.javaClass.classLoader
private val input = classLoader.getResource("text/day13")!!.readText()


fun main() {
    val (paper, foldInstructions) = parseInput()

    part1(paper, foldInstructions)
    part2(paper, foldInstructions)
}

fun parseInput(): Pair<Paper, List<FoldInstruction>> {
    val dots = input.lines()
        .takeWhile { it.isNotEmpty() }
        .map { it.split(",").map { it.toInt() } }
        .map { (x, y) -> Coordinate(x, y) }
        .toSet()
    val foldInstructions = input.lines()
        .dropWhile { it.isNotEmpty() }
        .drop(1)
        .map { it.removePrefix("fold along ").split("=") }
        .map { (direction, location) -> FoldInstruction(FoldDirection.from(direction), location.toInt()) }
    return Pair(Paper(dots), foldInstructions)
}

fun part1(paper: Paper, foldInstructions: List<FoldInstruction>) {
    val foldedPaper = foldPaper(paper, foldInstructions.take(1))
    println("Part 1: ${foldedPaper.dots.size} dots visible")
}

fun part2(paper: Paper, foldInstructions: List<FoldInstruction>) {
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

fun reflect(value: Int, over: Int): Int = over * 2 - value

data class Paper(val dots: Set<Coordinate>) {
    fun toStringGrid(): String {
        val height = dots.maxOf { it.y } + 1
        val width = dots.maxOf { it.x } + 1

        val board = Array(height) { Array(width) { " " } }
        dots.forEach { board[it.y][it.x] = "#" }
        return board.joinToString("\n") { it.joinToString("") }
    }
}

data class Coordinate(val x: Int, val y: Int)

data class FoldInstruction(val direction: FoldDirection, val location: Int)

enum class FoldDirection {
    UP, LEFT;

    companion object {
        fun from(direction: String): FoldDirection {
            return if (direction == "y") FoldDirection.UP else FoldDirection.LEFT
        }
    }
}