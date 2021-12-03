package day3

private val classLoader: ClassLoader = object {}.javaClass.classLoader
private val input = classLoader.getResource("text/day3")!!.readText()
private val allBits: List<List<Int>> = input.lines().map { it.trim().toList().map(Char::digitToInt) }
private val columns = allBits[0].size

fun main() {
    part1()
    part2()
}

fun part1() {
    fun selectOneBitPerColumn(bits: List<List<Int>>, bitSelector: (List<List<Int>>, Int) -> Int): List<Int> {
        return (0 until columns).map { bitSelector(bits, it) }
    }

    val gamma = selectOneBitPerColumn(allBits, ::getCommonBit).bitsToDecimal()
    val epsilon = selectOneBitPerColumn(allBits, ::getUncommonBit).bitsToDecimal()

    val power = gamma * epsilon

    println("gamma=[$gamma], epsilon=[$epsilon], power=[$power]")
}

fun part2() {
    val oxygenGeneratorRating = reduceToSingleRow(allBits, 0, ::getCommonBit).bitsToDecimal()
    val co2ScrubberRating = reduceToSingleRow(allBits, 0, ::getUncommonBit).bitsToDecimal()

    val lifeSupportRating = oxygenGeneratorRating * co2ScrubberRating

    println("oxygenGeneratorRating=[$oxygenGeneratorRating], co2ScrubberRating=[$co2ScrubberRating], lifeSupportRating=[$lifeSupportRating]")
}

private fun getCommonBit(bits: List<List<Int>>, col: Int): Int {
    return getCommonAndUncommonBits(bits, col).first
}

private fun getUncommonBit(bits: List<List<Int>>, col: Int): Int {
    return getCommonAndUncommonBits(bits, col).second
}

private fun getCommonAndUncommonBits(bits: List<List<Int>>, col: Int): Pair<Int, Int> {
    val countsByBit = bits.map { it[col] }.groupingBy { it }.eachCount()
    return if (countsByBit.values.toSet().size == 1) {
        // Bits are equally common -- force common bit to be 1
        Pair(1, 0)
    } else {
        val commonBit = countsByBit.toList().maxByOrNull { it.second }!!.first
        val uncommonBit = countsByBit.toList().minByOrNull { it.second }!!.first
        Pair(commonBit, uncommonBit)
    }
}

// This function could be better generalized by changing "bitSelector" into a more general reducer function (List<List<Int>>) -> Int,
// but we'd have to manage the advancing column number through some sort of mutable counter, which seemed awkward
private fun reduceToSingleRow(bits: List<List<Int>>, column: Int, bitSelector: (List<List<Int>>, Int) -> Int): List<Int> {
    if (bits.size == 1) {
        return bits.first()
    }

    val bit = bitSelector(bits, column)
    val reducedBits = bits.filter { it[column] == bit }
    return reduceToSingleRow(reducedBits, column + 1, bitSelector)
}

private fun List<Int>.bitsToDecimal(): Int {
    return this.joinToString("").toInt(2)
}