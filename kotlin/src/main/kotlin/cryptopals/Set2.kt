package cryptopals

import lib.*
import java.nio.charset.Charset
import java.util.*
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.reflect.KClass

fun main() {
    Set2().test()
}

private class Set2 {
    fun test() {
        sanityCheck()
        part9()
        part10()
        part11()
        part12()
        part13()
        part14()
        part15()
        part16()
    }

    private fun sanityCheck() {
        val key = "YELLOW SUBMARINE".toByteArray()
        val input = "This is a test that is >16 chars and not a multiple of 16"

        listOf(Ecb(), Cbc()).forEach { algo ->
            val encrypted = algo.encrypt(input.toByteArray().pkcs7Pad(), key)
            val decrypted = algo.decrypt(encrypted, key).pkcs7Unpad()
            checkEquals(decrypted.toString(Charset.defaultCharset()), input)
        }
    }

    private fun part9() {
        val padded = "YELLOW SUBMARINE".toByteArray().pkcs7Pad(20)
        checkEquals(padded.toString(Charset.defaultCharset()), """YELLOW SUBMARINE""")
        checkEquals(padded.pkcs7Unpad().toString(Charset.defaultCharset()), "YELLOW SUBMARINE")
    }

    private fun part10() {
        val input = loadResourceAsString("text/cryptopals/set2part10").lines().joinToString("")
        val inputBytes = Base64.getDecoder().decode(input)
        val key = "YELLOW SUBMARINE".toByteArray()
        val cbcDecrypted = Cbc().decrypt(inputBytes, key)
        checkEquals(cbcDecrypted.toString(Charset.defaultCharset()).lines()[0], "I'm back and I'm ringin' the bell ")
    }

    private fun part11() {
        // Input needs to have at least 3 repeating blocks or it won't be reliably detectable as ECB
        val input = ByteArray(50)

        (0..20).forEach {
            val (actualAlgo, encrypted) = randomlyEncrypt(input)
            val detectedAlgo = determineEncryptionAlgo(encrypted)
            checkEquals(actualAlgo, detectedAlgo)
        }
    }

    private fun part12() {
        val key = Random.nextBytes(BLOCK_SIZE)
        val ecb = Ecb()
        val original = Base64.getDecoder().decode(
            "Um9sbGluJyBpbiBteSA1LjAKV2l0aCBteSByYWctdG9wIGRvd24gc28gbXkgaGFpciBjYW4gYmxvdwpUaGUgZ2lybGllcyBvbiBzdGFuZGJ5IHdhdmluZyBqdXN0IHRvIHNheSBoaQpEaWQgeW91IHN0b3A/IE5vLCBJIGp1c3QgZHJvdmUgYnkK"
        )

        fun oracleEncrypt(input: ByteArray): ByteArray = ecb.encrypt((input + original).pkcs7Pad(), key)

        fun determineBlockSizeAndPayloadSize(): Pair<Int, Int> {
            // Feed the oracle successively larger pads until a result is longer than feeding in a shorter pad.
            // The longer pad will be BLOCK_SIZE bytes longer than the previous.
            // We will also know how far away from the block boundary the original payload is based on how many bytes we added to cross it.
            val sizeChanges = (0..20).map { oracleEncrypt(ByteArray(it)).size }.zipWithNext().map { (a, b) -> b - a }
            val sizeChangedAt = sizeChanges.indexOfFirst { it > 0 }

            val blockSize = sizeChanges[sizeChangedAt]
            val payloadSize = oracleEncrypt(ByteArray(0)).size - sizeChangedAt - 1

            return blockSize to payloadSize
        }

        val (blockSize, payloadSize) = determineBlockSizeAndPayloadSize()
        checkEquals(blockSize, 16)
        checkEquals(payloadSize, original.size)

        checkEquals(determineEncryptionAlgo(oracleEncrypt(ByteArray(50))), Ecb::class)

        val decryptedBytes = (0 until payloadSize).fold(ByteArray(0)) { knownBytes, _ ->
            val prefixSize = (blockSize - 1 - knownBytes.size).mod(blockSize)
            val prefix = ByteArray(prefixSize)

            val targetBlockNumber = knownBytes.size / blockSize
            val targetBlock = oracleEncrypt(prefix).drop(targetBlockNumber * blockSize).take(blockSize)

            val foundByte = (0..256).firstNotNullOf {
                val guessByte = it.toByte()
                val encryptedGuessBlock =
                    oracleEncrypt(prefix + knownBytes + guessByte).drop(targetBlockNumber * blockSize).take(blockSize)
                if (encryptedGuessBlock == targetBlock) guessByte else null
            }

            knownBytes + foundByte
        }

        checkEquals(decryptedBytes.toString(Charset.defaultCharset()).lines()[0], "Rollin' in my 5.0")
    }

    private fun part13() {
        fun parse(input: String): Map<String, String> =
            input.split('&').associate { val (k, v) = it.split('='); k to v }

        fun Map<String, String>.toUriParamString() = entries.joinToString("&") { "${it.key}=${it.value}" }

        // A good URI encoder would encode away our padding bytes -- so we won't use one
        fun weakEncode(input: String) = input.replace("&", "%26").replace("=", "%3D")

        fun profileFor(input: String) = mapOf(
            "email" to weakEncode(input),
            "uid" to "10",
            "role" to "user"
        )

        val key = Random.nextBytes(BLOCK_SIZE)
        fun oracleEncrypt(input: String) =
            Ecb().encrypt(profileFor(input).toUriParamString().toByteArray().pkcs7Pad(), key)

        // Create an encrypted message whose second-to-last block ends with "&role=". Grab all but the last block.
        val attack1 = oracleEncrypt("aaaaaa@me.com")
        val attack1Chunks = attack1.toList().chunked(16).dropLast(1)

        // Generate an encrypted message whose second block is "admin" (with padding). Grab that block.
        val adminBlock = "admin".toByteArray().pkcs7Pad()
        val attack2 = oracleEncrypt("bbb@me.com".toByteArray().plus(adminBlock).toString(Charset.defaultCharset()))
        val attack2Chunks = attack2.toList().chunked(16).drop(1).take(1)

        // Smash'em together into a token with forged authorization
        val blocks = attack1Chunks + attack2Chunks
        val fabricatedBytes = blocks.flatMap { it }.toByteArray()

        val decryptedString = Ecb().decrypt(fabricatedBytes, key).pkcs7Unpad().toString(Charset.defaultCharset())
        val parsed = parse(decryptedString)
        checkEquals(parsed, mapOf("email" to "aaaaaa@me.com", "uid" to "10", "role" to "admin"))
    }

    private fun part14() {

    }

    private fun part15() {

    }

    private fun part16() {

    }

    private fun randomlyEncrypt(
        input: ByteArray,
        key: ByteArray = Random.nextBytes(16),
        actualAlgo: CryptoAlgo = if (Random.nextInt(2) == 0) Cbc(Random.nextBytes(16)) else Ecb()
    ): Pair<KClass<out CryptoAlgo>, ByteArray> {
        val inputJunked = Random.nextBytes(Random.nextInt(5..10)) + input + Random.nextBytes(Random.nextInt(5..10))
        val encrypted = actualAlgo.encrypt(inputJunked.pkcs7Pad(), key)
        return Pair(actualAlgo::class, encrypted)
    }
}