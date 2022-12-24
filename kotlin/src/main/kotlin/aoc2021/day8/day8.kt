package aoc2021.day8

import lib.intersectAll
import lib.loadResourceMatchingPackageName

private val input = loadResourceMatchingPackageName(object {}.javaClass)

private val lettersToDigit = mapOf(
    "abcefg".toSet() to 0,
    "cf".toSet() to 1,
    "acdeg".toSet() to 2,
    "acdfg".toSet() to 3,
    "bcdf".toSet() to 4,
    "abdfg".toSet() to 5,
    "abdefg".toSet() to 6,
    "acf".toSet() to 7,
    "abcdefg".toSet() to 8,
    "abcdfg".toSet() to 9,
)

fun main() {
    val displays = input.lines().map { line ->
        val (uniqueSignals, outputSignals) = line.split("|").map { it.trim().split(" ") }
        Display(uniqueSignals.toSet(), outputSignals)
    }

    part1(displays)
    part2(displays)
}

private fun part1(displays: List<Display>) {
    val uniqueDigitLengths = listOf(2, 4, 3, 7)
    val countOf1478 =
        displays.sumOf { display -> display.outputSignals.count { signal -> uniqueDigitLengths.contains(signal.length) } }

    println("The total count of of 1s, 4s, 7s, and 8s in all displays is $countOf1478")
}

private fun part2(displays: List<Display>) {
    val sum = displays.sumOf { display ->
        val translationKey = getTranslationKey(display.uniqueSignals)
        fun translateLetters(letters: Set<Char>) = letters.map { translationKey[it]!! }.toSet()

        display.outputSignals
            .map { translateLetters(it.toSet()) }
            .map { lettersToDigit[it]!! }
            .joinToString("")
            .toInt()
    }

    println("The sum of all displays is $sum")
}

/**
 * Alright, look. Here's the deal. I didn't want to brute force this because it felt cheap; I wanted to use actual deduction.
 * I'm pretty confident there are better ways to deduce the answer, but I came up with this on my own, and I'm happy with it.
 *
 * We could model all of this logic using bitwise operations for improved performance, but it would be far harder to read IMO.
 */
private fun getTranslationKey(uniqueSignals: Set<String>): Map<Char, Char> {
    val one = uniqueSignals.single { it.length == 2 }.toSet()
    val four = uniqueSignals.single { it.length == 4 }.toSet()
    val seven = uniqueSignals.single { it.length == 3 }.toSet()
    val eight = uniqueSignals.single { it.length == 7 }.toSet()

    val twoThreeFive = uniqueSignals.filter { it.length == 5 }.map { it.toSet() }.intersectAll()
    val zeroSixNine = uniqueSignals.filter { it.length == 6 }.map { it.toSet() }.intersectAll()

    // The top segment is the one contained by 7 and not 1
    val topLetter = seven.minus(one).single()
    // The middle segment is the one contained by all of [2, 3, 4, 5]
    val middleLetter = twoThreeFive.intersect(four).single()
    // The bottom segment is the one contained by all of [2, 3, 5] and which isn't the top or middle segment
    val bottomLetter = twoThreeFive.minus(listOf(topLetter, middleLetter)).single()
    // The rest of these explanations are left as an exercise for the reader
    val bottomRightLetter = zeroSixNine.intersect(seven).minus(topLetter).single()
    val topRightLetter = one.minus(bottomRightLetter).single()
    val topLeftLetter = four.minus(listOf(middleLetter, topRightLetter, bottomRightLetter)).single()
    val bottomLeftLetter = eight
        .minus(listOf(topLetter, middleLetter, bottomLetter, topRightLetter, bottomRightLetter, topLeftLetter))
        .single()

    return mapOf(
        topLetter to 'a',
        topLeftLetter to 'b',
        topRightLetter to 'c',
        middleLetter to 'd',
        bottomLeftLetter to 'e',
        bottomRightLetter to 'f',
        bottomLetter to 'g',
    )
}

private data class Display(val uniqueSignals: Set<String>, val outputSignals: List<String>)
