package aoc2024.day20

import lib.*

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
private val track = parseInput()

fun main() {
  printTimeTaken {
    val path = generateSequence(track.car to track.car) { (previous, current) ->
      val dest = current.neighbors4().filter { it != previous && !track.walls.contains(it) }
      if(dest.isEmpty()) null else current to dest.single()
    }.takeWhile { it.first != track.destination }.toList()
    val timeWithoutCheat = path.size - 1
    println("Without cheating: $timeWithoutCheat")
    
    val timeMap = path.reversed().mapIndexed { index, (_, position) -> position to index }.toMap()
//    println(timeMap)
    
    val cheats = track.walls.mapNotNull { wall ->
      val neighbors = wall.neighbors4()
        .filter { track.area.contains(it) && !track.walls.contains(it) }
        .sortedByDescending { timeMap[it] }
      if(neighbors.size < 2) {
        return@mapNotNull null
      }
      
      val source = neighbors.first()
      val destination = neighbors.last()
      
      val timeWithCheat = (timeWithoutCheat - timeMap[source]!!) + timeMap[destination]!! + 2
      timeWithCheat
    }
    
//    println(cheats)
//    println(cheats.map { timeWithoutCheat - it }.groupingBy { it }.eachCount().toSortedMap())
    println(cheats.map { timeWithoutCheat - it }.count { it >= 100 })
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
