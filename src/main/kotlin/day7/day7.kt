package day7

import kotlin.math.abs

private val classLoader: ClassLoader = object {}.javaClass.classLoader
private val input = classLoader.getResource("text/day7")!!.readText()

fun main() {
    val numbers = input.split(",").map(String::toLong)

    // it seems like we should be able to restrict the search space to something relative to the average (?),
    // but in my test cases the average was nowhere near the correct answer ¯\_(ツ)_/¯
    val range = (numbers.minOrNull()!!..numbers.maxOrNull()!!)

    printCheapest(range, numbers) { x, y -> abs(x - y) }
    printCheapest(range, numbers) { x, y ->
        val distance = abs(x - y)
        (distance * distance + distance) / 2 // sum(1..n) = (n^2 + n) / 2
    }
}

private fun printCheapest(
    range: LongRange, numbers: List<Long>,
    costFunction: (Long, Long) -> Long
) {
    val (cheapestDestination, cost) = getCheapestDestination(range, numbers, costFunction)
    println("Cheapest destination is $cheapestDestination at a cost of $cost")
}

private fun getCheapestDestination(
    potentialDestinations: LongRange,
    startingPoints: List<Long>,
    costFunction: (Long, Long) -> Long
) = potentialDestinations
    .map { destination ->
        val totalCost = startingPoints.sumOf { costFunction(it, destination) }
        Pair(destination, totalCost)
    }
    .minByOrNull { (_, cost) -> cost }!!