package aoc2021.day6

import lib.loadResourceMatchingPackageName

private val input = loadResourceMatchingPackageName(object {}.javaClass, "text/")

// A mapping of TIME_UNTIL_SPAWN->NUMBER_OF_FISH
typealias CountBySpawnTime = Map<Int, Long>

fun main() {
    val initialCounts = input.split(",").map(String::toInt).groupBy { it }.mapValues { it.value.size.toLong() }

    calculateAndPrintSizeAfter(initialCounts, 80)
    calculateAndPrintSizeAfter(initialCounts, 256)
}

private fun calculateAndPrintSizeAfter(initialCounts: CountBySpawnTime, days: Int) {
    val finalCounts = (0 until days).fold(initialCounts) { it, _ -> age1Day(it) }
    println("Fish after $days days ${finalCounts.values.sum()}")
}

private fun age1Day(counts: CountBySpawnTime): CountBySpawnTime {
    return mapOf(
        0 to counts.getOrDefault(1, 0),
        1 to counts.getOrDefault(2, 0),
        2 to counts.getOrDefault(3, 0),
        3 to counts.getOrDefault(4, 0),
        4 to counts.getOrDefault(5, 0),
        5 to counts.getOrDefault(6, 0),
        6 to counts.getOrDefault(7, 0) + counts.getOrDefault(0, 0), // including fish that just finished spawning
        7 to counts.getOrDefault(8, 0),
        8 to counts.getOrDefault(0, 0)                              // fish that just spawned
    )
}
