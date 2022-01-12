package day19

import lib.Point3D
import lib.cartesianProduct
import lib.loadResourceAsString
import lib.printTimeTaken

private val input = loadResourceAsString("text/day19")

fun main() {
    // Scanners with more beacons are more likely to overlap with each other
    val scanners = parseInput().sortedByDescending { it.beaconOffsets.size }

    printTimeTaken {
        val reorientedScanners = moveScannersToOrigin(scanners)

        part1(reorientedScanners)
        part2(reorientedScanners)
    }
}

private fun parseInput(): List<Scanner> {
    val iterator = input.lines().iterator()
    return iterator.asSequence().map {
        val name = it.split(" ")[2]
        val beaconOffsets = iterator.asSequence().takeWhile { it.isNotEmpty() }.map {
            val (x, y, z) = it.split(",").map { it.toInt() }
            Point3D(x, y, z)
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
        .maxOf { (a, b) -> a.distanceTo(b).manhattanDistanceTo() }
    System.out.println("Part 2: $maxDistance")
}

private fun moveScannersToOrigin(scanners: List<Scanner>): List<MovedScanner> {
    // Scanners with more beacons are more likely to overlap with each other, so prioritize them
    val sortedScanners = scanners.sortedByDescending { it.beaconOffsets.size }

    // Assume the first scanner is oriented to the origin and then start progressively re-orienting and moving other scanners
    val movedScanners = mutableListOf(MovedScanner(sortedScanners[0], Point3D(0, 0, 0)))
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

private data class Scanner(val name: String, val beaconOffsets: List<Point3D>) {
    fun generateReorientations(): List<Scanner> =
        reorientationFunctions.map { func -> Scanner(name, beaconOffsets.map { func(it) }) }

    fun move(amount: Point3D): Scanner = Scanner(name, beaconOffsets.map { it.move(amount) })

    companion object {
        // I think about this as a cube: for each of six faces, rotate in four directions
        val faceSelects = listOf<(Point3D) -> Point3D>(
            { Point3D(it.x, it.y, it.z) },
            { Point3D(it.z, it.y, -it.x) },
            { Point3D(-it.x, it.y, -it.z) },
            { Point3D(-it.z, it.y, it.x) },
            { Point3D(it.x, it.z, -it.y) },
            { Point3D(it.x, -it.z, it.y) },
        )
        val rotations = listOf<(Point3D) -> Point3D>(
            { Point3D(it.x, it.y, it.z) },
            { Point3D(-it.y, it.x, it.z) },
            { Point3D(-it.x, -it.y, it.z) },
            { Point3D(it.y, -it.x, it.z) },
        )

        // Curry all 6*4=24 function pairs
        val reorientationFunctions =
            faceSelects.cartesianProduct(rotations).map { (a, b) -> ({ it: Point3D -> a(b(it)) }) }
    }
}

/**
 * A scanner that has been reoriented and relocated to be at the same orientation and location as the origin, with all
 * of its beacon offsets recalculated accordingly.
 */
private data class MovedScanner(val scanner: Scanner, val movedBy: Point3D)