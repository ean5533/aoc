package day17

import day11.cartesianProduct
import java.lang.Integer.max
import java.lang.Integer.min
import kotlin.math.sqrt

val input = "target area: x=253..280, y=-73..-46"

fun main() {
    val target = parseInput()

    part1(target)
    part2(target)
}

private fun parseInput(): Area {
    val (x1, x2, y1, y2) = input.replace("target area: x=", "").replace("y=", "")
        .split(", ").flatMap { it.split("..") }.map { it.toInt() }
    return Area(min(x1, x2), max(x1, x2), max(y1, y2), min(y1, y2))
}

fun part1(target: Area) {
    // The largest Y velocity is the one that will go all the way from START to TARGET AREA BOTTOM in one step (accounting for having one greater velocity due to gravity)
    val maxPossibleYVelocity = -1 * (target.bottom + 1)
    val totalHeight = (1..maxPossibleYVelocity).sum()
    println("Part 1: $totalHeight")
}

fun part2(target: Area) {
    // x=sqrt(n) is an extremely rough lower bound on the smallest number where 1+2+..+x can possibly hit n
    val minPossibleXVelocity = sqrt(target.left.toDouble()).toInt()
    val maxPossibleXVelocity = target.right
    val minPossibleYVelocity = target.bottom
    val maxPossibleYVelocity = -1 * (minPossibleYVelocity + 1)

    val velocitiesThatWillCrossTarget = (minPossibleXVelocity..maxPossibleXVelocity)
        .cartesianProduct((minPossibleYVelocity..maxPossibleYVelocity))
        .filter { (x, y) -> crossesTarget(target, ProbeState(0, 0, x, y)) }

    println("Part 2: ${velocitiesThatWillCrossTarget.size}")
}

fun crossesTarget(target: Area, probeStateInit: ProbeState): Boolean {
    return generateSequence(probeStateInit) { it.step() }
        .takeWhile { it.isInsideOrApproaching(target) }
        .last()
        .isInside(target)
}

data class Area(val left: Int, val right: Int, val top: Int, val bottom: Int)

data class ProbeState(val x: Int, val y: Int, val xVelocity: Int, val yVelocity: Int) {
    fun step(): ProbeState = ProbeState(x + xVelocity, y + yVelocity, max(xVelocity - 1, 0), yVelocity - 1)

    fun isInsideOrApproaching(target: Area): Boolean =
        y >= target.bottom && x <= target.right && !(xVelocity == 0 && x < target.left)

    fun isInside(target: Area): Boolean = x >= target.left && x <= target.right && y <= target.top && y >= target.bottom
}