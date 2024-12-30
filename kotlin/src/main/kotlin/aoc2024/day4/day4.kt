package aoc2024.day4

import lib.Area2D
import lib.Point2D
import lib.loadResourceMatchingPackageName

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
private val grid = input.lines().map { it.toList() }
private val area = Area2D(grid[0].size, grid.size)
private const val target = "XMAS"

fun main() {
  part1()
  part2()
}

private fun part1() {
  val finds = area.points().sumOf {
    it.lines8(target.length)
      .filter { area.contains(it) }
      .map { it.toSequence().map { grid[it.y][it.x] }.joinToString("") }
      .count { it == target }
  }
  println(finds)
}

private fun part2() {
  val finds = area.shrink(1).points()
    .filter { grid[it.y][it.x] == 'A' }
    .count { theA ->
      diagonalOffsetPairs.all {
        theA.translations(it).map {
          grid[it.y][it.x]
        }.toSet() == mAndS
      }
    }
  println(finds)
}

private val mAndS = setOf('M', 'S')
private val diagonalOffsetPairs: List<List<Point2D>> =
  listOf(listOf(Point2D(-1, -1), Point2D(1, 1)), listOf(Point2D(-1, 1), Point2D(1, -1)))
