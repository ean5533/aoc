package day1

fun main() {
    part1()
    part2()
}

fun part1() {
    val increases = input
        .zipWithNext { a, b -> if (b > a) 1 else 0 }
        .sum()
    println(increases)
}

fun part2() {
    val increases = input
        .windowed(3, 1)
        .map { it.sum() }
        .zipWithNext { a, b -> if (b > a) 1 else 0 }
        .sum()
    println(increases)
}