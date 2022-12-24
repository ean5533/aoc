package aoc2022.day16

import lib.*

fun main() {
    val cave = parseInput()
    val caveState = CaveState(cave)

    printTimeTaken {
        val exhaustiveSearch = bestScoreSearch(CaveSearchState(caveState))
        val history = exhaustiveSearch.current.orderedStateHistory()
        println("Part1: " + exhaustiveSearch.current.potentialPressureReleased)
    }
}

private fun parseInput(): Cave {
    val regex = "Valve ([A-Z]+) has flow rate=([0-9]+); tunnels? leads? to valves? (.+)".toRegex()
    val valvesToChildNames = loadResourceMatchingPackageName(object {}.javaClass).trim().lines().map {
        val (name, rate, children) = regex.matchEntire(it)!!.groupValues.drop(1)
        Valve(name, rate.toInt()) to children
    }

    val valves = valvesToChildNames.associate { it.first.name to it.first }
    val tunnels = valvesToChildNames.associate {
        it.first to it.second.split(", ").map { valves[it]!! }
    }

    return Cave(valves["AA"]!!, tunnels)
}

private class CaveSearchState(current: CaveState) :
    BestScoreSearchState<CaveState, Pair<Map<Valve, ValveState>, Valve>>(current) {
    override val score: Int = current.potentialPressureReleased

    override val estimatedScoreToOptimalTermination: Int by lazy {
        val terminalStateCost = (generateSequence(current) { it.moveToAndOpenBestClosedValve() }
            .takeWhile { it.minutesLeft >= 0 }.lastOrNull() ?: current).potentialPressureReleased
        (terminalStateCost - current.potentialPressureReleased)
    }
    
    override val terminationScoreUpperBound: Int by lazy {
        // How much we could possibly save if we somehow walked toward all closed valves simultaneously
        current.remainingValvesByPotentialPressureReleased.filter{it.second > 0}.sumOf { it.second }
    }

    override fun getNextStates() = current.nextStates.map { CaveSearchState(it) }

    override fun isTerminal(): Boolean = current.minutesLeft == 0

    override fun stateIdentity(): Pair<Map<Valve, ValveState>, Valve> = current.valveStates to current.current
}

private data class Valve(val name: String, val flowRate: Int)

private data class Cave(val start: Valve, val tunnels: Map<Valve, List<Valve>>) {
    private val shortestPaths = tunnels.keys.associateWith { a ->
        tunnels.keys.associateWith { b ->
            aStarSearch(a, { tunnels[it]!!.map { it to 1 } }, { it == b })!!.steps
        }
    }

    fun shortestPath(a: Valve, b: Valve) = shortestPaths[a]!![b]!!
}

private data class ValveState(val open: Boolean = false)

private data class CaveState(
    val cave: Cave,
    val current: Valve = cave.start,
    val minutesLeft: Int = 30,
    val potentialPressureReleased: Int = 0,
    val valveStates: Map<Valve, ValveState> = cave.tunnels.keys.associateWith { ValveState() },
    val prior: CaveState? = null
) {
    val nextStates by lazy {
        valveStates.keys
            .filter { !valveStates[it]!!.open && it.flowRate > 0 }
            .map { moveToAndOpenValve(it) }
            .filter { it.minutesLeft >= 0 }
    }

    val remainingValvesByPotentialPressureReleased by lazy {
        valveStates.filter { !it.value.open }
            .map { it.key to it.key.flowRate * (minutesLeft - cave.shortestPath(current, it.key).size - 1) }
    }

    fun moveToAndOpenValve(valve: Valve): CaveState {
        val newMinutesLeft = minutesLeft - cave.shortestPath(current, valve).size - 1
        return copy(
            current = valve,
            minutesLeft = newMinutesLeft,
            valveStates = valveStates + (valve to valveStates[valve]!!.copy(open = true)),
            potentialPressureReleased = potentialPressureReleased + newMinutesLeft * valve.flowRate,
            prior = this
        )
    }

    fun moveToAndOpenBestClosedValve(): CaveState =
        remainingValvesByPotentialPressureReleased.maxBy { it.second }.first.let { moveToAndOpenValve(it) }

    fun orderedStateHistory(): List<CaveState> = (prior?.orderedStateHistory() ?: listOf()) + this

    override fun toString(): String =
        "CaveState(${current.name}, $minutesLeft, $potentialPressureReleased, ${valveStates.map { (valve, state) -> "${valve.name}: ${if (state.open) "open" else "closed"}" }})"
}
