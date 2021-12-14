package day14

private val classLoader: ClassLoader = object {}.javaClass.classLoader
private val input = classLoader.getResource("text/day14")!!.readText()


fun main() {
    val (template, rules) = parseInput()

    part1(template, rules)
    part2(template, rules)
}

fun parseInput(): Pair<String, Map<String, String>> {
    val template = input.lines()[0]
    val rules = input.lines().drop(2).map { it.split(" -> ") }.associate { (a, b) -> a to b }
    return Pair(template, rules)
}

fun part1(template: String, rules: Map<String, String>) {
    val pairCounts = applyRulesAndGetPairCounts(template, 10, rules)
    val delta = getMostVsLeastDelta(pairCounts)
    println("Part 1: $delta")
}

fun part2(template: String, rules: Map<String, String>) {
    val pairCounts = applyRulesAndGetPairCounts(template, 40, rules)
    val delta = getMostVsLeastDelta(pairCounts)
    println("Part 2: $delta")
}

private fun applyRulesAndGetPairCounts(template: String, times: Int, rules: Map<String, String>): Map<String, Long> {
    val originalPairCounts = template.windowed(2).groupingBy { it }.eachCount().mapValues { it.value.toLong() }
    return (1..times).fold(originalPairCounts) { pairCounts, _ ->
        pairCounts
            .flatMap { (pair, count) ->
                val newPairs = rules[pair]?.let { listOf(pair[0] + it, it + pair[1]) } ?: listOf(pair)
                newPairs.map { it to count }
            }
            .groupingBy { it.first }
            .sumCounts()
    }
}

private fun getMostVsLeastDelta(newPairCounts: Map<String, Long>): Long {
    val letterCounts = newPairCounts
        .flatMap { (pair, count) -> listOf(pair[0] to count, pair[1] to count) }
        .groupingBy { it.first }
        .sumCounts()
    val max = letterCounts.maxOf { it.value } / 2
    val min = letterCounts.minOf { it.value } / 2
    return max - min + 1
}

fun <T> Grouping<Pair<T, Long>, T>.sumCounts(): Map<T, Long> {
    return fold(0L) { total, count -> total + count.second }
}