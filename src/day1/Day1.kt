package day1

fun main() {
    val increases = input
        .zipWithNext { a, b -> if (b > a) 1 else 0 }
        .sum()
    println(increases)
}