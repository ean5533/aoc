package aoc2021.day16

import lib.loadResourceAsString
import lib.next
import lib.takeWhileInclusive

private val input = loadResourceAsString("text/aoc2021/day16")


fun main() {
    val packet = parseInput()

    part1(packet)
    part2(packet)
}

private fun part1(packet: Packet) {
    println("Part 1: ${packet.sumOfVersions()}")
}

private fun part2(packet: Packet) {
    println("Part 2: ${packet.value()}")
}

private fun parseInput(): Packet {
    val bits = input.map { it.digitToInt(16).toString(2).padStart(4, '0') }.joinToString("")
    return parsePacket(bits.iterator())
}

private fun parsePacket(iterator: Iterator<Char>): Packet {
    val version = iterator.next(3).joinToString("").toInt(2)
    val type = iterator.next(3).joinToString("").toInt(2)

    return if (type == 4) {
        val bitGroups = iterator.asSequence().windowed(5, 5).takeWhileInclusive { it[0].digitToInt() == 1 }.toList()
        val literal = bitGroups.flatMap { it.drop(1) }.joinToString("").toLong(2)
        LiteralPacket(version, type, literal)
    } else {
        val lengthType = iterator.next().digitToInt()
        val subPackets = if (lengthType == 0) {
            val subPacketLength = iterator.next(15).joinToString("").toInt(2)
            val subIterator = iterator.next(subPacketLength).iterator()
            generateSequence {}.takeWhile { subIterator.hasNext() }.map { parsePacket(subIterator) }.toList()
        } else {
            val numberOfSubPackets = iterator.next(11).joinToString("").toInt(2)
            (0 until numberOfSubPackets).map { parsePacket(iterator) }
        }
        OperatorPacket(version, type, subPackets)
    }
}

private interface Packet {
    val version: Int
    val type: Int

    fun sumOfVersions(): Int
    fun value(): Long
}

private data class LiteralPacket(override val version: Int, override val type: Int, val value: Long) : Packet {
    override fun sumOfVersions(): Int {
        return version
    }

    override fun value(): Long {
        return value
    }
}

private data class OperatorPacket(override val version: Int, override val type: Int, val subPackets: List<Packet>) : Packet {
    override fun sumOfVersions(): Int {
        return version + subPackets.sumOf { it.sumOfVersions() }
    }

    override fun value(): Long {
        return when (type) {
            0 -> subPackets.sumOf { it.value() }
            1 -> subPackets.map { it.value() }.reduce { a, b -> a * b }
            2 -> subPackets.minOf { it.value() }
            3 -> subPackets.maxOf { it.value() }
            5 -> if (subPackets[0].value() > subPackets[1].value()) 1 else 0
            6 -> if (subPackets[0].value() < subPackets[1].value()) 1 else 0
            7 -> if (subPackets[0].value() == subPackets[1].value()) 1 else 0
            else -> throw RuntimeException("unknown operator packet type $type")
        }
    }
}
