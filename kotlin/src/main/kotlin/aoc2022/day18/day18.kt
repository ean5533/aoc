package aoc2022.day18

import lib.*

private val input = loadResourceMatchingPackageName(object {}.javaClass, "text/").trim()
private val lava = input.lines().map { it.split(",").map { it.toInt() }.toPoint3D() }.let { Lava(it.toSet()) }

fun main() {
    printTimeTaken {
        println("part1: " + lava.surfaceArea())
    }
    printTimeTaken {
        println("part2: " + lava.fillInAirBubbles().surfaceArea())
    }
}

data class Lava(val droplets: Set<Point3D>) {
    fun surfaceArea(): Int = droplets.sumOf { it.neighbors().count { !droplets.contains(it) } }

    fun fillInAirBubbles(): Lava {
        val bounds = Area3D(
            droplets.minOf { it.x } - 1..droplets.maxOf { it.x } + 1,
            droplets.minOf { it.y } - 1..droplets.maxOf { it.y } + 1,
            droplets.minOf { it.z } - 1..droplets.maxOf { it.z } + 1
        )
        val outside = bounds.origin().startFloodFill { !droplets.contains(it) && bounds.contains(it) }.toSet()

        // There may be multiple pockets of "inside". To find them all, keep flood filling from arbitrary droplets that haven't already been found until the inside has the correct area
        val expectedInsideSize = bounds.size().toInt() - outside.size
        return generateSequence(listOf<Point3D>() to droplets) { (inside, remainingDroplets) ->
            val nextInside = remainingDroplets.first().startFloodFill { !outside.contains(it) }
            inside + nextInside to remainingDroplets - nextInside
        }.takeWhileInclusive { expectedInsideSize != it.first.size }.last().let { Lava(it.first.toSet()) }
    }
}
