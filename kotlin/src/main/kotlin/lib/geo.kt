package lib

import java.lang.Integer.max
import java.lang.Integer.min
import kotlin.math.abs
import kotlin.math.absoluteValue

data class Point2D(val x: Int, val y: Int) {
  fun scale(scalar: Int) = copy(x = x * scalar, y = y * scalar)
  fun shift(amountX: Int, amountY: Int) = copy(x = x + amountX, y = y + amountY)
  fun shiftX(amount: Int) = copy(x = x + amount)
  fun shiftY(amount: Int) = copy(y = y + amount)
  fun moveWithin(area: Area2D, move: Point2D): Point2D = (this + move).let {
    when {
      it.x < area.xMin -> it.copy(x = area.xMax)
      it.x > area.xMax -> it.copy(x = area.xMin)
      it.y < area.yMin -> it.copy(y = area.yMax)
      it.y > area.yMax -> it.copy(y = area.yMin)
      else -> it
    }
  }

  fun manhattanDistanceTo(other: Point2D): Int = abs(x - other.x) + abs(y - other.y)
  fun translations(translations: List<Point2D>): List<Point2D> = translations.map { this + it }
  fun neighbors4(): List<Point2D> = translations(adjacency4Offsets)
  fun neighbors8(): List<Point2D> = translations(adjacency8Offsets)
  
  fun lines8(length: Int): List<Line2D> {
    require(length >= 1)
    return adjacency8Offsets.map { Line2D(this, this + it.scale(length-1)) }
  }

  fun adjacentToCardinally(other: Point2D): Boolean {
    return neighbors4().contains(other)
  }

  fun adjacentToDiagonally(other: Point2D): Boolean {
    // Performs a little better than neighbors8().contains(other)
    return x >= other.x - 1 &&
      x <= other.x + 1 &&
      y >= other.y - 1 &&
      y <= other.y + 1
  }

  fun adjacentToDiagonally(line: Line2D): Boolean {
    check(line.start.x == line.end.x || line.start.y == line.end.y) {
      "This method's implementation currently only works for flat lines"
    }

    return x >= line.start.x - 1 &&
      x <= line.end.x + 1 &&
      y >= line.start.y - 1 &&
      y <= line.end.y + 1
  }

  operator fun plus(other: Point2D) = Point2D(x + other.x, y + other.y)
  operator fun minus(other: Point2D) = Point2D(x - other.x, y - other.y)
  operator fun rangeTo(other: Point2D) = Line2D(this, other)

  fun flipY(area: Area2D): Point2D {
    val offset = (area.yMax + area.yMin) / 2
    return copy(y = (y - offset) * -1 + offset + 1)
  }

  fun flipX(area: Area2D): Point2D {
    val offset = (area.xMin + area.xMax) / 2
    return copy(x = (x - offset) * -1 + offset + 1)
  }

  fun transpose(): Point2D = copy(x = y, y = x)
  fun antiTranspose(area: Area2D): Point2D = transpose().flipX(area).flipY(area)

  override fun toString(): String = "[x:$x, y:$y]"

  companion object {
    val ORIGIN = Point2D(0, 0)
    private val adjacency4Offsets: List<Point2D> = listOf(Point2D(0, 1), Point2D(0, -1), Point2D(1, 0), Point2D(-1, 0))
    private val adjacency8Offsets: List<Point2D> = (-1..1).cartesianProduct(-1..1).minus(0 to 0).map { it.toPoint2D() }
  }
}

fun Iterable<Int>.toPoint2D(): Point2D {
  val (x, y) = this.take(2)
  return Point2D(x, y)
}

fun Pair<Int, Int>.toPoint2D(): Point2D = Point2D(first, second)

data class Line2D(val start: Point2D, val end: Point2D) {
  init {
    // Ensure it's a vertical, horizontal, or 45-degree diagonal line (this class can't handle anything else)
    check(start.x == end.x || start.y == end.y || (end.x - start.x).absoluteValue == (end.y - start.y).absoluteValue)
  }

  fun toSequence(): Sequence<Point2D> {
    val xRange = if (start.x == end.x) generateSequence { start.x } else (start.x smartRange end.x).asSequence()
    val yRange = if (start.y == end.y) generateSequence { start.y } else (start.y smartRange end.y).asSequence()

    return xRange.zip(yRange).map { (x, y) -> Point2D(x, y) }
  }
}

data class Area2D(val xRange: IntRange, val yRange: IntRange) {
  constructor(size: Int, origin: Point2D = Point2D.ORIGIN) : this(size, size, origin)
  constructor(width: Int, height: Int, origin: Point2D = Point2D.ORIGIN) :
    this(origin.x until origin.x + width, origin.y until origin.y + height)

