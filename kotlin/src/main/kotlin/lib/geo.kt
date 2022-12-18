package lib

import java.lang.Integer.max
import java.lang.Integer.min
import kotlin.math.abs

data class Point2D(val x: Int, val y: Int) {
    fun shift(shiftX: Int, shiftY: Int) = copy(x = x + shiftX, y = y + shiftY)
    fun manhattanDistanceTo(other: Point2D): Int = abs(x - other.x) + abs(y - other.y)
}

fun Iterable<Int>.toPoint2D(): Point2D {
    val (x, y) = this.take(2)
    return Point2D(x, y)
}
fun Pair<Int, Int>.toPoint2D(): Point2D = Point2D(first, second)

data class Line2D(val start: Point2D, val end: Point2D) {
    fun toSequence(): Sequence<Point2D> {
        val xRange = if (start.x == end.x) generateSequence { start.x } else (start.x smartRange end.x).asSequence()
        val yRange = if (start.y == end.y) generateSequence { start.y } else (start.y smartRange end.y).asSequence()

        return xRange.zip(yRange).map { (x, y) -> Point2D(x, y) }
    }
}

data class Area2D(val xRange: IntRange, val yRange: IntRange) {
    val left = xRange.start
    val right = xRange.endInclusive
    val top = yRange.start
    val bottom = yRange.endInclusive

    companion object {
        fun create(x1: Int, x2: Int, y1: Int, y2: Int): Area2D =
            Area2D(min(x1, x2)..max(x1, x2), min(y1, y2)..max(y1, y2))
    }
}

data class Point3D(val x: Int, val y: Int, val z: Int) {
    fun manhattanDistanceTo(other: Point3D = Point3D(0, 0, 0)): Int =
        abs(x - other.x) + abs(y - other.y) + abs(z - other.z)

    fun distanceTo(other: Point3D): Point3D = Point3D(x - other.x, y - other.y, z - other.z)
    fun move(amount: Point3D): Point3D = Point3D(x + amount.x, y + amount.y, z + amount.z)
}

data class Area3D(val xRange: IntRange, val yRange: IntRange, val zRange: IntRange) {
    fun size(): Long = xRange.count().toLong() * yRange.count().toLong() * zRange.count().toLong()

    fun minus(other: Area3D): List<Area3D> {
        val (xSliced, xLeftover) = this.extractXAxisSlice(other.xRange)
        val (ySliced, yLeftover) = xSliced?.extractYAxisSlice(other.yRange) ?: null to listOf()
        val (_, zLeftover) = ySliced?.extractZAxisSlice(other.zRange) ?: null to listOf()

        return xLeftover + yLeftover + zLeftover
    }

    /**
     * Extracts a slice of the region that is [range]-wide, retaining the y- and z-axis dimensions.
     * Returns the extracted slice (if region contained it) plus the remaining regions (if any remainined)
     *
     * - If [range] is a strict subset of the region, result will be the extracted middle slice, plus the two end slices.
     * - If [range] overlaps one side or the other, result will be the extracted end slice, plus the other end slice.
     * - If [range] is non-strict superset of the region, result will be the entire region, plus nothing.
     * - If [range] is disjoint from the region, result will be nothing plus the entire region.
     */
    fun extractXAxisSlice(range: IntRange): Pair<Area3D?, List<Area3D>> {
        val (extractedXRange, remainingXRanges) = xRange.extractSlice(range)
        return extractedXRange?.let { Area3D(it, yRange, zRange) } to
                remainingXRanges.map { Area3D(it, yRange, zRange) }
    }

    /**
     * Identical to [extractXAxisSlice], but operates on the Y axis. I couldn't figure out how to generalize this code.
     */
    fun extractYAxisSlice(range: IntRange): Pair<Area3D?, List<Area3D>> {
        val (extractedYRange, remainingYRanges) = yRange.extractSlice(range)
        return extractedYRange?.let { Area3D(xRange, it, zRange) } to
                remainingYRanges.map { Area3D(xRange, it, zRange) }
    }

    /**
     * Identical to [extractXAxisSlice], but operates on the Z axis. I couldn't figure out how to generalize this code.
     */
    fun extractZAxisSlice(range: IntRange): Pair<Area3D?, List<Area3D>> {
        val (extractedZRange, remainingZRanges) = zRange.extractSlice(range)
        return extractedZRange?.let { Area3D(xRange, yRange, it) } to
                remainingZRanges.map { Area3D(xRange, yRange, it) }
    }
}

interface Cell {
    val location: Point2D
}

interface Topology<T : Cell> {
    val cells: Map<Point2D, T>

    fun get4NeighborsOf(cell: T): List<T> = getNeighborsOf(cell, adjacency4Offsets)

    fun get8NeighborsOf(cell: T): List<T> = getNeighborsOf(cell, adjacency8Offsets)

    private fun getNeighborsOf(cell: T, adjacencyOffsets: List<Pair<Int, Int>>): List<T> = adjacencyOffsets
        .mapNotNull { (x, y) -> cells[cell.location.copy(x = cell.location.x + x, y = cell.location.y + y)] }

    companion object {
        private val adjacency4Offsets: List<Pair<Int, Int>> = listOf(-1, 1).cartesianProduct(listOf(-1, 1))
        private val adjacency8Offsets: List<Pair<Int, Int>> = (-1..1).cartesianProduct(-1..1).minus(0 to 0)
    }
}
