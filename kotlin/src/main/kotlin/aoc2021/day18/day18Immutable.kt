package aoc2021.day18

import lib.checkEquals
import lib.loadResourceAsString

private val input = loadResourceAsString("text/aoc2021/day18")


fun main() {
    val snailfishies = Day18Immutable.parseInput()

    Day18Immutable.part1(snailfishies)
    Day18Immutable.part2()
}

/**
 * An attempt to solve day 18 using immutable data structures. Becomes completely untenable when trying to figure out
 * how to model the explode() operation.
 */
object Day18Immutable {
    fun part1(snailfishies: List<Snailfish>) {
        val poppop = snailfishies.reduce { a, b -> a.plus(b).reduce() }.magnitude()
        println("Part 1: $poppop")
    }

    fun part2() {
        println("Part 2: ")
    }

    fun parseInput(): List<Snailfish> {
        return input.lines().map { parseSnailfish(it.iterator()) }
    }

    fun parseSnailfish(iterator: CharIterator): Snailfish {
        val next = iterator.next()

        return if (next.isDigit()) {
            LiteralSnailfish(next.digitToInt())
        } else {
            val left = parseSnailfish(iterator)
            checkEquals(iterator.next(), ',')
            val right = parseSnailfish(iterator)
            checkEquals(iterator.next(), ']')
            PairSnailfish(left, right)
        }
    }

    sealed interface Snailfish {
        fun plus(other: Snailfish) = PairSnailfish(this, other)

        fun reduce(): Snailfish {
            var current = this
            var next = this as Snailfish?
            while (next != null) {
                current = next
                next = current.explodeOrNull(1) ?: current.splitOrNull()
            }

            return current
        }

        fun explodeOrNull(nestingLevel: Int): Snailfish?
        fun splitOrNull(): Snailfish?
        fun magnitude(): Long
    }

    data class LiteralSnailfish(val value: Int) : Snailfish {
        override fun explodeOrNull(nestingLevel: Int): Snailfish? = null

        override fun splitOrNull(): Snailfish? {
            if (value <= 9) {
                return null
            }

            val left = value / 2
            val right = value - left
            return PairSnailfish(LiteralSnailfish(left), LiteralSnailfish(right))
        }

        override fun magnitude(): Long = value.toLong()

        override fun toString(): String {
            return value.toString()
        }
    }

    data class PairSnailfish(val left: Snailfish, val right: Snailfish) : Snailfish {
        override fun explodeOrNull(nestingLevel: Int): Snailfish? {
            // uh...
            TODO("not yet implemented")
        }

        override fun splitOrNull(): Snailfish? {
            return left.splitOrNull()?.let { PairSnailfish(it, right) } ?: right.splitOrNull()
                ?.let { PairSnailfish(left, it) }
        }

        override fun magnitude(): Long = left.magnitude() * 3 + right.magnitude() * 2

        override fun toString(): String {
            return "[$left,$right]"
        }
    }
}
