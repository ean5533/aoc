package day1

private val classLoader: ClassLoader = object {}.javaClass.classLoader
private val input = classLoader.getResource("text/day1")!!.readText()
private val numbers: List<Int> = input.lines().map { it.trim().toInt() }

fun main() {
    part1()
    part2()
}

fun part1() {
    val increases = numbers
        .zipWithNext { a, b -> if (b > a) 1 else 0 }
        .sum()
    println(increases)
}

fun part2() {
    val increases = numbers
        .windowed(3, 1)
        .map { it.sum() }
        .zipWithNext { a, b -> if (b > a) 1 else 0 }
        .sum()
    println(increases)
}