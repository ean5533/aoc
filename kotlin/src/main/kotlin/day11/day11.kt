package day11

private val classLoader: ClassLoader = object {}.javaClass.classLoader
private val input = classLoader.getResource("text/day11")!!.readText()


fun main() {
    part1(parseInput())
    part2(parseInput())
}

fun parseInput(): MutableTopology {
    return input.lines()
        .flatMapIndexed { y, line ->
            line.mapIndexed { x, char ->
                MutableOctopus(
                    Coordinate(x, y),
                    char.digitToInt()
                )
            }
        }
        .associateBy { it.location }
        .let(::MutableTopology)
}

fun part1(topology: MutableTopology) {
    val flashes = (1..100).sumOf { topology.stepOnce() }

    println("Part 1: There were $flashes flashes")
}

fun part2(topology: MutableTopology) {
    val numberOfOctoBuddies = topology.cells.size
    val firstAllFlash =
        generateSequence(1, Int::inc).takeWhile { topology.stepOnce() != numberOfOctoBuddies }.last() + 1

    println("Part 2: They all flashed on generation $firstAllFlash")
}

data class MutableTopology(val cells: Map<Coordinate, MutableOctopus>) {
    val adjacencyOffsets = (-1..1).cartesianProduct(-1..1).minus(0 to 0)

    /**
     * Increases the energy of all octofriends by 1 and counts the number of flashes (including chains) that occur
     */
    fun stepOnce(): Int {
        increaseEnergyOf(cells.keys.asSequence())
        return cells.values.count { it.flashIfReady() }
    }

    private fun getNeighborsOf(cell: MutableOctopus): List<MutableOctopus> {
        return adjacencyOffsets
            .mapNotNull { (x, y) -> cells[cell.location.copy(x = cell.location.x + x, y = cell.location.y + y)] }
    }

    private fun increaseEnergyOf(locations: Sequence<Coordinate>) {
        val neighborsOfFlashes = locations
            .map { cells[it]!! }
            .filter { it.increaseEnergy() }
            .flatMap { getNeighborsOf(it) }
            .map { it.location }
            .toList()

        if (neighborsOfFlashes.isNotEmpty()) {
            increaseEnergyOf(neighborsOfFlashes.asSequence())
        }
    }

    /**
     * Prints cells in a pretty grid. Useful for debugging.
     */
    @Suppress("unused")
    fun expensiveToString(): String {
        val width = cells.keys.maxOf { it.x } + 1
        val height = cells.keys.maxOf { it.y } + 1
        val array = Array(height) { Array(width) { ' ' } }

        cells.values.forEach { array[it.location.y][it.location.x] = it.energyLevel().digitToChar() }

        return array.joinToString("\n") { it.joinToString("") }
    }
}

data class Coordinate(val x: Int, val y: Int)

data class MutableOctopus(val location: Coordinate, private var energyLevel: Int) {
    private val FLASH_LEVEL = 10

    fun energyLevel(): Int = energyLevel

    /**
     * Increases energy level by 1 and returns true if this caused the octopus to flash
     */
    fun increaseEnergy(): Boolean = ++energyLevel == FLASH_LEVEL

    /**
     * Causes octopus to flash if it's ready, resetting its energy level. Returns true if flash occurred.
     */
    fun flashIfReady(): Boolean {
        return if (energyLevel >= FLASH_LEVEL) {
            energyLevel = 0
            true
        } else {
            false
        }
    }
}

fun <S, T> Iterable<S>.cartesianProduct(other: Iterable<T>) =
    flatMap { first -> other.map { second -> first to second } }