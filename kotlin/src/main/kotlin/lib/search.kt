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
interface SearchState<T> {
  val current: T
  val cost: Int
  val nextStates: List<SearchState<T>>
  val isSolution: Boolean
  val distanceHeuristic: Int get() = 0
}

/**
 * Performs an A* search (https://en.wikipedia.org/wiki/A*_search_algorithm), returning the lowest cost path from the initial state to a solution state.
 *
 * @param initialState The starting point for the search
 */
fun <T, S : SearchState<T>> aStarSearch(initialState: S): S? {
  val queue = PriorityQueue<S>(compareBy({ it.cost + it.distanceHeuristic })).also { it.add(initialState) }
  val seenToLowestCost = mutableMapOf(initialState.current to initialState.cost)

  while (queue.isNotEmpty()) {
    val state = queue.remove()
    if (state.isSolution) {
      return state
    }

    val nextStates = state.nextStates

    nextStates
      // Only keep searching if we have found a cheaper path to a previously found state
      .filter { nextState -> seenToLowestCost[nextState.current]?.let { nextState.cost < it } ?: true }
      .forEach { nextState ->
        @Suppress("UNCHECKED_CAST")
        queue.add(nextState as S)
        seenToLowestCost[nextState.current] = nextState.cost
      }
  }

  return null
}

/**
 * Performs an A* search (https://en.wikipedia.org/wiki/A*_search_algorithm), returning the lowest cost path from the initial state to a solution state.
 *
 * This overload can be used if you want to see all of the individual states on the path to the end state. It can also just be used as a helper to avoid creating your own [SearchState] implementation
 *
 * This overload doesn't perform as well as one where you provide your own [SearchState] due to marshalling of lots of Pair classes (about 1.5x-2x worse, from a few data points), even if [retainIntermediateStates] is false.
 *
 * Additionally, creating your own SearchState allows you to customize what data will be in the final search state (e.g. for retaining intermediate states or deltas or anything else).
 *
 * @param initialState The starting point for the search
 * @param getNextStates A function that produces the next set of possible states (plus the cost to get there) from some input state
 * @param isSolution A function that returns whether or not a given state is a valid solution state.
 * @param retainIntermediateStates If true, results will contain all intermediate states needed to achieve result. Drastically increases memory footprint, but useful for debugging.
 */
fun <T> aStarSearch(
  initialState: T,
  getNextStates: (T) -> List<Pair<T, Int>>,
  isSolution: (T) -> Boolean,
  distanceHeuristic: (T) -> Int = { 0 },
  retainIntermediateStates: Boolean = false,
): PathSearchState<T>? {
  return aStarSearch(
    PathSearchState(
      initialState,
      getNextStates,
      isSolution,
      distanceHeuristic,
      retainIntermediateStates,
      listOf()
    )
  )
}

/**
 * A search state that retains the path that was followed to arrive at it
 */
class PathSearchState<T>(
  override val current: T,
  val getNextStates: (T) -> List<Pair<T, Int>>,
  val getIsSolution: (T) -> Boolean,
  val getDistanceHeuristic: (T) -> Int = { 0 },
  val retainIntermediateStates: Boolean = false,
  val steps: List<PathStep<T>>,
) : SearchState<T> {
  override val cost = steps.sumOf { it.cost }

  override val nextStates: List<SearchState<T>> by lazy {
    getNextStates(current).map { (nextState, cost) ->
      val maybeIntermediateState = if (retainIntermediateStates) nextState else null
      PathSearchState(
        nextState,
        getNextStates,
        getIsSolution,
        getDistanceHeuristic,
        retainIntermediateStates,
        steps + PathStep(cost, maybeIntermediateState)
      )
    }
  }

  override val isSolution by lazy { getIsSolution(current) }
  override val distanceHeuristic by lazy { getDistanceHeuristic(current) }
}

data class PathStep<out T>(val cost: Int, val maybeState: T?)

/**
 * This algorithm is can be used to search a space with multiple terminal states, returning the one that has the best final score.
 *
 * This algorithm is some sort of weird bastardization of A* search, I think? I don't know, I didn't investigate existing algorithms, I just made up something that worked for the use case.
 *
 * This algorithm does not step after finding a path to some terminal state; it will keep searching other paths under the assumption that a higher scoring path to some other terminal state may exist. The algorithm uses provided heuristics to decide which paths to check first and which paths can be short-circuited. The closer these heuristics match the true score of the optimal path, the faster it will perform.
 *
 * @param trimStates If true, previously seen states will be actively discarded if they are known to never lead to a valid solution. This reduces overall memory usage at the cost of more CPU cycles (and thus wallclock time).
 * @param printSolutions If true, every solution found will be printed as it is found. Useful for debugging.
 */
fun <T, I, S : BestScoreSearchState<T, I>> bestScoreSearch(
  initialState: S,
  trimStates: Boolean = false,
  printSolutions: Boolean = false,
): S {
  val queue = PriorityQueue<S>(compareByDescending({ it.score + it.estimatedScoreToOptimalTermination }))
    .also { it.add(initialState) }
  val seenToHighestScoreState = mutableMapOf(initialState.stateIdentity to initialState)
  var bestTerminalScoreFound: Int = Int.MIN_VALUE

  while (queue.isNotEmpty()) {
    val state = queue.remove()
    if (state.isTerminal) {
      bestTerminalScoreFound = if (state.score > bestTerminalScoreFound) {
        if (trimStates) {
          queue.removeIf { it.score + it.terminationScoreUpperBound <= state.score }
          seenToHighestScoreState.entries.removeIf { it.value.score + it.value.terminationScoreUpperBound < state.score }
        }
        if (printSolutions) println(state.score)
        state.score
      } else bestTerminalScoreFound

      continue // Found a path to some solution, not necessarily optimal
    }
    if (state.score + state.terminationScoreUpperBound <= bestTerminalScoreFound) {
      continue // This path can never lead to a better solution
    }

    val nextStates = state.nextStates

    nextStates
      .filter { nextState ->
        // Only keep searching if we have found a cheaper path to a previously found state
        seenToHighestScoreState[nextState.stateIdentity]?.let { nextState.score > it.score } ?: true
      }
      .forEach { nextState ->
        @Suppress("UNCHECKED_CAST")
        queue.add(nextState as S)
        seenToHighestScoreState[nextState.stateIdentity] = nextState
      }
  }

  return seenToHighestScoreState.filter { it.value.isTerminal }.maxBy { it.value.score }.value
}

/**
 * @property score The total score from reaching this state
 * @property estimatedScoreToOptimalTermination A guess as to the final score gained to reach a terminal state from this state. The closer this is to the true score, the faster the optimal solution will be found.
 * @property terminationScoreUpperBound An upper bound to the possible score gained after reaching a terminal state from this state (e.g. every path to the destination will score at most this much). The closer this is to the true score, the faster the optimal solution will be found. Must never be lower than the true best score, or else the algorithm may provide incorrect (suboptimal) results.
 * @property nextStates Produces the next set of possible states from some input state
 * @property isTerminal Decides if a search state is a terminal (ending) state.
 * @property stateIdentity Provides a unique representation of this state for purposes of avoiding checking the same state twice.
 * @param T The type of state being searched
 * @param I The type of the unique identity of the state being searched (which may be the same as the state itself)
 */
interface BestScoreSearchState<T, I> {
  val current: T
  val score: Int
  val estimatedScoreToOptimalTermination: Int
  val terminationScoreUpperBound: Int
  val nextStates: List<BestScoreSearchState<T, I>>
  val isTerminal: Boolean
  val stateIdentity: I
}
