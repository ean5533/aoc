package aoc2022.day19

import lib.BestScoreSearchState
import lib.bestScoreSearch
import lib.loadResourceMatchingPackageName
import lib.printTimeTaken

private val blueprints = parseInput()

fun main() {
    part1()
    part2()
}

private fun part1() {
    printTimeTaken {
        println("Start part1...")
        var qualityLevelSum = 0
        blueprints.asSequence().map { blueprint ->
            bestScoreSearch(GeodeSearchState(ResourceState(blueprint, 24))).current
        }.forEachIndexed { index, it ->
            val qualityLevel = (index + 1) * it.geode
            println("Blueprint ${index + 1} (of ${blueprints.size}) produced ${it.geode} geodes, so quality level $qualityLevel ($it)")
            qualityLevelSum += qualityLevel
        }
        println("part1: $qualityLevelSum")
    }
}

private fun part2() {
    printTimeTaken {
        println("Start part2...")
        var geodeProduct = 1
        blueprints.take(3).asSequence().map { blueprint ->
            bestScoreSearch(GeodeSearchState(ResourceState(blueprint, 32))).current
        }.forEachIndexed { index, it ->
            println("Blueprint ${index + 1} (of ${blueprints.size}) produced ${it.geode} geodes ($it)")
            geodeProduct *= it.geode
        }
        println("part2: $geodeProduct")
    }
}

private fun parseInput(): List<Blueprint> {
    val regex =
        "Blueprint [0-9]+: Each ore robot costs ([0-9]+) ore. Each clay robot costs ([0-9]+) ore. Each obsidian robot costs ([0-9]+) ore and ([0-9]+) clay. Each geode robot costs ([0-9]+) ore and ([0-9]+) obsidian.".toRegex()
    return loadResourceMatchingPackageName(object {}.javaClass).trim().lines().map {
        regex.matchEntire(it)!!.groupValues.drop(1).map { it.toInt() }
            .let { Blueprint(it[0], it[1], it[2], it[3], it[4], it[5]) }
    }
}

private class GeodeSearchState(override val current: ResourceState) : BestScoreSearchState<ResourceState, ResourceState> {
    override val score: Int = current.geode

    // All current geode bots produce geodes until time runs out
    override val estimatedScoreToOptimalTermination: Int = current.geode + current.geodeBot * (current.timeLeft)

    // All the current geode bots, plus a new geode bot every minute, all producing a geodes until time runs out
    override val terminationScoreUpperBound: Int = (current.geodeBot..current.timeLeft + current.geodeBot).sumOf { it }

    override val nextStates by lazy { current.nextAvailableMoves().map { GeodeSearchState(it) } }
    override val isTerminal = current.timeLeft == 0
    override val stateIdentity: ResourceState = current
}

private data class Blueprint(
    val oreBotOreCost: Int,
    val clayBotOreCost: Int,
    val obsidianBotOreCost: Int,
    val obsidianBotClayCost: Int,
    val geodeBotOreCost: Int,
    val geodeBotObsidianCost: Int,
) {
    val oreMaxCost = listOf(oreBotOreCost, clayBotOreCost, obsidianBotOreCost, geodeBotOreCost).max()
    val clayMaxCost = obsidianBotClayCost
    val obsidianMaxCost = geodeBotObsidianCost
}

private data class ResourceState(
    val blueprint: Blueprint,
    val timeLeft: Int,
    val ore: Int = 0,
    val oreBot: Int = 1,
    val oreBotsIncoming: Int = 0,
    val clay: Int = 0,
    val clayBot: Int = 0,
    val clayBotsIncoming: Int = 0,
    val obsidian: Int = 0,
    val obsidianBot: Int = 0,
    val obsidianBotsIncoming: Int = 0,
    val geode: Int = 0,
    val geodeBot: Int = 0,
    val geodeBotsIncoming: Int = 0,
) {
    fun tick(): ResourceState {
        return copy(
            timeLeft = timeLeft - 1,
            ore = ore + oreBot,
            oreBot = oreBot + oreBotsIncoming,
            oreBotsIncoming = 0,
            clay = clay + clayBot,
            clayBot = clayBot + clayBotsIncoming,
            clayBotsIncoming = 0,
            obsidian = obsidian + obsidianBot,
            obsidianBot = obsidianBot + obsidianBotsIncoming,
            obsidianBotsIncoming = 0,
            geode = geode + geodeBot,
            geodeBot = geodeBot + geodeBotsIncoming,
            geodeBotsIncoming = 0
        )
    }

    fun nextAvailableMoves(): List<ResourceState> {
        return listOf(
            this,
            copy(
                ore = ore - blueprint.oreBotOreCost,
                oreBotsIncoming = oreBotsIncoming + 1
            ),
            copy(
                ore = ore - blueprint.clayBotOreCost,
                clayBotsIncoming = clayBotsIncoming + 1
            ),
            copy(
                ore = ore - blueprint.obsidianBotOreCost,
                clay = clay - blueprint.obsidianBotClayCost,
                obsidianBotsIncoming = obsidianBotsIncoming + 1
            ),
            copy(
                ore = ore - blueprint.geodeBotOreCost,
                obsidian = obsidian - blueprint.geodeBotObsidianCost,
                geodeBotsIncoming = geodeBotsIncoming + 1
            ),
        )
            .filter { it.ore >= 0 && it.clay >= 0 && it.obsidian >= 0 && it.geode >= 0 } // don't overspend
            .filter { it.oreBot + it.oreBotsIncoming <= it.blueprint.oreMaxCost } // don't produce more ore than we can spend in a turn
            .filter { it.clayBot + it.clayBotsIncoming <= it.blueprint.clayMaxCost } // don't produce more clay than we can spend in a turn
            .filter { it.obsidianBot + it.obsidianBotsIncoming <= it.blueprint.obsidianMaxCost } // don't produce more obsidian than we can spend in a turn
            .map { it.tick() }
    }
}
