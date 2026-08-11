package com.webtoapp.core.webview

import android.content.Context
import com.webtoapp.core.logging.AppLogger
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Trust anchors the user imported via the editor's "Network trust → Import custom CA" panel.
 *
 * At export time [com.webtoapp.core.apkbuilder.ApkBuilder] writes each imported certificate's
 * canonical DER bytes under `assets/wta_custom_ca/` inside the generated APK. At runtime this
 * store scans that directory once, loads every certificate into a KeyStore, and exposes
 * [isServerCertTrusted] for the WebView SSL error handler.
 *
 * The built template APK otherwise pins a `network_security_config.xml` that trusts only the
 * system store (editing the compiled binary resource table per-app is fragile and high blast
 * radius). This store is the additive, safe channel that lets a generated app honor the user's
 * imported CAs without touching the template's resource table: an empty asset directory means
 * no anchors, and [isServerCertTrusted] returns false with zero behavior change.
 *
 * Limitation: WebView's [android.net.http.SslError] exposes only the server leaf, not the chain,
 * so we validate the leaf directly against the imported anchors (the common case: the imported
 * CA is the direct issuer of the server's certificate). Full PKIX path building across an
 * intermediate the server did not send is not possible from this hook; source-project export
 * remains the path for that.
 */
object CustomCaTrustStore {
    private const val TAG = "CustomCaTrustStore"
    private const val ASSET_DIR = "wta_custom_ca"

    @Volatile private var validator: CustomCaValidator? = null
    @Volatile private var initialized = false

    @Synchronized
    fun init(context: Context) {
        if (initialized) return
        val anchors = loadFromAssets(context)
        validator = CustomCaValidator(anchors)
        initialized = true
        if (anchors.isNotEmpty()) {
            AppLogger.i(TAG, "Loaded ${anchors.size} custom CA anchor(s) from assets/$ASSET_DIR")
        }
    }

    /** Test/host entry point: build directly from in-memory DER blobs, bypassing the asset scan. */
    @Synchronized
    fun initFromDer(certBytes: List<ByteArray>) {
        val factory = CertificateFactory.getInstance("X.509")
        val anchors = certBytes.mapNotNull { bytes ->
            runCatching {
                factory.generateCertificates(bytes.inputStream())
                    .filterIsInstance<X509Certificate>()
            }.getOrDefault(emptyList())
        }.flatten()
        validator = CustomCaValidator(anchors)
        initialized = true
    }

    @Synchronized
    fun reset() {
        validator = null
        initialized = false
    }

    fun hasAnchors(): Boolean = validator?.hasAnchors() == true

    /** True iff [leaf] cryptographically validates against an imported anchor. Fail-closed. */
    fun isServerCertTrusted(leaf: X509Certificate): Boolean =
        validator?.isServerCertTrusted(leaf) == true

    private fun loadFromAssets(context: Context): List<X509Certificate> {
        val names = runCatching { context.assets.list(ASSET_DIR) }.getOrNull()
        if (names.isNullOrEmpty()) return emptyList()
        val factory = CertificateFactory.getInstance("X.509")
        val out = mutableListOf<X509Certificate>()
        names.sorted().forEach { name ->
            runCatching {
                context.assets.open("$ASSET_DIR/$name").use { input ->
                    out.addAll(
                        factory.generateCertificates(input).filterIsInstance<X509Certificate>()
                    )
                }
            }.onFailure { e ->
                AppLogger.w(TAG, "Failed to load custom CA asset $name: ${e.message}")
            }
        }
        return out
    }
}

/**
 * Pure-JVM (unit-testable) validator backed by a PKIX [X509TrustManager] seeded with the imported
 * anchors. Every public method is fail-closed: any unexpected exception yields `false`, never a
 * crash — this object sits on the WebView SSL error hot path.
 */
class CustomCaValidator(private val anchors: List<X509Certificate>) {
    private val trustManager: X509TrustManager? = buildTrustManager()

    fun hasAnchors(): Boolean = anchors.isNotEmpty()

    fun isServerCertTrusted(leaf: X509Certificate): Boolean {
        if (anchors.isEmpty()) return false

        // 1. Real PKIX path validation with the imported anchors as trust roots. Handles the case
        //    where the imported CA is the direct issuer of the server leaf.
        val tm = trustManager
        if (tm != null) {
            runCatching {
                tm.checkServerTrusted(arrayOf(leaf), "ECDHE_RSA")
                return true
            }
        }

        // 2. Signature fallback for cases where PKIX is picky (e.g. the anchor lacks basicConstraints
        //    CA:true, or a critical extension Android dislikes): accept the leaf if its issuer DN
        //    matches an anchor and the leaf's signature actually verifies against that anchor's key.
        val issuer = leaf.issuerX500Principal
        return anchors.any { anchor ->
            anchor.subjectX500Principal == issuer &&
                runCatching { leaf.verify(anchor.publicKey); true }.getOrDefault(false)
        }
    }

    private fun buildTrustManager(): X509TrustManager? = runCatching {
        if (anchors.isEmpty()) return null
        val ks = KeyStore.getInstance(KeyStore.getDefaultType())
        ks.load(null, null)
        anchors.forEachIndexed { i, cert -> ks.setCertificateEntry("wta_custom_ca_$i", cert) }
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(ks)
        tmf.trustManagers.firstOrNull { it is X509TrustManager } as? X509TrustManager
    }.onFailure { e ->
        AppLogger.w("CustomCaValidator", "TrustManager build failed: ${e.message}")
    }.getOrNull()
}
