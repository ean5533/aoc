package aoc2021.day15

import lib.*

private val input = loadResourceMatchingPackageName(object {}.javaClass)


fun main() {
    val cave = parseInput()

    printTimeTaken {
        val end = findPathToEnd(cave)
        println("Part 1: ${end.cost}")
    }

    printTimeTaken {
        val end = findPathToEnd(cave.expandBy(5))
        println("Part 2: ${end.cost}")
    }
}

private fun parseInput(): Cave {
    return input.lines()
        .flatMapIndexed { y, line -> line.mapIndexed { x, char -> Cell(Point2D(x, y), char.digitToInt()) } }
        .associateBy { it.location }
        .let(::Cave)
}

private fun findPathToEnd(cave: Cave): SearchState<Cell> {
    return aStarSearch(cave.getStart(), { cave.getNeighborsOf(it).map { it to it.cost } }, { it == cave.getEnd() })
        ?: throw IllegalStateException("Could not find a solution")
}

private data class Cave(override val cells: Map<Point2D, Cell>) : Topology<Cell> {
    val height: Int = cells.keys.maxOf { it.y } + 1
    val width: Int = cells.keys.maxOf { it.x } + 1

    fun getNeighborsOf(cell: Cell): Set<Cell> =
        setOfNotNull(
            cells[cell.location.copy(x = cell.location.x - 1)],
            cells[cell.location.copy(x = cell.location.x + 1)],
            cells[cell.location.copy(y = cell.location.y - 1)],
            cells[cell.location.copy(y = cell.location.y + 1)],
        )

    fun getStart(): Cell = cells[Point2D(0, 0)]!!
    fun getEnd(): Cell = cells[Point2D(width - 1, height - 1)]!!

    fun expandBy(factor: Int): Cave {
        val newCells = (0 until factor).cartesianProduct(0 until factor).flatMap { (xFactor, yFactor) ->
            cells.map {
                val newPoint = Point2D(it.key.x + (xFactor * width), it.key.y + (yFactor * height))
                val newCost = (it.value.cost + xFactor + yFactor).modToRange(1, 9)
                newPoint to it.value.copy(location = newPoint, cost = newCost)
            }
        }.toMap()

        return Cave(newCells)
    }

}

private data class Cell(override val location: Point2D, val cost: Int) : lib.Cell
