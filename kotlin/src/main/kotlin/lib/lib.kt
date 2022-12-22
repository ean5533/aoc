package lib

import kotlin.system.measureTimeMillis

fun loadResourceAsString(path: String): String {
  val classLoader: ClassLoader = object {}.javaClass.classLoader
  return classLoader.getResource(path)!!.readText()
}

fun loadResourceMatchingPackageName(javaClass: Class<Any>, prefix: String = ""): String {
  val path = prefix + javaClass.packageName.replace(".", "/")
  return loadResourceAsString(path)
}

fun printTimeTaken(block: () -> Unit) {
  measureTimeMillis(block).also { println("(took $it ms)") }
}

fun <T> tryOrNull(fn: () -> T): T? = try {
  fn()
} catch (ex: Exception) {
  null
}
