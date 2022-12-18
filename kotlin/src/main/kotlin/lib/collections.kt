@file:Suppress("unused")

package lib

fun <S, T> Iterable<S>.cartesianProduct(other: Iterable<T>) =
    flatMap { first -> other.map { second -> first to second } }

infix fun Int.smartRange(to: Int): IntProgression {
    return if (this < to) this..to else this downTo to
}

fun <T> Grouping<Pair<T, Long>, T>.sumCounts(): Map<T, Long> {
    return fold(0L) { total, count -> total + count.second }
}

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
    this.start <= other.start && this.endInclusive >= other.endInclusive

/**
 * Returns true if this range overlaps with [other], either partially, or if either fully contains the other
 */
fun IntRange.containsAny(other: IntRange): Boolean =
    this.contains(other.start) || // this contains at least the beginning of [other]
        this.contains(other.endInclusive) || // this contains at least the end of [other]
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

fun <T> List<T>.pair() = this[0] to this[1]

fun <T> List<List<T>>.transpose(): List<List<T>> {
    val new = mutableListOf<MutableList<T>>()

    (0 until this[0].size).forEach { colNum ->
        val col = mutableListOf<T>().also { new.add(it) }
        this.forEach { row -> col.add(row[colNum]) }
    }

    return new
}

fun <T> List<List<T>>.rotate90(): List<List<T>> {
    return this.transpose().map { it.reversed() }
}

fun <T> List<T>.replaceFirst(new: T): List<T> = ArrayList<T>(this.size).also { 
    it.add(new)
    it.addAll(drop(1))
}

fun <T> List<T>.replaceFirst(transform: (T) -> T): List<T> = replaceFirst(transform(first()))
