package aoc2022.day20

import lib.loadResourceMatchingPackageName

private val inputNumbers =
    loadResourceMatchingPackageName(object {}.javaClass, "text/").trim().lines().map { Value(it.toLong()) }

fun main() {
    part1()
    part2()
}

private fun part1() {
    val mixableNumbers = inputNumbers.toMutableList()
    mix(mixableNumbers, inputNumbers)
    println("part1: ${groveCoordinates(mixableNumbers).sum()}")
}

private fun part2() {
    val multipliedNumbers = inputNumbers.map { Value(it.value * 811589153) }
    val mixableNumbers = multipliedNumbers.toMutableList()
    repeat(10) { mix(mixableNumbers, multipliedNumbers) }
    println("part2: ${groveCoordinates(mixableNumbers).sum()}")
}

private fun mix(mixableNumbers: MutableList<Value>, originalNumbers: List<Value>) {
    originalNumbers.forEach {
        val curIndex = mixableNumbers.indexOf(it)
        val newIndex = (curIndex + it.value).mod(originalNumbers.size - 1)
        mixableNumbers.removeAt(curIndex)
        mixableNumbers.add(newIndex, it)
    }
}

private fun groveCoordinates(numbers: MutableList<Value>): List<Long> {
    val zeroIndex = numbers.indexOfFirst { it.value == 0L }
    return (1000..3000 step 1000).map { numbers[(zeroIndex + it).mod(inputNumbers.size)].value }
}

// An arbitrary box to contain values so I can find them later based on memory ref.
private class Value(val value: Long) {
    override fun toString(): String = value.toString()
}
