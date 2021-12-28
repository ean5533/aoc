package day11

import lib.*

private val input = loadResourceAsString("text/day11")


fun main() {
    part1(parseInput())
    part2(parseInput())
}

private fun parseInput(): MutableTopology {
    return input.lines()
        .flatMapIndexed { y, line ->
            line.mapIndexed { x, char ->
                MutableOctopus(
                    Point2D(x, y),
                    char.digitToInt()
                )
            }
        }
        .associateBy { it.location }
        .let(::MutableTopology)
}

private fun part1(topology: MutableTopology) {
    val flashes = (1..100).sumOf { topology.stepOnce() }

    println("Part 1: There were $flashes flashes")
}

private fun part2(topology: MutableTopology) {
    val numberOfOctoBuddies = topology.cells.size
    val firstAllFlash =
        generateSequence(1, Int::inc).takeWhile { topology.stepOnce() != numberOfOctoBuddies }.last() + 1

    println("Part 2: They all flashed on generation $firstAllFlash")
}

private data class MutableTopology(override val cells: Map<Point2D, MutableOctopus>): Topology<MutableOctopus> {
    /**
     * Increases the energy of all octofriends by 1 and counts the number of flashes (including chains) that occur
     */
    fun stepOnce(): Int {
        increaseEnergyOf(cells.keys.asSequence())
        return cells.values.count { it.flashIfReady() }
    }

    private fun increaseEnergyOf(locations: Sequence<Point2D>) {
        val neighborsOfFlashes = locations
            .map { cells[it]!! }
            .filter { it.increaseEnergy() }
            .flatMap { get8NeighborsOf(it) }
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

private data class MutableOctopus(override val location: Point2D, private var energyLevel: Int): Cell {
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
