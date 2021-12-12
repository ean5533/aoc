package day12

private val classLoader: ClassLoader = object {}.javaClass.classLoader
private val input = classLoader.getResource("text/day12")!!.readText()


fun main() {
    val connections = parseInput()

    part1(connections)
    part2(connections)
}

fun parseInput(): Map<String, List<String>> {
    return input.lines()
        .map { it.split("-") }
        .flatMap { (begin, end) -> listOf(begin to end, end to begin) }
        .groupBy({ it.first }, { it.second })
}

fun part1(connections: Map<String, List<String>>) {
    val paths = findPathsToEndFrom("start", connections, false)
    println("Part 1: There are $paths distinct paths")
}

fun part2(connections: Map<String, List<String>>) {
    val paths = findPathsToEndFrom("start", connections, true)
    println("Part 2: There are $paths distinct paths")
}

fun findPathsToEndFrom(
    node: String,
    connections: Map<String, List<String>>,
    mayRevisit: Boolean,
    path: List<String> = listOf()
): Int {
    if (node == "end") {
        return 1
    }

    val newPath = path + node
    val newMayRevisit = mayRevisit && (node.all { it.isUpperCase() } || !path.contains(node))
    val offLimits = setOf("start") + if (newMayRevisit) setOf() else newPath.filter { it.all { it.isLowerCase() } }

    return connections[node]!!.minus(offLimits).sumOf { findPathsToEndFrom(it, connections, newMayRevisit, newPath) }
}