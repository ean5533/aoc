package aoc2022.day16.part2

import lib.*

/**
 * A solution that works for part 1 and part 2... sort of.
 * 
 * It's a much slower solution to part 1, because it has to keep far more states. The other part 1 solution just keeps the states between valves being opened (2+ minutes covered by each state), whereas this one keeps a state for every single minute.
 * 
 * It can mostly solve part 2. It takes about 5 seconds to discover the actual optimal solution, but it never finishes deciding that it's the most optimal solution before it eventually blows out the heap.
 * 
 */
fun main() {
    val cave = parseInput()

    printTimeTaken {
        val caveState = CaveState(cave)
        val searchResult = bestScoreSearch(CaveSearchState(caveState))
        println("Part1: " + searchResult.current.potentialPressureReleased)
    }
    printTimeTaken {
        val caveState = CaveState(
            cave,
            minutesLeft = 26,
            workers = listOf(WorkerState(cave.start, "You"), WorkerState(cave.start, "Elephant"))
        )
        val searchresult = bestScoreSearch(CaveSearchState(caveState), trimStates = true, printSolutions = true)
        println("Part2: " + searchresult.current.potentialPressureReleased)
    }
}

private fun parseInput(): Cave {
    val regex = "Valve ([A-Z]+) has flow rate=([0-9]+); tunnels? leads? to valves? (.+)".toRegex()
    val valvesToChildNames = loadResourceAsString("text/aoc2022/day16").trim().lines().map {
        val (name, rate, children) = regex.matchEntire(it)!!.groupValues.drop(1)
        Valve(name, rate.toInt()) to children
    }

    val valves = valvesToChildNames.associate { it.first.name to it.first }
    val tunnels = valvesToChildNames.associate { it.first to it.second.split(", ").map { valves[it]!! } }

    return Cave(valves["AA"]!!, tunnels)
}

private class CaveSearchState(override val current: CaveState) :
    BestScoreSearchState<CaveState, Triple<Map<Valve, ValveState>, List<WorkerState>, Int>> {
    override val score: Int = current.potentialPressureReleased

    override val estimatedScoreToOptimalTermination: Int by lazy {
        val terminalStateScore = guessBestScore(current)
        terminalStateScore - current.potentialPressureReleased
    }

    override val terminationScoreUpperBound: Int by lazy {
        // How much we could possibly save if every worker could reach any valve in one step
        val bestTerminalStateScore = guessBestScore(current.withTeleporting())
        bestTerminalStateScore - current.potentialPressureReleased
    }

    private fun guessBestScore(state: CaveState) = (generateSequence(state) { it.bestMoveEducatedGuess }
        .takeWhile { it.minutesLeft >= 0 }.lastOrNull() ?: state).potentialPressureReleased

    override val nextStates by lazy { current.nextStates.map { CaveSearchState(it) } }
    override val isTerminal = current.minutesLeft == 0
    override val stateIdentity by lazy { Triple(current.valveStates, current.workers, current.minutesLeft) }
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

private data class WorkerState(val position: Valve, val name: String = "You", val timeUntilOpen: Int = 0) {
    override fun toString(): String = "$name:${position.name}($timeUntilOpen)"
}

private data class CaveState(
    val cave: Cave,
    val workers: List<WorkerState> = listOf(WorkerState(cave.start)),
    val minutesLeft: Int = 30,
    val potentialPressureReleased: Int = 0,
    val valveStates: Map<Valve, ValveState> = cave.tunnels.keys.associateWith { ValveState() },
    val teleporting: Boolean = false,
) {
    val unoccupiedWorkers = workers.filter { it.timeUntilOpen <= 0 }
    val openableValves =
        valveStates.filter { !it.value.open && it.key.flowRate > 0 && !workers.any { (position) -> position == it.key } }

    val nextStates by lazy {
        if (minutesLeft <= 0) return@lazy listOf()

        val nextStates = unoccupiedWorkers.fold(listOf(this)) { states, worker ->
            states.flatMap { state ->
                state.openableValvesByPotentialPressureReleased(worker)
                    .map { state.moveToAndOpenValve(it.first, worker) }
            }
        }.map { it.tick }

        if (nextStates.isEmpty()) listOf(tick) else nextStates
    }

    val bestMoveEducatedGuess by lazy {
        unoccupiedWorkers.fold(this) { state, worker ->
            state.bestValve(worker)?.let { state.moveToAndOpenValve(it, worker) } ?: state
        }.tick
    }

    private fun openableValvesByPotentialPressureReleased(workerState: WorkerState): List<Pair<Valve, Int>> {
        return openableValves.map {
            val shortestPath = cave.shortestPath(workerState.position, it.key)
            it.key to it.key.flowRate * (minutesLeft - shortestPath.size - 1)
        }
    }

    private fun moveToAndOpenValve(valve: Valve, workerState: WorkerState): CaveState {
        val timeUntilOpen = if (teleporting) 1 else cave.shortestPath(workerState.position, valve).size + 1
        val newWorkerState = workerState.copy(position = valve, timeUntilOpen = timeUntilOpen)

        return copy(workers = (workers - workerState) + newWorkerState)
    }

    private fun bestValve(workerState: WorkerState) =
        openableValvesByPotentialPressureReleased(workerState).maxByOrNull { it.second }?.first

    private val tick by lazy {
        val newMinutesLeft = minutesLeft - 1
        val newWorkers = workers.map { it.copy(timeUntilOpen = it.timeUntilOpen - 1) }
        val openedValves = newWorkers.filter { it.timeUntilOpen == 0 }.map { it.position }
        val newPressureReleased = potentialPressureReleased + openedValves.sumOf { it.flowRate } * newMinutesLeft
        val newValveStates =
            valveStates.mapValues { if (openedValves.contains(it.key)) it.value.copy(open = true) else it.value }
        copy(
            minutesLeft = newMinutesLeft,
            workers = newWorkers,
            potentialPressureReleased = newPressureReleased,
            valveStates = newValveStates,
        )
    }

    override fun toString(): String =
        "CaveState(${workers}, $minutesLeft, $potentialPressureReleased, ${valveStates.map { (valve, state) -> "${valve.name}: ${if (state.open) "open" else "closed"}" }})"

    fun withTeleporting(): CaveState = copy(teleporting = true)
}
