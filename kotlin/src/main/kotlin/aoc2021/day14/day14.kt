package aoc2021.day14

import lib.loadResourceMatchingPackageName
import lib.sumCounts

private val input = loadResourceMatchingPackageName(object {}.javaClass, "text/")


fun main() {
    val (template, rules) = parseInput()

    part1(template, rules)
    part2(template, rules)
}

private fun parseInput(): Pair<String, Map<String, String>> {
    val polymer = input.lines()[0]
    val rules = input.lines().drop(2).map { it.split(" -> ") }.associate { (a, b) -> a to b }
    return Pair(polymer, rules)
}

private fun part1(polymer: String, rules: Map<String, String>) {
    val delta = applyRulesAndMaxMinDelta(polymer, 10, rules)
    println("Part 1: $delta")
}

private fun part2(polymer: String, rules: Map<String, String>) {
    val delta = applyRulesAndMaxMinDelta(polymer, 40, rules)
    println("Part 2: $delta")
}

private fun applyRulesAndMaxMinDelta(polymer: String, times: Int, rules: Map<String, String>): Long {
    // Keep track of the counts of unique pairs instead of the whole polymer. Repeatedly turn each pair into a new pair
    // of pairs by applying transform rules, and sum up the counts of the new pairs.
    val originalPairCounts = polymer.windowed(2).groupingBy { it }.eachCount().mapValues { it.value.toLong() }
    val newPairCounts = (1..times).fold(originalPairCounts) { pairCounts, _ ->
        pairCounts
            .flatMap { (pair, count) -> rules[pair]!!.let { listOf(pair[0] + it, it + pair[1]) }.map { it to count } }
            .groupingBy { it.first }
            .sumCounts()
    }

    // Count the first letter of each pair. Add one extra for the second letter of the final pair (which is the last letter in the polymer)
    val letterCounts = newPairCounts
        .map { (pair, count) -> pair[0] to count }
        .plus(polymer.last() to 1L)
        .groupingBy { it.first }
        .sumCounts()

    return letterCounts.maxOf { it.value } - letterCounts.minOf { it.value }
}

