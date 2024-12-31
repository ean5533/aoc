package aoc2024.day10

import lib.*

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
private val topology = Topology(input.lines().map { it.toList().map { it.digitToInt() } })

fun main() {
  part1()
  part2()
}

fun part1() {
  println(topology.trailheads)
  println(topology.nextFrom(topology.trailheads.first()))

  val score = topology.trailheads.sumOf { 
    val finalPoints = (0..8).fold(setOf(it)) { currentPoints, _ ->
      currentPoints.flatMap { topology.nextFrom(it) }.toSet()
    }
    finalPoints.size
  }

  println(score)
}

fun part2() {
  println(topology.trailheads)
  println(topology.nextFrom(topology.trailheads.first()))

  val score = topology.trailheads.sumOf {
    val finalPoints = (0..8).fold(listOf(it)) { currentPoints, _ ->
      currentPoints.flatMap { topology.nextFrom(it) }
    }
    finalPoints.size
  }

  println(score)
}

private data class Topology(val cells: List<List<Int>>) {
  val area = Area2D(cells[0].size, cells.size)
  val trailheads = cells.flatMapIndexed { y, row -> row.indexesOf(0).map { Point2D(it, y) } }.toSet()

  fun nextFrom(point: Point2D): List<Point2D> {
    val currentNumber = cells[point.y][point.x]
    return point.neighbors4()
      .filter { area.contains(it) }
      .filter { cells[it.y][it.x] == currentNumber + 1 }
  }
}