  val xMin = xRange.start
  val xMax = xRange.endInclusive
  val yMin = yRange.start
  val yMax = yRange.endInclusive
  val width = xMax - xMin + 1
  val height = yMax - yMin + 1

  fun origin(): Point2D = Point2D(xRange.start, yRange.start)
  fun size(): Long = xRange.count().toLong() * yRange.count().toLong()
  fun points(): List<Point2D> = xRange.flatMap { x -> yRange.map { y -> Point2D(x, y) } }
  fun contains(x: Int, y: Int) = xRange.contains(x) && yRange.contains(y)
  fun contains(point: Point2D) = xRange.contains(point.x) && yRange.contains(point.y)
  fun contains(line: Line2D) = contains(line.start) && contains(line.end)
  
  fun shrink(amount: Int): Area2D {
    require(width > amount * 2)
    require(height > amount * 2)
    return create(xMin + amount, xMax - amount, yMin + amount, yMax - amount)
  }

  companion object {
    fun create(x1: Int, x2: Int, y1: Int, y2: Int): Area2D =
      Area2D(min(x1, x2)..max(x1, x2), min(y1, y2)..max(y1, y2))
  }
}

data class Point3D(val x: Int, val y: Int, val z: Int) {
  fun shift(amountX: Int, amountY: Int, amountZ: Int) = copy(x = x + amountX, y = y + amountY, z = z + amountZ)
  fun shiftX(amount: Int) = copy(x = x + amount)
  fun shiftY(amount: Int) = copy(y = y + amount)
  fun shiftZ(amount: Int) = copy(z = z + amount)

  fun manhattanDistanceTo(other: Point3D = Point3D(0, 0, 0)): Int =
    abs(x - other.x) + abs(y - other.y) + abs(z - other.z)

  fun distanceTo(other: Point3D): Point3D = Point3D(x - other.x, y - other.y, z - other.z)
  fun move(amount: Point3D): Point3D = Point3D(x + amount.x, y + amount.y, z + amount.z)
  fun neighbors(): List<Point3D> = adjacencyOffsets.map { this + it }

  operator fun plus(other: Point3D) = Point3D(x + other.x, y + other.y, z + other.z)
  operator fun minus(other: Point3D) = Point3D(x - other.x, y - other.y, y - other.z)

  fun startFloodFill(include: (Point3D) -> Boolean): Set<Point3D> {
    val filled = mutableSetOf<Point3D>()
    val queue = ArrayDeque(listOf(this))
    queue.asSequence().forEach { if (include(it) && filled.add(it)) queue.addAll(it.neighbors()) }
    return filled
  }

  override fun toString(): String = "[x:$x, y:$y, z:$z]"

  companion object {
    val ORIGIN = Point3D(0, 0, 0)
    private val adjacencyOffsets = listOf(
      ORIGIN.shiftX(-1),
      ORIGIN.shiftX(1),
      ORIGIN.shiftY(-1),
      ORIGIN.shiftY(1),
      ORIGIN.shiftZ(-1),
      ORIGIN.shiftZ(1),
    )
  }
}

fun Iterable<Int>.toPoint3D(): Point3D {
  val (x, y, z) = this.take(3)
  return Point3D(x, y, z)
}

fun Triple<Int, Int, Int>.toPoint3D(): Point3D = Point3D(first, second, third)

data class Area3D(val xRange: IntRange, val yRange: IntRange, val zRange: IntRange) {
  fun origin(): Point3D = Point3D(xRange.start, yRange.start, zRange.start)
  fun size(): Long = xRange.count().toLong() * yRange.count().toLong() * zRange.count().toLong()
  fun points(): List<Point3D> = xRange.flatMap { x -> yRange.flatMap { y -> zRange.map { z -> Point3D(x, y, z) } } }
  fun contains(point: Point3D) = xRange.contains(point.x) && yRange.contains(point.y) && zRange.contains(point.z)

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

@Deprecated("AoC 2021 only")
interface Cell {
  val location: Point2D
}

@Suppress("DEPRECATION")
@Deprecated("AoC 2021 only")
interface Topology<T : Cell> {
  val cells: Map<Point2D, T>

  fun get4NeighborsOf(cell: T): List<T> = cell.location.neighbors4().mapNotNull { cells[it] }
  fun get8NeighborsOf(cell: T): List<T> = cell.location.neighbors8().mapNotNull { cells[it] }
}
