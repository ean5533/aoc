package aoc2022.day2

import lib.incrementInsideRange
import lib.loadResourceMatchingPackageName

private val input = loadResourceMatchingPackageName(object {}.javaClass, "text/").trim()

private const val ROCK = 1
private const val PAPER = 2
private const val SCISSORS = 3

private const val LOSE = 0
private const val DRAW = 3
private const val WIN = 6

fun main() {
    part1()
    part2()
}

private fun part1() {
    val mapping = mapOf(
        "A" to ROCK,
        "B" to PAPER,
        "C" to SCISSORS,
        "X" to ROCK,
        "Y" to PAPER,
        "Z" to SCISSORS,
    )

    val scores = input.trim().lines().map {
        val (them, us) = it.split(" ").mapNotNull { mapping[it] }
        victoryScore(them, us) + us
    }

    println("sum = ${scores.sum()}")
}

private fun part2() {
    val mapping = mapOf(
        "A" to ROCK,
        "B" to PAPER,
        "C" to SCISSORS,
        "X" to -1,
        "Y" to 0,
        "Z" to 1,
    )
    val ranking = listOf(ROCK, PAPER, SCISSORS)

    val scores = input.trim().lines().map {
        val (them, offset) = it.split(" ").mapNotNull { mapping[it] }
        val indexOfUs = ranking.indexOf(them).incrementInsideRange(0, 2, offset)
        val us = ranking.elementAt(indexOfUs)
        victoryScore(them, us) + us
    }

    println("sum2 = ${scores.sum()}")
}

private fun victoryScore(them: Int, us: Int): Int = when {
    them == us -> DRAW
    them.incrementInsideRange(1, 3) == us -> WIN
    else -> LOSE
}
