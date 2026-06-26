package com.webtoapp.core.webview

import android.util.LruCache
import com.webtoapp.core.logging.AppLogger
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.ExtendedKeyUsage
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.GeneralName
import org.bouncycastle.asn1.x509.GeneralNames
import org.bouncycastle.asn1.x509.KeyPurposeId
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.File
import java.math.BigInteger
import java.net.Socket
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Principal
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Date
import java.util.Locale
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.X509KeyManager
import javax.security.auth.x500.X500Principal

object TlsMitmCaManager {

    private const val TAG = "TlsMitmCaManager"
    private const val CA_KEY_SIZE = 2048
    private const val CA_VALIDITY_DAYS = 3650L
    private const val LEAF_VALIDITY_DAYS = 365L
    private const val CA_CN = "WebToApp TLS MITM CA"
    private const val CA_ORG = "WebToApp"
    private const val CACHE_SIZE = 256

    private val leafCache = LruCache<String, KeyStore.PrivateKeyEntry>(CACHE_SIZE)

    @Volatile
    private var caKeyPair: KeyPair? = null

    @Volatile
    private var caCert: X509Certificate? = null

    @Volatile
    private var caPrincipal: X500Principal? = null

    @Volatile
    private var initialized = false

    @Synchronized
    fun init(caDir: File) {
        if (initialized) return
        try {
            val keyFile = File(caDir, "mitm_ca_key.bks")
            val certFile = File(caDir, "mitm_ca_cert.cer")

            if (certFile.exists() && keyFile.exists()) {
                val keyBytes = keyFile.readBytes()
                val certBytes = certFile.readBytes()
                val key = KeyFactory.getInstance("RSA")
                    .generatePrivate(PKCS8EncodedKeySpec(keyBytes))
                val cert = CertificateFactory.getInstance("X.509")
                    .generateCertificate(certBytes.inputStream()) as X509Certificate
                caKeyPair = KeyPair(cert.publicKey, key)
                caCert = cert
                caPrincipal = cert.issuerX500Principal
                AppLogger.i(TAG, "Loaded existing MITM CA from ${caDir.absolutePath}")
            } else {
                caDir.mkdirs()
                generateCa(caDir)
            }
            initialized = true
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to init CA manager", e)
            try {
                generateCa(caDir)
                initialized = true
            } catch (e2: Exception) {
                AppLogger.e(TAG, "Fallback CA generation also failed", e2)
            }
        }
    }

    private fun generateCa(caDir: File) {
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(CA_KEY_SIZE)
        val pair = keyGen.generateKeyPair()

        val issuer = X500Principal("CN=$CA_CN, O=$CA_ORG")
        val name = X500Name(issuer.name)
        val serial = BigInteger.valueOf(System.currentTimeMillis())

        val notBefore = Date()
        val notAfter = Date(notBefore.time + CA_VALIDITY_DAYS * 24 * 60 * 60 * 1000L)

        val certBuilder = JcaX509v3CertificateBuilder(
            name,
            serial,
            notBefore,
            notAfter,
            name,
            pair.public
        )

        certBuilder.addExtension(
            Extension.basicConstraints,
            true,
            BasicConstraints(true)
        )
        certBuilder.addExtension(
            Extension.keyUsage,
            true,
            KeyUsage(
                KeyUsage.digitalSignature or
                KeyUsage.keyCertSign or
                KeyUsage.cRLSign
            )
        )

        val holder = certBuilder.build(
            JcaContentSignerBuilder("SHA256withRSA").build(pair.private)
        )
        val cert = JcaX509CertificateConverter().getCertificate(holder)

        File(caDir, "mitm_ca_key.bks").writeBytes(pair.private.encoded)
        File(caDir, "mitm_ca_cert.cer").writeBytes(cert.encoded)

        caKeyPair = pair
        caCert = cert
        caPrincipal = issuer
        AppLogger.i(TAG, "Generated new MITM CA in ${caDir.absolutePath}")
    }

    fun getCaCertificate(): X509Certificate? = caCert

    fun isCaInitialized(): Boolean = initialized

