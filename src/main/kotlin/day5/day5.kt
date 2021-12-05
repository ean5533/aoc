package day5

private val classLoader: ClassLoader = object {}.javaClass.classLoader
private val input = classLoader.getResource("text/day5")!!.readText()

fun main() {
    val ventLines = parseInput()
    val topology = Topology(ventLines)

    part1(topology)
    part2(topology)
}

fun parseInput(): List<VentLine> {
    return input.lines().map { line ->
        val (startX, startY, endX, endY) = line.split(" -> ").flatMap { it.split(",").map(String::toInt) }
        VentLine(Coordinate(startX, startY), Coordinate(endX, endY))
    }
}

fun part1(topology: Topology) {
    val overlaps = topology.toVentCounts(true).values.count { it > 1 }
    println("Overlap count (cardinal only) $overlaps")
}

fun part2(topology: Topology) {
    val overlaps = topology.toVentCounts(false).values.count { it > 1 }
    println("Overlap count (all) $overlaps")
}

data class Coordinate(val x: Int, val y: Int)

data class VentLine(val start: Coordinate, val end: Coordinate) {
    fun toSequence(): Sequence<Coordinate> {
        val xRange = if (start.x == end.x) generateSequence { start.x } else (start.x smartRange end.x).asSequence()
        val yRange = if (start.y == end.y) generateSequence { start.y } else (start.y smartRange end.y).asSequence()

        return xRange.zip(yRange).map { (x, y) -> Coordinate(x, y) }
    }
}

data class Topology(val width: Int, val height: Int, val ventLines: List<VentLine>) {
    constructor(ventLines: List<VentLine>) : this(
        ventLines.maxOf { listOf(it.start.x, it.end.x).maxOrNull()!! } + 1,
        ventLines.maxOf { listOf(it.start.y, it.end.y).maxOrNull()!! } + 1,
        ventLines
    )

    fun toVentCounts(cardinalLinesOnly: Boolean): Map<Coordinate, Int> {
        return ventLines
            .filter { it.start.x == it.end.x || it.start.y == it.end.y || !cardinalLinesOnly }
            .flatMap(VentLine::toSequence)
            .groupBy { it }
            .mapValues { it.value.count() }
    }
}

infix fun Int.smartRange(to: Int): IntProgression {
    return if (this < to) this..to else this downTo to
}