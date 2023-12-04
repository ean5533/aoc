package aoc2023.day4

import lib.loadResourceMatchingPackageName
import lib.printTimeTaken
import java.lang.Math.pow

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
private val cards = input.lines().associate {
  val (id, rest) = it.drop(5).split(":")
  val (winning, held) = rest.split('|').map { it.split(" ").mapNotNull { it.toIntOrNull() }.toSet() }
  id.trim().toInt() to Card(winning, held)
}

fun main() {  
  part1()
  printTimeTaken { part2Mutable() }
  printTimeTaken { part2Immutable() }
}

private fun part1() {
  println(cards.values.sumOf { it.points() })
}

private fun part2Mutable() {
  val cards = cards.toMutableMap()

  cards.keys.forEach { id ->
    val card = cards[id]!!
    val nextIds = (1..card.matchCount()).map { it + id }.filter { it <= cards.size }
    nextIds.forEach { cards[it] = cards[it]!!.let { it.copy(multiplier = it.multiplier + card.multiplier) } }
  }

  println(cards.values.sumOf { it.multiplier })
}

private fun part2Immutable() {
  val newCards = cards.keys.fold(cards) { cards, id ->
    val card = cards[id]!!
    val nextIds = (1..card.matchCount()).map { it + id }.filter { it <= cards.size }
    cards + nextIds.map { next -> cards[next]!!.let { next to it.copy(multiplier = it.multiplier + card.multiplier) } }
  }

  println(newCards.values.sumOf { it.multiplier })
}

private data class Card(val winningNumbers: Set<Int>, val heldNumbers: Set<Int>, val multiplier: Int = 1) {
  fun points(): Long = pow(2.0, matchCount().toDouble() - 1).toLong()
  fun matchCount() = winningNumbers.intersect(heldNumbers).size
}
