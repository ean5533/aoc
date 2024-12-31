package aoc2024.day9

import lib.*

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()

fun main() {
  part1()
  part2()
}

private fun parseInput(): Memory {
  var currentPosition = 0
  val filePositions = input.toList()
    .mapIndexedNotNull { index, c ->
      val size = c.digitToInt()
      val result = if (index % 2 == 0) {
        File(index / 2L, size) to currentPosition
      } else null

      currentPosition += size

      result
    }.toMap()

  return Memory(filePositions)
}

fun part1() {
  val memory = parseInput()
  val rawMemory = memory.raw
  println(rawMemory.joinToString("") { it?.toString() ?: "." })

  var start = 0
  var end = rawMemory.size - 1

  while (true) {
    while (start < end && rawMemory[start] != null) start++
    if (start >= end) break

    while (start < end && rawMemory[end] == null) end--

    rawMemory[start] = rawMemory[end]
    rawMemory[end] = null
  }

  println(rawMemory.joinToString("") { it?.toString() ?: "." })

  val checksum = rawMemory.toList().mapIndexedNotNull { index, id -> id?.let { it * index } }.sum()
  println(checksum)
}

fun part2() {
  val memory = parseInput()
  val rawMemory = memory.raw
  println(rawMemory.joinToString("") { it?.toString() ?: "." })

  memory.filePositions.entries.sortedByDescending { it.key.id }.forEach { (file, position) ->
    var current = 0
    while (current < position - 1) {

      while (current < position - 1 && memory.raw[current] != null) {
        current++
      }

      if ((current..<current + file.size).all { memory.raw[it] == null }) {
        (current..<current + file.size).forEach { memory.raw[it] = file.id }
        (position..<position + file.size).forEach { memory.raw[it] = null }
        break
      }
      
      current++
    }
  }

  println(rawMemory.joinToString("") { it?.toString() ?: "." })
  val checksum = rawMemory.toList().mapIndexedNotNull { index, id -> id?.let { it * index } }.sum()
  println(checksum)
}

private class Memory(val filePositions: Map<File, Int>) {
  val raw: Array<Long?>

  init {
    val slots = filePositions.entries.maxOf { it.value + it.key.size }
    raw = Array(slots) { null }

    filePositions.forEach { (id, size), start ->
      (start..<start + size).forEach { raw[it] = id }
    }


  }
}

private data class File(val id: Long, val size: Int)
