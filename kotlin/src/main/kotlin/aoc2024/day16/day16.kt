package aoc2024.day16

import lib.*
import lib.Point2D.Companion.DOWN
import lib.Point2D.Companion.LEFT
import lib.Point2D.Companion.RIGHT
import lib.Point2D.Companion.UP

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
private val maze = parseInput()

fun main() {
  printTimeTaken {
    val final = aStarSearch(MazeSearchState(maze))
    println(final?.cost)
  }
}

private fun parseInput(): Maze {
  val lines = input.lines()
  
  fun findAll(target: Char): Set<Point2D> =
    lines.flatMapIndexed { y, line -> line.toList().indexesOf(target).map { Point2D(it, y) } }.toSet()

  val maze = Maze(
    Area2D(lines[0].length, lines.size),
    findAll('#'),
    Reindeer(findAll('S').single()),
    findAll('E').single(),
  )

  return maze
}

private data class Maze(val area2D: Area2D, val walls: Set<Point2D>, val reindeer: Reindeer, val destination: Point2D) {
  override fun toString(): String {
    return area2D.yRange.joinToString("\n") { y ->
      area2D.xRange.joinToString("") { x ->
        val point = Point2D(x, y)
        when {
          walls.contains(point) -> "#"
          reindeer.position == point -> when(reindeer.facing) {
            RIGHT -> ">"
            LEFT -> "<"
            UP -> "^"
            DOWN -> "v"
            else -> throw IllegalStateException()
          }
          destination == point -> "E"
          else -> "."
        }
      }
    }
  }
}

private data class Reindeer(val position: Point2D, val facing: Point2D = Point2D.RIGHT) {
  fun step() = copy(position = position + facing)
  fun turnLeft() = copy(facing = Point2D(facing.y, -facing.x))
  fun turnRight() = copy(facing = Point2D(-facing.y, facing.x))
}

private data class MazeSearchState(
  override val current: Maze,
  override val cost: Int = 0,
) : SearchState<Maze> {
  override val isSolution: Boolean = current.reindeer.position == current.destination
  override val nextStates: List<SearchState<Maze>> by lazy {
    listOfNotNull(
      copy(cost = cost + 1000, current = current.copy(reindeer = current.reindeer.turnLeft())),
      copy(cost = cost + 1000, current = current.copy(reindeer = current.reindeer.turnRight())),
      copy(cost = cost + 1, current = current.copy(reindeer = current.reindeer.step()))
        .takeIf { !it.current.walls.contains(it.current.reindeer.position) },
    )
  }
  override val distanceHeuristic: Int = current.reindeer.position.manhattanDistanceTo(current.destination)
}
