package aoc2023.day5

import kotlinx.datetime.Clock
import lib.loadResourceMatchingPackageName
import lib.printTimeTaken

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()

fun main() {
  part1()
  printTimeTaken { part2Faster() }
  printTimeTaken { part2Slower() }
}

private fun part1() {
  val garden = parseInput(false)
  val minLocation = garden.seeds.minOf { it.minOf { garden.translate(it) } }
  println(minLocation)
}

private fun part2Faster() {
  // Solves part 2 by testing all locations until one is found with a valid seed
  // Takes about 50 seconds
  val garden = parseInput(true).invert()
  val minLocation = generateSequence(0L) { it + 1 }
    .map { it to garden.translate(it) }
    .filter { (_, seed) -> garden.seeds.any { it.contains(seed) } }
    .first()
    .first

  println(minLocation)
}

private fun part2Slower() {
  // Solves part 2 by testing all seeds and take the min result
  // Takes about 6 minutes
  val start = Clock.System.now()
  val garden = parseInput(true)
  val minLocation = garden.seeds.minOf {
    println("total time so far [${Clock.System.now() - start}], working a new range of size " + it.count())
    it.minOf { garden.translate(it) }
  }
  println(minLocation)
}

private fun parseInput(seedsAreRanges: Boolean): Garden {
  fun parseMap(lines: Iterator<String>): List<GardenMap> {
    lines.next()
    return lines.asSequence().takeWhile { it.isNotBlank() }.map {
      val (destination, source, size) = it.split(" ").map { it.toLong() }
      GardenMap(destination, source, size)
    }.toList()
  }
  
  val lines = input.lines().iterator()

  val seedLine = lines.next().drop(7).split(" ").map { it.toLong() }
  val seeds = if (seedsAreRanges) {
    seedLine.windowed(2, 2).map { it.first() until it.first() + it.last() }
  } else {
    seedLine.map { it..it }
  }
  lines.next()

  return Garden(
    seeds,
    parseMap(lines),
    parseMap(lines),
    parseMap(lines),
    parseMap(lines),
    parseMap(lines),
    parseMap(lines),
    parseMap(lines),
  )
}

private data class Garden(
  val seeds: List<LongRange>,
  val seedToSoil: List<GardenMap>,
  val soilToFertilizer: List<GardenMap>,
  val fertilizerToWater: List<GardenMap>,
  val waterToLight: List<GardenMap>,
  val lightToTemperature: List<GardenMap>,
  val temperatureToHumidity: List<GardenMap>,
  val humidityToLocation: List<GardenMap>,
) {
  fun translate(source: Long): Long {
    return source
      .translateVia(seedToSoil)
      .translateVia(soilToFertilizer)
      .translateVia(fertilizerToWater)
      .translateVia(waterToLight)
      .translateVia(lightToTemperature)
      .translateVia(temperatureToHumidity)
      .translateVia(humidityToLocation)
  }

  private fun Long.translateVia(maps: List<GardenMap>): Long =
    maps.firstNotNullOfOrNull { it.translate(this) } ?: this

  fun invert() = copy(
    seedToSoil = humidityToLocation.map { it.invert() },
    soilToFertilizer = temperatureToHumidity.map { it.invert() },
    fertilizerToWater = lightToTemperature.map { it.invert() },
    waterToLight = waterToLight.map { it.invert() },
    lightToTemperature = fertilizerToWater.map { it.invert() },
    temperatureToHumidity = soilToFertilizer.map { it.invert() },
    humidityToLocation = seedToSoil.map { it.invert() },
  )
}

private data class GardenMap(val destination: Long, val source: Long, val size: Long) {
  val delta = destination - source
  val sourceRange = source until source + size

  fun translate(source: Long): Long? =
    if (sourceRange.contains(source)) source + delta else null

  fun invert() = copy(destination = source, source = destination)
}
