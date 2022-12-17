package aoc2022.day15

import lib.Point2D
import lib.loadResourceMatchingPackageName
import lib.printTimeTaken
import kotlin.math.absoluteValue

private val sensors = parseInput()
//    val targetRow = 10
private val targetRow = 2000000
private val searchRange = 0..targetRow * 2

fun main() {

    printTimeTaken { part1() }
    printTimeTaken { part2() } // ~1 second on my M1
}

private fun part1() {
    val exclusionZone = sensors.mapNotNull { it.exclusionZoneAt(targetRow) }.flatMap { it }.toSet() -
        sensors.map { it.closestBeacon }.filter { it.y == targetRow }.map { it.x }.toSet()
    println("part1: " + exclusionZone.count())
}

private fun part2() {
    val distressBeacon = searchRange.firstNotNullOf { row ->
        // Start at x=0, check each exclusion zone. Each time an X value is excluded, skip ahead to the end of the zone.
        val unexcludedCol = sensors.mapNotNull { it.exclusionZoneAt(row) }
            .sortedWith(compareBy<IntRange> { it.start }.thenByDescending { it.endInclusive })
            .fold(searchRange.start) { colIndex, exclusionZone ->
                if (exclusionZone.contains(colIndex)) exclusionZone.endInclusive + 1 else colIndex
            }

        if (searchRange.contains(unexcludedCol)) Point2D(unexcludedCol, row) else null
    }
    println("part2: ${distressBeacon.x * 4000000L + distressBeacon.y} ($distressBeacon)")
}

private fun parseInput(): List<Sensor> {
    val regex = "Sensor at x=(-?[0-9]+), y=(-?[0-9]+): closest beacon is at x=(-?[0-9]+), y=(-?[0-9]+)".toRegex()
    return loadResourceMatchingPackageName(object {}.javaClass, "text/").trim().lines().map {
        val (sensorX, sensorY, beaconX, beaconY) = regex.find(it)!!.groupValues.drop(1).map { it.toInt() }
        Sensor(Point2D(sensorX, sensorY), Point2D(beaconX, beaconY))
    }
}

private data class Sensor(val position: Point2D, val closestBeacon: Point2D) {
    fun exclusionZoneAt(row: Int): IntRange? {
        val verticalOffset = (position.y - row).absoluteValue
        val offsetExclusionWidth = position.manhattanDistanceTo(closestBeacon) - verticalOffset
        return if (offsetExclusionWidth < 1) null
        else position.x - offsetExclusionWidth..position.x + offsetExclusionWidth
    }
}
