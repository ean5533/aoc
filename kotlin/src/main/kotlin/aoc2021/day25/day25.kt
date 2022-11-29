package aoc2021.day25

import lib.printTimeTaken
import lib.Point2D
import lib.loadResourceAsString


private val input = loadResourceAsString("text/aoc2021/day25")

fun main() {
    val topology = parseInput()

    printTimeTaken {
        val steps = generateSequence(topology to topology.step()) { (_, next) -> next to next.step() }
            .takeWhile { it.first != it.second }
            .count() + 1

        println("Part 1: $steps")
    }

    println("There is no part 2.")
}

private fun parseInput(): Topology {
    val width = input.lines().first().count()
    val height = input.lines().size
    return input.lines().flatMapIndexed { row, line ->
        line.toList().mapIndexedNotNull { col, char ->
            when (char) {
                '>' -> CukeDirection.RIGHT
                'v' -> CukeDirection.DOWN
                else -> null
            }?.let { Point2D(col, row) to it }

        }
    }
        .partition { it.second == CukeDirection.RIGHT }
        .let { (rights, downs) -> Topology(rights.toMap(), downs.toMap(), width, height) }
}

private enum class CukeDirection { RIGHT, DOWN }

private data class Topology(
    val rights: Map<Point2D, CukeDirection>,
    val downs: Map<Point2D, CukeDirection>,
    val width: Int,
    val height: Int
) {
    fun step(): Topology = stepRight().stepDown()

    private fun stepRight(): Topology {
        return copy(rights = rights.mapKeys {
            val maybeNew = it.key.copy(x = (it.key.x + 1) % width)
            if (isEmpty(maybeNew)) maybeNew else it.key
        })
    }

    private fun stepDown(): Topology {
        return copy(downs = downs.mapKeys {
            val maybeNew = it.key.copy(y = (it.key.y + 1) % height)
            if (isEmpty(maybeNew)) maybeNew else it.key
        })
    }

    private fun isEmpty(point: Point2D): Boolean = rights[point] == null && downs[point] == null

    override fun toString(): String {
        return (0 until height).joinToString("\n") { row ->
            (0 until width).joinToString("") { col ->
                when (Point2D(col, row)) {
                    in rights -> ">"
                    in downs -> "V"
                    else -> "."
                }
            }
        }
    }
}
