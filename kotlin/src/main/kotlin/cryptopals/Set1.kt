package cryptopals

import lib.*
import java.nio.charset.Charset
import java.util.*

fun main() {
    Set1().test()
}

private class Set1 {
    fun test() {
        part1()
        part2()
        part3()
        part4()
        part5()
        part6()
        part7()
        part8()
    }

    private fun part1() {
        val encoded =
            "49276d206b696c6c696e6720796f757220627261696e206c696b65206120706f69736f6e6f7573206d757368726f6f6d"
                .hexToBytes()
                .let { Base64.getEncoder().encode(it) }
        val expected = "SSdtIGtpbGxpbmcgeW91ciBicmFpbiBsaWtlIGEgcG9pc29ub3VzIG11c2hyb29t"
        checkEquals(encoded.toString(Charset.defaultCharset()), expected)
    }

    private fun part2() {
        val xor =
            "1c0111001f010100061a024b53535009181c".hexToBytes()
                .xor("686974207468652062756c6c277320657965".hexToBytes())
                .toHexString()
        val expectedXOr = "746865206b696420646f6e277420706c6179"
        checkEquals(xor, expectedXOr)
    }

    private fun part3() {
        val encrypted = "1b37373331363f78151b7f2b783431333d78397828372d363c78373e783a393b3736".hexToBytes()
        val (_, decrypted) = findBestDecryption(encrypted)
        checkEquals(decrypted, "Cooking MC's like a pound of bacon")
    }

    private fun part4() {
        val encryptedStrings = loadResourceAsString("text/cryptopals/set1part4").lines()
        val (_, bestDecryption) = encryptedStrings
            .map { findBestDecryption(it.trim().hexToBytes()) }
            .maxByOrNull { (_, decrypted) -> score(decrypted) }!!
        checkEquals(bestDecryption, "Now that the party is jumping\n")
    }

    private fun part5() {
        val input =
            """
            Burning 'em, if you ain't quick and nimble
            I go crazy when I hear a cymbal
            """.trimIndent()
        val rotatingKey = "ICE".toByteArray().asSequence().repeat()
        val encrypted = input.toByteArray().xor(rotatingKey).toHexString()
        val expected = """
            0b3637272a2b2e63622c2e69692a23693a2a3c6324202d623d63343c2a26226324272765272
            a282b2f20430a652e2c652a3124333a653e2b2027630c692b20283165286326302e27282f
            """.trimIndent().replace("\n", "")
        checkEquals(encrypted, expected)
    }

    private fun part6() {
        checkEquals("this is a test".toByteArray().hammingDistance("wokka wokka!!!".toByteArray()), 37)

        val input = loadResourceAsString("text/cryptopals/set1part6").lines().joinToString("")
        val unencoded = Base64.getDecoder().decode(input).toList()
        val keyScores = (2..40).associateWith { keySize ->
            val chunks = unencoded.asSequence().windowed(keySize, keySize, false).map { it.toByteArray() }.toList()
            val score = chunks
                .zipWithNext()
                .map { (first, second) -> first.hammingDistance(second) }
                .average()
            score / keySize
        }
        val bestKeySize = keyScores.minByOrNull { it.value }!!.key
        checkEquals(bestKeySize, 29)

        val chunks = unencoded.chunked(bestKeySize)
        val key = (0 until bestKeySize)
            .map { col -> chunks.mapNotNull { it.elementAtOrNull(col) }.toByteArray() }
            .map { findBestDecryption(it).first }
        checkEquals(key.toByteArray().toString(Charset.defaultCharset()), "Terminator X: Bring the noise")

        val decrypted = unencoded.toByteArray().xor(key.asSequence().repeat())
        checkEquals(decrypted.toString(Charset.defaultCharset()).lines()[0], "I'm back and I'm ringin' the bell ")
    }

    private fun part7() {
        val input = loadResourceAsString("text/cryptopals/set1part7").lines().joinToString("")
        val inputBytes = Base64.getDecoder().decode(input)
        val key = "YELLOW SUBMARINE".toByteArray()
        val decrypted = Ecb().decrypt(inputBytes, key)
        checkEquals(decrypted.toString(Charset.defaultCharset()).lines()[0], "I'm back and I'm ringin' the bell ")
    }

    private fun part8() {
        val input = loadResourceAsString("text/cryptopals/set1part8")
        val ecbEncryptedLineIndex = input.lines().indexOfFirst { it.hexToBytes().let { Ecb.isEcbEncrypted(it) } }
        checkEquals(ecbEncryptedLineIndex, 132)
    }

    private fun findBestDecryption(encrypted: ByteArray): Pair<Byte, String> = (0..255)
        .map { it.toByte() }
        .map { it to encrypted.xor(it).toString(Charset.defaultCharset()) }
        .maxByOrNull { (_, decrypted) -> score(decrypted) }!!

    private fun score(it: String): Double = it.count { it.isLetter() || it == ' ' }.toDouble() / it.length
}