package com.webtoapp.core.crypto

import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object AesCryptoEngine {

    private const val TAG = "AesCryptoEngine"
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_ALGORITHM = "AES"
    private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"

    private val keyCache = ConcurrentHashMap<String, CachedKey>()
    private const val KEY_CACHE_TTL_MS = 5 * 60 * 1000L
    private const val MAX_CACHE_SIZE = 10

    private val secureRandom = SecureRandom()

    private data class CachedKey(
        val key: SecretKey,
        val createdAt: Long = System.currentTimeMillis()
    ) {
        fun isExpired(): Boolean = System.currentTimeMillis() - createdAt > KEY_CACHE_TTL_MS
    }

    fun encrypt(
        plainData: ByteArray,
        password: String,
        associatedData: ByteArray? = null
    ): ByteArray {
        try {

            val salt = ByteArray(16).also { secureRandom.nextBytes(it) }
            val iv = ByteArray(CryptoConstants.AES_GCM_IV_SIZE).also { secureRandom.nextBytes(it) }

            val secretKey = deriveKey(password, salt)

            val cipher = Cipher.getInstance(ALGORITHM)
            val gcmSpec = GCMParameterSpec(CryptoConstants.AES_GCM_TAG_SIZE, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

            associatedData?.let { cipher.updateAAD(it) }

            val encryptedData = cipher.doFinal(plainData)

            return buildEncryptedPackage(salt, iv, encryptedData, associatedData != null)

        } catch (e: Exception) {
            AppLogger.e(TAG, "Encryption failed", e)
            throw CryptoException(Strings.cryptoEncryptFailed.format(e.message), e)
        }
    }

    fun decrypt(
        encryptedPackage: ByteArray,
        password: String,
        associatedData: ByteArray? = null
    ): ByteArray {
        try {

            val (salt, iv, encryptedData, hasAAD) = parseEncryptedPackage(encryptedPackage)

            if (hasAAD && associatedData == null) {
                throw CryptoException("缺少关联数据")
            }

            val secretKey = deriveKey(password, salt)

            val cipher = Cipher.getInstance(ALGORITHM)
            val gcmSpec = GCMParameterSpec(CryptoConstants.AES_GCM_TAG_SIZE, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

            associatedData?.let { cipher.updateAAD(it) }

            return cipher.doFinal(encryptedData)

        } catch (e: CryptoException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Decryption failed", e)
            throw CryptoException(Strings.cryptoDecryptFailed.format(e.message), e)
        }
    }

    fun encryptWithKey(
        plainData: ByteArray,
        secretKey: SecretKey,
        associatedData: ByteArray? = null
    ): ByteArray {
        try {
            val iv = ByteArray(CryptoConstants.AES_GCM_IV_SIZE).also { secureRandom.nextBytes(it) }

            val cipher = Cipher.getInstance(ALGORITHM)
            val gcmSpec = GCMParameterSpec(CryptoConstants.AES_GCM_TAG_SIZE, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

            associatedData?.let { cipher.updateAAD(it) }

            val encryptedData = cipher.doFinal(plainData)

            return ByteArrayOutputStream().use { baos ->
                DataOutputStream(baos).use { dos ->
                    dos.write(iv)
                    dos.write(encryptedData)
                }
                baos.toByteArray()
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Encryption failed", e)
            throw CryptoException(Strings.cryptoEncryptFailed.format(e.message), e)
        }
    }

    fun decryptWithKey(
        encryptedPackage: ByteArray,
        secretKey: SecretKey,
        associatedData: ByteArray? = null
    ): ByteArray {
        try {
            if (encryptedPackage.size < CryptoConstants.AES_GCM_IV_SIZE) {
                throw CryptoException("加密数据太短")
            }

            val iv = encryptedPackage.copyOfRange(0, CryptoConstants.AES_GCM_IV_SIZE)
            val encryptedData = encryptedPackage.copyOfRange(CryptoConstants.AES_GCM_IV_SIZE, encryptedPackage.size)

            val cipher = Cipher.getInstance(ALGORITHM)
            val gcmSpec = GCMParameterSpec(CryptoConstants.AES_GCM_TAG_SIZE, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

            associatedData?.let { cipher.updateAAD(it) }

            return cipher.doFinal(encryptedData)
        } catch (e: CryptoException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Decryption failed", e)
            throw CryptoException(Strings.cryptoDecryptFailed.format(e.message), e)
        }
    }

    /**
     * Builds a GCM decryption cipher for streaming use. The IV is passed
     * separately because streaming callers read it off the asset header.
     */
    fun newDecryptCipher(
        secretKey: SecretKey,
        iv: ByteArray,
        associatedData: ByteArray?
    ): Cipher {
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(
            Cipher.DECRYPT_MODE,
            secretKey,
            GCMParameterSpec(CryptoConstants.AES_GCM_TAG_SIZE, iv)
        )
        associatedData?.let { cipher.updateAAD(it) }
        return cipher
    }

    /** Builds a GCM encryption cipher for streaming use. */
    fun newEncryptCipher(
        secretKey: SecretKey,
        iv: ByteArray,
        associatedData: ByteArray?
    ): Cipher {
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(
            Cipher.ENCRYPT_MODE,
            secretKey,
            GCMParameterSpec(CryptoConstants.AES_GCM_TAG_SIZE, iv)
        )
        associatedData?.let { cipher.updateAAD(it) }
        return cipher
    }

    /** Fresh random IV for a streaming encrypt. */
    fun newRandomIv(): ByteArray =
        ByteArray(CryptoConstants.AES_GCM_IV_SIZE).also { secureRandom.nextBytes(it) }

    /**
     * Encrypts [plain] chunk by chunk into [sink]. The GCM tag is appended by
     * the final [Cipher.doFinal], so [sink] receives ciphertext + tag.
     */
    fun encryptStreaming(
        plain: InputStream,
        cipher: Cipher,
        sink: OutputStream,
        bufferSize: Int = DEFAULT_STREAM_BUFFER_SIZE
    ): Long {
        val buffer = ByteArray(bufferSize)
        var total = 0L

        while (true) {
            val read = plain.read(buffer)
            if (read < 0) break
            if (read == 0) continue
            val chunk = cipher.update(buffer, 0, read)
            if (chunk != null && chunk.isNotEmpty()) {
                sink.write(chunk)
                total += chunk.size
            }
        }

        val tail = cipher.doFinal()
        if (tail.isNotEmpty()) {
            sink.write(tail)
            total += tail.size
        }
        sink.flush()
        return total
    }

    /**
     * Decrypts [encrypted] chunk by chunk into [sink] instead of materialising
     * the plaintext in the heap. A 60 MB asset costs a [bufferSize]-sized array
     * plus whatever the cipher hands back per chunk.
     *
     * GCM authenticates only at [Cipher.doFinal], which runs after the last
     * chunk has already been written. A tampered asset therefore leaves partial
     * plaintext in [sink]; callers must discard their output when this throws.
     */
    fun decryptStreaming(
        encrypted: InputStream,
        secretKey: SecretKey,
        iv: ByteArray,
        associatedData: ByteArray?,
        sink: OutputStream,
        bufferSize: Int = DEFAULT_STREAM_BUFFER_SIZE
    ): Long {
        val cipher = newDecryptCipher(secretKey, iv, associatedData)
        val buffer = ByteArray(bufferSize)
        var total = 0L

        while (true) {
            val read = encrypted.read(buffer)
            if (read < 0) break
            if (read == 0) continue
            val chunk = cipher.update(buffer, 0, read)
            if (chunk != null && chunk.isNotEmpty()) {
                sink.write(chunk)
                total += chunk.size
            }
        }

        val tail = cipher.doFinal()
        if (tail.isNotEmpty()) {
            sink.write(tail)
            total += tail.size
        }
        sink.flush()
        return total
    }

    /**
     * Wraps an already-initialised GCM decrypt cipher as a stream. The tag sits
     * at the very end of the ciphertext, so the final [Cipher.doFinal] runs on
     * the last read and is where a corrupted asset surfaces as an exception.
     */
    internal class GcmDecryptingInputStream(
        private val source: InputStream,
        private val cipher: Cipher,
        private val bufferSize: Int = DEFAULT_STREAM_BUFFER_SIZE
    ) : InputStream() {

        private val cipherTextBuffer = ByteArray(bufferSize)
        private var plainBuffer: ByteArray? = null
        private var plainOffset = 0
        private var sourceExhausted = false

        override fun read(): Int {
            val single = ByteArray(1)
            return if (read(single, 0, 1) == -1) -1 else single[0].toInt() and 0xFF
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            if (len == 0) return 0
            while (true) {
                val buffered = plainBuffer
                if (buffered != null) {
                    if (plainOffset < buffered.size) {
                        val count = minOf(len, buffered.size - plainOffset)
                        System.arraycopy(buffered, plainOffset, b, off, count)
                        plainOffset += count
                        return count
                    }
                    plainBuffer = null
                    plainOffset = 0
                }
                if (sourceExhausted) return -1

                val read = source.read(cipherTextBuffer)
                if (read < 0) {
                    sourceExhausted = true
                    val tail = cipher.doFinal()
                    if (tail.isEmpty()) return -1
                    plainBuffer = tail
                    continue
                }
                if (read == 0) continue

                val plain = cipher.update(cipherTextBuffer, 0, read)
                if (plain == null || plain.isEmpty()) continue
                plainBuffer = plain
            }
        }

        override fun available(): Int {
            val buffered = plainBuffer
            return if (buffered == null) 0 else buffered.size - plainOffset
        }

        override fun close() {
            source.close()
        }
    }

    private const val DEFAULT_STREAM_BUFFER_SIZE = 64 * 1024

    fun deriveKey(password: String, salt: ByteArray): SecretKey {
        return deriveKey(password, salt, CryptoConstants.PBKDF2_ITERATIONS)
    }

    fun deriveKey(password: String, salt: ByteArray, iterations: Int): SecretKey {
        val spec = PBEKeySpec(
            password.toCharArray(),
            salt,
            iterations,
            CryptoConstants.AES_KEY_SIZE
        )

        return try {
            val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
            val keyBytes = factory.generateSecret(spec).encoded
            SecretKeySpec(keyBytes, KEY_ALGORITHM)
        } finally {

            spec.clearPassword()
        }
    }

    fun deriveKeyWithCache(password: String, salt: ByteArray): SecretKey {

        val cacheKey = (password + ":" + salt.toHexString()).toByteArray().sha256().toHexString()

        cleanExpiredCache()

        keyCache[cacheKey]?.let { cached ->
            if (!cached.isExpired()) {
                return cached.key
            }
            keyCache.remove(cacheKey)
        }

        val key = deriveKey(password, salt)

        if (keyCache.size < MAX_CACHE_SIZE) {
            keyCache.putIfAbsent(cacheKey, CachedKey(key))
        }

        return key
    }

    private fun cleanExpiredCache() {
        val expiredKeys = keyCache.entries
            .filter { it.value.isExpired() }
            .map { it.key }

        expiredKeys.forEach { keyCache.remove(it) }
    }

    fun clearKeyCache() {
        keyCache.clear()
    }

    fun deriveKeyFromPackage(packageName: String, signature: ByteArray): SecretKey {
        return deriveKeyFromPackage(packageName, signature, CryptoConstants.PBKDF2_ITERATIONS, null)
    }

    fun deriveKeyFromPackage(packageName: String, signature: ByteArray, iterations: Int): SecretKey {
        return deriveKeyFromPackage(packageName, signature, iterations, null)
    }

    fun deriveKeyFromPackage(
        packageName: String,
        signature: ByteArray,
        iterations: Int,
        customPassword: String?
    ): SecretKey {

        val password = if (!customPassword.isNullOrBlank()) {
            packageName + ":" + signature.toHexString() + ":" + customPassword
        } else {
            packageName + ":" + signature.toHexString()
        }

        val saltInput = if (!customPassword.isNullOrBlank()) {
            packageName.toByteArray() + signature + customPassword.toByteArray()
        } else {
            packageName.toByteArray() + signature
        }
        val salt = saltInput.sha256().copyOf(32)

        return deriveKey(password, salt, iterations)
    }

    fun isEncrypted(data: ByteArray): Boolean {
        if (data.size < 8) return false

        return try {
            ByteArrayInputStream(data).use { bais ->
                DataInputStream(bais).use { dis ->
                    val magic = dis.readInt()
                    magic == CryptoConstants.ENCRYPTED_HEADER_MAGIC
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun buildEncryptedPackage(
        salt: ByteArray,
        iv: ByteArray,
        encryptedData: ByteArray,
        hasAAD: Boolean
    ): ByteArray {
        return ByteArrayOutputStream().use { baos ->
            DataOutputStream(baos).use { dos ->

                dos.writeInt(CryptoConstants.ENCRYPTED_HEADER_MAGIC)

                dos.writeByte(CryptoConstants.ENCRYPTED_HEADER_VERSION)

                dos.writeByte(if (hasAAD) 1 else 0)

                dos.writeShort(salt.size)
                dos.write(salt)

                dos.writeShort(iv.size)
                dos.write(iv)

                dos.write(encryptedData)
            }
            baos.toByteArray()
        }
    }

    private fun parseEncryptedPackage(data: ByteArray): EncryptedPackage {
        return ByteArrayInputStream(data).use { bais ->
            DataInputStream(bais).use { dis ->

                val magic = dis.readInt()
                if (magic != CryptoConstants.ENCRYPTED_HEADER_MAGIC) {
                    throw CryptoException("无效的加密文件格式")
                }

                val version = dis.readByte().toInt()
                if (version > CryptoConstants.ENCRYPTED_HEADER_VERSION) {
                    throw CryptoException(Strings.cryptoUnsupportedVersion.format(version))
                }

                val flags = dis.readByte().toInt()
                val hasAAD = (flags and 1) != 0

                val saltLen = dis.readShort().toInt()
                if (saltLen < 0 || saltLen > 64) {
                    throw CryptoException("Invalid salt length: $saltLen (expected 0-64)")
                }
                val salt = ByteArray(saltLen)
                dis.readFully(salt)

                val ivLen = dis.readShort().toInt()
                if (ivLen < 0 || ivLen > 32) {
                    throw CryptoException("Invalid IV length: $ivLen (expected 0-32)")
                }
                val iv = ByteArray(ivLen)
                dis.readFully(iv)

                val encryptedData = dis.readBytes()

                EncryptedPackage(salt, iv, encryptedData, hasAAD)
            }
        }
    }

    private data class EncryptedPackage(
        val salt: ByteArray,
        val iv: ByteArray,
        val encryptedData: ByteArray,
        val hasAAD: Boolean
    )
}

class CryptoException(message: String, cause: Throwable? = null) : Exception(message, cause)

fun ByteArray.toHexString(): String = joinToString("") { "%02x".format(it) }

fun ByteArray.sha256(): ByteArray {
    val digest = java.security.MessageDigest.getInstance("SHA-256")
    return digest.digest(this)
}
