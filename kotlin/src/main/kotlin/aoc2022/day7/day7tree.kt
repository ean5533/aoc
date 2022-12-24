package aoc2022.day7

import lib.loadResourceMatchingPackageName

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()
private val root = Directory("/")

/**
 * My original solution before finding out part 2 was trivial and didn't require any of the tree structure stuff.
 * 
 * See day7minimal.kt for a simpler, hackier solution.
 */
fun main() {
    parseInput()
    part1()
    part2()
}

private fun parseInput() {
    val dirMap = mutableMapOf(root.path to root)
    var currentDirectory = root
    input.lines().drop(1).forEach {
        when {
            it == "$ cd .." ->
                currentDirectory = dirMap[currentDirectory.parentPath]!!
            it.startsWith("$ cd") -> {
                val target = it.split(" ")[2]
                currentDirectory = dirMap[currentDirectory.path + target + "/"]!!
            }
            it == "$ ls" -> {}
            it.startsWith("dir") -> {
                val dir = Directory(currentDirectory.path + it.split(" ")[1] + "/")
                currentDirectory.contents.add(dir)
                dirMap.put(dir.path, dir)
            }
            it[0].digitToIntOrNull() != null -> {
                val (size, name) = it.split(" ")
                currentDirectory.contents.add(File(currentDirectory.path + name, size.toLong()))
            }
            else -> throw NotImplementedError()
        }
    }
}

private fun part1() {
    val smallDirsSum = root.getRecursive { it is Directory && it.size <= 100000 }.sumOf { it.size }
    println("part1 = $smallDirsSum")
}

private fun part2() {
    val spaceNeeded = 30000000 - (70000000 - root.size)
    val smallestDeletionToSatify = root.getRecursive { it is Directory }
        .map { it.size }
        .filter { it > spaceNeeded }
        .minByOrNull { it }!!
    println("part2 = $smallestDeletionToSatify")
}

private interface Element {
    val size: Long
}

private data class Directory(val path: String, val contents: MutableList<Element> = mutableListOf()) : Element {
    fun getRecursive(predicate: (Element) -> Boolean): List<Element> {
        return contents.filter(predicate) + contents.filterIsInstance<Directory>()
            .flatMap { it.getRecursive(predicate) }
    }

    val parentPath: String? =
        if (path == "/") null else path.trimEnd('/').let { it.substring(0, it.lastIndexOf("/") + 1) }

    override val size: Long
        get() = contents.sumOf { it.size }
}

private data class File(val path: String, override val size: Long) : Element
