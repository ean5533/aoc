package aoc2024.day18

import lib.*
import kotlin.math.pow

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()

//private val area = Area2D(7, 7)
private val area = Area2D(71, 71)
//private val take = 12
private val take = 1024

fun main() {
  val corruption = input.lines().map { it.split(",").map { it.toInt() }.toPoint2D() }

  printTimeTaken {
    val final = aStarSearch(EscapeSearchState(corruption.take(take).toSet()))
    println(final!!.cost)
  }

  printTimeTaken {
    var min = take
    var max = corruption.size
    while (true) {
      val guess = Math.ceil((max + min) / 2.0).toInt()
      val final = aStarSearch(EscapeSearchState(corruption.take(guess).toSet()))
      if (final == null) {
        max = guess - 1
      } else {
        min = guess
      }

      if (min == max) {
        println(corruption[min].let { "${it.x},${it.y}" })
        break
      }
    }
  }
}

private data class EscapeSearchState(
  val walls: Set<Point2D>,
  override val current: Point2D = Point2D.ORIGIN,
  override val cost: Int = 0,
) : SearchState<Point2D> {
  override val isSolution: Boolean = current == TARGET
  override val nextStates: List<SearchState<Point2D>> by lazy {
    current.neighbors4()
      .filter { area.contains(it) && !walls.contains(it) }
      .map { copy(current = it, cost = cost + 1) }
  }

  companion object {
    private val TARGET = Point2D(area.xMax, area.yMax)
  }
}
