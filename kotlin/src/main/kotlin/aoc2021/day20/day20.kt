package aoc2021.day20

import lib.Point2D
import lib.cartesianProduct
import lib.loadResourceMatchingPackageName

private val input = loadResourceMatchingPackageName(object {}.javaClass, "text/")

private const val DARK = 0
private const val LIGHT = 1

fun main() {
    val (algorithm, image) = parseInput()

    part1(algorithm, image)
    part2(algorithm, image)
}

private fun part1(algorithm: Map<Int, Int>, image: Image) {
    val newImage = (0 until 2).fold(image) { prevImage, _ -> prevImage.transformWith(algorithm) }
    val lightPixels = newImage.pixels.filter { it.value == LIGHT }.size
    println("Part 1: $lightPixels")
}

private fun part2(algorithm: Map<Int, Int>, image: Image) {
    val newImage = (0 until 50).fold(image) { prevImage, _ -> prevImage.transformWith(algorithm) }
    val lightPixels = newImage.pixels.filter { it.value == LIGHT }.size
    println("Part 2: $lightPixels")
}

private fun parseInput(): Pair<Map<Int, Int>, Image> {
    val algorithm = input.lines()[0].mapIndexed { i, char -> i to if (char == '#') LIGHT else DARK }.toMap()

    val inputImage = input.lines().drop(2).map { it.split("").filter { it.isNotEmpty() } }
    val lightPixels = inputImage.flatMapIndexed { rowIndex, row ->
        row.mapIndexed { colIndex, char -> Point2D(colIndex, rowIndex) to if (char == "#") LIGHT else DARK }
    }.toMap()

    return algorithm to Image(lightPixels, DARK)
}

private data class Image(val pixels: Map<Point2D, Int>, val fillPixel: Int) {
    private val width = pixels.maxOf { it.key.x } + 1
    private val height = pixels.maxOf { it.key.y } + 1
    private val adjacencyOffsets = (-1..1).cartesianProduct(-1..1)

    fun getPixelValuesOf(location: Point2D): List<Int> {
        return adjacencyOffsets.map { (y, x) ->
            pixels.getOrDefault(location.copy(x = location.x + x, y = location.y + y), fillPixel)
        }
    }

    fun transformWith(algorithm: Map<Int, Int>): Image {
        val newPixels = (-1..height).flatMap { row ->
            (-1..width).map { col ->
                val transformIndex = getPixelValuesOf(Point2D(col, row)).joinToString("").toInt(2)
                Point2D(col + 1, row + 1) to algorithm[transformIndex]!!
            }
        }.toMap()

        val newFillPixel = algorithm[fillPixel.toString().repeat(9).toInt(2)]!!

        return Image(newPixels, newFillPixel)
    }

    override fun toString(): String {
        return (0 until height).joinToString("\n") { row ->
            (0 until width).joinToString("") { col ->
                val pixelValue = pixels.getOrDefault(Point2D(col, row), 0)
                if (pixelValue == LIGHT) "#" else "."
            }
        }
    }
}
