package aoc2022.day23

import lib.*

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()

fun main() {
  val ground =
    input.lines().flatMapIndexed { y, line -> line.mapIndexedNotNull { x, c -> if (c == '#') Point2D(x, y) else null } }
      .let { Ground(it.toSet()) }

  printTimeTaken {
    val final = (1..10).fold(ground) { a, _ -> a.proposeAndMove() }
    println("part1: " + final.countEmptyGround())
  }

  printTimeTaken {
    val round = generateSequence(ground to ground.proposeAndMove()) { (_, current) ->
      current to current.proposeAndMove()
    }.takeWhileInclusive { (previous, current) -> previous.elves != current.elves }.last().second.round
    println("part2: $round")
  }
}

private data class Ground(
  val elves: Set<Point2D>,
  val round: Int = 0,
  val directions: List<Point2D> = listOf(NORTH, SOUTH, WEST, EAST),
) {
  fun proposeAndMove(): Ground = move(propose())

  private fun propose(): List<Pair<Point2D, Point2D>> {
    return elves.map { it to it.neighbors8().intersect(elves) }
      .filter { it.second.isNotEmpty() }
      .mapNotNull { (elf, neighbors) ->
        directions.firstOrNull { direction ->
          direction == NORTH && !neighbors.any { it.y == elf.y - 1 } ||
            direction == SOUTH && !neighbors.any { it.y == elf.y + 1 } ||
            direction == WEST && !neighbors.any { it.x == elf.x - 1 } ||
            direction == EAST && !neighbors.any { it.x == elf.x + 1 }
        }?.let { elf to elf + it }
      }
  }

  private fun move(proposals: List<Pair<Point2D, Point2D>>): Ground =
    proposals.groupBy { it.second }.filter { it.value.size == 1 }.map { it.value.single() }
      .fold(elves) { e, (from, to) -> e - from + to }.let { Ground(it, round + 1, directions.rotate()) }

  fun countEmptyGround(): Int {
    val area = Area2D(
      (elves.minOf { it.x }..elves.maxOf { it.x }),
      (elves.minOf { it.y }..elves.maxOf { it.y })
    )
    return (area.points() - elves).size
  }

  override fun toString(): String {
    return (elves.minOf { it.y }..elves.maxOf { it.y }).joinToString("\n") { y ->
      (elves.minOf { it.x }..elves.maxOf { it.x }).joinToString("") { x ->
        elves.firstOrNull { it == Point2D(x, y) }?.let { "#" } ?: "."
      }
    }
  }
}

private val NORTH = Point2D(0, -1)
private val SOUTH = Point2D(0, 1)
private val WEST = Point2D(-1, 0)
private val EAST = Point2D(1, 0)

