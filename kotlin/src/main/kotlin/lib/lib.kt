package lib

import kotlin.system.measureTimeMillis

fun loadResourceAsString(path: String): String {
    val classLoader: ClassLoader = object {}.javaClass.classLoader
    return classLoader.getResource(path)!!.readText()
}

fun printTimeTaken(block: () -> Unit) {
    measureTimeMillis(block).also { println("(took $it ms)") }
}