package aoc2023.day3

import lib.Line2D
import lib.Point2D
import lib.loadResourceMatchingPackageName
import lib.pair

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
private val board = Board.parse(input)

fun main() {
  part1()
  part2()
}

private fun part1() {
  val validNumbers = board.numbers.filter { number ->
    board.symbols.any { symbol -> symbol.point.adjacentToDiagonally(number.line) }
  }
  println(validNumbers.sumOf { it.value })
}

private fun part2() {
  val gears = board.symbols.mapNotNull { symbol ->
    val numbers = board.numbers.filter { number -> symbol.point.adjacentToDiagonally(number.line) }
    if (numbers.size == 2) Gear(symbol, numbers.pair()) else null
  }
  println(gears.sumOf { it.numbers.first.value * it.numbers.second.value })
}

private data class Board(val numbers: List<Number>, val symbols: List<Symbol>) {
  companion object {
    fun parse(input: String): Board {
      val lines = input.lines()
      val symbols = lines.flatMapIndexed { y, line ->
        line.mapIndexedNotNull { x, character ->
          if (!character.isDigit() && character != '.') Symbol(character, Point2D(x, y)) else null
        }
      }

      // This is pretty gross, but I don't care enough to make it better
      val numbers = lines.flatMapIndexed { y, line ->
        var i = 0
        val numbers = mutableListOf<Number>()

        while (i < line.length) {
          if (line[i].isDigit()) {
            var digits = listOf<Char>()
            while (i < line.length && line[i].isDigit()) digits += line[i++]

            val value = digits.joinToString("").toInt()
            val points = Point2D(i - digits.size, y)..Point2D(i - 1, y)
            numbers += Number(value, points)
          } else {
            i++
          }
        }

        numbers
      }

      return Board(numbers, symbols)
    }
  }
}

private data class Number(val value: Int, val line: Line2D)
private data class Symbol(val value: Char, val point: Point2D)
private data class Gear(val symbol: Symbol, val numbers: Pair<Number, Number>)
