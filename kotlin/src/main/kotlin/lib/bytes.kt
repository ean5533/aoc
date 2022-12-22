package lib

import kotlin.experimental.xor

fun String.hexToBytes(): ByteArray = chunked(2).map { it.toInt(16).toByte() }.toByteArray()

fun ByteArray.toHexString(): String = joinToString("") { it.toInt().toString(16).padStart(2, '0') }

fun ByteArray.xor(byte: Byte): ByteArray = map { it.xor(byte) }.toByteArray()

fun ByteArray.xor(bytes: ByteArray): ByteArray = xor(bytes.asSequence())

fun ByteArray.xor(bytes: Sequence<Byte>): ByteArray =
  asSequence()
    .zip(bytes)
    .map { (first, second) -> first.xor(second) }
    .toList()
    .toByteArray()

fun ByteArray.hammingDistance(other: ByteArray): Int {
  require(size == other.size)
  return asSequence()
    .zip(other.asSequence())
    .sumOf { (first, second) -> first.xor(second).toInt().toString(2).count { it == '1' } }
}
