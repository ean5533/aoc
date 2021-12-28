import lib.Point2D
import java.io.File
import java.lang.Math.abs

// https://codingmystery.com/

fun main() {
    part1()
    part2()
    part3()
}

private fun part1() {
    val inputKey = File("/home/ean5533/Downloads/Blank Sheet Of Paper.txt").readLines()
    val inputPuzzle = File("/home/ean5533/Downloads/Shredded Sheet Of Paper.txt").readLines()

    val key = inputKey.mapIndexed { i, line -> line.take(25) to i }.toMap()

    val solved = inputPuzzle
        .map { line -> key[line.take(25)]!! to line }
        .sortedBy { it.first }
        .joinToString("\n") { it.second }

    println(solved)
}

private fun part2() {
    val maze = File("/home/ean5533/Downloads/Map Of The Tunnels.txt").readLines()
    val instructions = File("/home/ean5533/Downloads/List Of Instructions.txt").readLines()

    val walls = maze.asSequence().drop(5).flatMapIndexed { row, line ->
        line.asSequence()
            .mapIndexedNotNull { col, char ->
                when (char) {
                    '█' -> Point2D(col + 1, row + 1)
                    else -> null
                }
            }
    }.toSet()

    val end = instructions.asSequence().flatMap { it.split(", ") }.fold(Point2D(4, 22)) { current, step ->
        val next = when (step) {
            "E" -> current.copy(x = current.x + 1)
            "W" -> current.copy(x = current.x - 1)
            "N" -> current.copy(y = current.y - 1)
            "S" -> current.copy(y = current.y + 1)
            else -> throw RuntimeException("Unexpected step '$step'")
        }
        if (walls.contains(next)) current else next
    }

    println(end)
}

private fun part3() {
    val map = File("/home/ean5533/Downloads/2d Grid Of Particles.txt").readLines()

    val particles = map.drop(4).flatMapIndexed { row, line ->
        line.mapIndexedNotNull { col, char ->
            when (char) {
                '•' -> Point2D(col - 1, row - 1)
                else -> null
            }
        }
    }

    println(particles.sumOf { abs(it.x - 50) + abs(it.y - 25) })
}