package day23

import day21.printTimeTaken
import java.lang.Math.abs
import java.util.*

//val input = """
//#############
//#...........#
//###B#C#B#D###
//  #A#D#C#A#
//  #########
//""".trimIndent()

val input = """
#############
#...........#
###D#B#C#C###
  #D#A#B#A#
  #########
""".trimIndent()

val insert = """
  #D#C#B#A#
  #D#B#A#C#
""".trimIndent()

fun main() {
    printTimeTaken {
        val solution = solve(parse(input))
        val cost = solution.moves.sumOf { it.cost }
        println("Part 1: $cost")
    }

    printTimeTaken {
        val newInput = insert(insert, input)
        val solution = solve(parse(newInput))
        val cost = solution.moves.sumOf { it.cost }
        println("Part 2: $cost")
    }
}

private fun parse(input: String): Building {
    val regex = ".*#([A-D])#([A-D])#([A-D])#([A-D])#.*".toRegex()
    val initialRooms = listOf(listOf<Node>(), listOf(), listOf(), listOf())
    val (aNodes, bNodes, cNodes, dNodes) = input.lines().drop(2).takeWhile { it.matches(regex) }
        .fold(initialRooms) { rooms, line ->
            val (a, b, c, d) = regex.matchEntire(line)!!.destructured
            listOf(
                rooms[0].plus(Node(Amphipod.valueOf(a))),
                rooms[1].plus(Node(Amphipod.valueOf(b))),
                rooms[2].plus(Node(Amphipod.valueOf(c))),
                rooms[3].plus(Node(Amphipod.valueOf(d))),
            )
        }

    return Building(aNodes, bNodes, cNodes, dNodes)
}

fun insert(toInsert: String, original: String): String =
    (original.lines().take(3) + toInsert.lines() + original.lines().drop(3)).joinToString("\n")

fun solve(building: Building): State {
    val initialState = State(building, listOf())
    val queue = PriorityQueue<State>(compareBy({ it.cost })).also { it.add(initialState) }
    val seenBuildingsToLowestCost = mutableMapOf(initialState.building to 0)

    while (queue.isNotEmpty()) {
        val state = queue.remove()
        if (state.isSolved()) {
            return state
        }

        val nextStates = state.getNextPossibleStates()

        nextStates
            // Only keep searching if we have found a cheaper path to a previously found state
            .filter { nextState -> seenBuildingsToLowestCost[nextState.building]?.let { nextState.cost < it } ?: true }
            .forEach {
                queue.add(it)
                seenBuildingsToLowestCost[it.building] = it.cost
            }
    }

    throw IllegalStateException("Could not find a solution")
}

data class Building(val spaces: List<Space>) {
    constructor(aNodes: List<Node>, bNodes: List<Node>, cNodes: List<Node>, dNodes: List<Node>) : this(
        listOf(
            HallSpace("OuterLeftHall", Node()),
            HallSpace("InnerLeftHall", Node()),
            Room("A-Room", aNodes, Amphipod.A),
            HallSpace("AB-Space", Node()),
            Room("B-Room", bNodes, Amphipod.B),
            HallSpace("BC-Space", Node()),
            Room("C-Room", cNodes, Amphipod.C),
            HallSpace("CD-Space", Node()),
            Room("D-Room", dNodes, Amphipod.D),
            HallSpace("InnerRightHall", Node()),
            HallSpace("OuterRightHall", Node()),
        )
    )

    private val rooms: List<Room> = spaces.filterIsInstance<Room>()

    val roomMap by lazy { this.spaces.associateBy { it.name } }

    val traverseableSpaces: List<Pair<Space, Space>> =
        (0 until this.spaces.size).flatMap { roomIndex ->
            // RULE: Can't move through other amphipods in the hallway
            val roomsToLeft = (roomIndex - 1 downTo 0).map { this.spaces[it] }.takeWhile { it.canTraverse() }
            val roomsToRight =
                (roomIndex + 1 until this.spaces.size).map { this.spaces[it] }.takeWhile { it.canTraverse() }
            (roomsToLeft + roomsToRight)
                .map { this.spaces[roomIndex] to it }
                // RULE: amphipods only move to or from rooms, not hallway to hallway
                .filter { (source, destination) -> source is Room || destination is Room }
        }

    fun traverseSteps(source: Space, destination: Space): Int {
        val spacesCrossed = abs(spaces.indexOf(source) - spaces.indexOf(destination)) - 1
        return source.traverseSteps() + spacesCrossed + destination.traverseSteps()
    }

