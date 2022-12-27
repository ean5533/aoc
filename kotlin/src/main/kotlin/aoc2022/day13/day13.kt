package aoc2022.day13

import lib.*

fun main() {
  val packetPairs = parseInput()

  val orderedPairIndices = packetPairs.mapIndexedNotNull { index, (first, second) ->
    if (PacketComparator.compare(first, second) <= 0) index + 1 else null
  }
  println("Part 1: " + orderedPairIndices.sum())

  val dividerPackets = listOf(Packet.List(Packet.List(2)), Packet.List(Packet.List(6)))
  val sorted = (packetPairs.flatMap { it.toList() } + dividerPackets).sortedWith(PacketComparator)
  val dividerIndices = dividerPackets.map { sorted.indexOf(it) + 1 }
  println("Part 2: " + dividerIndices.reduce { a, b -> a * b })
}

private object PacketComparator : Comparator<Packet> {
  override fun compare(first: Packet, second: Packet): Int {
    return when {
      first is Packet.Literal && second is Packet.Literal -> first.value.compareTo(second.value)
      first is Packet.Literal && second is Packet.List -> compare(Packet.List(listOf(first)), second)
      first is Packet.List && second is Packet.Literal -> compare(first, Packet.List(listOf(second)))
      first is Packet.List && second is Packet.List -> {
        val zipComparison = first.values.zip(second.values)
          .map { compare(it.first, it.second) }
          .takeWhileInclusive { it == 0 }.lastOrNull() ?: 0
        if (zipComparison != 0) zipComparison else first.values.size.compareTo(second.values.size)
      }

      else -> throw IllegalStateException()
    }
  }
}

private fun parseInput(): List<Pair<Packet.List, Packet.List>> =
  loadResourceMatchingPackageName(object {}.javaClass).trim().lines().windowed(3, 3, true)
    .map { it.take(2).map { parseList(it.peekingIterator()) }.pair() }

private fun parse(iterator: PeekingIterator<Char>): Packet? = iterator.peek()?.let {
  when {
    it == '[' -> parseList(iterator)
    it.isDigit() -> parseLiteral(iterator)
    else -> throw IllegalStateException()
  }
}

private fun parseList(iterator: PeekingIterator<Char>): Packet.List {
  check(iterator.next() == '[')
  val list = iterator.whilePeek { it != ']' }
    .map { parse(iterator)!!.also { if (iterator.peek() == ',') iterator.next() } }
    .let { Packet.List(it.toList()) }
  check(iterator.next() == ']')
  return list
}

private fun parseLiteral(iterator: PeekingIterator<Char>) =
  iterator.takeWhilePeek { it!!.isDigit() }.joinToString("").toInt().let { Packet.Literal(it) }

private sealed interface Packet {
  data class Literal(val value: Int) : Packet
  data class List(val values: kotlin.collections.List<Packet>) : Packet {
    constructor(vararg values: Int) : this(values.map { Literal(it) })
    constructor(vararg packets: Packet) : this(packets.toList())
  }
}
