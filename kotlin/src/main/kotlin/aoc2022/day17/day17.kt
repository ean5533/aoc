package aoc2022.day17

import lib.*

val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
val jets = input.map { if (it == '<') -1 else 1 }

fun main() {
    part1()
    part2()
}

private fun part1() {
    printTimeTaken {
        val final = Tower().addShapes(2022, uniqueShapes.repeat().iterator(), jets.repeat().iterator())
        println("part1: " + (final.untrimmedHeight + 1))
    }
}

private fun part2() {
    printTimeTaken {
        val shapeIterator = uniqueShapes.repeat().iterator()
        val jetIterator = jets.repeat().iterator()

        val (firstCycleStart, secondCycleStart) = findCycle(shapeIterator, jetIterator)
        val cycleLength = secondCycleStart.shapeCount - firstCycleStart.shapeCount
        val cycleHeight = secondCycleStart.untrimmedHeight - firstCycleStart.untrimmedHeight
        println("Cycle length $cycleLength shapes, starting somewhere around ${firstCycleStart.shapeCount}, providing $cycleHeight height per cycle")

        val additionalShapesNeeded = 1000000000000 - secondCycleStart.shapeCount
        val cyclesNeeded = additionalShapesNeeded / cycleLength
        val heightOfAllCycles = cyclesNeeded * cycleHeight

        val remainingShapesNeeded = additionalShapesNeeded - cyclesNeeded * cycleLength
        val finalTower = secondCycleStart.addShapes(remainingShapesNeeded.toInt(), shapeIterator, jetIterator)

        val totalHeight = heightOfAllCycles + finalTower.untrimmedHeight + 1
        println("part2: $totalHeight")
    }
}

private fun findCycle(shapesRepeated: Iterator<Shape>, jetsRepeated: Iterator<Int>): Pair<Tower, Tower> {
    val towers = mutableListOf(Tower())
    var dupe: Tower?
    while (true) {
        val new = towers.last().addShape(shapesRepeated.next(), jetsRepeated)
        dupe = towers.firstOrNull { it.repeats(new) }
        towers.add(new)
        if (dupe != null) break
    }
    check(dupe != null)
    return dupe to towers.last()
}

private data class Tower(
    val towerRocks: Set<Point2D> = setOf(),
    val bottomTrimmed: Long = 0L,
    val shapeCount: Long = 0,
) {
    private val top by lazy { towerRocks.maxOfOrNull { it.y } ?: -1 }
    private val topRocks by lazy {
        towerRocks.filter { top - it.y < ROWS_FOR_CYCLE_CHECK }.map { it.shift(0, -top) }.toSet()
    }

    val untrimmedHeight by lazy { top + bottomTrimmed }

    fun addShape(shape: Shape, jets: Iterator<Int>): Tower {
        val final = generateSequence(shape.shift(2, top + 4).shiftOrUnchanged(jets.next(), 0)) {
            it.shiftOrNull(0, -1)?.shiftOrUnchanged(jets.next(), 0)
        }.last()
        return this.copy(towerRocks = towerRocks + final.rocks, shapeCount = shapeCount + 1).trimmed()
    }

    fun addShapes(number: Int, shapes: Iterator<Shape>, jets: Iterator<Int>) =
        (1..number).fold(this) { tower, _ -> tower.addShape(shapes.next(), jets) }

    fun repeats(other: Tower): Boolean = topRocks == other.topRocks

    private fun Shape.shiftOrUnchanged(xShift: Int, yShift: Int): Shape = shiftOrNull(xShift, yShift) ?: this

    private fun Shape.shiftOrNull(xShift: Int, yShift: Int): Shape? =
        shift(xShift, yShift).let {
            when {
                it.left < 0 || it.right >= Companion.WIDTH || it.bottom < 0 -> null
                it.rocks.any { towerRocks.contains(it) } -> null
                else -> it
            }
        }

    private fun trimmed(): Tower {
        if (top < TRIM_AMOUNT * 2) return this

        return copy(
            towerRocks = towerRocks.filter { it.y >= TRIM_AMOUNT }.map { it.shift(0, -TRIM_AMOUNT) }.toSet(),
            bottomTrimmed = bottomTrimmed + TRIM_AMOUNT
        )
    }

    override fun toString(): String = sequence {
        yield("+-------+")
        yieldAll((top downTo 0).map { y ->
            val rockSpaces = towerRocks.filter { it.y == y }.map { it.x }.toSet()
            "|" + (0 until WIDTH).map { if (rockSpaces.contains(it)) "#" else ' ' }
                .joinToString("") { it.toString() } + "|"
        })
        yield("+-------+")
    }.joinToString("\n")

    companion object {
        private const val WIDTH = 7
        private const val ROWS_FOR_CYCLE_CHECK = 50
        private const val TRIM_AMOUNT = ROWS_FOR_CYCLE_CHECK * 2
    }
}

private data class Shape(val rocks: Set<Point2D>) {
    val left by lazy { rocks.minOf { it.x } }
    val right by lazy { rocks.maxOf { it.x } }
    val top by lazy { rocks.maxOf { it.y } }
    val bottom by lazy { rocks.minOf { it.y } }

    fun shift(shiftX: Int, shiftY: Int): Shape = copy(rocks = rocks.map { it.shift(shiftX, shiftY) }.toSet())

    override fun toString(): String = (top downTo bottom).joinToString("\n") { y ->
        (left..right).joinToString("") { x ->
            if (rocks.contains(Point2D(x, y))) "#" else " "
        }
    }
}

private fun Set<Point2D>.toShape(): Shape = Shape(this)

private val uniqueShapes: List<Shape> = listOf(
    setOf(
        Point2D(0, 0),
        Point2D(1, 0),
        Point2D(2, 0),
        Point2D(3, 0),
    ).toShape(),
    setOf(
        Point2D(1, 2),
        Point2D(0, 1),
        Point2D(1, 1),
        Point2D(2, 1),
        Point2D(1, 0),
    ).toShape(),
    setOf(
        Point2D(2, 2),
        Point2D(2, 1),
        Point2D(0, 0),
        Point2D(1, 0),
        Point2D(2, 0),
    ).toShape(),
    setOf(
        Point2D(0, 3),
        Point2D(0, 2),
        Point2D(0, 1),
        Point2D(0, 0),
    ).toShape(),
    setOf(
        Point2D(0, 1),
        Point2D(1, 1),
        Point2D(0, 0),
        Point2D(1, 0),
    ).toShape(),
)
