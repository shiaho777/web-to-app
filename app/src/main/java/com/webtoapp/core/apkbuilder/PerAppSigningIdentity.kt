package com.webtoapp.core.apkbuilder

import android.content.Context
import android.util.AtomicFile
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.File
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.Date

object PerAppSigningIdentity {

    private const val TAG = "PerAppSigning"

    internal const val STORE_DIR = "app_signing"
    internal const val CREDENTIAL_FILE = ".app_signing_credential"
    private const val KEY_ALIAS = "app"
    private const val KEY_SIZE = 3072
    private const val VALIDITY_YEARS = 30L
    private const val CN_MAX_LEN = 64

    data class Identity(
        val packageName: String,
        val privateKey: PrivateKey,
        val certificate: X509Certificate,
        val storeFile: File,
        val createdNow: Boolean
    ) {
        fun certSha256(): ByteArray =
            MessageDigest.getInstance("SHA-256").digest(certificate.encoded)

        fun certSha256Hex(): String = fingerprint(certificate)

        fun toSigningIdentity() = JarSigner.SigningIdentity(privateKey, certificate)
    }

    @Synchronized
    fun identityFor(
        context: Context,
        packageName: String,
        requireExisting: Boolean = false,
        expectedFingerprint: String? = null
    ): Identity {
        val storeFile = storeFileFor(context, packageName)
        val existing = readExisting(context, packageName)
        if (existing != null) {
            check(expectedFingerprint == null || existing.certSha256Hex() == expectedFingerprint) {
                Strings.signingBackupConflict
            }
            return existing
        }
        check(!requireExisting && expectedFingerprint == null) { Strings.signingBackupMissingKey }
        val password = getOrCreateStorePassword(context)
        return try {
            create(storeFile, password, packageName)
        } finally {
            password.fill('\u0000')
        }
    }

    @Synchronized
    internal fun readExisting(context: Context, packageName: String): Identity? {
        val storeFile = storeFileFor(context, packageName)
        val atomicFile = AtomicFile(storeFile)
        val input = try {
            atomicFile.openRead()
        } catch (e: java.io.FileNotFoundException) {
            if (!storeFile.exists()) return null
            throw e
        }
        return input.use {
            val password = readStorePassword(context) ?: error(Strings.signingBackupMissingKey)
            val keyStore = KeyStore.getInstance("PKCS12")
            try {
                keyStore.load(it, password)
                val key = keyStore.getKey(KEY_ALIAS, password) as PrivateKey
                val certificate = keyStore.getCertificate(KEY_ALIAS) as X509Certificate
                Identity(packageName, key, certificate, storeFile, createdNow = false)
            } catch (e: Exception) {
                throw IllegalStateException(Strings.signingBackupMissingKey, e)
            } finally {
                password.fill('\u0000')
            }
        }
    }

    fun existingFingerprint(context: Context, packageName: String): String? =
        runCatching { readExisting(context, packageName) }
            .getOrNull()?.certSha256()?.joinToString(":") { "%02X".format(it) }

    internal fun fingerprint(certificate: X509Certificate): String =
        MessageDigest.getInstance("SHA-256").digest(certificate.encoded)
            .joinToString("") { "%02x".format(it) }

    @Synchronized
    internal fun restore(context: Context, identities: Map<String, JarSigner.SigningIdentity>) {
        val missing = identities.filter { (packageName, identity) ->
            val existing = readExisting(context, packageName)
            check(existing == null || existing.certSha256Hex() == fingerprint(identity.certificate)) {
                Strings.signingBackupConflict
            }
            existing == null
        }
        if (missing.isEmpty()) return
        val password = getOrCreateStorePassword(context)
        try {
            missing.forEach { (packageName, identity) ->
                writeStore(storeFileFor(context, packageName), password, identity.privateKey, identity.certificate)
            }
        } finally {
            password.fill('\u0000')
        }
    }

    private fun storeFileFor(context: Context, packageName: String): File {
        val dir = File(context.filesDir, STORE_DIR)
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(packageName.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        return File(dir, "sig_${digest.take(20)}.p12")
    }

    private fun readStorePassword(context: Context): CharArray? {
        val file = File(context.filesDir, CREDENTIAL_FILE)
        return try {
            AtomicFile(file).openRead().bufferedReader().use { reader ->
                reader.readText().trim().takeIf { it.isNotEmpty() }?.toCharArray()
            }
        } catch (e: java.io.FileNotFoundException) {
            if (file.exists()) throw e
            null
        }
    }

    private fun getOrCreateStorePassword(context: Context): CharArray {
        readStorePassword(context)?.let { return it }
        val directory = File(context.filesDir, STORE_DIR)
        check(directory.listFiles()?.none { it.name.endsWith(".p12") } != false) {
            Strings.signingBackupMissingKey
        }
        val random = SecureRandom()
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val password = CharArray(40) { chars[random.nextInt(chars.length)] }
        val encoded = String(password).toByteArray(Charsets.US_ASCII)
        try {
            AtomicFile(File(context.filesDir, CREDENTIAL_FILE)).replace { it.write(encoded) }
        } finally {
            encoded.fill(0)
        }
        return password
    }

    private fun AtomicFile.replace(write: (java.io.FileOutputStream) -> Unit) {
        val output = startWrite()
        try {
            write(output)
            finishWrite(output)
        } catch (error: Throwable) {
            failWrite(output)
            throw error
        }
    }

    private fun writeStore(
        storeFile: File,
        password: CharArray,
        key: PrivateKey,
        certificate: X509Certificate
    ) {
        val keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(null, password)
        keyStore.setKeyEntry(KEY_ALIAS, key, password, arrayOf(certificate))
        AtomicFile(storeFile).replace { keyStore.store(it, password) }
    }

    private fun create(storeFile: File, password: CharArray, packageName: String): Identity {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(KEY_SIZE, SecureRandom())
        val keyPair = keyPairGenerator.generateKeyPair()

        val now = System.currentTimeMillis()
        val subject = X500Name("CN=${packageName.take(CN_MAX_LEN)}")
        val certHolder = JcaX509v3CertificateBuilder(
            subject,
            BigInteger.valueOf(now),
            Date(now),
            Date(now + VALIDITY_YEARS * 365L * 24L * 60L * 60L * 1000L),
            subject,
            keyPair.public
        ).apply {
            addExtension(Extension.basicConstraints, true, BasicConstraints(false))
            addExtension(Extension.keyUsage, true, KeyUsage(KeyUsage.digitalSignature))
        }.build(JcaContentSignerBuilder("SHA256withRSA").build(keyPair.private))
        val cert = JcaX509CertificateConverter().getCertificate(certHolder)

        writeStore(storeFile, password, keyPair.private, cert)

        AppLogger.i(TAG, "Created per-app signing identity for $packageName (${storeFile.name})")
        return Identity(packageName, keyPair.private, cert, storeFile, createdNow = true)
    }
}
