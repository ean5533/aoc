package lib

fun Int.modToRange(min: Int, max: Int): Int = Math.floorMod((this - min), max - min + 1) + min
fun Int.incrementInsideRange(min: Int, max: Int, inc: Int = 1): Int = (this + inc).modToRange(min, max)

fun lcm(a: Int, b: Int): Int {
  val min = Integer.min(a, b)
  val max = Integer.max(a, b)
  return generateSequence(max) { it + max }.takeWhileInclusive { it % min != 0 }.last()
}
