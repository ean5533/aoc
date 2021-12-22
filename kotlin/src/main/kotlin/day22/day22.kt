package day22

import day21.printTimeTaken
import kotlin.math.max
import kotlin.math.min

private val classLoader: ClassLoader = object {}.javaClass.classLoader
private val input = classLoader.getResource("text/day22")!!.readText()

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

fun computeTopology(commands: List<Command>): Topology {
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
            Region(
                min(x1.toInt(), x2.toInt())..max(x1.toInt(), x2.toInt()),
                min(y1.toInt(), y2.toInt())..max(y1.toInt(), y2.toInt()),
                min(z1.toInt(), z2.toInt())..max(z1.toInt(), z2.toInt()),
            )
        )
    }
    return commands
}

enum class Action { ON, OFF }

data class Command(val action: Action, val region: Region) {
    fun inBounds(bound: Int): Boolean =
        region.xRange.start >= -bound && region.xRange.endInclusive <= bound &&
                region.yRange.start >= -bound && region.yRange.endInclusive <= bound &&
                region.zRange.start >= -bound && region.zRange.endInclusive <= bound
}

/**
 * Represents the topology as a collection of cuboidal regions (instead of individual coordinates).
 */
data class Topology(val regions: Set<Region>) {
    /**
     * Removes a region from the topology. For each region that this affects, up to 6 sub-regions may be generated.
     */
    fun remove(region: Region): Topology = regions.flatMap { it.minus(region) }.toSet().let { Topology(it) }

    /**
     * Adds a region to the topology. To prevent overlap, we first remove the new region from all existing regions.
     */
    fun add(region: Region): Topology = remove(region).regions.plus(region).toSet().let { Topology(it) }

    fun size(): Long = regions.sumOf { it.size() }
}

data class Region(val xRange: IntRange, val yRange: IntRange, val zRange: IntRange) {
    fun size(): Long = xRange.count().toLong() * yRange.count().toLong() * zRange.count().toLong()

    fun minus(other: Region): List<Region> {
        val (xSliced, xLeftover) = this.extractXAxisSlice(other.xRange)
        val (ySliced, yLeftover) = xSliced?.extractYAxisSlice(other.yRange) ?: null to listOf()
        val (_, zLeftover) = ySliced?.extractZAxisSlice(other.zRange) ?: null to listOf()

        return xLeftover + yLeftover + zLeftover
    }

    /**
     * Extracts a slice of the region that is [range]-wide, retaining the y- and z-axis dimensions.
     * Returns the extracted slice (if region contained it) plus the remaining regions (if any remainined)
     *
     * - If [range] is a strict subset of the region, result will be the extracted middle slice, plus the two end slices.
     * - If [range] overlaps one side or the other, result will be the extracted end slice, plus the other end slice.
     * - If [range] is non-strict superset of the region, result will be the entire region, plus nothing.
     * - If [range] is disjoint from the region, result will be nothing plus the entire region.
     */
    fun extractXAxisSlice(range: IntRange): Pair<Region?, List<Region>> {
        val (extractedXRange, remainingXRanges) = xRange.extractSlice(range)
        return extractedXRange?.let { Region(it, yRange, zRange) } to
                remainingXRanges.map { Region(it, yRange, zRange) }
    }

    /**
     * Identical to [extractXAxisSlice], but operates on the Y axis. I couldn't figure out how to generalize this code.
     */
    fun extractYAxisSlice(range: IntRange): Pair<Region?, List<Region>> {
        val (extractedYRange, remainingYRanges) = yRange.extractSlice(range)
        return extractedYRange?.let { Region(xRange, it, zRange) } to
                remainingYRanges.map { Region(xRange, it, zRange) }
    }

    /**
     * Identical to [extractXAxisSlice], but operates on the Z axis. I couldn't figure out how to generalize this code.
     */
    fun extractZAxisSlice(range: IntRange): Pair<Region?, List<Region>> {
        val (extractedZRange, remainingZRanges) = zRange.extractSlice(range)
        return extractedZRange?.let { Region(xRange, yRange, it) } to
                remainingZRanges.map { Region(xRange, yRange, it) }
    }
}

/**
 * Returns the portion of this range is is contained in [other], as well as the remaining portions of the range
 * that were not contained in [other]
 */
