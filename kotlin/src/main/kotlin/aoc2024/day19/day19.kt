package aoc2024.day19

import lib.*
import java.util.PriorityQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
private val options = input.lines().first().split(", ")
private val designs = input.lines().drop(2)

fun main() {
  printTimeTaken {
    part1()
  }
  printTimeTaken {
    part2()
  }
}

private fun part1() {
  val designSolutions = designs.associateWith { target ->
    val seen = mutableSetOf<String>()
    val queue = PriorityQueue<String>(compareByDescending { it.length }).also { it.addAll(options) }
    while (queue.any()) {
      val candidate = queue.poll()
      if (target == candidate) {
        return@associateWith candidate
      }
      seen += candidate
      if (target.startsWith(candidate)) queue.addAll(options.map { candidate + it }.filterNot { seen.contains(it) })
    }
    null
  }

  println(designSolutions.filter { it.value != null }.size)
}

private fun part2() {
  val designSolutions = designs.associateWith { target ->
    val seen = mutableMapOf<String, AtomicLong>()
    val queue = PriorityQueue<String>(compareBy { it.length }).also { it.add("") }
    while (queue.any()) {
      val candidate = queue.poll()
      val waysHere = seen.computeIfAbsent(candidate) { AtomicLong(1) }.get()
      options
        .map { candidate + it }
        .filter { target.startsWith(it) }
        .forEach {
          val seenBefore = seen.computeIfAbsent(it) { AtomicLong(0) }.getAndAdd(waysHere)
          if(seenBefore == 0L) queue.add(it)
        }
    }
    
    seen[target]?.get() ?: 0
  }

  println(designSolutions.values.sum())
}
