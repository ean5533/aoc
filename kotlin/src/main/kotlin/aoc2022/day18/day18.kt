package aoc2022.day18

import lib.*

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
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
        return Lava((bounds.points() - outside).toSet())
    }
}
