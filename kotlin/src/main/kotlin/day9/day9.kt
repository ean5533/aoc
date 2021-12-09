package day9

private val classLoader: ClassLoader = object {}.javaClass.classLoader
private val input = classLoader.getResource("text/day9")!!.readText()


fun main() {
    val topology = parseInput()

    part1(topology)
    part2(topology)
}

fun parseInput(): Topology {
    return input.lines()
        .flatMapIndexed { y, line -> line.mapIndexed { x, char -> Cell(Coordinate(x, y), char.digitToInt()) } }
        .associateBy { it.location }
        .let(::Topology)
}

fun part1(topology: Topology) {
    val sumOfLowHeights = topology.getLowPoints().sumOf { it.height + 1 }

    println("The sum of the low heights plus one is $sumOfLowHeights")
}

fun part2(topology: Topology) {
    val productOfBasinSizes = topology.getBasins().map { it.size }.sortedDescending().take(3).reduce { a, b -> a * b }

    println("The product of the basin sizes is $productOfBasinSizes")
}

data class Topology(val cells: Map<Coordinate, Cell>) {
    private fun getNeighborsOf(cell: Cell): List<Cell> =
        listOfNotNull(
            cells[cell.location.copy(x = cell.location.x - 1)],
            cells[cell.location.copy(x = cell.location.x + 1)],
            cells[cell.location.copy(y = cell.location.y - 1)],
            cells[cell.location.copy(y = cell.location.y + 1)],
        )

    fun getLowPoints(): Set<Cell> =
        cells.values.filter { cell -> cell.height < getNeighborsOf(cell).minOf { it.height } }.toSet()

    fun getBasins(): List<Basin> {
        return getLowPoints().map { lowPoint ->
            val basinCells = mutableSetOf(lowPoint)
            val toCheck = mutableSetOf(lowPoint)
            while (toCheck.isNotEmpty()) {
                toCheck.first()
                    .also(toCheck::remove)
                    .let(::getNeighborsOf)
                    .filterNot(basinCells::contains)
                    .filter { it.height < 9 }
                    .forEach {
                        toCheck.add(it)
                        basinCells.add(it)
                    }
            }

            Basin(basinCells.size)
        }
    }
}

data class Coordinate(val x: Int, val y: Int)

data class Cell(val location: Coordinate, val height: Int)

data class Basin(val size: Int)