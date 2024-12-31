@file:Suppress("unused")

package lib

fun <S, T> Iterable<S>.cartesianProduct(other: Iterable<T>) =
  flatMap { first -> other.map { second -> first to second } }

// TODO: improve perf
fun <T> Iterable<T>.uniquePairs() =
  flatMapIndexed { x, first -> mapIndexedNotNull { y, second -> if(x < y) first to second else null } }

infix fun Int.smartRange(to: Int): IntProgression = if (this < to) this..to else this downTo to

fun <T> Grouping<Pair<T, Long>, T>.sumCounts(): Map<T, Long> = fold(0L) { total, count -> total + count.second }

/**
 * Like takeWhile, but also includes the first element that didn't satisfy the condition
 */
inline fun <T> Sequence<T>.takeWhileInclusive(predicate: (T) -> Boolean): List<T> {
  val list = ArrayList<T>()
  for (item in this) {
    list.add(item)
    if (!predicate(item))
      break
  }
  return list
}

/**
 * Like takeWhile, but also includes the first element that didn't satisfy the condition
 */
inline fun <T> List<T>.takeWhileInclusive(predicate: (T) -> Boolean): List<T> {
  return asSequence().takeWhileInclusive(predicate)
}

/**
 * Returns true if this range fully encompasses [other]
 */
fun IntRange.containsAll(other: IntRange): Boolean =
  start <= other.start && endInclusive >= other.endInclusive

/**
 * Returns true if this range overlaps with [other], either partially, or if either fully contains the other
 */
fun IntRange.containsAny(other: IntRange): Boolean =
  contains(other.start) || // this contains at least the beginning of [other]
    contains(other.endInclusive) || // this contains at least the end of [other]
    other.containsAll(this)

/**
 * Returns the portion of this range is is contained in [other], as well as the remaining portions of the range
 * that were not contained in [other]
 */
fun IntRange.extractSlice(other: IntRange): Pair<IntRange?, List<IntRange>> {
  return if (start > other.endInclusive || endInclusive < other.start) {
    // Other is non-overlapping, original range unaffected
    null to listOf(this)
  } else if (start < other.start && endInclusive > other.endInclusive) {
    // Other is strictly contained, producing two new ranges
    other to listOf(start until other.start, (other.endInclusive + 1)..endInclusive)
  } else if (start >= other.start && endInclusive <= other.endInclusive) {
    // Other contains original range, resulting in nothing leftover
    (start..endInclusive) to listOf()
  } else if (start >= other.start && start <= other.endInclusive) {
    // Other overlaps with beginning of original range, producing one shorter range
    (start..other.endInclusive) to listOf((other.endInclusive + 1)..endInclusive)
  } else if (endInclusive <= other.endInclusive) {
    // Other overlaps with end of original range, producing one shorter range
    (other.start..endInclusive) to listOf(start until other.start)
  } else {
    throw IllegalStateException("this should be impossible")
  }
}

fun <T> Iterator<T>.next(num: Int): List<T> {
  return (0 until num).map { next() }
}

fun <T> List<Set<T>>.intersectAll(): Set<T> {
  return reduce { prev, next -> prev.intersect(next) }
}

fun <T> List<T>.split(delimiter: T): List<List<T>> {
  val iterator = iterator()
  return generateSequence {}.takeWhile { iterator.hasNext() }.map {
    iterator.asSequence().takeWhile { it != delimiter }.toList()
  }.toList()
}


fun <T> ArrayDeque<T>.push(element: T): Unit = addLast(element)
fun <T> ArrayDeque<T>.pop(): T? = removeLastOrNull()
fun <T> ArrayDeque<T>.peek(): T? = lastOrNull()

fun <T> Sequence<T>.repeat() = sequence { while (true) yieldAll(this@repeat) }
fun <T> List<T>.repeat() = asSequence().repeat()

fun <T> List<T>.pair() = get(0) to get(1)

fun <T> List<List<T>>.transpose(): List<List<T>> {
  val new = mutableListOf<MutableList<T>>()

  (0 until this[0].size).forEach { colNum ->
    val col = mutableListOf<T>().also { new.add(it) }
    forEach { row -> col.add(row[colNum]) }
  }

  return new
}

fun <T> List<List<T>>.rotate90(): List<List<T>> {
  return transpose().map { it.reversed() }
}

fun <T> List<T>.replaceFirst(new: T): List<T> = ArrayList<T>(size).also {
  it.add(new)
  it.addAll(drop(1))
}

fun <T> List<T>.replaceFirst(transform: (T) -> T): List<T> = replaceFirst(transform(first()))

fun <T> List<T>.circularGet(index: Int) = get(index.mod(size))
fun <T> List<T>.rotate(): List<T> = drop(1) + first()
fun <T> List<T>.moveToEnd(element: T): List<T> = filter { it != element } + element

class PeekingIterator<out T>(private val iterator: Iterator<T>) : Iterator<T> {
  private var peeked: T? = null
  override fun hasNext(): Boolean = peeked?.let { true } ?: iterator.hasNext()
  override fun next(): T = peeked?.also { peeked = null } ?: iterator.next()
  fun peek(): T? = peeked ?: if (hasNext()) iterator.next().also { peeked = it } else null
  fun whilePeek(predicate: (T?) -> Boolean): Sequence<Unit> = generateSequence { }.takeWhile { predicate(peek()) }
  fun takeWhilePeek(predicate: (T?) -> Boolean): Sequence<T> = whilePeek(predicate).map { next() }
}

fun <T> Iterable<T>.peekingIterator() = PeekingIterator(iterator())
fun <T> Sequence<T>.peekingIterator() = PeekingIterator(iterator())
fun CharSequence.peekingIterator() = PeekingIterator(iterator())

fun <T> Iterable<T>.indexesOf(e: T) = indexesOf { it == e }
inline fun <T> Iterable<T>.indexesOf(predicate: (T) -> Boolean): List<Int> =
  mapIndexedNotNull{ index, elem -> index.takeIf{ predicate(elem) } }

