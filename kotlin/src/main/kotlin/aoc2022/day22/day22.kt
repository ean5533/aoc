package aoc2022.day22

import lib.*

fun main() {
  val (board, instructions) = parseInput()
  val start = State(board.start)

  println("part1: ${execute(instructions, start, board).score()}")
  println("part2: ${execute(instructions, start, board.copy(isCube = true)).score()}")
}

private fun execute(instructions: List<Instruction>, start: State, board: Board): State =
  instructions.fold(start) { current, instruction -> current.execute(instruction, board) }

private fun parseInput(): Pair<Board, List<Instruction>> {
  val lines = loadResourceMatchingPackageName(object {}.javaClass, "text/").trimEnd().lines()

  val board = lines.dropLast(2).flatMapIndexed { y, line ->
    line.mapIndexedNotNull { x, char ->
      when (char) {
        in listOf(' ', null) -> null
        else -> Point2D(x, y) to char
      }
    }
  }.let { Board(it.toMap()) }

  val path = lines.last().replace("L", " L ").replace("R", " R ").split(" ").map {
    when (it) {
      in listOf("L", "R") -> Turn(it.single())
      else -> Move(it.toInt())
    }
  }

  return board to path
}

private sealed interface Instruction
private data class Move(val amount: Int) : Instruction
private data class Turn(val direction: Char) : Instruction

private val RIGHT = Point2D(1, 0)
private val DOWN = Point2D(0, 1)
private val LEFT = Point2D(-1, 0)
private val UP = Point2D(0, -1)

private data class State(
  val position: Point2D,
  val facing: Point2D = Directions.first(),
  val path: List<Pair<Point2D, Point2D>> = listOf(position to facing), // only used for debugging
) {
  fun execute(instruction: Instruction, board: Board): State = when (instruction) {
    is Move -> {
      val steps = (1..instruction.amount).runningFold(position to facing) { (p, f), _ -> board.step(p, f) }
      val (newPosition, newFacing) = steps.last()
      copy(position = newPosition, facing = newFacing, path = path + steps)
    }
    is Turn -> {
      val currentDirectionIndex = Directions.indexOf(facing)
      val directionIndexModifier = if (instruction.direction == 'L') -1 else 1
      val newFacing = Directions[((currentDirectionIndex + directionIndexModifier).mod(Directions.size))]
      copy(facing = newFacing, path = path + (position to newFacing))
    }
  }

  fun score() = 1000 * (position.y + 1) + 4 * (position.x + 1) + Directions.indexOf(facing)

  companion object {
    private val Directions = listOf(RIGHT, DOWN, LEFT, UP)
  }
}

private data class Region(val number: Int, val area: Area2D) {
  val origin = area.origin()
}


private data class Board(val cells: Map<Point2D, Char>, val isCube: Boolean = false) {
  val width = cells.maxOf { it.key.x } + 1
  val height = cells.maxOf { it.key.y } + 1
  val start = cells.keys.filter { it.y == 0 }.minBy { it.x }
  val regionSize = (0 until height).minOf { y -> cells.keys.count { it.y == y } }

  fun step(from: Point2D, facing: Point2D): Pair<Point2D, Point2D> {
    val intendedTo = from + facing
    val (actualTo, newFacing) = if (cells.containsKey(intendedTo)) intendedTo to facing else wrap(from, facing)
    return when (cells[actualTo]) {
      '.' -> actualTo to newFacing
      '#' -> from to facing
      else -> throw IllegalStateException()
    }
  }

  private fun wrap(from: Point2D, facing: Point2D): Pair<Point2D, Point2D> =
    if (isCube) {
      val (newFacing, newPositionFn) = regionsToEdgeFunctions.entries.first { it.key.area.contains(from) }.value[facing]!!
      newPositionFn(from) to newFacing
    } else {
      when (facing) {
        RIGHT -> cells.keys.filter { it.y == from.y }.minBy { it.x }
        DOWN -> cells.keys.filter { it.x == from.x }.minBy { it.y }
        LEFT -> cells.keys.filter { it.y == from.y }.maxBy { it.x }
        UP -> cells.keys.filter { it.x == from.x }.maxBy { it.y }
        else -> throw IllegalStateException()
      }.let { it to facing }
    }

  override fun toString(): String = (0 until height).joinToString("\n") { y ->
    (0 until width).mapNotNull { x -> cells[Point2D(x, y)] ?: ' ' }.joinToString("")
  }

  private fun buildFunction(regions: Map<Int, Region>, a: Int, b: Int, fn: (Point2D) -> Point2D): (Point2D) -> Point2D =
    { (it - regions[a]!!.origin).let { fn(it) } + regions[b]!!.origin }

