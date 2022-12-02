package lib

fun Int.modToRange(min: Int, max: Int): Int = Math.floorMod((this - min), max - min + 1) + min
fun Int.incrementInsideRange(min: Int, max: Int, inc: Int = 1): Int = (this + inc).modToRange(min, max)
