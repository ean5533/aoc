package day2.part1

import lib.loadResourceAsString
import java.lang.RuntimeException

private val input = loadResourceAsString("text/day2")
private val commands: List<Command> = input.lines().map(Command.Companion::parse)

fun main() {
    val finalPosition = commands.fold(Position(0, 0, 0)) { position, command ->
        command.execute(position)
    }

    println("Final position is $finalPosition, multiplied is ${finalPosition.depth * finalPosition.horizontal}")
}

data class Position(val depth: Int, val horizontal: Int, val aim: Int)

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

data class Forward(val amount: Int) : Command {
    override fun execute(start: Position): Position {
        return start.copy(
            horizontal = start.horizontal + amount,
            depth = start.depth + amount * start.aim
        )
    }
}

data class Down(val amount: Int) : Command {
    override fun execute(start: Position): Position {
        return start.copy(aim = start.aim + amount)
    }
}

data class Up(val amount: Int) : Command {
    override fun execute(start: Position): Position {
        return start.copy(aim = start.aim - amount)
    }
}