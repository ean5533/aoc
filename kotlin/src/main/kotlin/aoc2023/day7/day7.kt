package aoc2023.day7

import lib.loadResourceMatchingPackageName

private val input = loadResourceMatchingPackageName(object {}.javaClass).trim()

fun main() {
  val bids = input.lines().map {
    val (cards, bid) = it.split(" ")
    Bid(Hand(cards.toList()), bid.toInt())
  }
    
  println(calculateWinnings(bids, Part1Scorer()).sum())
  println(calculateWinnings(bids, Part2Scorer()).sum())
}

private fun calculateWinnings(bids: List<Bid>, scorer: Scorer): List<Int> =
  bids.sortedBy { scorer.score(it.hand) }.withIndex().map { (index, it) -> (index + 1) * it.amount }

private interface Scorer {
  fun score(hand: Hand): Long {
    fun Long.appendScorePiece(value: Int): Long {
      check(value < 100) { "Algorithm assumes two digit score pieces or lower" }
      return this * 100 + value
    }

    return 0L
      .appendScorePiece(typeScore(hand))
      .appendScorePiece(cardScore(hand.cards[0]))
      .appendScorePiece(cardScore(hand.cards[1]))
      .appendScorePiece(cardScore(hand.cards[2]))
      .appendScorePiece(cardScore(hand.cards[3]))
      .appendScorePiece(cardScore(hand.cards[4]))
  }

  fun typeScore(hand: Hand): Int
  fun cardScore(card: Char): Int
}

private class Part1Scorer : Scorer {
  private val singleCardScores = listOf('2', '3', '4', '5', '6', '7', '8', '9', 'T', 'J', 'Q', 'K', 'A')
    .withIndex().associate { (index, it) -> it to index }

  override fun typeScore(hand: Hand): Int {
    val counts = hand.cards.groupingBy { it }.eachCount()
    return when {
      counts.values.contains(5) -> 7
      counts.values.contains(4) -> 6
      counts.values.toList().sorted() == listOf(2, 3) -> 5
      counts.values.toList().sorted() == listOf(1, 1, 3) -> 4
      counts.values.toList().sorted() == listOf(1, 2, 2) -> 3
      counts.values.toList().sorted() == listOf(1, 1, 1, 2) -> 2
      counts.values.toList().sorted() == listOf(1, 1, 1, 1, 1) -> 1
      else -> throw IllegalStateException("Unscorable hand $hand")
    }
  }

  override fun cardScore(card: Char): Int =
    singleCardScores[card] ?: throw IllegalStateException("Unscorable card $card")
}

private class Part2Scorer : Scorer {
  private val singleCardScores = listOf('J', '2', '3', '4', '5', '6', '7', '8', '9', 'T', 'Q', 'K', 'A')
    .withIndex().associate { (index, it) -> it to index }

  override fun typeScore(hand: Hand): Int {
    val counts = hand.cards.groupingBy { it }.eachCount()
    val jokerCount = counts['J'] ?: 0
    val nonJokerCounts = counts - 'J'
    return when {
      (nonJokerCounts.values.maxOrNull() ?: 0) + jokerCount == 5 -> 7
      nonJokerCounts.values.max() + jokerCount == 4 -> 6
      nonJokerCounts.values.toList().sorted() == listOf(2, 3) ||
        nonJokerCounts.values.toList().sorted() == listOf(2, 2) -> 5
      nonJokerCounts.values.toList().sorted() == listOf(1, 1, 3) ||
        nonJokerCounts.values.toList().sorted() == listOf(1, 1, 2) ||
        nonJokerCounts.values.toList().sorted() == listOf(1, 1, 1) -> 4
      nonJokerCounts.values.toList().sorted() == listOf(1, 2, 2) -> 3
      nonJokerCounts.values.toList().sorted() == listOf(1, 1, 1, 2) ||
        nonJokerCounts.values.toList().sorted() == listOf(1, 1, 1, 1) -> 2
      nonJokerCounts.values.toList().sorted() == listOf(1, 1, 1, 1, 1) -> 1
      else -> throw IllegalStateException("Unscorable hand $hand")
    }
  }

  override fun cardScore(card: Char): Int =
    singleCardScores[card] ?: throw IllegalStateException("Unscorable card $card")
}

private data class Bid(val hand: Hand, val amount: Int)
private data class Hand(val cards: List<Char>) {
  init {
    check(cards.size == 5)
  }

  override fun toString(): String {
    return cards.joinToString("")
  }
}
