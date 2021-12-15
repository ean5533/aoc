package day15

import day11.cartesianProduct
import java.util.*

private val classLoader: ClassLoader = object {}.javaClass.classLoader
private val input = classLoader.getResource("text/day15")!!.readText()


fun main() {
    val topology = parseInput()

    part1(topology)
    part2(topology.expandBy(5))
}

private fun parseInput(): Topology {
    return input.lines()
        .flatMapIndexed { y, line -> line.mapIndexed { x, char -> Cell(Coordinate(x, y), char.digitToInt()) } }
        .associateBy { it.location }
        .let(::Topology)
}

fun part1(topology: Topology) {
    val end = findPathToEnd(topology)
    println("Part 1: ${end.totalCost}")
}

fun part2(topology: Topology) {
    val end = findPathToEnd(topology)
    println("Part 2: ${end.totalCost}")
}

/**
 * https://en.wikipedia.org/wiki/A*_search_algorithm
 */
private fun findPathToEnd(topology: Topology): PathNode {
    val start = PathNode(topology.getStart(), null)
    val destination = topology.getEnd()

    val seen = mutableSetOf<Coordinate>()
    val queue = PriorityQueue<PathNode>(topology.cells.size, compareBy { it.totalCost })
    queue.add(start)

    while (queue.isNotEmpty()) {
        val node = queue.remove()

        val newNeighbors = topology
            .getNeighborsOf(node.cell)
            .filterNot { seen.contains(it.location) }
            .map { PathNode(it, node) }

        val maybeEndNode = newNeighbors.firstOrNull { it.cell == destination }
        if (maybeEndNode != null) {
            return maybeEndNode
        }

        newNeighbors.forEach {
            seen.add(it.cell.location)
            queue.add(it)
        }
    }

    throw RuntimeException("Impossible")
}

data class Topology(val cells: Map<Coordinate, Cell>) {
    val height: Int = cells.keys.maxOf { it.y } + 1
    val width: Int = cells.keys.maxOf { it.x } + 1

    fun getNeighborsOf(cell: Cell): Set<Cell> =
        setOfNotNull(
            cells[cell.location.copy(x = cell.location.x - 1)],
            cells[cell.location.copy(x = cell.location.x + 1)],
            cells[cell.location.copy(y = cell.location.y - 1)],
            cells[cell.location.copy(y = cell.location.y + 1)],
        )

    fun getStart(): Cell = cells[Coordinate(0, 0)]!!
    fun getEnd(): Cell = cells[Coordinate(width - 1, height - 1)]!!

    fun expandBy(factor: Int): Topology {
        val newCells = (0 until factor).cartesianProduct(0 until factor).flatMap { (xFactor, yFactor) ->
            cells.map {
                val newCoordinate = Coordinate(it.key.x + (xFactor * width), it.key.y + (yFactor * height))
                val newCost = getCost(it.value.cost, xFactor, yFactor)
                newCoordinate to it.value.copy(location = newCoordinate, cost = newCost)
            }
        }.toMap()

        return Topology(newCells)
    }

    /**
     * Bizarre cost function where adding 1 to 9 rolls over into 1 again
     */
    private fun getCost(cost: Int, xFactor: Int, yFactor: Int): Int {
        var newCost = cost + xFactor + yFactor
        while (newCost > 9) newCost -= 9
        return newCost
    }
}

data class Cell(val location: Coordinate, val cost: Int)

data class Coordinate(val x: Int, val y: Int)

data class PathNode(val cell: Cell, val parent: PathNode?) {
    // Don't count start node in total cost
    val totalCost: Long = parent?.let { it.totalCost + cell.cost } ?: 0
}