    fun updateWith(updatedSpaces: List<Space>): Building {
        return roomMap.toMutableMap()
            .also { map -> updatedSpaces.forEach { map[it.name] = it } }
            .let { Building(it.values.toList()) }
    }

    fun allHome(): Boolean = rooms.all { (_, nodes, allowedAmphipod) -> nodes.all { it.contents == allowedAmphipod } }

    override fun toString(): String {
        val roomLines = (0 until rooms[0].nodes.size).joinToString("") {
            val fill = if (it == 0) "##" else "  "
            val fillEnd = if (it == 0) "##" else ""
            "$fill#${rooms[0].nodes[it]}#${rooms[1].nodes[it]}#${rooms[2].nodes[it]}#${rooms[3].nodes[it]}#$fillEnd\n"
        }

        val halls = spaces.filterIsInstance<HallSpace>()

        return "#############\n" +
                "#${halls[0].node}${halls[1].node}.${halls[2].node}.${halls[3].node}.${halls[4].node}.${halls[5].node}${halls[6].node}#\n" +
                roomLines +
                "  #########"
    }

}

interface Space {
    val name: String
    fun canTraverse(): Boolean
    fun traverseSteps(): Int
    fun popOrNull(): Pair<Amphipod, Space>?
    fun pushOrNull(amphipod: Amphipod): Space?
}

data class HallSpace(override val name: String, val node: Node) : Space {
    override fun canTraverse(): Boolean = node.isEmpty()

    override fun traverseSteps(): Int = if (node.isEmpty()) 1 else 0

    override fun popOrNull(): Pair<Amphipod, Space>? = node.contents?.let { it to copy(node = Node()) }

    override fun pushOrNull(amphipod: Amphipod): Space? = if (node.isEmpty()) copy(node = Node(amphipod)) else null
}

data class Room(
    override val name: String,
    val nodes: List<Node>,
    val allowedAmphipod: Amphipod,
    private val blockTravels: Boolean = false
) : Space {
    override fun canTraverse() = !(blockTravels && nodes.any { it.isNotEmpty() })

    override fun traverseSteps() = nodes.count { it.isEmpty() } + 1

    override fun popOrNull(): Pair<Amphipod, Room>? {
        val (empty, full) = nodes.partition { it.isEmpty() }

        // RULE: amphipods never leave their own room needlessly
        if (full.all { it.contents == allowedAmphipod }) {
            return null
        }

        return full.firstOrNull()?.let { it.contents!! to this.copy(nodes = empty.plus(Node()).plus(full.drop(1))) }
    }

    override fun pushOrNull(amphipod: Amphipod): Room? {
        // RULE: amphipods only move into their own room, and only if no foreign amphipods are present
        if (amphipod != allowedAmphipod || nodes.mapNotNull { it.contents }.any { it != allowedAmphipod }) {
            return null
        }

        val (empty, full) = nodes.partition { it.isEmpty() }
        if (empty.isEmpty()) {
            return null
        }

        return this.copy(nodes = empty.drop(1).plus(Node(amphipod)).plus(full))
    }
}

// TODO: could flyweight this for better perf
data class Node(val contents: Amphipod? = null) {
    fun isEmpty(): Boolean = contents == null
    fun isNotEmpty(): Boolean = !isEmpty()

    override fun toString(): String = contents?.toString() ?: "."
}

enum class Amphipod(val cost: Int) { A(1), B(10), C(100), D(1000) }

data class Move(val cost: Int, val description: String) {
    constructor(cost: Int, source: Space, destination: Space) : this(cost, "${source.name} to ${destination.name}")
}

data class State(val building: Building, val moves: List<Move>) {
    fun isSolved(): Boolean = building.allHome()

    val cost: Int = moves.sumOf { it.cost }

    fun getNextPossibleStates(): List<State> {
        val traverseableRooms = building.traverseableSpaces
        return traverseableRooms.mapNotNull { (source, destination) ->
            source.popOrNull()?.let { (amphipod, newSource) ->
                destination.pushOrNull(amphipod)?.let { newDestination ->
                    val stepsTaken = building.traverseSteps(source, destination)
                    State(
                        building.updateWith(listOf(newSource, newDestination)),
                        moves + Move(stepsTaken * amphipod.cost, source, destination)
                    )
                }
            }
        }
    }
}