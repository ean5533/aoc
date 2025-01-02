package aoc2024.day13

import lib.*
import lib.Point2D.Companion.DOWN
import lib.Point2D.Companion.LEFT
import lib.Point2D.Companion.ORIGIN
import lib.Point2D.Companion.RIGHT
import lib.Point2D.Companion.UP

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
private val machines = input.lines().chunked(4).map { (a, b, prize) ->
  Machine(
    a.split(",").map { it.split("+")[1].toInt() }.toPoint2D(),
    b.split(",").map { it.split("+")[1].toInt() }.toPoint2D(),
    prize.split(",").map { it.split("=")[1].toInt() }.toPoint2D(),
  )
}

fun main() {
  printTimeTaken {
    val solutions = machines.mapNotNull { aStarSearch(MachineState(it)) }
    println(solutions)
    println(solutions.sumOf { it.cost })
  }
}

data class Machine(val a: Point2D, val b: Point2D, val prize: Point2D)

data class MachineState(
  val machine: Machine,
  override val current: Point2D = ORIGIN,
  override val cost: Int = 0,
) : SearchState<Point2D> {
  override val isSolution: Boolean = current == machine.prize
  override val nextStates by lazy {
    listOf(
      copy(current = current + machine.a, cost = cost + 3),
      copy(current = current + machine.b, cost = cost + 1),
    ).filter { it.current.x <= machine.prize.x && it.current.y <= machine.prize.y }
  }

  override val distanceHeuristic: Int = machine.prize.y - current.y + machine.prize.x - current.x
}
