package aoc2024.day20

import lib.*
import java.util.*

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
private val maze = parseInput()

fun main() {
  printTimeTaken {
    val baseline = aStarSearch(MazeSearchState(maze.copy(cheatTimeLeft = 0)))!!
    println("Baseline: " + baseline.cost)

    val best = aStarSearch(MazeSearchState(maze))
    println("Best: " + best?.cost)
    
//    val all = searchSolutionsBetterThan(MazeSearchState(maze), baseline.cost - 100)
//    println(all)
//    println(all.groupingBy { baseline.cost - it.cost }.eachCount())
//    println(all.filter { it.cost == 20 })
    val count = countSolutionsBetterThan(MazeSearchState(maze), baseline.cost - 100)
    println("Count no worse than ${baseline.cost - 100}: " + count)
  }
}

/**
 * Returns all solutions that are no worse than than the given [targetScore]
 */
fun <T, S : SearchState<T>> searchSolutionsBetterThan(initialState: S, targetScore: Int): List<S> {
  val queue = PriorityQueue<S>(compareBy({ it.cost + it.distanceHeuristic })).also { it.add(initialState) }
  val seenToLowestCost = mutableMapOf(initialState.current to initialState.cost)
  val solutions = mutableListOf<S>()

  while (queue.isNotEmpty()) {
    val state = queue.remove()
    if (state.isSolution) {
      solutions.add(state)
      continue
    }

    val nextStates = state.nextStates

    nextStates
      // Only keep searching if we have found a cheaper path to a previously found state
      .filter { nextState -> seenToLowestCost[nextState.current]?.let { nextState.cost < it } ?: true }
      // And if the cost is lower than the target
      .filter { nextState -> nextState.cost <= targetScore }
      .forEach { nextState ->
        @Suppress("UNCHECKED_CAST")
        queue.add(nextState as S)
        seenToLowestCost[nextState.current] = nextState.cost
      }
  }

  return solutions
}

/**
 * Returns all solutions that are no worse than than the given [targetScore]
 */
fun <T, S : SearchState<T>> countSolutionsBetterThan(initialState: S, targetScore: Int): Int {
  val queue = PriorityQueue<S>(compareBy({ it.cost + it.distanceHeuristic })).also { it.add(initialState) }
  val seenToLowestCost = mutableMapOf(initialState.current to initialState.cost)
  var solutions = 0

  while (queue.isNotEmpty()) {
    val state = queue.remove()
    if (state.isSolution) {
      println("Found so far: " + ++solutions)
      continue
    }

    val nextStates = state.nextStates

    nextStates
      // Only keep searching if we have found a cheaper path to a previously found state
      .filter { nextState -> seenToLowestCost[nextState.current]?.let { nextState.cost < it } ?: true }
      // And if the cost is lower than the target
      .filter { nextState -> nextState.cost <= targetScore }
      .forEach { nextState ->
        @Suppress("UNCHECKED_CAST")
        queue.add(nextState as S)
        seenToLowestCost[nextState.current] = nextState.cost
      }
  }

  return solutions
}


private fun parseInput(): Maze {
  val lines = input.lines()

  fun findAll(target: Char): Set<Point2D> =
    lines.flatMapIndexed { y, line -> line.toList().indexesOf(target).map { Point2D(it, y) } }.toSet()

  val maze = Maze(
    Area2D(lines[0].length, lines.size),
    findAll('#'),
    findAll('S').single(),
    findAll('E').single(),
  )

  return maze
}

private data class Maze(
  val area2D: Area2D,
  val walls: Set<Point2D>,
  val car: Point2D,
  val destination: Point2D,
  val cheatTimeLeft: Int = CHEAT_TIME,
  val cheatedAt: List<Point2D> = listOf()
) {
  override fun toString(): String {
    return area2D.yRange.joinToString("\n") { y ->
      area2D.xRange.joinToString("") { x ->
        val point = Point2D(x, y)
        when {
          cheatedAt.contains(point) -> "X"
          walls.contains(point) -> "#"
          car == point -> "@"
          destination == point -> "E"
          else -> "."
        }
      }
    }
  }

  companion object {
    val CHEAT_TIME = 2
  }
}

private data class MazeSearchState(
  override val current: Maze,
  override val cost: Int = 0,
) : SearchState<Maze> {
  override val isSolution: Boolean = current.car == current.destination
  override val nextStates: List<SearchState<Maze>> by lazy {
    current.car.neighbors4().filter {
      current.area2D.contains(it) &&
        (!current.walls.contains(it) || current.cheatTimeLeft > 1)
    }.map {
      val isCheating = current.walls.contains(it) || (1..<Maze.CHEAT_TIME).contains(current.cheatTimeLeft)
      copy(
        cost = cost + 1,
        current = current.copy(
          car = it,
          cheatTimeLeft = if (isCheating) current.cheatTimeLeft - 1 else current.cheatTimeLeft,
          cheatedAt = current.cheatedAt + if(isCheating) listOf(it) else listOf()
        )
      )
    }
  }
  override val distanceHeuristic: Int = current.car.manhattanDistanceTo(current.destination)
}
