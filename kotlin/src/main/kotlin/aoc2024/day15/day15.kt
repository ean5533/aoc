package aoc2024.day15

import lib.*

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()

fun main() {
  val (warehouse, directions) = parseInput()
  
  val final = directions.fold(warehouse) {current, direction -> current.move(direction)}
  println(final)
  println(final.gpsCoordinates())
}

fun parseInput(): Pair<Warehouse, List<Point2D>> {
  val lines = input.lines()
  val areaLines = lines.takeWhile { it.isNotBlank() }
  val directions = lines.drop(areaLines.size + 1).joinToString("").map {
    when (it) {
      '<' -> Point2D.LEFT
      '>' -> Point2D.RIGHT
      '^' -> Point2D.UP
      'v' -> Point2D.DOWN
      else -> throw IllegalStateException()
    }
  }

  fun findAll(target: Char): Set<Point2D> =
    lines.flatMapIndexed { y, line -> line.toList().indexesOf(target).map { Point2D(it, y) } }.toSet()

  val warehouse = Warehouse(
    Area2D(areaLines[0].length, areaLines.size),
    findAll('#'),
    findAll('O'),
    findAll('@').single(),
  )

  return warehouse to directions
}

data class Warehouse(val area2D: Area2D, val walls: Set<Point2D>, val boxes: Set<Point2D>, val bot: Point2D) {
  fun move(direction: Point2D): Warehouse {
    // Find an empty space in that direction
    val final = generateSequence(bot + direction) { it + direction }.takeWhileInclusive { boxes.contains(it) }.last()
    
    if (walls.contains(final)) {
      // No move is possible
      return this
    }
    
    if(bot + direction == final) {
      // Just move the bot
      return copy(bot = bot + direction)
    }
    
    // Move bot + boxes
    return copy(
      bot = bot + direction,
      boxes = boxes - (bot + direction) + final
    )
  }
  
  fun gpsCoordinates() = boxes.sumOf { it.y * 100L + it.x }

  override fun toString(): String {
    return area2D.yRange.joinToString("\n") { y ->
      area2D.xRange.joinToString("") { x ->
        val point = Point2D(x, y)
        when {
          walls.contains(point) -> "#"
          boxes.contains(point) -> "O"
          bot == point -> "@"
          else -> "."
        }
      }
    }
  }
}
