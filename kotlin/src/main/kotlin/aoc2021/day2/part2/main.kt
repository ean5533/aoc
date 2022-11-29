package aoc2021.day2.part2

import lib.loadResourceAsString
import java.lang.RuntimeException

private val input = loadResourceAsString("text/aoc2021/day2")
private val commands: List<Command> = input.lines().map(Command::parse)

fun main() {
    val finalPosition = commands.fold(Position(0, 0)) { position, command ->
        command.execute(position)
    }

    println("Final position is $finalPosition, multiplied is ${finalPosition.depth * finalPosition.horizontal}")
}

data class Position(val depth: Int, val horizontal: Int)

sealed interface Command {
    fun execute(start: Position): Position

    companion object {
        fun parse(string: String): Command {
            val pieces = string.trim().split(" ")
            val distance = pieces[1].toInt()
            return when (pieces[0]) {
                "forward" -> Forward(distance)
                "down" -> Down(distance)
                "up" -> Up(distance)
                else -> throw RuntimeException("WHAT ARE YOU")
            }
        }
    }
}

data class Forward(val distance: Int) : Command {
    override fun execute(start: Position): Position {
        return start.copy(horizontal = start.horizontal + distance)
    }
}

data class Down(val distance: Int) : Command {
    override fun execute(start: Position): Position {
        return start.copy(depth = start.depth + distance)
    }
}

data class Up(val distance: Int) : Command {
    override fun execute(start: Position): Position {
        return start.copy(depth = start.depth - distance)
    }
}