fun IntRange.extractSlice(other: IntRange): Pair<IntRange?, List<IntRange>> {
    return if (start > other.endInclusive || endInclusive < other.start) {
        // Other is non-overlapping, original range unaffected
        null to listOf(this)
    } else if (start < other.start && endInclusive > other.endInclusive) {
        // Other is strictly contained, producing two new ranges
        other to listOf(start until other.start, (other.endInclusive + 1)..endInclusive)
    } else if (start >= other.start && endInclusive <= other.endInclusive) {
        // Other contains original range, resulting in nothing leftover
        (start..endInclusive) to listOf()
    } else if (start >= other.start && start <= other.endInclusive) {
        // Other overlaps with beginning of original range, producing one shorter range
        (start..other.endInclusive) to listOf((other.endInclusive + 1)..endInclusive)
    } else if (endInclusive <= other.endInclusive) {
        // Other overlaps with end of original range, producing one shorter range
        (other.start..endInclusive) to listOf(start until other.start)
    } else {
        throw IllegalStateException("this should be impossible")
    }
}

fun sanityTests() {
    // Funny story: I wrote all these tests looking for a bug that *didn't exist*.
    // (I was looking at the wrong sample output and had confused myself into thinking I was producing the wrong solution)
    check((5..10).extractSlice(0..0) == null to listOf(5..10))
    check((5..10).extractSlice(0..5) == (5..5) to listOf(6..10))
    check((5..10).extractSlice(5..5) == (5..5) to listOf(6..10))
    check((5..10).extractSlice(0..6) == (5..6) to listOf(7..10))
    check((5..10).extractSlice(5..6) == (5..6) to listOf(7..10))
    check((5..10).extractSlice(0..10) == (5..10) to listOf<IntRange>())
    check((5..10).extractSlice(5..10) == (5..10) to listOf<IntRange>())
    check((5..10).extractSlice(0..11) == (5..10) to listOf<IntRange>())
    check((5..10).extractSlice(5..11) == (5..10) to listOf<IntRange>())
    check((5..10).extractSlice(6..10) == (6..10) to listOf(5..5))
    check((5..10).extractSlice(6..11) == (6..10) to listOf(5..5))
    check((5..10).extractSlice(10..10) == (10..10) to listOf(5..9))
    check((5..10).extractSlice(10..11) == (10..10) to listOf(5..9))
    check((5..10).extractSlice(6..6) == (6..6) to listOf(5..5, 7..10))
    check((5..10).extractSlice(6..7) == (6..7) to listOf(5..5, 8..10))
    check((5..10).extractSlice(8..9) == (8..9) to listOf(5..7, 10..10))
    check((5..10).extractSlice(9..9) == (9..9) to listOf(5..8, 10..10))

    check(Region(5..10, 5..10, 5..10).extractXAxisSlice((0..0)) == null to listOf(Region(5..10, 5..10, 5..10)))
    check(
        Region(5..10, 5..10, 5..10).extractXAxisSlice((0..5)) ==
                Region(5..5, 5..10, 5..10) to listOf(Region(6..10, 5..10, 5..10))
    )
    check(
        Region(5..10, 5..10, 5..10).extractXAxisSlice((5..5)) ==
                Region(5..5, 5..10, 5..10) to listOf(Region(6..10, 5..10, 5..10))
    )
    check(
        Region(5..10, 5..10, 5..10).extractXAxisSlice((0..6)) ==
                Region(5..6, 5..10, 5..10) to listOf(Region(7..10, 5..10, 5..10))
    )
    check(
        Region(5..10, 5..10, 5..10).extractXAxisSlice((5..6)) ==
                Region(5..6, 5..10, 5..10) to listOf(Region(7..10, 5..10, 5..10))
    )
    check(
        Region(5..10, 5..10, 5..10).extractXAxisSlice((0..10)) ==
                Region(5..10, 5..10, 5..10) to listOf<Region>()
    )
    check(
        Region(5..10, 5..10, 5..10).extractXAxisSlice((5..10)) ==
                Region(5..10, 5..10, 5..10) to listOf<Region>()
    )
    check(
        Region(5..10, 5..10, 5..10).extractXAxisSlice((0..11)) ==
                Region(5..10, 5..10, 5..10) to listOf<Region>()
    )
    check(
        Region(5..10, 5..10, 5..10).extractXAxisSlice((5..11)) ==
                Region(5..10, 5..10, 5..10) to listOf<Region>()
    )
    check(
        Region(5..10, 5..10, 5..10).extractXAxisSlice((6..10)) ==
                Region(6..10, 5..10, 5..10) to listOf(Region(5..5, 5..10, 5..10))
    )
    check(
        Region(5..10, 5..10, 5..10).extractXAxisSlice((6..11)) ==
                Region(6..10, 5..10, 5..10) to listOf(Region(5..5, 5..10, 5..10))
    )
    check(
        Region(5..10, 5..10, 5..10).extractXAxisSlice((10..10)) ==
                Region(10..10, 5..10, 5..10) to listOf(Region(5..9, 5..10, 5..10))
    )
    check(
        Region(5..10, 5..10, 5..10).extractXAxisSlice((10..11)) ==
                Region(10..10, 5..10, 5..10) to listOf(Region(5..9, 5..10, 5..10))
    )
    check(
        Region(5..10, 5..10, 5..10).extractXAxisSlice((6..6)) ==
                Region(6..6, 5..10, 5..10) to listOf(Region(5..5, 5..10, 5..10), Region(7..10, 5..10, 5..10))
    )
    check(
        Region(5..10, 5..10, 5..10).extractXAxisSlice((6..7)) ==
                Region(6..7, 5..10, 5..10) to listOf(Region(5..5, 5..10, 5..10), Region(8..10, 5..10, 5..10))
    )
    check(
        Region(5..10, 5..10, 5..10).extractXAxisSlice((8..9)) ==
                Region(8..9, 5..10, 5..10) to listOf(Region(5..7, 5..10, 5..10), Region(10..10, 5..10, 5..10))
    )
    check(
        Region(5..10, 5..10, 5..10).extractXAxisSlice((9..9)) ==
                Region(9..9, 5..10, 5..10) to listOf(Region(5..8, 5..10, 5..10), Region(10..10, 5..10, 5..10))
    )

    check(Region(5..10, 5..10, 5..10).extractYAxisSlice((0..0)) == null to listOf(Region(5..10, 5..10, 5..10)))
    check(
        Region(5..10, 5..10, 5..10).extractYAxisSlice((0..5)) ==
                Region(5..10, 5..5, 5..10) to listOf(Region(5..10, 6..10, 5..10))
    )
    check(
        Region(5..10, 5..10, 5..10).extractYAxisSlice((5..5)) ==
                Region(5..10, 5..5, 5..10) to listOf(Region(5..10, 6..10, 5..10))
    )
    check(
        Region(5..10, 5..10, 5..10).extractYAxisSlice((0..6)) ==
                Region(5..10, 5..6, 5..10) to listOf(Region(5..10, 7..10, 5..10))
    )
    check(
        Region(5..10, 5..10, 5..10).extractYAxisSlice((5..6)) ==
                Region(5..10, 5..6, 5..10) to listOf(Region(5..10, 7..10, 5..10))
    )
    check(
        Region(5..10, 5..10, 5..10).extractYAxisSlice((0..10)) ==
                Region(5..10, 5..10, 5..10) to listOf<Region>()
    )
    check(
        Region(5..10, 5..10, 5..10).extractYAxisSlice((5..10)) ==
                Region(5..10, 5..10, 5..10) to listOf<Region>()
    )
    check(
        Region(5..10, 5..10, 5..10).extractYAxisSlice((0..11)) ==
                Region(5..10, 5..10, 5..10) to listOf<Region>()
    )
    check(
        Region(5..10, 5..10, 5..10).extractYAxisSlice((5..11)) ==
                Region(5..10, 5..10, 5..10) to listOf<Region>()
    )
    check(
        Region(5..10, 5..10, 5..10).extractYAxisSlice((6..10)) ==
                Region(5..10, 6..10, 5..10) to listOf(Region(5..10, 5..5, 5..10))
    )
    check(
        Region(5..10, 5..10, 5..10).extractYAxisSlice((6..11)) ==
                Region(5..10, 6..10, 5..10) to listOf(Region(5..10, 5..5, 5..10))
    )
    check(
        Region(5..10, 5..10, 5..10).extractYAxisSlice((10..10)) ==
                Region(5..10, 10..10, 5..10) to listOf(Region(5..10, 5..9, 5..10))
    )
    check(
        Region(5..10, 5..10, 5..10).extractYAxisSlice((10..11)) ==
                Region(5..10, 10..10, 5..10) to listOf(Region(5..10, 5..9, 5..10))
    )
    check(
        Region(5..10, 5..10, 5..10).extractYAxisSlice((6..6)) ==
                Region(5..10, 6..6, 5..10) to listOf(Region(5..10, 5..5, 5..10), Region(5..10, 7..10, 5..10))
    )
    check(
        Region(5..10, 5..10, 5..10).extractYAxisSlice((6..7)) ==
                Region(5..10, 6..7, 5..10) to listOf(Region(5..10, 5..5, 5..10), Region(5..10, 8..10, 5..10))
    )
    check(
        Region(5..10, 5..10, 5..10).extractYAxisSlice((8..9)) ==
                Region(5..10, 8..9, 5..10) to listOf(Region(5..10, 5..7, 5..10), Region(5..10, 10..10, 5..10))
    )
    check(
        Region(5..10, 5..10, 5..10).extractYAxisSlice((9..9)) ==
                Region(5..10, 9..9, 5..10) to listOf(Region(5..10, 5..8, 5..10), Region(5..10, 10..10, 5..10))
    )

    check(Region(5..10, 5..10, 5..10).extractZAxisSlice((0..0)) == null to listOf(Region(5..10, 5..10, 5..10)))
    check(
        Region(5..10, 5..10, 5..10).extractZAxisSlice((0..5)) ==
                Region(5..10, 5..10, 5..5) to listOf(Region(5..10, 5..10, 6..10))
    )
    check(
        Region(5..10, 5..10, 5..10).extractZAxisSlice((5..5)) ==
                Region(5..10, 5..10, 5..5) to listOf(Region(5..10, 5..10, 6..10))
    )
    check(
        Region(5..10, 5..10, 5..10).extractZAxisSlice((0..6)) ==
                Region(5..10, 5..10, 5..6) to listOf(Region(5..10, 5..10, 7..10))
    )
    check(
        Region(5..10, 5..10, 5..10).extractZAxisSlice((5..6)) ==
                Region(5..10, 5..10, 5..6) to listOf(Region(5..10, 5..10, 7..10))
    )
    check(
        Region(5..10, 5..10, 5..10).extractZAxisSlice((0..10)) ==
                Region(5..10, 5..10, 5..10) to listOf<Region>()
    )
    check(
        Region(5..10, 5..10, 5..10).extractZAxisSlice((5..10)) ==
                Region(5..10, 5..10, 5..10) to listOf<Region>()
    )
    check(
        Region(5..10, 5..10, 5..10).extractZAxisSlice((0..11)) ==
                Region(5..10, 5..10, 5..10) to listOf<Region>()
    )
    check(
        Region(5..10, 5..10, 5..10).extractZAxisSlice((5..11)) ==
                Region(5..10, 5..10, 5..10) to listOf<Region>()
    )
    check(
        Region(5..10, 5..10, 5..10).extractZAxisSlice((6..10)) ==
                Region(5..10, 5..10, 6..10) to listOf(Region(5..10, 5..10, 5..5))
    )
    check(
        Region(5..10, 5..10, 5..10).extractZAxisSlice((6..11)) ==
                Region(5..10, 5..10, 6..10) to listOf(Region(5..10, 5..10, 5..5))
    )
    check(
        Region(5..10, 5..10, 5..10).extractZAxisSlice((10..10)) ==
                Region(5..10, 5..10, 10..10) to listOf(Region(5..10, 5..10, 5..9))
    )
    check(
        Region(5..10, 5..10, 5..10).extractZAxisSlice((10..11)) ==
                Region(5..10, 5..10, 10..10) to listOf(Region(5..10, 5..10, 5..9))
    )
    check(
        Region(5..10, 5..10, 5..10).extractZAxisSlice((6..6)) ==
                Region(5..10, 5..10, 6..6) to listOf(Region(5..10, 5..10, 5..5), Region(5..10, 5..10, 7..10))
    )
    check(
        Region(5..10, 5..10, 5..10).extractZAxisSlice((6..7)) ==
                Region(5..10, 5..10, 6..7) to listOf(Region(5..10, 5..10, 5..5), Region(5..10, 5..10, 8..10))
    )
    check(
        Region(5..10, 5..10, 5..10).extractZAxisSlice((8..9)) ==
                Region(5..10, 5..10, 8..9) to listOf(Region(5..10, 5..10, 5..7), Region(5..10, 5..10, 10..10))
    )
    check(
        Region(5..10, 5..10, 5..10).extractZAxisSlice((9..9)) ==
                Region(5..10, 5..10, 9..9) to listOf(Region(5..10, 5..10, 5..8), Region(5..10, 5..10, 10..10))
    )


}