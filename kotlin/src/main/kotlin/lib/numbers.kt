package lib

fun Int.modToRange(min: Int, max: Int): Int = Math.floorMod((this - min), max - min + 1) + min
fun Int.incrementInsideRange(min: Int, max: Int, inc: Int = 1): Int = (this + inc).modToRange(min, max)

fun gcd(a: Int, b: Int): Int {
  return when {
    a < b -> gcd(b, a)
    a % b == 0 -> b
    else -> gcd(b, a % b)
  }
}

fun gcd(a: Long, b: Long): Long {
  return when {
    a < b -> gcd(b, a)
    a % b == 0L -> b
    else -> gcd(b, a % b)
  }
}

fun lcm(a: Int, b: Int): Int {
  val min = Integer.min(a, b)
  val max = Integer.max(a, b)
  return generateSequence(max) { it + max }.takeWhileInclusive { it % min != 0 }.last()
}

fun List<Int>.lcm() : Int {
  return fold(1) {lcm, next ->
    lcm * next / gcd(lcm, next)
  }
}

fun List<Long>.lcm() : Long {
  return fold(1) {lcm, next ->
    lcm * next / gcd(lcm, next)
  }
}
