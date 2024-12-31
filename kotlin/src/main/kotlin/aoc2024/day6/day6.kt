package aoc2024.day6

import lib.*

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
private val initialBoard = parseInput()


fun main() {
  part1()
  part2()
}

private fun parseInput(): Board {
  val lines = input.lines()
  val area = Area2D(lines[0].count(), lines.count())

  fun findAll(target: Char): Set<Point2D> =
    lines.flatMapIndexed { y, line -> line.toList().indexesOf(target).map { Point2D(it, y) } }.toSet()

  val guard = findAll('^').single()
  val obstacles = findAll('#')

  return Board(area, Guard(guard), obstacles)
}

private fun part1() {
  val final = generateSequence(initialBoard) { current -> current.next() }.last()
  println(final.traveled.flatMap { it.asSequence() }.toSet().count())
}

private fun part2() {
  // First find all potential spaces to put obstacles (everywere the guard walks, except where they start)
  val final = generateSequence(initialBoard) { current -> current.next() }.last()
  val candidates = final.traveled.flatMap { it.asSequence() }.toSet() - initialBoard.guard!!.position

  // For each one, try putting an obstacle check for looping
  val loopMakers = candidates.filter { candidate ->
    val alteredBoard = initialBoard.copy(obstacles = initialBoard.obstacles + candidate)

    generateSequence(alteredBoard) { current -> current.next() }
      .drop(1)
      .takeWhileInclusive { !it.isLooping() }
      .last()
      .isLooping()
  }

  println(loopMakers.size)
}

private data class Board(
  val area: Area2D,
  val guard: Guard?,
  val obstacles: Set<Point2D>,
  val traveled: List<Line2D> = listOf(),
  val guardHistory: List<Guard> = listOf(),
) {
  fun next(): Board? {
    if (guard == null) return null

    val trajectory = when (guard.facing) {
      Direction.UP -> Line2D(guard.position, guard.position.copy(y = area.yMin))
      Direction.RIGHT -> Line2D(guard.position, guard.position.copy(x = area.xMax))
      Direction.DOWN -> Line2D(guard.position, guard.position.copy(y = area.yMax))
      Direction.LEFT -> Line2D(guard.position, guard.position.copy(x = area.xMin))
    }

    val stoppedAt = trajectory.asSequence().takeWhile { !obstacles.contains(it) }.last()
    if (stoppedAt == trajectory.end) {
      return moveGuardTo(null, trajectory)
    }

    return moveGuardTo(stoppedAt, Line2D(guard.position, stoppedAt))
  }
  
  fun isLooping() = guardHistory.contains(guard)
  
  private fun moveGuardTo(position: Point2D?, through: Line2D): Board {
    val newGuard = position?.let { guard?.copy(position = position, facing = guard.facing.next()) }
    return copy(
      guard = newGuard,
      traveled = traveled + through,
      guardHistory = guardHistory + listOfNotNull(guard)
    )
  }
}

private data class Guard(val position: Point2D, val facing: Direction = Direction.UP)

private enum class Direction {
  UP, RIGHT, DOWN, LEFT;

  fun next(): Direction = when (this) {
    UP -> RIGHT
    RIGHT -> DOWN
    DOWN -> LEFT
    LEFT -> UP
  }
}
