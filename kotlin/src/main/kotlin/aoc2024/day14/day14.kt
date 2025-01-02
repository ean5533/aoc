package aoc2024.day14

import lib.*

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
private val area = input.lines()[0].split(",").map { it.toInt() }.let { Area2D(it[0], it[1]) }
private val robots = input.lines().drop(1).map {
  val (position, velocity) = it.split(" ").map { it.split("=")[1].split(",").map { it.toInt() }.toPoint2D() }
  Robot(position, velocity)
}

fun main() {
  part1()
  part2()
}

private fun part1() {
  val newRobots = robots.map { (0..<100).fold(it) { robot, _ -> robot.next() } }
  val nw = newRobots.filter { it.position.x + 1 < area.width / 2.0 && it.position.y + 1 < area.height / 2.0 }
  val ne = newRobots.filter { it.position.x > area.width / 2.0 && it.position.y + 1 < area.height / 2.0 }
  val sw = newRobots.filter { it.position.x + 1 < area.width / 2.0 && it.position.y > area.height / 2.0 }
  val se = newRobots.filter { it.position.x > area.width / 2.0 && it.position.y > area.height / 2.0 }
  println(nw.size * ne.size * sw.size * se.size)
}

fun part2() {
  var bots = robots
  var seconds = 0
  while (true) {
    bots = bots.map { it.next() }
    seconds++

    // I heard a rumor that the christmas tree shows up when all the bots are in unique positions
    val positions = bots.map { it.position }
    if (positions.toSet().size == positions.size) {
      break
    }
  }

  println("Maybe at $seconds?")
}

private data class Robot(val position: Point2D, val velocity: Point2D) {
  fun next() = copy(position = position.shiftWithin(velocity, area))
}
