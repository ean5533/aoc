package day10

import kotlin.collections.ArrayDeque

private val classLoader: ClassLoader = object {}.javaClass.classLoader
private val input = classLoader.getResource("text/day10")!!.readText()

private val openToClose = mapOf(
    '(' to ')',
    '[' to ']',
    '{' to '}',
    '<' to '>'
)

private val corruptionPoints = mapOf(
    ')' to 3L,
    ']' to 57L,
    '}' to 1197L,
    '>' to 25137L
)

private val completionPoints = mapOf(
    ')' to 1L,
    ']' to 2L,
    '}' to 3L,
    '>' to 4L
)

fun main() {
    part1()
    part2()
}

fun part1() {
    val corruptionScore = input.lines().map(::analyze)
        .mapNotNull { it.invalidPart.firstOrNull() }
        .sumOf { corruptionPoints[it]!! }

    println("Part 1: corruption score $corruptionScore")
}

fun part2() {
    val completionScores = input.lines().map(::analyze)
        .filter { it.invalidPart.isEmpty() }
        .map { it.closersNeededToBalance }
        .map(::toCompletionScore)
        .sorted()

    val middleScore = completionScores[completionScores.size / 2]

    println("Part 2: middle completion score $middleScore")
}

fun toCompletionScore(it: String): Long {
    return it.fold(0L) { total, nextChar ->
        total * 5 + completionPoints[nextChar]!!
    }
}

private fun analyze(line: String): Analysis {
    val closersExpected = ArrayDeque<Char>()

    val validPart = line.takeWhile { char ->
        val closer = openToClose[char]

        if (closer != null) {
            // Found an opener
            closersExpected.push(closer)
            true
        } else if (closersExpected.peek() != char) {
            // Found an unexpected closer
            false
        } else {
            // Found the expected closer
            closersExpected.pop()
            true
        }
    }

    val invalidPart = line.removePrefix(validPart)

    return Analysis(validPart, invalidPart, closersExpected.asReversed().joinToString(""))
}

data class Analysis(val validPart: String, val invalidPart: String, val closersNeededToBalance: String)

fun <T> ArrayDeque<T>.push(element: T): Unit = addLast(element)
fun <T> ArrayDeque<T>.pop(): T? = removeLastOrNull()
fun <T> ArrayDeque<T>.peek(): T? = lastOrNull()