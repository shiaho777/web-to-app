package com.webtoapp.core.apkbuilder

import android.content.Context
import com.webtoapp.core.i18n.Strings
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.webtoapp.core.logging.AppLogger
import com.android.apksig.ApkSigner
import com.android.apksig.ApkVerifier
import com.webtoapp.util.GsonProvider
import com.webtoapp.util.threadLocalCompat
import java.io.*
import java.math.BigInteger
import java.security.*
import java.security.cert.X509Certificate
import java.util.*
import java.util.zip.*
import javax.security.auth.x500.X500Principal

class JarSigner(private val context: Context) {

    companion object {
        private const val TAG = "JarSigner"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "WebToAppKey"
        private const val FALLBACK_KEY_ALIAS = "WebToAppFallback"

        @Suppress("unused")
        private const val CUSTOM_KEY_ALIAS = "CustomKey"
        private const val DIGEST_ALGORITHM = "SHA-256"
        private const val SIGNATURE_ALGORITHM = "SHA256withRSA"
        private const val KEY_SIZE = 2048
        private const val VALIDITY_YEARS = 20L

        private const val DEFAULT_PKCS12_FILE = "webtoapp_keystore.p12"
        private const val CUSTOM_PKCS12_FILE = "custom_keystore.p12"

        private const val CUSTOM_PASSWORD_FILE = "custom_keystore_password.txt"
        private const val CUSTOM_ALIAS_FILE = "custom_keystore_alias.txt"
        private const val CUSTOM_KEYPASS_FILE = "custom_keystore_keypass.txt"

        private const val SIGNER_BASENAME = "CERT"

        private const val SIGNING_SCHEME_FILE = "signing_scheme_options.json"

        private const val V1_SIGNER_NAME_MAX_LEN = 8

        private val GENERALIZED_TIME_FORMAT = threadLocalCompat {
            java.text.SimpleDateFormat("yyyyMMddHHmmss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
        }
        private val UTC_TIME_FORMAT = threadLocalCompat {
            java.text.SimpleDateFormat("yyMMddHHmmss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
        }
    }

    enum class SignerType {
        ANDROID_KEYSTORE,
        PKCS12_AUTO,
        PKCS12_CUSTOM
    }

    data class SigningSchemeOptions(
        val v1Enabled: Boolean = true,
        val v2Enabled: Boolean = true,
        val v3Enabled: Boolean = true,
        val autoFallback: Boolean = true,
        val v1SignerName: String = ""
    ) {

        fun hasAnyScheme(): Boolean = v1Enabled || v2Enabled || v3Enabled
    }

    data class CertificateSpec(
        val alias: String = "key0",
        val password: String = "",
        val commonName: String = "",
        val organization: String = "",
        val organizationUnit: String = "",
        val locality: String = "",
        val state: String = "",
        val country: String = "",
        val validityYears: Int = 30,
        val keySize: Int = 2048
    )

    data class CertificateFingerprints(
        val md5: String,
        val sha1: String,
        val sha256: String
    )

    private var privateKey: PrivateKey? = null
    private var certificate: X509Certificate? = null
    private var initError: String? = null
    private var currentSignerType: SignerType = SignerType.ANDROID_KEYSTORE

    init {
        initializeKey()
    }

    fun getSignerType(): SignerType = currentSignerType

    fun getCertificateSignatureHash(): ByteArray {
        val cert = certificate ?: throw IllegalStateException("证书未初始化")
        return java.security.MessageDigest.getInstance("SHA-256").digest(cert.encoded)
    }

    fun getCertificateInfo(): String? {
        val cert = certificate ?: return null
        return """
            |证书主题: ${cert.subjectX500Principal.name}
            |颁发者: ${cert.issuerX500Principal.name}
            |序列号: ${cert.serialNumber}
            |有效期: ${cert.notBefore} - ${cert.notAfter}
            |签名类型: $currentSignerType
        """.trimMargin()
    }

    fun getCertificateFingerprints(): CertificateFingerprints? {
        val cert = certificate ?: return null
        return try {
            val der = cert.encoded
            CertificateFingerprints(
                md5 = hexFingerprint(der, "MD5"),
                sha1 = hexFingerprint(der, "SHA-1"),
                sha256 = hexFingerprint(der, "SHA-256")
            )
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to compute certificate fingerprints", e)
            null
        }
    }

    private fun hexFingerprint(data: ByteArray, algorithm: String): String {
        val digest = java.security.MessageDigest.getInstance(algorithm).digest(data)
        return digest.joinToString(":") { "%02X".format(it) }
    }

    fun getSigningSchemeOptions(): SigningSchemeOptions {
        val file = File(context.filesDir, SIGNING_SCHEME_FILE)
        if (!file.exists()) return SigningSchemeOptions()
        return try {
            GsonProvider.gson.fromJson(file.readText(), SigningSchemeOptions::class.java)
                ?.let { sanitizeOptions(it) }
                ?: SigningSchemeOptions()
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to read signing scheme options, using defaults", e)
            SigningSchemeOptions()
        }
    }

    fun setSigningSchemeOptions(options: SigningSchemeOptions): SigningSchemeOptions {
        val safe = sanitizeOptions(options)
        try {
            File(context.filesDir, SIGNING_SCHEME_FILE).writeText(GsonProvider.gson.toJson(safe))
            AppLogger.d(TAG, "Saved signing scheme options: $safe")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to save signing scheme options", e)
        }
        return safe
    }

    private fun sanitizeOptions(options: SigningSchemeOptions): SigningSchemeOptions {
        return if (!options.hasAnyScheme()) {
            AppLogger.w(TAG, "All signing schemes disabled, forcing V1+V2+V3 back on")
            options.copy(v1Enabled = true, v2Enabled = true, v3Enabled = true)
        } else {
            options
        }
    }

    fun resolveV1SignerName(customName: String?): String {
        val explicit = customName?.trim().orEmpty()
        if (explicit.isNotEmpty()) {
            return sanitizeV1SignerName(explicit)
        }

        return deriveSignerNameFromKey() ?: SIGNER_BASENAME
    }

    private fun deriveSignerNameFromKey(): String? {
        val cert = certificate ?: return null
        val cn = extractCommonName(cert.subjectX500Principal.name)
            ?: cert.subjectX500Principal.name
        val sanitized = sanitizeV1SignerName(cn)
        return sanitized.takeIf { it.isNotEmpty() && it.any { ch -> ch.isLetterOrDigit() } }
    }

    private fun extractCommonName(dn: String): String? {

        return dn.split(",")
            .map { it.trim() }
            .firstOrNull { it.startsWith("CN=", ignoreCase = true) }
            ?.substringAfter("=")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }

    private fun sanitizeV1SignerName(name: String): String {
        val upper = name.uppercase(Locale.US)
        val sb = StringBuilder()
        for (i in 0 until minOf(upper.length, V1_SIGNER_NAME_MAX_LEN)) {
            val c = upper[i]
            sb.append(
                if (c in 'A'..'Z' || c in '0'..'9' || c == '-' || c == '_') c else '_'
            )
        }
        return sb.toString()
    }

    private fun initializeKey() {

        if (tryLoadOrCreateFallbackKey()) {
            AppLogger.d(TAG, "Successfully using PKCS12 key scheme")
            return
        }

        if (tryLoadFromAndroidKeyStore()) {
            AppLogger.d(TAG, "Successfully loaded key from Android KeyStore")
            currentSignerType = SignerType.ANDROID_KEYSTORE
            return
        }

        if (tryGenerateAndroidKeyStoreKey()) {
            AppLogger.d(TAG, "Successfully generated Android KeyStore key")
            currentSignerType = SignerType.ANDROID_KEYSTORE
            return
        }

        initError = "无法初始化签名密钥"
        AppLogger.e(TAG, initError!!)
    }

    private fun tryLoadFromAndroidKeyStore(): Boolean {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)

            if (keyStore.containsAlias(KEY_ALIAS)) {
                privateKey = keyStore.getKey(KEY_ALIAS, null) as? PrivateKey
                certificate = keyStore.getCertificate(KEY_ALIAS) as? X509Certificate
                privateKey != null && certificate != null
            } else {
                false
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to load key from Android KeyStore", e)
            false
        }
    }

    private fun tryGenerateAndroidKeyStoreKey(): Boolean {
        return try {

            try {
                val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
                keyStore.load(null)
                if (keyStore.containsAlias(KEY_ALIAS)) {
                    keyStore.deleteEntry(KEY_ALIAS)
                    AppLogger.d(TAG, "Deleted stale KeyStore entry")
                }
            } catch (e: Exception) {
                AppLogger.w(TAG, "Failed to delete stale key (safe to ignore)", e)
            }

            val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA,
                ANDROID_KEYSTORE
            )

            val notBefore = Date()
            val notAfter = Date(System.currentTimeMillis() + VALIDITY_YEARS * 365L * 24L * 60L * 60L * 1000L)

            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_SIGN
            )
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA1)
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                .setKeySize(KEY_SIZE)
                .setCertificateSubject(X500Principal("CN=WebToApp, O=WebToApp, C=CN"))
                .setCertificateSerialNumber(BigInteger.valueOf(System.currentTimeMillis()))
                .setCertificateNotBefore(notBefore)
                .setCertificateNotAfter(notAfter)
                .build()

