package aoc2022.day8

import lib.loadResourceAsString
import lib.takeWhileInclusive
import lib.transpose

private val input = loadResourceAsString("text/aoc2022/day8").trim()
private val grid = input.lines().map { it.toList().map { Tree(it.digitToInt()) } }

fun main() {
    grid.forEach { process(it) }
    grid.transpose().forEach { process(it) }

    println(grid.joinToString("\n") { it.toString() })
    println("part1: ${grid.sumOf { it.count { it.visible } }}")
    println("part2: ${grid.maxOf { it.maxOf { it.scenicScore } }}")
}

private fun process(trees: List<Tree>) {
    markTallestVisibleFromLeft(trees)
    markTallestVisibleFromLeft(trees.reversed())

    // Seems like there should be a way to reduce the set of trees to check...
    (0 until trees.size).forEach {
        updateScenicScoreFromLeft(trees, it)
        updateScenicScoreFromLeft(trees.reversed(), trees.size - it - 1)
    }
}

fun markTallestVisibleFromLeft(trees: List<Tree>) {
    val tallest = trees.maxOfOrNull { it.height } ?: return
    val indexOfTallest = trees.indexOfFirst { it.height == tallest }
    trees[indexOfTallest].visible = true
    markTallestVisibleFromLeft(trees.slice(0 until indexOfTallest))
}

fun updateScenicScoreFromLeft(trees: List<Tree>, index: Int) {
    val tallest = trees[index].height
    val leftScore = trees.slice(0 until index).reversed().takeWhileInclusive { it.height < tallest }.count()
    trees[index].scenicScore *= leftScore
}

class Tree(val height: Int, var visible: Boolean = false, var scenicScore: Long = 1) {
    override fun toString(): String {
        return "$height" + (if (visible) "T" else "F") + scenicScore.toString().padStart(5, '0')
    }
}
