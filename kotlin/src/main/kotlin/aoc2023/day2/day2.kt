package aoc2023.day2

import lib.loadResourceMatchingPackageName

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
private val games = input.lines().map {
  val (id, details) = it.drop(5).split(": ")
  val cubes = details.trim().split(";").map {
    it.trim().split(",").associate {
      val (amount, color) = it.trim().split(" ")
      color to amount.toInt()
    }
  }
  Game(id.toInt(), cubes)
}


fun main() {
  part1()
  part2()
}

private fun part1() {
  val maxes = mapOf("red" to 12, "green" to 13, "blue" to 14)
  val validGames =
    games.filter { it.rounds.none { round -> maxes.any { (color, maxAmount) -> (round[color] ?: 0) > maxAmount } } }
  println(validGames.sumOf { it.id })
}

private fun part2() {
  val gameMins = games.map { game ->
    game.rounds.fold(mapOf("red" to 0, "green" to 0, "blue" to 0)) { roundMins, round ->
      roundMins.map { (color, value) -> color to Math.max(value, round[color] ?: 0) }.toMap()
    }
  }

  val powers = gameMins.map { it.values.reduce { acc, i -> acc * i } }
  println(powers.sum())
}

private data class Game(val id: Int, val rounds: List<Map<String, Int>>)
