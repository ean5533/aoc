package day25

import day21.printTimeTaken


private val classLoader: ClassLoader = object {}.javaClass.classLoader
private val input = classLoader.getResource("text/day25")!!.readText()

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
            }?.let { Coordinate(col, row) to it }

        }
    }
        .partition { it.second == CukeDirection.RIGHT }
        .let { (rights, downs) -> Topology(rights.toMap(), downs.toMap(), width, height) }
}

data class Coordinate(val x: Int, val y: Int)

enum class CukeDirection { RIGHT, DOWN }

data class Topology(
    val rights: Map<Coordinate, CukeDirection>,
    val downs: Map<Coordinate, CukeDirection>,
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

    private fun isEmpty(coordinate: Coordinate): Boolean = rights[coordinate] == null && downs[coordinate] == null

    override fun toString(): String {
        return (0 until height).joinToString("\n") { row ->
            (0 until width).joinToString("") { col ->
                when (Coordinate(col, row)) {
                    in rights -> ">"
                    in downs -> "V"
                    else -> "."
                }
            }
        }
    }
}