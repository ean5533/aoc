package day22

import lib.*
import kotlin.math.max
import kotlin.math.min

private val input = loadResourceAsString("text/day22")

fun main() {
    sanityTests()

    val commands = parseInput()

    printTimeTaken {
        val topology = computeTopology(commands.filter { it.inBounds(50) })
        println("Part 1: ${topology.size()}")
    }

    printTimeTaken {
        val topology2 = computeTopology(commands)
        println("Part 2: ${topology2.size()}")
    }
}

private fun computeTopology(commands: List<Command>): Topology {
    return commands.fold(Topology(setOf())) { topology, (action, region) ->
        if (action == Action.ON) topology.add(region) else topology.remove(region)
    }
}

private fun parseInput(): List<Command> {
    val regex = """(on|off) x=(-?\d+)..(-?\d+),y=(-?\d+)..(-?\d+),z=(-?\d+)..(-?\d+)""".toRegex()
    val commands = input.lines().map {
        val (action, x1, x2, y1, y2, z1, z2) = regex.matchEntire(it)!!.destructured
        Command(
            if (action == "on") Action.ON else Action.OFF,
            Area3D(
                min(x1.toInt(), x2.toInt())..max(x1.toInt(), x2.toInt()),
                min(y1.toInt(), y2.toInt())..max(y1.toInt(), y2.toInt()),
                min(z1.toInt(), z2.toInt())..max(z1.toInt(), z2.toInt()),
            )
        )
    }
    return commands
}

private enum class Action { ON, OFF }

private data class Command(val action: Action, val region: Area3D) {
    fun inBounds(bound: Int): Boolean =
        region.xRange.start >= -bound && region.xRange.endInclusive <= bound &&
                region.yRange.start >= -bound && region.yRange.endInclusive <= bound &&
                region.zRange.start >= -bound && region.zRange.endInclusive <= bound
}

/**
 * Represents the topology as a collection of cuboidal regions (instead of individual coordinates).
 */
private data class Topology(val regions: Set<Area3D>) {
    /**
     * Removes a region from the topology. For each region that this affects, up to 6 sub-regions may be generated.
     */
    fun remove(region: Area3D): Topology = regions.flatMap { it.minus(region) }.toSet().let { Topology(it) }

    /**
     * Adds a region to the topology. To prevent overlap, we first remove the new region from all existing regions.
     */
    fun add(region: Area3D): Topology = remove(region).regions.plus(region).toSet().let { Topology(it) }

    fun size(): Long = regions.sumOf { it.size() }
}

