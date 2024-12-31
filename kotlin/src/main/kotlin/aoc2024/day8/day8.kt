package aoc2024.day8

import lib.*

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
private val board = parseInput()

fun main() {
  part1()
  part2()
}

private fun parseInput(): Board {
  val lines = input.lines()
  val area = Area2D(lines[0].count(), lines.count())
  
  val antennas = lines.flatMapIndexed { y, line -> line.toList().mapIndexedNotNull { x, c -> if(c != '.') x to c else null }.map { Point2D(it.first, y) to it.second } }.toSet()

  return Board(area, antennas.toMap())
}

fun part1() {
  val antinodes = board.antennaGroups.mapValues { (_, points) ->
    // For each pair of antennas...
    points.uniquePairs()
      // ...draw a line between the antennas, extend it in both directions, and get the ends...
      .flatMap { listOf(Line2D(it.first, it.second).scaleEnd(2).end, Line2D(it.second, it.first).scaleEnd(2).end) }
      // ...but only count the points inside the original area
      .filter { board.area.contains(it) }
  }
  
  println(antinodes)
  println(antinodes.values.flatMap { it }.toSet().size)
}

fun part2() {
  val antinodes = board.antennaGroups.mapValues { (_, points) ->
    // For each pair of antennas...
    points.uniquePairs()
      // ...draw a line between the antennas, extend it in both directions until it escapes the area, and get all points...
      .flatMap {
        var line = Line2D(it.first, it.second)
        line = generateSequence(line) { current -> current.scaleEnd(2) }
          .takeWhileInclusive { board.area.contains(it.end) }.last()
        line = generateSequence(line.flip()) { current -> current.scaleEnd(2) }
          .takeWhileInclusive { board.area.contains(it.end) }.last()

        // ...but only count the points inside the original area 
        line.points().filter { board.area.contains(it) }
      }
  }

  println(antinodes)
  println(antinodes.values.flatMap { it }.toSet().size)
}

typealias Antenna = Char

private data class Board(
  val area: Area2D,
  val antennas: Map<Point2D, Antenna>,
) {
  val antennaGroups = antennas.entries.groupBy({it.value}, {it.key})
}
