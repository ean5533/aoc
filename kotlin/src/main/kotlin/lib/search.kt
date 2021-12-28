package lib

import java.util.*

/**
 * A representation of the current state of search.
 *
 * Each state needs to know the total cost to arrive at it, whether or not it is a solution, and what the next possible states are.
 *
 * Storing additional information in the search state (such as the complete path needed to arrive at some state) is not necessary, but callers
 * may do so if it satisfies their needs.
 */
abstract class SearchState<T>(val current: T) {
    abstract val cost: Int
    abstract fun getNextStates(): List<SearchState<T>>
    abstract fun isSolution(): Boolean
}

/**
 * Performs an A* search (https://en.wikipedia.org/wiki/A*_search_algorithm), returning the lowest cost path from the initial state to a solution state.
 *
 * @param initialState The starting point for the search
 */
fun <T> aStarSearch(initialState: SearchState<T>): SearchState<T>? {
    val queue = PriorityQueue<SearchState<T>>(compareBy({ it.cost })).also { it.add(initialState) }
    val seenToLowestCost = mutableMapOf(initialState.current to 0)

    while (queue.isNotEmpty()) {
        val path = queue.remove()
        if (path.isSolution()) {
            return path
        }

        val nextStates = path.getNextStates()

        nextStates
            // Only keep searching if we have found a cheaper path to a previously found state
            .filter { nextState -> seenToLowestCost[nextState.current]?.let { nextState.cost < it } ?: true }
            .forEach { nextState ->
                queue.add(nextState)
                seenToLowestCost[nextState.current] = nextState.cost
            }
    }

    return null
}

/**
 * Performs an A* search (https://en.wikipedia.org/wiki/A*_search_algorithm), returning the lowest cost path from the initial state to a solution state.
 *
 * This overload is just a helper to avoid creating your own [SearchState] implementation, but it doesn't perform as well due to marshalling of lots of Pair classes (about 1.5x-2x worse, from a few data points).
 *
 * Additionally, creating your own SearchState allows you to customize what data will be in the final search state (e.g. for retaining intermediate states or deltas or anything else).
 *
 * @param initialState The starting point for the search
 * @param getNextStates A function that produces the next state of possible states (plus the cost to get there) from some input state
 * @param isSolution A function that returns whether or not a given state is a valid solution state.
 * @param retainIntermediateStates If true, results will contain all intermediate states needed to achieve result. Drastically increases memory footprint, but useful for debugging.
 */
fun <T> aStarSearch(
    initialState: T,
    getNextStates: (T) -> List<Pair<T, Int>>,
    isSolution: (T) -> Boolean,
    retainIntermediateStates: Boolean = false
): SearchState<T>? {
    data class Step(val cost: Int, val maybeState: T?)

    class Path(currentState: T, val steps: List<Step>) : SearchState<T>(currentState) {
        override val cost = steps.sumOf { it.cost }

        override fun getNextStates(): List<SearchState<T>> {
            return getNextStates(current).map { (nextState, cost) ->
                val maybeIntermediateState = if (retainIntermediateStates) nextState else null
                Path(nextState, steps + Step(cost, maybeIntermediateState))
            }
        }

        override fun isSolution(): Boolean {
            return isSolution(current)
        }
    }

    return aStarSearch(Path(initialState, listOf()))
}