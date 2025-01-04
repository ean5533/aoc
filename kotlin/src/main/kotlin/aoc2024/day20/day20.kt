package aoc2024.day20

import lib.*

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
private val track = parseInput()

fun main() {
  val path = generateSequence(track.car to track.car) { (previous, current) ->
    val dest = current.neighbors4().filter { it != previous && !track.walls.contains(it) }
    if (dest.isEmpty()) null else current to dest.single()
  }.takeWhile { it.first != track.destination }.toList()
  val timeWithoutCheat = path.size - 1
  println("Without cheating: $timeWithoutCheat")
  
  part1(path)
  part2(path)
}

private fun part1(pathTimes: List<Pair<Point2D, Point2D>>) {
  printTimeTaken {
    val timeWithoutCheat = pathTimes.size - 1
    val timeMap = pathTimes.reversed().mapIndexed { index, (_, position) -> position to index }.toMap()

    // For every wall, find out how far you could get if you phased through it
    val cheats = track.walls.mapNotNull { wall ->
      val neighbors = wall.neighbors4()
        .filter { track.area.contains(it) && !track.walls.contains(it) }
        .sortedByDescending { timeMap[it] }
      if (neighbors.size < 2) {
        return@mapNotNull null
      }

      val source = neighbors.first()
      val destination = neighbors.last()

      val timeWithCheat = (timeWithoutCheat - timeMap[source]!!) + timeMap[destination]!! + 2
      timeWithCheat
    }

    // Count the ones where the time saved was at least 100
    println(cheats.map { timeWithoutCheat - it }.count { it >= 100 })
  }
}

private fun part2(path: List<Pair<Point2D, Point2D>>) {
  printTimeTaken {
    val timeWithoutCheat = path.size - 1
    val goalTime = timeWithoutCheat - 100
    val timeMap = path.reversed().mapIndexed { index, (_, position) -> position to index }.toMap()
    
    // For every point on the path, count all other points on the path <=20 spaces away and find out how much time would be saved by phasing to them.
    // Don't bother checking any path points that are already within 100 of the goal
    // This solution would also work for part 1, but it's slower.
    val fasterRoutes = timeMap.entries.filter { it.value > 100 }.sumOf { (source, timeFromSource) ->
      val timeToSource = timeWithoutCheat - timeFromSource
      timeMap.keys
        .filter { source.manhattanDistanceTo(it) <= 20 }
        .map { timeToSource + source.manhattanDistanceTo(it) + timeMap[it]!! }
        .count { it <= goalTime }
    }
    println(fasterRoutes)
  }
}


private fun parseInput(): Track {
  val lines = input.lines()

  fun findAll(target: Char): Set<Point2D> =
    lines.flatMapIndexed { y, line -> line.toList().indexesOf(target).map { Point2D(it, y) } }.toSet()

  val track = Track(
    Area2D(lines[0].length, lines.size),
    findAll('#'),
    findAll('S').single(),
    findAll('E').single(),
  )

  return track
}

private data class Track(
  val area: Area2D,
  val walls: Set<Point2D>,
  val car: Point2D,
  val destination: Point2D,
) {
  override fun toString(): String {
    return area.yRange.joinToString("\n") { y ->
      area.xRange.joinToString("") { x ->
        val point = Point2D(x, y)
        when {
          walls.contains(point) -> "#"
          car == point -> "@"
          destination == point -> "E"
          else -> "."
        }
      }
    }
  }
}
