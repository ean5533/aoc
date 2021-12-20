package day19

import day11.cartesianProduct
import java.lang.Math.abs
import kotlin.system.measureTimeMillis

private val classLoader: ClassLoader = object {}.javaClass.classLoader
private val input = classLoader.getResource("text/day19")!!.readText()

fun main() {
    // Scanners with more beacons are more likely to overlap with each other
    val scanners = parseInput().sortedByDescending { it.beaconOffsets.size }

    val time = measureTimeMillis {
        val reorientedScanners = moveScannersToOrigin(scanners)

        part1(reorientedScanners)
        part2(reorientedScanners)
    }
    println("(evaluation after parsing took $time ms)")
}

fun parseInput(): List<Scanner> {
    val iterator = input.lines().iterator()
    return generateSequence { }.takeWhile { iterator.hasNext() }.map {
        val name = iterator.next().split(" ")[2]
        val beaconOffsets = iterator.asSequence().takeWhile { it.isNotEmpty() }.map {
            val (x, y, z) = it.split(",").map { it.toInt() }
            Offset(x, y, z)
        }
        Scanner(name, beaconOffsets.toList())
    }.toList()
}

private fun part1(movedScanners: List<MovedScanner>) {
    val size = movedScanners.flatMap { it.scanner.beaconOffsets }.distinct().size
    System.out.println("Part 1: $size")
}

private fun part2(movedScanners: List<MovedScanner>) {
    val originalLocations = movedScanners.map { it.movedBy }
    val maxDistance = originalLocations.cartesianProduct(originalLocations)
        .maxOf { (a, b) -> a.distanceTo(b).manhattanDistance() }
    System.out.println("Part 2: $maxDistance")
}

private fun moveScannersToOrigin(scanners: List<Scanner>): List<MovedScanner> {
    // Scanners with more beacons are more likely to overlap with each other, so prioritize them
    val sortedScanners = scanners.sortedByDescending { it.beaconOffsets.size }

    // Assume the first scanner is oriented to the origin and then start progressively re-orienting and moving other scanners
    val movedScanners = mutableListOf(MovedScanner(sortedScanners[0], Offset(0, 0, 0)))
    val unmovedScanners = sortedScanners.drop(1).toMutableList()

    // Loop over each unmoved scanner, generating all of its possible reorientations and looking for one that
    // is a valid reorientation of some other already-oriented scanner (one is guaranteed to be found), then move
    // the reoriented scanner to the origin (calculating new beacon distances in the process)
    while (unmovedScanners.isNotEmpty()) {
        val movedScanner = unmovedScanners.firstNotNullOf { unorientedScanner ->
            unorientedScanner.generateReorientations().firstNotNullOfOrNull { possibleReorientation ->
                movedScanners.firstNotNullOfOrNull {
                    // Calculate the distances between every pair of points and take the first distance that occurs
                    // at least twelve times. If found, then this is a valid reorientation
                    it.scanner.beaconOffsets
                        .cartesianProduct(possibleReorientation.beaconOffsets)
                        .map { (a, b) -> a.distanceTo(b) }
                        .groupingBy { it }
                        .eachCount()
                        .entries
                        .firstOrNull { it.value >= 12 }
                        ?.let { MovedScanner(possibleReorientation.move(it.key), it.key) }
                }
            }
        }
        movedScanners.add(movedScanner)
        unmovedScanners.removeIf { it.name == movedScanner.scanner.name }
    }

    return movedScanners.toList()
}

data class Offset(val x: Int, val y: Int, val z: Int) {
    fun distanceTo(other: Offset): Offset = Offset(x - other.x, y - other.y, z - other.z)

    fun move(amount: Offset): Offset = Offset(x + amount.x, y + amount.y, z + amount.z)

    fun manhattanDistance(): Int = abs(x) + abs(y) + abs(z)
}

data class Scanner(val name: String, val beaconOffsets: List<Offset>) {
    fun generateReorientations(): List<Scanner> =
        reorientationFunctions.map { func -> Scanner(name, beaconOffsets.map { func(it) }) }

    fun move(amount: Offset): Scanner = Scanner(name, beaconOffsets.map { it.move(amount) })

    companion object {
        // I think about this as a cube: for each of six faces, rotate in four directions
        val faceSelects = listOf<(Offset) -> Offset>(
            { Offset(it.x, it.y, it.z) },
            { Offset(it.z, it.y, -it.x) },
            { Offset(-it.x, it.y, -it.z) },
            { Offset(-it.z, it.y, it.x) },
            { Offset(it.x, it.z, -it.y) },
            { Offset(it.x, -it.z, it.y) },
        )
        val rotations = listOf<(Offset) -> Offset>(
            { Offset(it.x, it.y, it.z) },
            { Offset(-it.y, it.x, it.z) },
            { Offset(-it.x, -it.y, it.z) },
            { Offset(it.y, -it.x, it.z) },
        )

        // Curry all 6*4=24 function pairs
        val reorientationFunctions =
            faceSelects.cartesianProduct(rotations).map { (a, b) -> ({ it: Offset -> a(b(it)) }) }
    }
}

/**
 * A scanner that has been reoriented and relocated to be at the same orientation and location as the origin, with all
 * of its beacon offsets recalculated accordingly.
 */
data class MovedScanner(val scanner: Scanner, val movedBy: Offset)