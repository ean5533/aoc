package aoc2022.day24

import lib.*

private val RIGHT = Point2D(1, 0)
private val DOWN = Point2D(0, 1)
private val LEFT = Point2D(-1, 0)
private val UP = Point2D(0, -1)
private val CHAR_TO_DIRECTION = mapOf('>' to RIGHT, 'v' to DOWN, '<' to LEFT, '^' to UP)
private val DIRECTION_TO_CHAR = CHAR_TO_DIRECTION.entries.associateBy({ it.value }) { it.key }

private lateinit var valley: Valley

fun main() {
  printTimeTaken {
    valley = parseInput()
    println("input parsed and blizzards precalculated")
  }

  lateinit var step1: ValleySearchState
  printTimeTaken {
    step1 = aStarSearch(ValleySearchState())!!
    println("part1: ${step1.cost}")
  }
  
  printTimeTaken {
    val step2 = aStarSearch(step1.copy(goal = valley.start))!!
    val step3 = aStarSearch(step2.copy(goal = valley.end))
    println("part2: ${step3!!.cost}")
  }
}

private fun parseInput(): Valley {
  val lines = loadResourceMatchingPackageName(object {}.javaClass).trim().lines()
  val area = Area2D(lines[0].length - 2, lines.size - 2)
  val initialBlizzards = lines.drop(1).dropLast(1)
    .flatMapIndexed { y, line ->
      line.drop(1).dropLast(1).mapIndexedNotNull { x, c -> if (c == '.') null else Point2D(x, y) to c }
    }
    .map { Blizzard(it.first, CHAR_TO_DIRECTION[it.second]!!) }
    .groupBy { it.position }

  val blizzardConfigurations = (0 until lcm(area.width, area.height) - 1).runningFold(initialBlizzards) { it, _ ->
    it.values.flatMap { it.map { it.moveWithin(area) } }.groupBy { it.position }
  }

  return Valley(area, blizzardConfigurations)
}

private data class Blizzard(val position: Point2D, val direction: Point2D) {
  fun moveWithin(area: Area2D) = copy(position = position.moveWithin(area, direction))
}

private data class Valley(val area: Area2D, val blizzardConfigurations: List<Map<Point2D, List<Blizzard>>>) {
  val start = Point2D(0, -1)
  val end = Point2D(area.xMax, area.yMax + 1)

  fun inBounds(it: Point2D): Boolean = area.contains(it) || it == start || it == end
}

private data class ValleyState(val blizzardConfigurationNumber: Int = 0, val you: Point2D = Point2D(0, -1))

private data class ValleySearchState(
  override val current: ValleyState = ValleyState(),
  override val cost: Int = 0,
  val goal: Point2D = valley.end,
) : SearchState<ValleyState> {
  override val nextStates: List<SearchState<ValleyState>> by lazy {
    val newBlizzardNumber = (current.blizzardConfigurationNumber + 1).mod(valley.blizzardConfigurations.size)
    val newBlizzards = valley.blizzardConfigurations[newBlizzardNumber]
    (current.you.neighbors4() + current.you).filter { valley.inBounds(it) && !newBlizzards.containsKey(it) }
      .map { current.copy(blizzardConfigurationNumber = newBlizzardNumber, you = it) }
      .map { copy(current = it, cost = cost + 1) }
  }

  override val isSolution by lazy { distanceHeuristic == 0 }
  override val distanceHeuristic: Int by lazy { current.you.manhattanDistanceTo(goal)} 

  fun draw(): String = valley.area.points().groupBy { it.y }.entries.sortedBy { it.key }.joinToString("\n") {
    it.value.joinToString("") {
      val localBlizzards = valley.blizzardConfigurations[current.blizzardConfigurationNumber][it]
      when {
        it == current.you -> "E"
        localBlizzards?.size == 1 -> DIRECTION_TO_CHAR[localBlizzards.single().direction].toString()
        localBlizzards != null -> localBlizzards.size.digitToChar().toString()
        else -> "."
      }
    }
  }

  override fun toString(): String = draw()
}
