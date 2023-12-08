package aoc2023.day8

import lib.lcm
import lib.loadResourceMatchingPackageName
import lib.repeat
import lib.takeWhileInclusive

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
private val instructions = input.lines()[0].asSequence().repeat()
private val map = input.lines().drop(2).associate {
  val (source, rest) = it.split(" = ")
  val (left, right) = rest.trim('(', ')').split(", ")
  source to Transition(source, left, right)
}

fun main() {
  part1()
  part2()
}

private fun part1() {
  val startGhosts = listOf(Ghost("AAA"))
  val endGhosts = findTargets(startGhosts)
  val stepsNeeded = endGhosts.map { it.cycleLength!!.toLong() }.lcm()
  println(stepsNeeded)
}

private fun part2() {
  val startGhosts = map.keys.filter { it.endsWith("A") }.map { Ghost(it) }
  val endGhosts = findTargets(startGhosts)
  val stepsNeeded = endGhosts.map { it.cycleLength!!.toLong() }.lcm()
  println(stepsNeeded)
}

private fun findTargets(initialGhosts: List<Ghost>): List<Ghost> {
  return instructions.runningFold(initialGhosts) { ghosts, direction ->
    ghosts.map {
      val next = map[it.current]!!.move(direction)
      it.stepTo(next)
    }
  }
    .takeWhileInclusive { it.any { it.cycleLength == null } }
    .last()
}

private data class Transition(val source: String, val left: String, val right: String) {
  fun move(direction: Char): String = when (direction) {
    'R' -> right
    'L' -> left
    else -> throw IllegalStateException("Illegal direction $direction")
  }
}

private data class Ghost(val current: String, val stepsSinceGoal: Int? = null, val cycleLength: Int? = null) {
  fun stepTo(next: String): Ghost {
    val newStepsSinceGoal = when {
      stepsSinceGoal != null -> stepsSinceGoal + 1
      next.endsWith("Z") -> 0
      else -> null
    }

    val newCycleLength = when {
      cycleLength != null -> cycleLength
      next.endsWith("Z") && (newStepsSinceGoal ?: 0) > 0 -> newStepsSinceGoal
      else -> null
    }

    return copy(
      current = next,
      stepsSinceGoal = newStepsSinceGoal,
      cycleLength = newCycleLength
    )
  }
}