private fun sanityTests() {
    // Funny story: I wrote all these tests looking for a bug that *didn't exist*.
    // (I was looking at the wrong sample output and had confused myself into thinking I was producing the wrong solution)
    checkEquals((5..10).extractSlice(0..0), null to listOf(5..10))
    checkEquals((5..10).extractSlice(0..5), (5..5) to listOf(6..10))
    checkEquals((5..10).extractSlice(5..5), (5..5) to listOf(6..10))
    checkEquals((5..10).extractSlice(0..6), (5..6) to listOf(7..10))
    checkEquals((5..10).extractSlice(5..6), (5..6) to listOf(7..10))
    checkEquals((5..10).extractSlice(0..10), (5..10) to listOf<IntRange>())
    checkEquals((5..10).extractSlice(5..10), (5..10) to listOf<IntRange>())
    checkEquals((5..10).extractSlice(0..11), (5..10) to listOf<IntRange>())
    checkEquals((5..10).extractSlice(5..11), (5..10) to listOf<IntRange>())
    checkEquals((5..10).extractSlice(6..10), (6..10) to listOf(5..5))
    checkEquals((5..10).extractSlice(6..11), (6..10) to listOf(5..5))
    checkEquals((5..10).extractSlice(10..10), (10..10) to listOf(5..9))
    checkEquals((5..10).extractSlice(10..11), (10..10) to listOf(5..9))
    checkEquals((5..10).extractSlice(6..6), (6..6) to listOf(5..5, 7..10))
    checkEquals((5..10).extractSlice(6..7), (6..7) to listOf(5..5, 8..10))
    checkEquals((5..10).extractSlice(8..9), (8..9) to listOf(5..7, 10..10))
    checkEquals((5..10).extractSlice(9..9), (9..9) to listOf(5..8, 10..10))

    checkEquals(Area3D(5..10, 5..10, 5..10).extractXAxisSlice((0..0)), null to listOf(Area3D(5..10, 5..10, 5..10)))
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractXAxisSlice((0..5)),
        Area3D(5..5, 5..10, 5..10) to listOf(Area3D(6..10, 5..10, 5..10))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractXAxisSlice((5..5)),
        Area3D(5..5, 5..10, 5..10) to listOf(Area3D(6..10, 5..10, 5..10))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractXAxisSlice((0..6)),
        Area3D(5..6, 5..10, 5..10) to listOf(Area3D(7..10, 5..10, 5..10))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractXAxisSlice((5..6)),
        Area3D(5..6, 5..10, 5..10) to listOf(Area3D(7..10, 5..10, 5..10))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractXAxisSlice((0..10)),
        Area3D(5..10, 5..10, 5..10) to listOf<Area3D>()
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractXAxisSlice((5..10)),
        Area3D(5..10, 5..10, 5..10) to listOf<Area3D>()
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractXAxisSlice((0..11)),
        Area3D(5..10, 5..10, 5..10) to listOf<Area3D>()
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractXAxisSlice((5..11)),
        Area3D(5..10, 5..10, 5..10) to listOf<Area3D>()
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractXAxisSlice((6..10)),
        Area3D(6..10, 5..10, 5..10) to listOf(Area3D(5..5, 5..10, 5..10))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractXAxisSlice((6..11)),
        Area3D(6..10, 5..10, 5..10) to listOf(Area3D(5..5, 5..10, 5..10))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractXAxisSlice((10..10)),
        Area3D(10..10, 5..10, 5..10) to listOf(Area3D(5..9, 5..10, 5..10))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractXAxisSlice((10..11)),
        Area3D(10..10, 5..10, 5..10) to listOf(Area3D(5..9, 5..10, 5..10))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractXAxisSlice((6..6)),
        Area3D(6..6, 5..10, 5..10) to listOf(Area3D(5..5, 5..10, 5..10), Area3D(7..10, 5..10, 5..10))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractXAxisSlice((6..7)),
        Area3D(6..7, 5..10, 5..10) to listOf(Area3D(5..5, 5..10, 5..10), Area3D(8..10, 5..10, 5..10))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractXAxisSlice((8..9)),
        Area3D(8..9, 5..10, 5..10) to listOf(Area3D(5..7, 5..10, 5..10), Area3D(10..10, 5..10, 5..10))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractXAxisSlice((9..9)),
        Area3D(9..9, 5..10, 5..10) to listOf(Area3D(5..8, 5..10, 5..10), Area3D(10..10, 5..10, 5..10))
    )

    checkEquals(Area3D(5..10, 5..10, 5..10).extractYAxisSlice((0..0)), null to listOf(Area3D(5..10, 5..10, 5..10)))
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractYAxisSlice((0..5)),
        Area3D(5..10, 5..5, 5..10) to listOf(Area3D(5..10, 6..10, 5..10))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractYAxisSlice((5..5)),
        Area3D(5..10, 5..5, 5..10) to listOf(Area3D(5..10, 6..10, 5..10))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractYAxisSlice((0..6)),
        Area3D(5..10, 5..6, 5..10) to listOf(Area3D(5..10, 7..10, 5..10))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractYAxisSlice((5..6)),
        Area3D(5..10, 5..6, 5..10) to listOf(Area3D(5..10, 7..10, 5..10))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractYAxisSlice((0..10)),
        Area3D(5..10, 5..10, 5..10) to listOf<Area3D>()
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractYAxisSlice((5..10)),
        Area3D(5..10, 5..10, 5..10) to listOf<Area3D>()
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractYAxisSlice((0..11)),
        Area3D(5..10, 5..10, 5..10) to listOf<Area3D>()
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractYAxisSlice((5..11)),
        Area3D(5..10, 5..10, 5..10) to listOf<Area3D>()
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractYAxisSlice((6..10)),
        Area3D(5..10, 6..10, 5..10) to listOf(Area3D(5..10, 5..5, 5..10))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractYAxisSlice((6..11)),
        Area3D(5..10, 6..10, 5..10) to listOf(Area3D(5..10, 5..5, 5..10))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractYAxisSlice((10..10)),
        Area3D(5..10, 10..10, 5..10) to listOf(Area3D(5..10, 5..9, 5..10))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractYAxisSlice((10..11)),
        Area3D(5..10, 10..10, 5..10) to listOf(Area3D(5..10, 5..9, 5..10))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractYAxisSlice((6..6)),
        Area3D(5..10, 6..6, 5..10) to listOf(Area3D(5..10, 5..5, 5..10), Area3D(5..10, 7..10, 5..10))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractYAxisSlice((6..7)),
        Area3D(5..10, 6..7, 5..10) to listOf(Area3D(5..10, 5..5, 5..10), Area3D(5..10, 8..10, 5..10))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractYAxisSlice((8..9)),
        Area3D(5..10, 8..9, 5..10) to listOf(Area3D(5..10, 5..7, 5..10), Area3D(5..10, 10..10, 5..10))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractYAxisSlice((9..9)),
        Area3D(5..10, 9..9, 5..10) to listOf(Area3D(5..10, 5..8, 5..10), Area3D(5..10, 10..10, 5..10))
    )

    checkEquals(Area3D(5..10, 5..10, 5..10).extractZAxisSlice((0..0)), null to listOf(Area3D(5..10, 5..10, 5..10)))
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractZAxisSlice((0..5)),
        Area3D(5..10, 5..10, 5..5) to listOf(Area3D(5..10, 5..10, 6..10))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractZAxisSlice((5..5)),
        Area3D(5..10, 5..10, 5..5) to listOf(Area3D(5..10, 5..10, 6..10))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractZAxisSlice((0..6)),
        Area3D(5..10, 5..10, 5..6) to listOf(Area3D(5..10, 5..10, 7..10))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractZAxisSlice((5..6)),
        Area3D(5..10, 5..10, 5..6) to listOf(Area3D(5..10, 5..10, 7..10))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractZAxisSlice((0..10)),
        Area3D(5..10, 5..10, 5..10) to listOf<Area3D>()
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractZAxisSlice((5..10)),
        Area3D(5..10, 5..10, 5..10) to listOf<Area3D>()
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractZAxisSlice((0..11)),
        Area3D(5..10, 5..10, 5..10) to listOf<Area3D>()
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractZAxisSlice((5..11)),
        Area3D(5..10, 5..10, 5..10) to listOf<Area3D>()
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractZAxisSlice((6..10)),
        Area3D(5..10, 5..10, 6..10) to listOf(Area3D(5..10, 5..10, 5..5))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractZAxisSlice((6..11)),
        Area3D(5..10, 5..10, 6..10) to listOf(Area3D(5..10, 5..10, 5..5))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractZAxisSlice((10..10)),
        Area3D(5..10, 5..10, 10..10) to listOf(Area3D(5..10, 5..10, 5..9))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractZAxisSlice((10..11)),
        Area3D(5..10, 5..10, 10..10) to listOf(Area3D(5..10, 5..10, 5..9))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractZAxisSlice((6..6)),
        Area3D(5..10, 5..10, 6..6) to listOf(Area3D(5..10, 5..10, 5..5), Area3D(5..10, 5..10, 7..10))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractZAxisSlice((6..7)),
        Area3D(5..10, 5..10, 6..7) to listOf(Area3D(5..10, 5..10, 5..5), Area3D(5..10, 5..10, 8..10))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractZAxisSlice((8..9)),
        Area3D(5..10, 5..10, 8..9) to listOf(Area3D(5..10, 5..10, 5..7), Area3D(5..10, 5..10, 10..10))
    )
    checkEquals(
        Area3D(5..10, 5..10, 5..10).extractZAxisSlice((9..9)),
        Area3D(5..10, 5..10, 9..9) to listOf(Area3D(5..10, 5..10, 5..8), Area3D(5..10, 5..10, 10..10))
    )


}