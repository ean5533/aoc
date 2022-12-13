package aoc2022.day13

import com.google.gson.Gson
import lib.loadResourceAsString
import lib.pair
import lib.takeWhileInclusive

fun main() {
    val packetPairs = parseInput()

    val orderedPairIndices = packetPairs.mapIndexedNotNull { index, (first, second) ->
        if (PacketComparator.compare(first, second) <= 0) index + 1 else null
    }
    println("Part 1: " + orderedPairIndices.sum())

    val dividerPackets = listOf(listOf(listOf(2.0)), listOf(listOf(6.0)))
    val sorted = (packetPairs.flatMap { it.toList() } + dividerPackets).sortedWith(PacketComparator)
    val dividerIndices = dividerPackets.map { sorted.indexOf(it) + 1 }
    println("Part 2: " + dividerIndices.reduce { a, b -> a * b })
}

private object PacketComparator : Comparator<Any> {
    override fun compare(first: Any, second: Any): Int {
        return when {
            first is Double && second is Double -> first.compareTo(second)
            first is Double && second is List<*> -> compare(listOf(first), second)
            first is List<*> && second is Double -> compare(first, listOf(second))
            first is List<*> && second is List<*> -> {
                val zipComparison = first.filterNotNull().zip(second.filterNotNull())
                    .map { compare(it.first, it.second) }
                    .takeWhileInclusive { it == 0 }.lastOrNull() ?: 0
                if (zipComparison != 0) zipComparison else first.size.compareTo(second.size)
            }

            else -> throw IllegalStateException()
        }
    }
}

private fun parseInput(): List<Pair<List<*>, List<*>>> {
    return loadResourceAsString("text/aoc2022/day13").trim().lines().windowed(3, 3, true).map {
        it.take(2).map { Gson().fromJson(it, List::class.java) }.pair()
    }
}
