package day18

import lib.cartesianProduct
import lib.checkEquals
import lib.loadResourceAsString

private val input = loadResourceAsString("text/day18")


fun main() {
    day18Mutable.part1()
    day18Mutable.part2()
}

/**
 * An awful solution that I'm really not happy with. The explode operation is really bizarre and hard to model, and
 * forced me to add mutability and bidirectional links and just general horror.
 */
object day18Mutable {
    fun part1() {
        val poppop = parseInput().reduce { a, b -> a.plus(b).reduce() }.magnitude()
        println("Part 1: $poppop")
    }

    fun part2() {
        val numFishies = parseInput().size
        // Due to the fishes being mutable we need to keep creating new ones for each combo we want to check :(
        val combinedFishies = (0 until numFishies)
            .cartesianProduct((0 until numFishies))
            .filter { it.first != it.second }
            .map { (i1, i2) ->
                val fishies = parseInput()
                fishies[i1].plus(fishies[i2]).reduce()
            }
        val poppop = combinedFishies.maxOf { it.magnitude() }
        println("Part 2: $poppop")
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
        var parent: PairSnailfish?

        fun plus(other: Snailfish): PairSnailfish = PairSnailfish(this, other)

        fun reduce(): Snailfish {
            while (this.explodeOrNull(1) != null || this.splitOrNull() != null) {
                // do nothing
            }

            return this
        }

        fun explodeOrNull(nestingLevel: Int): Snailfish?
        fun splitOrNull(): Snailfish?
        fun magnitude(): Long
        fun leftmostLiteral(): LiteralSnailfish
        fun rightmostLiteral(): LiteralSnailfish
    }

    data class LiteralSnailfish(var value: Int) : Snailfish {
        override var parent: PairSnailfish? = null

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

        override fun leftmostLiteral(): LiteralSnailfish = this

        override fun rightmostLiteral(): LiteralSnailfish = this
    }

    data class PairSnailfish(var left: Snailfish, var right: Snailfish) : Snailfish {
        init {
            left.parent = this
            right.parent = this
        }

        override var parent: PairSnailfish? = null

        override fun explodeOrNull(nestingLevel: Int): Snailfish? {
            val leftExplosion = left.explodeOrNull(nestingLevel + 1)
            if (leftExplosion != null) {
                left = leftExplosion.also { it.parent = this }
                return this
            }
            val rightExplosion = right.explodeOrNull(nestingLevel + 1)
            if (rightExplosion != null) {
                right = rightExplosion.also { it.parent = this }
                return this
            }

            if (nestingLevel <= 4) {
                return null
            }

            // To get this far, left and right must be literals
            val leftValue = (left as LiteralSnailfish).value
            parent?.findLeftLiteralOf(this)?.also { it.value += leftValue }
            val rightValue = (right as LiteralSnailfish).value
            parent?.findRightLiteralOf(this)?.also { it.value += rightValue }
            return LiteralSnailfish(0)
        }

        override fun splitOrNull(): Snailfish? {
            val leftSplit = left.splitOrNull()
            if (leftSplit != null) {
                left = leftSplit.also { it.parent = this }
                return this
            }
            val rightSplit = right.splitOrNull()
            if (rightSplit != null) {
                right = rightSplit.also { it.parent = this }
                return this
            }

            return null

        }

        override fun magnitude(): Long = left.magnitude() * 3 + right.magnitude() * 2

        override fun toString(): String {
            return "[$left,$right]"
        }

        override fun leftmostLiteral(): LiteralSnailfish = left.leftmostLiteral()

        override fun rightmostLiteral(): LiteralSnailfish = right.rightmostLiteral()

        fun findLeftLiteralOf(snailfish: Snailfish): LiteralSnailfish? {
            return if (snailfish === right) {
                left.rightmostLiteral()
            } else {
                parent?.findLeftLiteralOf(this)
            }
        }

        fun findRightLiteralOf(snailfish: Snailfish): LiteralSnailfish? {
            return if (snailfish === left) {
                right.leftmostLiteral()
            } else {
                parent?.findRightLiteralOf(this)
            }
        }
    }
}