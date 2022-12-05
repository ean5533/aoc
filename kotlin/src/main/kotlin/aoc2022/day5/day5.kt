package aoc2022.day5

import lib.loadResourceAsString
import lib.transpose

private val input = loadResourceAsString("text/aoc2022/day5").lines()

fun main() {
    val (warehouse, moves) = parseInput()
    part1(warehouse, moves)
    part2(warehouse, moves)
}

private fun part1(warehouse: Warehouse, moves: List<Move>) {
    val moved = warehouse.move(moves, CrateMover9000)
    println("tops1 = ${moved.stacks.map { it.first() }.joinToString("")}")
}

private fun part2(warehouse: Warehouse, moves: List<Move>) {
    val moved = warehouse.move(moves, CrateMover9001)
    println("tops2 = ${moved.stacks.map { it.first() }.joinToString("")}")
}

private fun parseInput(): Pair<Warehouse, List<Move>> {
    val stackCount = input.first { it.startsWith(" 1") }.split("   ").count()
    val stacks = input.takeWhile { it.contains('[') }
        .map { line -> (0 until stackCount).map { line[1 + it * 4] } }
        .transpose()
        .map { it.filterNot { it == ' ' } }
    val warehouse = Warehouse(stacks)

    val moves = input.filter { it.startsWith("move") }
        .map {
            val pieces = it.split(' ')
            Move(pieces[1].toInt(), pieces[3].toInt(), pieces[5].toInt())
        }

    return warehouse to moves
}

data class Warehouse(val stacks: List<List<Char>>) {
    fun move(moves: Iterable<Move>, crane: Crane): Warehouse =
        moves.fold(this) { warehouse, move -> warehouse.move(move, crane) }

    fun move(move: Move, crane: Crane): Warehouse {
        val (newFrom, newTo) = crane.move(stacks[move.from - 1], stacks[move.to - 1], move.amount)
        val newStacks = stacks.mapIndexed { index, stack ->
            when (index) {
                move.from - 1 -> newFrom
                move.to - 1 -> newTo
                else -> stack
            }
        }
        return Warehouse(newStacks)
    }
}

data class Move(val amount: Int, val from: Int, val to: Int)

interface Crane {
    fun move(from: List<Char>, to: List<Char>, amount: Int): Pair<List<Char>, List<Char>>
}

object CrateMover9000 : Crane {
    override fun move(from: List<Char>, to: List<Char>, amount: Int): Pair<List<Char>, List<Char>> =
        from.drop(amount) to (from.take(amount).reversed() + to)
}

object CrateMover9001 : Crane {
    override fun move(from: List<Char>, to: List<Char>, amount: Int): Pair<List<Char>, List<Char>> =
        from.drop(amount) to (from.take(amount) + to)
}