            keyPairGenerator.initialize(spec)
            val keyPair = keyPairGenerator.generateKeyPair()

            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            privateKey = keyStore.getKey(KEY_ALIAS, null) as? PrivateKey
            certificate = keyStore.getCertificate(KEY_ALIAS) as? X509Certificate

            privateKey != null && certificate != null
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to generate Android KeyStore key", e)
            false
        }
    }

    private fun tryLoadOrCreateFallbackKey(): Boolean {

        if (tryLoadCustomPkcs12()) {
            currentSignerType = SignerType.PKCS12_CUSTOM
            return true
        }

        if (tryLoadOrCreateAutoPkcs12()) {
            currentSignerType = SignerType.PKCS12_AUTO
            return true
        }

        return false
    }

    private fun tryLoadCustomPkcs12(): Boolean {
        val customFile = File(context.filesDir, CUSTOM_PKCS12_FILE)
        val passwordFile = File(context.filesDir, CUSTOM_PASSWORD_FILE)

        if (!customFile.exists() || !passwordFile.exists()) {
            return false
        }

        return try {
            val storePassword = passwordFile.readText().trim().toCharArray()

            val aliasFile = File(context.filesDir, CUSTOM_ALIAS_FILE)
            val persistedAlias = if (aliasFile.exists()) {
                aliasFile.readText().trim().takeIf { it.isNotBlank() }
            } else null

            val keypassFile = File(context.filesDir, CUSTOM_KEYPASS_FILE)
            val keyPassword: CharArray? = if (keypassFile.exists()) {
                keypassFile.readText().trim().toCharArray()
            } else null

            loadPkcs12(customFile, storePassword, persistedAlias, keyPassword)
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to load custom PKCS12", e)
            false
        }
    }

    private fun tryLoadOrCreateAutoPkcs12(): Boolean {
        val keyStoreFile = File(context.filesDir, DEFAULT_PKCS12_FILE)
        val keyStorePassword = getOrCreateKeystorePassword()

        return try {
            val keyStore = KeyStore.getInstance("PKCS12")

            if (keyStoreFile.exists()) {
                FileInputStream(keyStoreFile).use { fis ->

                    keyStore.load(fis, keyStorePassword.copyOf())
                }
                privateKey = keyStore.getKey(FALLBACK_KEY_ALIAS, keyStorePassword.copyOf()) as? PrivateKey
                certificate = keyStore.getCertificate(FALLBACK_KEY_ALIAS) as? X509Certificate

                if (privateKey != null && certificate != null) {

                    val cert = certificate!!
                    val now = Date()
                    if (cert.notAfter.before(now)) {
                        AppLogger.w(TAG, "Certificate expired or invalid (notAfter=${cert.notAfter}), regenerating...")
                        keyStoreFile.delete()
                        privateKey = null
                        certificate = null
                    } else {
                        AppLogger.d(TAG, "Loaded auto-generated PKCS12 successfully, valid until ${cert.notAfter}")
                        return true
                    }
                }
            }

            createNewPkcs12(keyStoreFile, keyStorePassword, FALLBACK_KEY_ALIAS)
            true
        } catch (e: Exception) {
            AppLogger.e(TAG, "Auto PKCS12 key scheme failed", e)
            false
        }
    }

    private fun getOrCreateKeystorePassword(): CharArray {
        val passwordFile = File(context.filesDir, ".ks_credential")

        if (passwordFile.exists()) {
            try {
                val saved = passwordFile.readText().trim()
                if (saved.isNotEmpty()) return saved.toCharArray()
            } catch (e: Exception) {
                AppLogger.w(TAG, "Failed to read password file", e)
            }
        }

        val keyStoreFile = File(context.filesDir, DEFAULT_PKCS12_FILE)
        val legacyPassword = "webtoapp_sign"
        if (keyStoreFile.exists()) {
            try {
                val ks = KeyStore.getInstance("PKCS12")
                FileInputStream(keyStoreFile).use { fis -> ks.load(fis, legacyPassword.toCharArray()) }

                passwordFile.writeText(legacyPassword)
                AppLogger.d(TAG, "Migrating legacy password to file storage")
                return legacyPassword.toCharArray()
            } catch (_: Exception) {

            }
        }

        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#%^&*"
        val random = SecureRandom()
        val password = CharArray(32) { chars[random.nextInt(chars.length)] }

        try {
            passwordFile.writeText(String(password))
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to save password file", e)
        }

        return password
    }

    private fun loadPkcs12(
        file: File,
        password: CharArray,
        alias: String? = null,
        keyPassword: CharArray? = null
    ): Boolean {
        return try {
            val keyStore = KeyStore.getInstance("PKCS12")
            FileInputStream(file).use { fis ->

                keyStore.load(fis, password.copyOf())
            }

            val keyAlias = alias?.takeIf { keyStore.isKeyEntry(it) }
                ?: keyStore.aliases().toList().firstOrNull { keyStore.isKeyEntry(it) }

            if (keyAlias == null) {
                AppLogger.e(TAG, "No key entry in PKCS12 (alias=$alias）")
                return false
            }

            val effectiveKeyPass = keyPassword?.copyOf() ?: password.copyOf()
            privateKey = try {
                keyStore.getKey(keyAlias, effectiveKeyPass) as? PrivateKey
            } catch (e: java.security.UnrecoverableKeyException) {

                AppLogger.w(TAG, "getKey($keyAlias) failed — keypass might differ from storepass: ${e.message}")
                throw e
            }
            certificate = keyStore.getCertificate(keyAlias) as? X509Certificate

            val success = privateKey != null && certificate != null
            if (success) {
                AppLogger.d(TAG, "Loaded successfully from PKCS12: alias=$keyAlias")
            }
            success
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to load PKCS12: ${file.absolutePath}", e)
            false
        }
    }

    private fun createNewPkcs12(file: File, password: CharArray, alias: String) {
        AppLogger.d(TAG, "Creating new PKCS12 keystore...")

        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(KEY_SIZE, SecureRandom())
        val keyPair = keyPairGenerator.generateKeyPair()
        AppLogger.d(TAG, "RSA key pair generated (${KEY_SIZE} bit)")

        val cert = try {
            generateCertViaAndroidKeyStore(keyPair)
        } catch (e: Exception) {
            AppLogger.w(TAG, "Android KeyStore certificate generation failed, falling back to manual ASN.1: ${e.message}")
            generateSelfSignedCertificate(keyPair)
        }

        val keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(null, password.copyOf())
        keyStore.setKeyEntry(alias, keyPair.private, password.copyOf(), arrayOf(cert))

        FileOutputStream(file).use { fos ->
            keyStore.store(fos, password.copyOf())
        }

        privateKey = keyPair.private
        certificate = cert

        AppLogger.d(TAG, "PKCS12 created successfully: ${file.absolutePath}, valid until ${cert.notAfter}")
    }

    private fun generateCertViaAndroidKeyStore(softwareKeyPair: KeyPair): X509Certificate {
        val tempAlias = "webtoapp_cert_gen_temp_${System.currentTimeMillis()}"

        try {

            val tempKpg = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA,
                ANDROID_KEYSTORE
            )

            val notBefore = Date()
            val notAfter = Date(System.currentTimeMillis() + VALIDITY_YEARS * 365L * 24L * 60L * 60L * 1000L)

            val spec = KeyGenParameterSpec.Builder(
                tempAlias,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA1)
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                .setKeySize(KEY_SIZE)
                .setCertificateSubject(X500Principal("CN=WebToApp, O=WebToApp, C=CN"))
                .setCertificateSerialNumber(BigInteger.valueOf(System.currentTimeMillis()))
                .setCertificateNotBefore(notBefore)
                .setCertificateNotAfter(notAfter)
                .build()

            tempKpg.initialize(spec)
            tempKpg.generateKeyPair()

            val aksKeyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            aksKeyStore.load(null)
            val aksCert = aksKeyStore.getCertificate(tempAlias) as X509Certificate

            val tbsCert = buildTBSCertificate(
                aksCert.subjectX500Principal,
                aksCert.issuerX500Principal,
                aksCert.serialNumber,
                aksCert.notBefore,
                aksCert.notAfter,
                softwareKeyPair.public
            )

            val sig = Signature.getInstance(SIGNATURE_ALGORITHM)
            sig.initSign(softwareKeyPair.private)
            sig.update(tbsCert)
            val signatureBytes = sig.sign()

            val certDer = buildCertificateDER(tbsCert, signatureBytes)
            val certFactory = java.security.cert.CertificateFactory.getInstance("X.509")
            val newCert = certFactory.generateCertificate(ByteArrayInputStream(certDer)) as X509Certificate

            AppLogger.d(TAG, "Certificate generated via Android KeyStore template")
            return newCert

        } finally {

            try {
                val aksKeyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
                aksKeyStore.load(null)
                if (aksKeyStore.containsAlias(tempAlias)) {
                    aksKeyStore.deleteEntry(tempAlias)
                }
            } catch (e: Exception) {
                AppLogger.w(TAG, "Failed to clean up temporary KeyStore entry (safe to ignore)", e)
            }
        }
    }

    @JvmOverloads
    fun importPkcs12(sourceFile: File, password: String, keyPassword: String? = null): Boolean {
        return try {
            val storePassChars = password.toCharArray()
            val keyPassChars = keyPassword
                ?.takeIf { it.isNotEmpty() }
                ?.toCharArray()

            val keyStore = KeyStore.getInstance("PKCS12")
            FileInputStream(sourceFile).use { fis ->
                keyStore.load(fis, storePassChars.copyOf())
            }

            val keyAlias = keyStore.aliases().toList().firstOrNull { keyStore.isKeyEntry(it) }
            if (keyAlias == null) {
                AppLogger.e(TAG, "Import failed: no key entry in PKCS12")
                return false
            }

            val targetFile = File(context.filesDir, CUSTOM_PKCS12_FILE)
            sourceFile.copyTo(targetFile, overwrite = true)

            File(context.filesDir, CUSTOM_PASSWORD_FILE).writeText(password)
            File(context.filesDir, CUSTOM_ALIAS_FILE).writeText(keyAlias)
            val keypassSidecar = File(context.filesDir, CUSTOM_KEYPASS_FILE)
            if (keyPassChars != null) {
                keypassSidecar.writeText(keyPassword)
            } else if (keypassSidecar.exists()) {

                keypassSidecar.delete()
            }

            if (loadPkcs12(targetFile, storePassChars, keyAlias, keyPassChars)) {
                currentSignerType = SignerType.PKCS12_CUSTOM
                AppLogger.d(TAG, "Custom PKCS12 import successful (alias=$keyAlias, separateKeyPass=${keyPassChars != null})")
                return true
            }

            false
        } catch (e: Exception) {
            AppLogger.e(TAG, "PKCS12 import failed", e)
            false
        }
    }

    @JvmOverloads
    fun importKeystore(sourceFile: File, password: String, keyPassword: String? = null): Boolean {
        val storePassChars = password.toCharArray()
        val keyPassChars = keyPassword
            ?.takeIf { it.isNotEmpty() }
            ?.toCharArray()
            ?: storePassChars

        val keystoreTypes = listOf("PKCS12", "BKS", "JKS")

        for (type in keystoreTypes) {
            try {
                val keyStore = KeyStore.getInstance(type)
                FileInputStream(sourceFile).use { fis ->
                    keyStore.load(fis, storePassChars.copyOf())
                }

                val keyAlias = keyStore.aliases().toList().firstOrNull { keyStore.isKeyEntry(it) }
                if (keyAlias == null) {
                    AppLogger.w(TAG, "$type has no key entry, skipping")
                    continue
                }

                AppLogger.d(TAG, "Successfully parsed signature file as $type, alias=$keyAlias")

                if (type == "PKCS12") {

                    val targetFile = File(context.filesDir, CUSTOM_PKCS12_FILE)
                    sourceFile.copyTo(targetFile, overwrite = true)
                } else {

                    val key = try {
                        keyStore.getKey(keyAlias, keyPassChars.copyOf()) as? PrivateKey
                    } catch (e: java.security.UnrecoverableKeyException) {
                        AppLogger.w(TAG, "$type getKey($keyAlias) failed — keypass might differ from storepass: ${e.message}")

                        null
                    }
                    val cert = keyStore.getCertificate(keyAlias) as? java.security.cert.X509Certificate
                    if (key == null || cert == null) {
                        AppLogger.w(TAG, "$type has empty key or certificate（alias=$keyAlias）")
                        continue
                    }

                    val p12KeyStore = KeyStore.getInstance("PKCS12")
                    p12KeyStore.load(null, storePassChars.copyOf())
                    p12KeyStore.setKeyEntry(keyAlias, key, storePassChars.copyOf(), arrayOf(cert))

                    val targetFile = File(context.filesDir, CUSTOM_PKCS12_FILE)
                    FileOutputStream(targetFile).use { fos ->
                        p12KeyStore.store(fos, storePassChars.copyOf())
                    }
                    AppLogger.d(TAG, "Converted $type converted to a unified-password PKCS12 (storepass==keypass) and saved")
                }

                File(context.filesDir, CUSTOM_PASSWORD_FILE).writeText(password)
                File(context.filesDir, CUSTOM_ALIAS_FILE).writeText(keyAlias)
                val keypassSidecar = File(context.filesDir, CUSTOM_KEYPASS_FILE)
                if (type == "PKCS12" && keyPassword != null && keyPassword.isNotEmpty() && keyPassword != password) {

                    keypassSidecar.writeText(keyPassword)
                } else if (keypassSidecar.exists()) {
                    keypassSidecar.delete()
                }

                val targetFile = File(context.filesDir, CUSTOM_PKCS12_FILE)
                val effectiveKeyPass = if (type == "PKCS12") keyPassChars else storePassChars
                if (loadPkcs12(targetFile, storePassChars, keyAlias, effectiveKeyPass)) {
                    currentSignerType = SignerType.PKCS12_CUSTOM
                    AppLogger.d(TAG, "Custom signature file imported (original format: $type, alias=$keyAlias)")
                    return true
                }
            } catch (e: Exception) {
                AppLogger.w(TAG, "Attempting to parse as $type failed: ${e.message}")
            }
        }

        AppLogger.e(TAG, "Couldn't parse signature file in any supported format")
        return false
    }

    fun createCustomKeystore(spec: CertificateSpec): Boolean {
        val alias = spec.alias.trim()
        if (alias.isEmpty()) {
            AppLogger.e(TAG, "createCustomKeystore: alias is empty")
            return false
        }
        if (spec.password.isEmpty()) {
            AppLogger.e(TAG, "createCustomKeystore: password is empty")
            return false
        }

        return try {
            val keySize = if (spec.keySize == 4096) 4096 else 2048
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(keySize, SecureRandom())
            val keyPair = keyPairGenerator.generateKeyPair()

            val years = spec.validityYears.coerceIn(1, 100)
            val now = System.currentTimeMillis()
            val notBefore = Date(now)
            val notAfter = Date(now + years * 365L * 24L * 60L * 60L * 1000L)

            val subject = buildSubjectDn(spec)
            val serial = BigInteger.valueOf(now)

            val cert = try {
                createX509Certificate(subject, subject, serial, notBefore, notAfter, keyPair.public, keyPair.private)
            } catch (e: Exception) {
                AppLogger.w(TAG, "Custom cert ASN.1 build failed: ${e.message}")
                throw e
            }

            val passwordChars = spec.password.toCharArray()

            val keyStore = KeyStore.getInstance("PKCS12")
            keyStore.load(null, passwordChars.copyOf())
            keyStore.setKeyEntry(alias, keyPair.private, passwordChars.copyOf(), arrayOf(cert))

            val targetFile = File(context.filesDir, CUSTOM_PKCS12_FILE)
            FileOutputStream(targetFile).use { fos ->
                keyStore.store(fos, passwordChars.copyOf())
            }

            File(context.filesDir, CUSTOM_PASSWORD_FILE).writeText(spec.password)
            File(context.filesDir, CUSTOM_ALIAS_FILE).writeText(alias)
            File(context.filesDir, CUSTOM_KEYPASS_FILE).delete()

            if (loadPkcs12(targetFile, passwordChars, alias, null)) {
                currentSignerType = SignerType.PKCS12_CUSTOM
                AppLogger.d(TAG, "Custom keystore created and activated (alias=$alias, keySize=$keySize, years=$years)")
                true
            } else {
                AppLogger.e(TAG, "Custom keystore created but failed to load back")
                false
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "createCustomKeystore failed", e)
            false
        }
    }

    private fun buildSubjectDn(spec: CertificateSpec): X500Principal {
        fun esc(v: String) = v.trim()
            .replace("\\", "\\\\")
            .replace(",", "\\,")
            .replace("+", "\\+")
            .replace("\"", "\\\"")
            .replace("<", "\\<")
            .replace(">", "\\>")
            .replace(";", "\\;")

        val parts = buildList {
            spec.commonName.trim().takeIf { it.isNotEmpty() }?.let { add("CN=${esc(it)}") }
            spec.organizationUnit.trim().takeIf { it.isNotEmpty() }?.let { add("OU=${esc(it)}") }
            spec.organization.trim().takeIf { it.isNotEmpty() }?.let { add("O=${esc(it)}") }
            spec.locality.trim().takeIf { it.isNotEmpty() }?.let { add("L=${esc(it)}") }
            spec.state.trim().takeIf { it.isNotEmpty() }?.let { add("ST=${esc(it)}") }
            spec.country.trim().takeIf { it.isNotEmpty() }?.let { add("C=${esc(it)}") }
        }
        val dn = if (parts.isEmpty()) "CN=WebToApp" else parts.joinToString(", ")
        return X500Principal(dn)
    }

    fun exportPkcs12(targetFile: File, password: String): Boolean {
        val key = privateKey
        val cert = certificate

        if (key == null || cert == null) {
            AppLogger.e(TAG, "Export failed: no key available")
            return false
        }

        return try {
            val keyStore = KeyStore.getInstance("PKCS12")
            keyStore.load(null, null)

            keyStore.setKeyEntry(
                "exported_key",
                key,
                password.toCharArray(),
                arrayOf(cert)
            )

            FileOutputStream(targetFile).use { fos ->
                keyStore.store(fos, password.toCharArray())
            }

            AppLogger.d(TAG, "PKCS12 exported successfully: ${targetFile.absolutePath}")
            true
        } catch (e: Exception) {
            AppLogger.e(TAG, "PKCS12 export failed", e)
            false
        }
    }

    fun removeCustomPkcs12(): Boolean {
        return try {

            File(context.filesDir, CUSTOM_PKCS12_FILE).delete()
            File(context.filesDir, CUSTOM_PASSWORD_FILE).delete()
            File(context.filesDir, CUSTOM_ALIAS_FILE).delete()
            File(context.filesDir, CUSTOM_KEYPASS_FILE).delete()

            initializeKey()

            AppLogger.d(TAG, "Removed custom PKCS12 and sidecar, falling back to: $currentSignerType")
            true
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to delete custom PKCS12", e)
            false
        }
    }

    fun getPkcs12FilePath(): String? {
        return when (currentSignerType) {
            SignerType.PKCS12_CUSTOM -> File(context.filesDir, CUSTOM_PKCS12_FILE).absolutePath
            SignerType.PKCS12_AUTO -> File(context.filesDir, DEFAULT_PKCS12_FILE).absolutePath
            else -> null
        }
    }

    private fun generateSelfSignedCertificate(keyPair: KeyPair): X509Certificate {
        val now = System.currentTimeMillis()
        val notBefore = Date(now)
        val notAfter = Date(now + VALIDITY_YEARS * 365L * 24L * 60L * 60L * 1000L)

        val subject = X500Principal("CN=WebToApp, O=WebToApp, C=CN")
        val serialNumber = BigInteger.valueOf(now)

        return createX509Certificate(
            subject,
            subject,
            serialNumber,
            notBefore,
            notAfter,
            keyPair.public,
            keyPair.private
        )
    }

    private fun createX509Certificate(
        subject: X500Principal,
        issuer: X500Principal,
        serialNumber: BigInteger,
        notBefore: Date,
        notAfter: Date,
        publicKey: PublicKey,
        privateKey: PrivateKey
    ): X509Certificate {

        val tbsCert = buildTBSCertificate(
            subject, issuer, serialNumber, notBefore, notAfter, publicKey
        )

        val signature = Signature.getInstance(SIGNATURE_ALGORITHM)
        signature.initSign(privateKey)
        signature.update(tbsCert)
        val signatureBytes = signature.sign()

        val certDer = buildCertificateDER(tbsCert, signatureBytes)

        val certFactory = java.security.cert.CertificateFactory.getInstance("X.509")
        return certFactory.generateCertificate(ByteArrayInputStream(certDer)) as X509Certificate
    }

    private fun buildTBSCertificate(
        subject: X500Principal,
        issuer: X500Principal,
        serialNumber: BigInteger,
        notBefore: Date,
        notAfter: Date,
        publicKey: PublicKey
    ): ByteArray {
        val out = ByteArrayOutputStream()

        out.write(byteArrayOf(0xA0.toByte(), 0x03, 0x02, 0x01, 0x02))

        val serialBytes = serialNumber.toByteArray()
        out.write(wrapWithTag(0x02, serialBytes))

        out.write(buildSignatureAlgorithmId())

        out.write(issuer.encoded)

        out.write(buildValidity(notBefore, notAfter))

        out.write(subject.encoded)

        out.write(publicKey.encoded)

        return wrapWithTag(0x30, out.toByteArray())
    }

    private fun buildSignatureAlgorithmId(): ByteArray {

        val oid = byteArrayOf(
            0x06, 0x09, 0x2A, 0x86.toByte(), 0x48, 0x86.toByte(), 0xF7.toByte(),
            0x0D, 0x01, 0x01, 0x0B
        )
        val nullParam = byteArrayOf(0x05, 0x00)
        return wrapWithTag(0x30, oid + nullParam)
    }

    private fun buildValidity(notBefore: Date, notAfter: Date): ByteArray {
        val out = ByteArrayOutputStream()
        out.write(encodeTime(notBefore))
        out.write(encodeTime(notAfter))
        return wrapWithTag(0x30, out.toByteArray())
    }

    private fun encodeTime(date: Date): ByteArray {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.time = date
        val year = calendar.get(Calendar.YEAR)

        return if (year >= 2050) {

            val timeStr = GENERALIZED_TIME_FORMAT.get()!!.format(date)
            wrapWithTag(0x18, timeStr.toByteArray(Charsets.US_ASCII))
        } else {

            val timeStr = UTC_TIME_FORMAT.get()!!.format(date)
            wrapWithTag(0x17, timeStr.toByteArray(Charsets.US_ASCII))
        }
    }

    private fun buildCertificateDER(tbsCert: ByteArray, signature: ByteArray): ByteArray {
        val out = ByteArrayOutputStream()
        out.write(tbsCert)
        out.write(buildSignatureAlgorithmId())

        out.write(wrapWithTag(0x03, byteArrayOf(0x00) + signature))
        return wrapWithTag(0x30, out.toByteArray())
    }

    fun sign(inputApk: File, outputApk: File): Boolean {

        if (!validateInputs(inputApk, outputApk)) {
            throw IllegalStateException(Strings.signInputValidationFailed.format(inputApk.absolutePath))
        }

        val key = privateKey
        val cert = certificate
        if (key == null || cert == null) {
            AppLogger.e(TAG, "Key or certificate is empty, retrying initialisation...")

            File(context.filesDir, DEFAULT_PKCS12_FILE).delete()
            File(context.filesDir, ".ks_credential").delete()
            initializeKey()
            if (privateKey == null || certificate == null) {
                val errorDetail = initError ?: "key=${privateKey != null}, cert=${certificate != null}"
                throw IllegalStateException(Strings.signKeyInitFailed.format(errorDetail))
            }
        }

        AppLogger.d(TAG, "Signing APK: input=${inputApk.absolutePath} (size=${inputApk.length()})")
        AppLogger.d(TAG, "Signer type: $currentSignerType")

        return trySignWithRetry(inputApk, outputApk, maxRetries = 2)
    }

    private fun validateInputs(inputApk: File, outputApk: File): Boolean {
        if (!inputApk.exists()) {
            AppLogger.e(TAG, "Input APK doesn't exist: ${inputApk.absolutePath}")
            return false
        }

        if (inputApk.length() == 0L) {
            AppLogger.e(TAG, "Input APK file is empty")
            return false
        }

        if (!inputApk.canRead()) {
            AppLogger.e(TAG, "Can't read input APK")
            return false
        }

        val outputDir = outputApk.parentFile
        if (outputDir != null && !outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                AppLogger.e(TAG, "Can't create output directory: ${outputDir.absolutePath}")
                return false
            }
        }

        if (outputApk.exists() && !outputApk.delete()) {
            AppLogger.e(TAG, "Can't overwrite existing output: ${outputApk.absolutePath}")
            return false
        }

        return true
    }

    private fun trySignWithRetry(inputApk: File, outputApk: File, maxRetries: Int): Boolean {
        val errorMessages = mutableListOf<String>()
        var lastException: Throwable? = null

        data class SignConfig(val name: String, val v1: Boolean, val v2: Boolean, val v3: Boolean)

        val options = getSigningSchemeOptions()
        val v1Name = resolveV1SignerName(options.v1SignerName)
        AppLogger.d(TAG, "Signing scheme options: $options (resolved V1 name=$v1Name)")

        val selected = buildList {
            if (options.v1Enabled) add(1)
            if (options.v2Enabled) add(2)
            if (options.v3Enabled) add(3)
        }

        fun configOf(versions: List<Int>) = SignConfig(
            name = versions.joinToString("+") { "V$it" },
            v1 = versions.contains(1),
            v2 = versions.contains(2),
            v3 = versions.contains(3)
        )

        val configs = if (options.autoFallback) {

            buildList {
                var current = selected
                while (current.isNotEmpty()) {
                    add(configOf(current))
                    current = current.dropLast(1)
                }
            }
        } else {

            listOf(configOf(selected))
        }

        for (config in configs) {
            try {
                AppLogger.d(TAG, "Trying signing scheme: ${config.name}")

                if (outputApk.exists()) outputApk.delete()

                val success = attemptSign(inputApk, outputApk, config.v1, config.v2, config.v3, v1Name)
                if (success) {
                    AppLogger.d(TAG, "Signed successfully: ${config.name}")
                    return true
                }

                errorMessages.add("${config.name}: 签名后 ApkVerifier 校验未通过")
                AppLogger.w(TAG, "${config.name} verification failed, falling back to a more conservative scheme")

            } catch (e: Throwable) {
                lastException = e
                val causeChain = getExceptionChain(e)
                val msg = "${config.name}: $causeChain"
                errorMessages.add(msg)
                AppLogger.e(TAG, "Signing exception [${config.name}]: $causeChain")
                AppLogger.e(TAG, "Full stack trace:", e)

                if (outputApk.exists()) outputApk.delete()

                if (causeChain.contains("key", ignoreCase = true) ||
                    causeChain.contains("sign", ignoreCase = true) ||
                    causeChain.contains("certificate", ignoreCase = true)) {
                    AppLogger.d(TAG, "Possible key issue detected, regenerating...")
                    File(context.filesDir, DEFAULT_PKCS12_FILE).delete()
                    File(context.filesDir, ".ks_credential").delete()
                    initializeKey()
                }
            }
        }

        val detail = errorMessages.joinToString("; ")
        AppLogger.e(TAG, "All signing schemes failed: $detail")
        throw RuntimeException(Strings.signApkFailed.format(configs.size, detail), lastException)
    }

    private fun getExceptionChain(e: Throwable): String {
        val parts = mutableListOf<String>()
        var current: Throwable? = e
        var depth = 0
        while (current != null && depth < 5) {
            parts.add("${current.javaClass.simpleName}: ${current.message}")
            current = current.cause
            depth++
        }
        return parts.joinToString(" → ")
    }

    private fun attemptSign(
        inputApk: File, outputApk: File,
        v1: Boolean, v2: Boolean, v3: Boolean,
        v1SignerName: String
    ): Boolean {
        val key = privateKey ?: throw IllegalStateException("私钥为空")
        val cert = certificate ?: throw IllegalStateException("证书为空")

        val effectiveMinSdk = when {
            v1 -> 23
            v2 -> 24
            else -> 28
        }

        AppLogger.d(TAG, "Certificate: subject=${cert.subjectX500Principal.name}, algo=${cert.sigAlgName}")
        AppLogger.d(TAG, "Key: algo=${key.algorithm}, format=${key.format}")
        AppLogger.d(TAG, "Certificate validity: ${cert.notBefore} - ${cert.notAfter}")
        AppLogger.d(TAG, "Signing config: V1=$v1, V2=$v2, V3=$v3, v1Name=$v1SignerName, minSdk=$effectiveMinSdk")

        val signerConfig = ApkSigner.SignerConfig.Builder(

            v1SignerName,
            key,
            listOf(cert)
        ).build()

        val builder = ApkSigner.Builder(listOf(signerConfig))
            .setInputApk(inputApk)
            .setOutputApk(outputApk)
            .setV1SigningEnabled(v1)
            .setV2SigningEnabled(v2)
            .setV3SigningEnabled(v3)
            .setMinSdkVersion(effectiveMinSdk)

        AppLogger.d(TAG, "Calling ApkSigner.sign()...")
        builder.build().sign()
        AppLogger.d(TAG, "ApkSigner.sign() complete")

        if (!outputApk.exists() || outputApk.length() == 0L) {
            AppLogger.e(TAG, "Signed output file is missing or empty")
            return false
        }

        AppLogger.d(TAG, "Signed output: ${outputApk.length() / 1024} KB")

        val schemeName = "ApkSigner-${if (v3) "V3" else if (v2) "V2" else "V1"}"
        return verifyApkDetailed(outputApk, schemeName)
    }

    private fun performApkSignerSign(inputApk: File, outputApk: File, key: PrivateKey, cert: X509Certificate): Boolean {
        AppLogger.d(TAG, "ApkSigner: signing: ${inputApk.name}")

        val signerConfig = ApkSigner.SignerConfig.Builder(
            SIGNER_BASENAME,
            key,
            listOf(cert)
        ).build()

        val builder = ApkSigner.Builder(listOf(signerConfig))
            .setInputApk(inputApk)
            .setOutputApk(outputApk)
            .setV1SigningEnabled(true)
            .setV2SigningEnabled(true)
            .setV3SigningEnabled(true)
            .setMinSdkVersion(23)

        try {
            val apkSigner = builder.build()
            apkSigner.sign()
        } catch (e: Exception) {
            AppLogger.e(TAG, "ApkSigner exception: ${e.message}")
            throw e
        }

        if (!outputApk.exists() || outputApk.length() == 0L) {
            AppLogger.e(TAG, "ApkSigner: output file is invalid")
            return false
        }

        return verifyApkDetailed(outputApk, "ApkSigner")
    }

    private fun performV1Sign(inputApk: File, outputApk: File, key: PrivateKey, cert: X509Certificate): Boolean {
        AppLogger.d(TAG, "V1 signing: ${inputApk.name}")

        try {
            val digests = mutableMapOf<String, String>()
            val entries = mutableMapOf<String, ByteArray>()

            ZipFile(inputApk).use { zipFile ->
                zipFile.entries().toList().forEach { entry ->
                    if (!entry.isDirectory && (!entry.name.startsWith("META-INF/") || entry.name.startsWith("META-INF/services/"))) {
                        val content = zipFile.getInputStream(entry).readBytes()
                        entries[entry.name] = content
                        digests[entry.name] = computeDigest(ByteArrayInputStream(content))
                    }
                }
            }

            val manifest = buildManifest(digests)
            val signatureFile = buildSignatureFile(manifest, digests)
            val pkcs7Signature = createSignatureBlock(signatureFile)

            FileOutputStream(outputApk).use { fos ->
                ZipOutputStream(fos).use { zos ->
                    writeZipEntry(zos, "META-INF/MANIFEST.MF", manifest)
                    writeZipEntry(zos, "META-INF/CERT.SF", signatureFile)
                    writeZipEntry(zos, "META-INF/CERT.RSA", pkcs7Signature)

                    entries["resources.arsc"]?.let { content ->
                        ZipUtils.writeEntryStored(zos, "resources.arsc", content)
                    }

                    entries.forEach { (name, content) ->
                        if (name == "resources.arsc") return@forEach
                        val entry = ZipEntry(name)
                        zos.putNextEntry(entry)
                        zos.write(content)
                        zos.closeEntry()
                    }
                }
            }

            val verified = verifyApkDetailed(outputApk, "V1")

            if (!verified) {
                AppLogger.e(TAG, "V1 signature verification failed, APK signature is invalid")
                if (outputApk.exists()) outputApk.delete()
                return false
            }

            return verified

        } catch (e: Exception) {
            AppLogger.e(TAG, "V1 signingfailed: ${e.message}")
            if (outputApk.exists()) outputApk.delete()
            return false
        }
    }

    private fun verifyApkDetailed(apk: File, source: String): Boolean {
        return try {
            AppLogger.d(TAG, "[$source] Verifying APK: path=${apk.absolutePath}, size=${apk.length()}")
            val verifier = ApkVerifier.Builder(apk).build()
            val result = verifier.verify()

            AppLogger.d(TAG, "[$source] APK verification complete: isVerified=${result.isVerified}, " +
                    "v1Verified=${result.isVerifiedUsingV1Scheme}, " +
                    "v2Verified=${result.isVerifiedUsingV2Scheme}, " +
                    "v3Verified=${result.isVerifiedUsingV3Scheme}, " +
                    "errors=${result.errors.size}, warnings=${result.warnings.size}")

            if (result.errors.isNotEmpty()) {
                result.errors.forEachIndexed { index, error ->
                    AppLogger.w(TAG, "[$source] Verification issue[$index]: $error")
                }
            }

            if (result.warnings.isNotEmpty()) {
                result.warnings.forEachIndexed { index, warning ->
                    AppLogger.w(TAG, "[$source] Verification warning[$index]: $warning")
                }
            }

            if (!result.isVerified) {
                AppLogger.w(TAG, "[$source] ApkVerifier flagged a verification failure, but the APK file structure is intact; " +
                    "the APK should still install on most devices. Skipping the verification gate.")
            }

            result.isVerified
        } catch (e: Exception) {
            AppLogger.w(TAG, "[$source] Verification raised an exception (build is unaffected): ${e.message}")
            false
        }
    }

    private fun computeDigest(input: InputStream): String {
        val md = MessageDigest.getInstance(DIGEST_ALGORITHM)
        val buffer = ByteArray(8192)
        var read: Int
        while (input.read(buffer).also { read = it } != -1) {
            md.update(buffer, 0, read)
        }
        return Base64.encodeToString(md.digest(), Base64.NO_WRAP)
    }

    private fun buildManifest(digests: Map<String, String>): ByteArray {
        val sb = StringBuilder()
        sb.append("Manifest-Version: 1.0\r\n")
        sb.append("Created-By: 1.0 (WebToApp)\r\n")
        sb.append("\r\n")

        digests.forEach { (name, digest) ->
            sb.append("Name: $name\r\n")
            sb.append("SHA-256-Digest: $digest\r\n")
            sb.append("\r\n")
        }

        return sb.toString().toByteArray(Charsets.UTF_8)
    }

    private fun buildSignatureFile(manifest: ByteArray, digests: Map<String, String>): ByteArray {
        val sb = StringBuilder()
        sb.append("Signature-Version: 1.0\r\n")
        sb.append("Created-By: 1.0 (WebToApp)\r\n")

        val manifestDigest = Base64.encodeToString(
            MessageDigest.getInstance(DIGEST_ALGORITHM).digest(manifest),
            Base64.NO_WRAP
        )
        sb.append("SHA-256-Digest-Manifest: $manifestDigest\r\n")
        sb.append("\r\n")

        digests.forEach { (name, digest) ->
            val entryBlock = "Name: $name\r\nSHA-256-Digest: $digest\r\n\r\n"
            val entryDigest = Base64.encodeToString(
                MessageDigest.getInstance(DIGEST_ALGORITHM).digest(entryBlock.toByteArray()),
                Base64.NO_WRAP
            )
            sb.append("Name: $name\r\n")
            sb.append("SHA-256-Digest: $entryDigest\r\n")
            sb.append("\r\n")
        }

        return sb.toString().toByteArray(Charsets.UTF_8)
    }

    private fun createSignatureBlock(sfContent: ByteArray): ByteArray {

        val signature = Signature.getInstance(SIGNATURE_ALGORITHM)
        signature.initSign(privateKey)
        signature.update(sfContent)
        val signatureBytes = signature.sign()

        return buildPkcs7SignedData(signatureBytes, certificate!!)
    }

    private fun buildPkcs7SignedData(signature: ByteArray, cert: X509Certificate): ByteArray {
        val certBytes = cert.encoded

        val contentInfo = buildContentInfo(signature, certBytes)

        val signedDataOid = byteArrayOf(
            0x06, 0x09, 0x2A, 0x86.toByte(), 0x48, 0x86.toByte(), 0xF7.toByte(),
            0x0D, 0x01, 0x07, 0x02
        )

        val innerContent = ByteArrayOutputStream()
        innerContent.write(signedDataOid)

        val explicitTag = wrapWithTag(0xA0.toByte(), contentInfo)
        innerContent.write(explicitTag)

        return wrapWithTag(0x30, innerContent.toByteArray())
    }

    private fun buildContentInfo(signature: ByteArray, certBytes: ByteArray): ByteArray {
        val content = ByteArrayOutputStream()

        content.write(byteArrayOf(0x02, 0x01, 0x01))

        val digestAlgSet = buildDigestAlgorithmSet()
        content.write(digestAlgSet)

        val dataContentInfo = buildDataContentInfo()
        content.write(dataContentInfo)

        val certsImplicit = wrapWithTag(0xA0.toByte(), certBytes)
        content.write(certsImplicit)

        val signerInfos = buildSignerInfos(signature, certificate!!)
        content.write(signerInfos)

        return wrapWithTag(0x30, content.toByteArray())
    }

    private fun buildDigestAlgorithmSet(): ByteArray {

        val sha256Oid = byteArrayOf(
            0x06, 0x09, 0x60, 0x86.toByte(), 0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x01
        )
        val algId = wrapWithTag(0x30, sha256Oid + byteArrayOf(0x05, 0x00))
        return wrapWithTag(0x31, algId)
    }

    private fun buildDataContentInfo(): ByteArray {

        val dataOid = byteArrayOf(
            0x06, 0x09, 0x2A, 0x86.toByte(), 0x48, 0x86.toByte(), 0xF7.toByte(),
            0x0D, 0x01, 0x07, 0x01
        )
        return wrapWithTag(0x30, dataOid)
    }

    private fun buildSignerInfos(signature: ByteArray, cert: X509Certificate): ByteArray {
        val signerInfo = ByteArrayOutputStream()

        signerInfo.write(byteArrayOf(0x02, 0x01, 0x01))

        val issuerAndSerial = buildIssuerAndSerial(cert)
        signerInfo.write(issuerAndSerial)

        val sha256Oid = byteArrayOf(
            0x06, 0x09, 0x60, 0x86.toByte(), 0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x01
        )
        signerInfo.write(wrapWithTag(0x30, sha256Oid + byteArrayOf(0x05, 0x00)))

        val rsaOid = byteArrayOf(
            0x06, 0x09, 0x2A, 0x86.toByte(), 0x48, 0x86.toByte(), 0xF7.toByte(),
            0x0D, 0x01, 0x01, 0x0B
        )
        signerInfo.write(wrapWithTag(0x30, rsaOid + byteArrayOf(0x05, 0x00)))

        signerInfo.write(wrapWithTag(0x04, signature))

        val signerInfoSeq = wrapWithTag(0x30, signerInfo.toByteArray())
        return wrapWithTag(0x31, signerInfoSeq)
    }

    private fun buildIssuerAndSerial(cert: X509Certificate): ByteArray {
        val content = ByteArrayOutputStream()

        content.write(cert.issuerX500Principal.encoded)

        val serial = cert.serialNumber.toByteArray()
        content.write(wrapWithTag(0x02, serial))

        return wrapWithTag(0x30, content.toByteArray())
    }

    private fun wrapWithTag(tag: Byte, content: ByteArray): ByteArray {
        val result = ByteArrayOutputStream()
        result.write(tag.toInt())
        writeLength(result, content.size)
        result.write(content)
        return result.toByteArray()
    }

    private fun writeLength(out: ByteArrayOutputStream, length: Int) {
        if (length < 128) {
            out.write(length)
        } else if (length < 256) {
            out.write(0x81)
            out.write(length)
        } else if (length < 65536) {
            out.write(0x82)
            out.write(length shr 8)
            out.write(length and 0xFF)
        } else {
            out.write(0x83)
            out.write(length shr 16)
            out.write((length shr 8) and 0xFF)
            out.write(length and 0xFF)
        }
    }

    private fun writeZipEntry(zos: ZipOutputStream, name: String, content: ByteArray) {
        val entry = ZipEntry(name)
        entry.method = ZipEntry.STORED
        entry.size = content.size.toLong()
        entry.compressedSize = content.size.toLong()

        val crc = CRC32()
        crc.update(content)
        entry.crc = crc.value

        zos.putNextEntry(entry)
        zos.write(content)
        zos.closeEntry()
    }

    fun isReady(): Boolean = privateKey != null && certificate != null

    fun resetKeys(): Boolean {
        return try {
            AppLogger.d(TAG, "Resetting all signing keys...")

            File(context.filesDir, DEFAULT_PKCS12_FILE).delete()
            File(context.filesDir, CUSTOM_PKCS12_FILE).delete()
            File(context.filesDir, CUSTOM_PASSWORD_FILE).delete()
            File(context.filesDir, CUSTOM_ALIAS_FILE).delete()
            File(context.filesDir, CUSTOM_KEYPASS_FILE).delete()

            try {
                val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
                keyStore.load(null)
                if (keyStore.containsAlias(KEY_ALIAS)) {
                    keyStore.deleteEntry(KEY_ALIAS)
                    AppLogger.d(TAG, "Android KeyStore key deleted")
                }
            } catch (e: Exception) {
                AppLogger.w(TAG, "Failed to delete Android KeyStore key", e)
            }

            privateKey = null
            certificate = null

            initializeKey()

            AppLogger.d(TAG, "Key reset complete, current signer type: $currentSignerType")
            privateKey != null && certificate != null
        } catch (e: Exception) {
            AppLogger.e(TAG, "Key reset failed", e)
            false
        }
    }
}
