package day9

import lib.Point2D
import lib.Topology
import lib.loadResourceAsString

private val input = loadResourceAsString("text/day9")


fun main() {
    val cave = parseInput()

    part1(cave)
    part2(cave)
}

private fun parseInput(): Cave {
    return input.lines()
        .flatMapIndexed { y, line -> line.mapIndexed { x, char -> Cell(Point2D(x, y), char.digitToInt()) } }
        .associateBy { it.location }
        .let(::Cave)
}

private fun part1(cave: Cave) {
    val sumOfLowHeights = cave.getLowPoints().sumOf { it.height + 1 }

    println("The sum of the low heights plus one is $sumOfLowHeights")
}

private fun part2(cave: Cave) {
    val productOfBasinSizes = cave.getBasins().sortedDescending().take(3).reduce { a, b -> a * b }

    println("The product of the basin sizes is $productOfBasinSizes")
}

private data class Cave(override val cells: Map<Point2D, Cell>): Topology<Cell> {

    fun getLowPoints(): Set<Cell> =
        cells.values.filter { cell -> cell.height < get4NeighborsOf(cell).minOf { it.height } }.toSet()

    fun getBasins(): List<Int> {
        return getLowPoints().map { lowPoint ->
            val basinCells = mutableSetOf(lowPoint)
            val toCheck = mutableSetOf(lowPoint)
            while (toCheck.isNotEmpty()) {
                toCheck.first()
                    .also(toCheck::remove)
                    .let(::get4NeighborsOf)
                    .filterNot(basinCells::contains)
                    .filter { it.height < 9 }
                    .forEach {
                        toCheck.add(it)
                        basinCells.add(it)
                    }
            }

            basinCells.size
        }
    }
}

private data class Cell(override val location: Point2D, val height: Int): lib.Cell
