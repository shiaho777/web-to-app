package com.webtoapp.core.backup

import com.webtoapp.core.apkbuilder.JarSigner
import com.webtoapp.core.i18n.Strings
import com.webtoapp.util.AppConstants
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator
import org.bouncycastle.crypto.params.KeyParameter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.security.KeyFactory
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPrivateCrtKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

internal object PerAppSigningBackup {
    const val ENTRY = "signing/per_app_keys.v1"
    const val MAX_BYTES = 8 * 1024 * 1024
    const val MIN_PASSWORD_LENGTH = 12
    private const val ITERATIONS = 600_000
    private const val SALT_SIZE = 16
    private const val NONCE_SIZE = 12
    private const val MAX_KEY_BYTES = 8 * 1024
    private const val MAX_IDENTITIES = 2_000
    private val header = "WTA-SIGNING-1".toByteArray(Charsets.US_ASCII)

    fun encrypt(identities: Map<String, JarSigner.SigningIdentity>, password: String): ByteArray {
        require(password.length >= MIN_PASSWORD_LENGTH && password.isNotBlank()) {
            Strings.signingBackupPasswordHint
        }
        require(identities.size <= MAX_IDENTITIES)
        val random = SecureRandom()
        val salt = ByteArray(SALT_SIZE).also(random::nextBytes)
        val nonce = ByteArray(NONCE_SIZE).also(random::nextBytes)
        val cipher = cipher(Cipher.ENCRYPT_MODE, password, salt, nonce)
        val encrypted = ByteArrayOutputStream()
        encrypted.write(header)
        encrypted.write(salt)
        encrypted.write(nonce)
        javax.crypto.CipherOutputStream(encrypted, cipher).use { stream ->
            DataOutputStream(stream).use { data ->
                data.writeInt(identities.size)
                identities.forEach { (packageName, identity) ->
                    data.writeUTF(packageName)
                    val key = identity.privateKey.encoded
                    try {
                        data.writeBlob(key)
                        data.writeBlob(identity.certificate.encoded)
                    } finally {
                        key.fill(0)
                    }
                }
            }
        }
        return encrypted.toByteArray().also { require(it.size <= MAX_BYTES) }
    }

    fun decrypt(payload: ByteArray, password: String): Map<String, JarSigner.SigningIdentity> {
        try {
            require(payload.size <= MAX_BYTES)
            val input = DataInputStream(ByteArrayInputStream(payload))
            val fileHeader = ByteArray(header.size).also(input::readFully)
            require(fileHeader.contentEquals(header))
            val salt = ByteArray(SALT_SIZE).also(input::readFully)
            val nonce = ByteArray(NONCE_SIZE).also(input::readFully)
            val clear = cipher(Cipher.DECRYPT_MODE, password, salt, nonce)
                .doFinal(payload, header.size + SALT_SIZE + NONCE_SIZE, input.available())
            try {
                return decode(clear)
            } finally {
                clear.fill(0)
            }
        } catch (error: Exception) {
            throw SigningBackupUnreadable(error)
        }
    }

    private fun decode(clear: ByteArray): Map<String, JarSigner.SigningIdentity> {
        val keyFactory = KeyFactory.getInstance("RSA")
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val input = DataInputStream(ByteArrayInputStream(clear))
        val count = input.readInt()
        require(count in 1..MAX_IDENTITIES)
        val identities = LinkedHashMap<String, JarSigner.SigningIdentity>(count)
        repeat(count) {
            val packageName = input.readUTF()
            require(AppConstants.PACKAGE_NAME_REGEX.matches(packageName))
            val encodedKey = input.readBlob()
            val privateKey = try {
                keyFactory.generatePrivate(PKCS8EncodedKeySpec(encodedKey)) as RSAPrivateCrtKey
            } finally {
                encodedKey.fill(0)
            }
            val certificate = certificateFactory.generateCertificate(input.readBlob().inputStream()) as X509Certificate
            val publicKey = certificate.publicKey as RSAPublicKey
            require(privateKey.modulus == publicKey.modulus && privateKey.publicExponent == publicKey.publicExponent)
            require(identities.put(packageName, JarSigner.SigningIdentity(privateKey, certificate)) == null)
        }
        require(input.read() == -1)
        return identities
    }

    private fun cipher(mode: Int, password: String, salt: ByteArray, nonce: ByteArray): Cipher {
        val passwordBytes = password.toByteArray(Charsets.UTF_8)
        val generator = PKCS5S2ParametersGenerator(SHA256Digest())
        val key = try {
            generator.init(passwordBytes, salt, ITERATIONS)
            (generator.generateDerivedParameters(256) as KeyParameter).key
        } finally {
            passwordBytes.fill(0)
        }
        return try {
            Cipher.getInstance("AES/GCM/NoPadding").apply {
                init(mode, SecretKeySpec(key, "AES"), GCMParameterSpec(128, nonce))
                updateAAD(header)
            }
        } finally {
            key.fill(0)
        }
    }

    private fun DataOutputStream.writeBlob(bytes: ByteArray) {
        require(bytes.size in 1..MAX_KEY_BYTES)
        writeInt(bytes.size)
        write(bytes)
    }

    private fun DataInputStream.readBlob(): ByteArray {
        val size = readInt()
        require(size in 1..MAX_KEY_BYTES)
        return ByteArray(size).also(::readFully)
    }
}

internal class SigningBackupPasswordRequired : IOException()

internal class SigningBackupUnreadable(cause: Throwable) : IOException(Strings.signingBackupUnreadable, cause)
