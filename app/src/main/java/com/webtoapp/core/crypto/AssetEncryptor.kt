package com.webtoapp.core.crypto

import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import java.io.InputStream
import java.io.OutputStream
import javax.crypto.SecretKey

class AssetEncryptor(private val secretKey: SecretKey) {

    companion object {
        private const val TAG = "AssetEncryptor"

        /** Longest asset path the encrypted header can carry. */
        private const val MAX_ASSET_PATH_LENGTH = 1024

        private const val COPY_BUFFER_SIZE = 64 * 1024
    }

    fun encrypt(data: ByteArray, assetPath: String): ByteArray {
        AppLogger.d(TAG, "加密资源: $assetPath")

        return try {
            val aad = assetPath.toByteArray(Charsets.UTF_8)

            val encrypted = AesCryptoEngine.encryptWithKey(
                plainData = data,
                secretKey = secretKey,
                associatedData = aad
            )

            val result = buildEncryptedAsset(encrypted, assetPath)
            AppLogger.d(TAG, "加密完成: $assetPath")
            result

        } catch (e: Exception) {
            AppLogger.e(TAG, "加密资源失败: $assetPath", e)
            throw CryptoException(Strings.cryptoEncryptAssetFailed.format(assetPath), e)
        }
    }

    fun encryptText(text: String, assetPath: String): ByteArray {
        return encrypt(text.toByteArray(Charsets.UTF_8), assetPath)
    }

    /**
     * Encrypts [input] straight into [output] instead of materialising the
     * asset in the heap. The layout matches [encrypt] byte for byte:
     * 4-byte path length, path, 12-byte IV, ciphertext, 16-byte GCM tag — so
     * the runtime [AssetDecryptor] reads both forms with the same parser.
     *
     * Returns the number of bytes written, header included.
     */
    fun encryptStreaming(
        input: InputStream,
        assetPath: String,
        output: OutputStream,
        bufferSize: Int = COPY_BUFFER_SIZE
    ): Long {
        val pathBytes = assetPath.toByteArray(Charsets.UTF_8)
        val pathLength = pathBytes.size
        if (pathLength > MAX_ASSET_PATH_LENGTH) {
            throw CryptoException("资源路径过长: $pathLength")
        }

        val iv = AesCryptoEngine.newRandomIv()
        val cipher = AesCryptoEngine.newEncryptCipher(secretKey, iv, pathBytes)

        val header = ByteArray(4 + pathLength + iv.size)
        header[0] = ((pathLength shr 24) and 0xFF).toByte()
        header[1] = ((pathLength shr 16) and 0xFF).toByte()
        header[2] = ((pathLength shr 8) and 0xFF).toByte()
        header[3] = (pathLength and 0xFF).toByte()
        System.arraycopy(pathBytes, 0, header, 4, pathLength)
        System.arraycopy(iv, 0, header, 4 + pathLength, iv.size)
        output.write(header)

        val bodyBytes = AesCryptoEngine.encryptStreaming(
            plain = input,
            cipher = cipher,
            sink = output,
            bufferSize = bufferSize,
        )
        AppLogger.d(TAG, "流式加密完成: $assetPath (${header.size + bodyBytes} bytes)")
        return header.size + bodyBytes
    }

    fun encryptJson(json: String, assetPath: String): ByteArray {
        return encryptText(json, assetPath)
    }

    fun encryptBatch(assets: Map<String, ByteArray>): Map<String, ByteArray> {
        AppLogger.d(TAG, "批量加密 ${assets.size} 个资源")

        return assets.mapValues { (path, data) ->
            encrypt(data, path)
        }
    }

    private fun buildEncryptedAsset(encryptedData: ByteArray, assetPath: String): ByteArray {
        val pathBytes = assetPath.toByteArray(Charsets.UTF_8)
        val pathLength = pathBytes.size

        return ByteArray(4 + pathLength + encryptedData.size).apply {

            this[0] = ((pathLength shr 24) and 0xFF).toByte()
            this[1] = ((pathLength shr 16) and 0xFF).toByte()
            this[2] = ((pathLength shr 8) and 0xFF).toByte()
            this[3] = (pathLength and 0xFF).toByte()

            System.arraycopy(pathBytes, 0, this, 4, pathLength)

            System.arraycopy(encryptedData, 0, this, 4 + pathLength, encryptedData.size)
        }
    }
}

data class EncryptionConfig(
    val enabled: Boolean = false,
    val customPassword: String? = null,
    // "SIGNATURE" (default): key is derived from the install-time signing cert —
    // breaks when the store re-signs (Play App Signing). "EMBEDDED": a random key
    // is generated at build time and stored obfuscated in the APK, surviving
    // re-signing entirely (#917).
    val keyMode: String = "SIGNATURE"
) {
    companion object {
        private const val PBKDF2_ITERATIONS = CryptoConstants.PBKDF2_ITERATIONS

        val DISABLED = EncryptionConfig(enabled = false)

        val MAXIMUM = EncryptionConfig(enabled = true)
    }

    fun shouldEncrypt(assetPath: String): Boolean {
        if (!enabled) return false
        return true
    }

    fun getKeyDerivationIterations(): Int = PBKDF2_ITERATIONS

    fun hasSecurityProtection(): Boolean = enabled
}
