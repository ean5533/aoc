package aoc2021.day5

import lib.Line2D
import lib.Point2D
import lib.loadResourceMatchingPackageName

private val input = loadResourceMatchingPackageName(object {}.javaClass)

fun main() {
    val ventLines = parseInput()
    val topology = Topology(ventLines)

    part1(topology)
    part2(topology)
}

private fun parseInput(): List<Line2D> {
    return input.lines().map { line ->
        val (startX, startY, endX, endY) = line.split(" -> ").flatMap { it.split(",").map(String::toInt) }
        Point2D(startX, startY)..Point2D(endX, endY)
    }
}

private fun part1(topology: Topology) {
    val overlaps = topology.toVentCounts(true).values.count { it > 1 }
    println("Overlap count (cardinal only) $overlaps")
}

private fun part2(topology: Topology) {
    val overlaps = topology.toVentCounts(false).values.count { it > 1 }
    println("Overlap count (all) $overlaps")
}

private data class Topology(val width: Int, val height: Int, val ventLines: List<Line2D>) {
    constructor(ventLines: List<Line2D>) : this(
        ventLines.maxOf { listOf(it.start.x, it.end.x).maxOrNull()!! } + 1,
        ventLines.maxOf { listOf(it.start.y, it.end.y).maxOrNull()!! } + 1,
        ventLines
    )

    fun toVentCounts(cardinalLinesOnly: Boolean): Map<Point2D, Int> {
        return ventLines
            .filter { it.start.x == it.end.x || it.start.y == it.end.y || !cardinalLinesOnly }
            .flatMap(Line2D::asSequence)
            .groupBy { it }
            .mapValues { it.value.count() }
    }
}

