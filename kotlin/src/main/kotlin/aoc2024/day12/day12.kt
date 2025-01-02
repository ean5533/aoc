package aoc2024.day12

import lib.*
import lib.Point2D.Companion.DOWN
import lib.Point2D.Companion.LEFT
import lib.Point2D.Companion.RIGHT
import lib.Point2D.Companion.UP

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
private val plots = parseInput()

fun main() {
  println(plots.map { it.type to it.price1() })
  println(plots.sumOf { it.price1() })
  println(plots.map { it.type to it.price2() })
  println(plots.sumOf { it.price2() })
}

private fun parseInput(): List<Plot> {
  val plots = mutableListOf<Plot>()
  val garden = input.lines().flatMapIndexed { y, line -> line.mapIndexed { x, c -> Point2D(x, y) to c } }.toMap()
  val remaining =
    garden.entries.map { it.toPair() }.toMutableList()

  while (remaining.any()) {
    val plant = remaining.first().second
    val cells = remaining.first().first.startFloodFill { garden[it] == plant }
    plots += Plot(plant, cells)
    remaining.removeIf { cells.contains(it.first) }
  }

  return plots
}

private data class Plot(val type: Char, val cells: Set<Point2D>) {
  fun area() = cells.size
  fun perimeter() = cells.flatMap { it.neighbors4() }.minus(cells).size
  fun fences(): Int {
    val fences = mutableSetOf<Pair<Point2D, Line2D>>()
    val perimeter = cells.flatMap { it.neighbors4() }.minus(cells)
    val queue = perimeter.toMutableList()
    while (queue.any()) {
      val next = queue.first()

      listOfNotNull(
        if (cells.contains(next.shift(RIGHT))) {
          RIGHT to next.startFloodFill { perimeter.contains(it) && cells.contains(it.shift(RIGHT)) && it.x == next.x }
            .asLine()
        } else null,
        if (cells.contains(next.shift(LEFT))) {
          LEFT to next.startFloodFill { perimeter.contains(it) && cells.contains(it.shift(LEFT)) && it.x == next.x }
            .asLine()
        } else null,
        if (cells.contains(next.shift(UP))) {
          UP to next.startFloodFill { perimeter.contains(it) && cells.contains(it.shift(UP)) && it.y == next.y }
            .asLine()
        } else null,
        if (cells.contains(next.shift(DOWN))) {
          DOWN to next.startFloodFill { perimeter.contains(it) && cells.contains(it.shift(DOWN)) && it.y == next.y }
            .asLine()
        } else null,
      ).forEach {
        if (fences.add(it))
          it.second.points().forEach { queue.remove(it) } // remove only one of each element found per flood
      }
    }
    return fences.size
  }

  fun price1() = area() * perimeter()
  fun price2() = area() * fences()
}

private fun Set<Point2D>.asLine(): Line2D {
  return when {
    map { it.x }.toSet().size == 1 -> Line2D(Point2D(first().x, minOf { it.y }), Point2D(first().x, maxOf { it.y }))
    map { it.y }.toSet().size == 1 -> Line2D(Point2D(minOf { it.x }, first().y), Point2D(maxOf { it.x }, first().y))
    else -> throw IllegalStateException()
  }
}
