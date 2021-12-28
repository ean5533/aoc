package lib

fun Int.modToRange(min: Int, max: Int): Int = ((this - min) % max) + min
fun Int.incrementInsideRange(min: Int, max: Int): Int = (this + 1).modToRange(min, max)