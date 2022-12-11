package aoc2022.day11

import lib.loadResourceAsString

private val worryModulator = parseMonkies().map { it.testDivisor }.reduce { a, b -> a * b }

fun main() {
    println("part1")
    exec(20, 3)
    println("part2")
    exec(10000, 1)
}

private fun exec(rounds: Int, worryDivisor: Int) {
    val monkeys = parseMonkies()

    (0 until rounds).forEach {
        monkeys.forEach {
            it.inspectAndThrow(worryDivisor).forEach { (item, destinationMonkey) ->
                monkeys[destinationMonkey].items.add(item)
            }
        }
    }

    println(monkeys.sortedByDescending { it.inspectCount }.joinToString("\n") { "${it.index}: ${it.inspectCount}" })
    println(monkeys.map { it.inspectCount }.sortedDescending().take(2).reduce { a, b -> a * b })
}

fun parseMonkies(): List<Monkey> {
    val iterator = loadResourceAsString("text/aoc2022/day11").trim().lines().iterator()

    return iterator.asSequence().mapNotNull {
        if (it.isEmpty()) return@mapNotNull null

        val index = it.split(" ")[1].trimEnd(':').toInt()
        val items = iterator.next().split(": ")[1].split(", ").map { Item(it.toLong()) }.toMutableList()
        val (operator, operationValue) = iterator.next().split("old ")[1].split(" ")
        val operation = when (operator) {
            "*" -> { value: Long -> value * (operationValue.toLongOrNull() ?: value) }
            "+" -> { value: Long -> value + (operationValue.toLongOrNull() ?: value) }
            else -> throw IllegalStateException()
        }
        val testDivisor = iterator.next().split(": ")[1].split(" ")[2].toLong()
        val testTrueMonkey = iterator.next().split(": ")[1].split(" ")[3].toInt()
        val testFalseMonkey = iterator.next().split(": ")[1].split(" ")[3].toInt()
        Monkey(index, items, operation, testDivisor, testTrueMonkey, testFalseMonkey)
    }.toList()
}

class Monkey(
    val index: Int,
    val items: MutableList<Item>,
    val operation: (Long) -> Long,
    val testDivisor: Long,
    val testTrueMonkey: Int,
    val testFalseMonkey: Int
) {
    var inspectCount = 0L
        private set

    fun inspectAndThrow(worryDivisor: Int): List<Pair<Item, Int>> {
        return items.map {
            val newWorry = (operation(it.worryLevel) / worryDivisor) % worryModulator
            val destinationMonkey = if (newWorry % testDivisor == 0L) testTrueMonkey else testFalseMonkey
            Item(newWorry) to destinationMonkey
        }.also {
            inspectCount += items.size
            items.clear()
        }
    }

}

data class Item(val worryLevel: Long)