  private val originArea = Area2D(Point2D(0, 0), regionSize)
  private val regionsToEdgeFunctions: Map<Region, Map<Point2D, Pair<Point2D, (Point2D) -> Point2D>>> by lazy {
    // Cheat cheat cheat cheat. Not sure how to generate this programmatically yet.
    if (regionSize == 4) {
      val regions = listOf(
        Region(1, Area2D(Point2D(2, 0), regionSize)),
        Region(2, Area2D(Point2D(0, 1), regionSize)),
        Region(3, Area2D(Point2D(1, 1), regionSize)),
        Region(4, Area2D(Point2D(2, 1), regionSize)),
        Region(5, Area2D(Point2D(2, 2), regionSize)),
        Region(6, Area2D(Point2D(3, 2), regionSize)),
      ).associateBy { it.number }
      regions.values.associate { region ->
        when (region.number) {
          1 -> region to mapOf(
            RIGHT to (LEFT to buildFunction(regions, 1, 6) { it.flipY(originArea) }),
            LEFT to (DOWN to buildFunction(regions, 1, 3) { it.transpose() }),
            UP to (DOWN to buildFunction(regions, 1, 2) { it.flipX(originArea) }),
          )
          2 -> region to mapOf(
            DOWN to (UP to buildFunction(regions, 2, 5) { it.flipX(originArea) }),
            LEFT to (UP to buildFunction(regions, 2, 6) { it.transpose() }),
            UP to (DOWN to buildFunction(regions, 2, 1) { it.flipX(originArea) }),
          )
          3 -> region to mapOf(
            DOWN to (RIGHT to buildFunction(regions, 3, 5) { it.transpose() }),
            UP to (RIGHT to buildFunction(regions, 3, 1) { it.transpose() }),
          )
          4 -> region to mapOf(
            RIGHT to (DOWN to buildFunction(regions, 4, 6) { it.antiTranspose(originArea) }),
          )
          5 -> region to mapOf(
            LEFT to (UP to buildFunction(regions, 5, 3) { it.transpose() }),
            DOWN to (UP to buildFunction(regions, 5, 2) { it.flipX(originArea) })
          )
          6 -> region to mapOf(
            RIGHT to (LEFT to buildFunction(regions, 6, 1) { it.flipY(originArea) }),
            DOWN to (RIGHT to buildFunction(regions, 6, 2) { it.transpose() }),
            UP to (LEFT to buildFunction(regions, 6, 4) { it.transpose() })
          )
          else -> throw IllegalStateException()
        }
      }
    } else {
      val regions = listOf(
        Region(1, Area2D(Point2D(1, 0), regionSize)),
        Region(2, Area2D(Point2D(2, 0), regionSize)),
        Region(3, Area2D(Point2D(1, 1), regionSize)),
        Region(4, Area2D(Point2D(0, 2), regionSize)),
        Region(5, Area2D(Point2D(1, 2), regionSize)),
        Region(6, Area2D(Point2D(0, 3), regionSize)),
      ).associateBy { it.number }
      regions.values.associate { region ->
        when (region.number) {
          1 -> region to mapOf(
            LEFT to (RIGHT to buildFunction(regions, 1, 4) { it.flipY(originArea) }),
            UP to (RIGHT to buildFunction(regions, 1, 6) { it.transpose() }),
          )
          2 -> region to mapOf(
            RIGHT to (LEFT to buildFunction(regions, 2, 5) { it.flipY(originArea) }),
            DOWN to (LEFT to buildFunction(regions, 2, 3) { it.transpose() }),
            UP to (UP to buildFunction(regions, 2, 6) { it.flipY(originArea) }),
          )
          3 -> region to mapOf(
            RIGHT to (UP to buildFunction(regions, 3, 2) { it.transpose() }),
            LEFT to (DOWN to buildFunction(regions, 3, 4) { it.transpose() }),
          )
          4 -> region to mapOf(
            LEFT to (RIGHT to buildFunction(regions, 4, 1) { it.flipY(originArea) }),
            UP to (RIGHT to buildFunction(regions, 4, 3) { it.transpose() }),
          )
          5 -> region to mapOf(
            RIGHT to (LEFT to buildFunction(regions, 5, 2) { it.flipY(originArea) }),
            DOWN to (LEFT to buildFunction(regions, 5, 6) { it.transpose() }),
          )
          6 -> region to mapOf(
            RIGHT to (UP to buildFunction(regions, 6, 5) { it.transpose() }),
            DOWN to (DOWN to buildFunction(regions, 6, 2) { it.flipY(originArea) }),
            LEFT to (DOWN to buildFunction(regions, 6, 1) { it.transpose() }),
          )
          else -> throw IllegalStateException()
        }
      }
    }
  }
}