    fun isSignedByLocalCa(cert: X509Certificate?): Boolean {
        if (cert == null || !initialized) return false
        val ca = caCert ?: return false
        return try {
            cert.issuerX500Principal == ca.subjectX500Principal
        } catch (_: Exception) {
            false
        }
    }

    @Synchronized
    fun getOrCreateLeafEntry(host: String): KeyStore.PrivateKeyEntry? {
        if (!initialized) return null
        val normalizedHost = host.lowercase(Locale.ROOT).trim()
        leafCache.get(normalizedHost)?.let { return it }

        val pair = caKeyPair ?: return null
        val caCertLocal = caCert ?: return null
        val caPrivateKey = pair.private

        try {
            val leafKeyGen = KeyPairGenerator.getInstance("RSA")
            leafKeyGen.initialize(CA_KEY_SIZE)
            val leafPair = leafKeyGen.generateKeyPair()

            val issuer = caPrincipal ?: return null
            val issuerName = X500Name(issuer.name)
            val subjectName = X500Name("CN=$normalizedHost")

            val serial = BigInteger.valueOf(System.currentTimeMillis())
            val notBefore = Date()
            val notAfter = Date(notBefore.time + LEAF_VALIDITY_DAYS * 24 * 60 * 60 * 1000L)

            val certBuilder = JcaX509v3CertificateBuilder(
                issuerName,
                serial,
                notBefore,
                notAfter,
                subjectName,
                leafPair.public
            )

            certBuilder.addExtension(
                Extension.subjectAlternativeName,
                false,
                GeneralNames(arrayOf(GeneralName(GeneralName.dNSName, normalizedHost)))
            )

            certBuilder.addExtension(
                Extension.basicConstraints,
                true,
                BasicConstraints(false)
            )

            certBuilder.addExtension(
                Extension.keyUsage,
                true,
                KeyUsage(
                    KeyUsage.digitalSignature or
                    KeyUsage.keyEncipherment
                )
            )

            certBuilder.addExtension(
                Extension.extendedKeyUsage,
                false,
                ExtendedKeyUsage(arrayOf(KeyPurposeId.id_kp_serverAuth))
            )

            val holder = certBuilder.build(
                JcaContentSignerBuilder("SHA256withRSA").build(caPrivateKey)
            )
            val leafCert = JcaX509CertificateConverter().getCertificate(holder)

            val keyStore = KeyStore.getInstance("PKCS12")
            keyStore.load(null, null)
            keyStore.setKeyEntry(
                normalizedHost,
                leafPair.private,
                null,
                arrayOf(leafCert, caCertLocal)
            )

            val entry = keyStore.getEntry(
                normalizedHost,
                KeyStore.PasswordProtection(null)
            ) as KeyStore.PrivateKeyEntry

            leafCache.put(normalizedHost, entry)
            return entry
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to sign leaf cert for $normalizedHost", e)
            return null
        }
    }

    fun createKeyManagerFactory(host: String): KeyManagerFactory? {
        val entry = getOrCreateLeafEntry(host) ?: return null
        val keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(null, null)
        keyStore.setKeyEntry(host, entry.privateKey, null, entry.certificateChain)
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        kmf.init(keyStore, null)
        return kmf
    }

    fun clearCache() {
        leafCache.evictAll()
    }

    private class SingleHostKeyManager(
        private val host: String,
        private val entry: KeyStore.PrivateKeyEntry
    ) : X509KeyManager {

        override fun getClientAliases(keyType: String, issuers: Array<Principal>): Array<String> = arrayOf(host)

        override fun chooseClientAlias(keyType: Array<String>, issuers: Array<Principal>, socket: Socket): String = host

        override fun getServerAliases(keyType: String, issuers: Array<Principal>): Array<String> = arrayOf(host)

        override fun chooseServerAlias(keyType: String, issuers: Array<Principal>, socket: Socket): String = host

        override fun getCertificateChain(alias: String?): Array<X509Certificate> {
            return entry.certificateChain.map { it as X509Certificate }.toTypedArray()
        }

        override fun getPrivateKey(alias: String?): PrivateKey = entry.privateKey
    }
}
