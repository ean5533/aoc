package lib

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.reflect.KClass

const val BLOCK_SIZE = 16

interface CryptoAlgo {
    fun encrypt(input: ByteArray, key: ByteArray): ByteArray

    fun decrypt(input: ByteArray, key: ByteArray): ByteArray
}

class Ecb : CryptoAlgo {
    private val transformation: String = "AES/ECB/NoPadding"
    private val cipher = Cipher.getInstance(transformation)

    override fun encrypt(input: ByteArray, key: ByteArray): ByteArray {
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"))
        return cipher.doFinal(input)
    }

    override fun decrypt(input: ByteArray, key: ByteArray): ByteArray {
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"))
        return cipher.doFinal(input)
    }

    companion object {
        fun isEcbEncrypted(bytes: ByteArray): Boolean =
            bytes.toList().chunked(BLOCK_SIZE).groupingBy { it }.eachCount().any { it.value > 1 }
    }
}

class Cbc(val iv: ByteArray = ByteArray(BLOCK_SIZE) { 0.toByte() }) : CryptoAlgo {
    private val ecb = Ecb()

    override fun encrypt(input: ByteArray, key: ByteArray): ByteArray {
        val (_, encrypted) = input.toList().chunked(BLOCK_SIZE)
            .fold(Pair(iv, ByteArray(0))) { (prev, runningEncryption), chunk ->
                val currentBytes = chunk.toByteArray()
                val currentEncrypted = ecb.encrypt(currentBytes.xor(prev), key)
                Pair(currentEncrypted, runningEncryption + currentEncrypted)
            }

        return encrypted
    }

    override fun decrypt(input: ByteArray, key: ByteArray): ByteArray {
        val (_, decrypted) = input.toList().chunked(BLOCK_SIZE)
            .fold(Pair(iv, ByteArray(0))) { (prev, runningDecryption), chunk ->
                val currentBytes = chunk.toByteArray()
                val currentDecrypted = ecb.decrypt(currentBytes, key).xor(prev.asSequence())
                Pair(currentBytes, runningDecryption + currentDecrypted)
            }

        return decrypted
    }
}

fun ByteArray.pkcs7Pad(length: Int = BLOCK_SIZE): ByteArray {
    val amountToAdd = (length - (size % length))
    return this + ByteArray(amountToAdd) { amountToAdd.toByte() }
}

fun ByteArray.pkcs7Unpad(): ByteArray {
    val paddingByte = last()
    return toList().dropLast(paddingByte.toInt()).toByteArray()
}

fun determineEncryptionAlgo(bytes: ByteArray): KClass<out CryptoAlgo> =
    if (Ecb.isEcbEncrypted(bytes)) Ecb::class else Cbc::class