package com.webtoapp.core.playstore.aab

import android.content.Context
import android.util.Base64
import com.webtoapp.core.apkbuilder.JarSigner
import com.webtoapp.core.logging.AppLogger
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.Signature
import java.security.cert.X509Certificate
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class AabSigner(private val context: Context) {

    companion object {
        private const val TAG = "AabSigner"
        private const val MANIFEST_NAME = "META-INF/MANIFEST.MF"
        private const val SIGNATURE_FILE_NAME = "META-INF/CERT.SF"
        private const val SIGNATURE_BLOCK_NAME = "META-INF/CERT.RSA"

        private const val DIGEST_ALGORITHM = "SHA-256"
        private const val SIGNATURE_ALGORITHM = "SHA256withRSA"

        private const val MANIFEST_VERSION = "1.0"
        private const val SIGNATURE_VERSION = "1.0"
    }

    fun sign(inputAab: File, outputAab: File): Boolean {
        require(inputAab.exists()) { "Input AAB not found: ${inputAab.absolutePath}" }
        outputAab.parentFile?.mkdirs()
        if (outputAab.exists()) outputAab.delete()

        val (privateKey, certificate) = loadKeyAndCert() ?: run {
            AppLogger.e(TAG, "无法加载签名密钥/证书")
            return false
        }

        AppLogger.d(TAG, "开始 JAR v1 签名 AAB: ${inputAab.length() / 1024}KB")

        try {
            ZipFile(inputAab).use { inZip ->
                ZipOutputStream(FileOutputStream(outputAab)).use { out ->

                    val manifest = StringBuilder()
                    manifest.append("Manifest-Version: $MANIFEST_VERSION\r\n")
                    manifest.append("Created-By: WebToApp AAB Signer\r\n")
                    manifest.append("\r\n")

                    val perEntryManifestSections = LinkedHashMap<String, String>()

                    val entries = inZip.entries().toList().sortedBy { it.name }
                    for (entry in entries) {
                        if (entry.isDirectory) continue

                        if (entry.name.startsWith("META-INF/")) continue

                        val digest = sha256Base64Streamed(inZip, entry)
                        val section = buildString {
                            append("Name: ${entry.name}\r\n")
                            append("SHA-256-Digest: $digest\r\n")
                            append("\r\n")
                        }
                        manifest.append(section)
                        perEntryManifestSections[entry.name] = section
                    }

                    val manifestBytes = manifest.toString().toByteArray(Charsets.UTF_8)

                    val signatureFile = StringBuilder()
                    signatureFile.append("Signature-Version: $SIGNATURE_VERSION\r\n")
                    signatureFile.append("Created-By: WebToApp AAB Signer\r\n")

                    signatureFile.append(
                        "SHA-256-Digest-Manifest: ${sha256Base64(manifestBytes)}\r\n"
                    )
                    signatureFile.append("\r\n")

                    for ((name, section) in perEntryManifestSections) {
                        signatureFile.append("Name: $name\r\n")
                        signatureFile.append("SHA-256-Digest: ${sha256Base64(section.toByteArray(Charsets.UTF_8))}\r\n")
                        signatureFile.append("\r\n")
                    }
                    val sfBytes = signatureFile.toString().toByteArray(Charsets.UTF_8)

                    val rsaBytes = buildPkcs7Signature(sfBytes, privateKey, certificate)

                    writeEntry(out, MANIFEST_NAME, manifestBytes)
                    writeEntry(out, SIGNATURE_FILE_NAME, sfBytes)
                    writeEntry(out, SIGNATURE_BLOCK_NAME, rsaBytes)

                    for (entry in entries) {
                        if (entry.isDirectory) continue
                        if (entry.name.startsWith("META-INF/")) continue

                        out.putNextEntry(ZipEntry(entry.name))
                        inZip.getInputStream(entry).use { it.copyTo(out) }
                        out.closeEntry()
                    }
                }
            }

            AppLogger.d(TAG, "AAB 签名完成: ${outputAab.length() / 1024}KB")
            return true
        } catch (e: Exception) {
            AppLogger.e(TAG, "AAB 签名失败", e)
            if (outputAab.exists()) outputAab.delete()
            return false
        }
    }

    private fun loadKeyAndCert(): Pair<PrivateKey, X509Certificate>? {
        val tempPassword = "wta_aab_export_${System.currentTimeMillis()}"
        val tempFile = File(context.cacheDir, "aab_signer_keystore.p12")

        return try {
            val signer = JarSigner(context)
            if (!signer.exportPkcs12(tempFile, tempPassword)) {
                AppLogger.e(TAG, "JarSigner.exportPkcs12 失败")
                return null
            }

            val ks = java.security.KeyStore.getInstance("PKCS12")
            tempFile.inputStream().use { ks.load(it, tempPassword.toCharArray()) }
            val alias = ks.aliases().toList().firstOrNull { ks.isKeyEntry(it) }
                ?: return null
            val key = ks.getKey(alias, tempPassword.toCharArray()) as? PrivateKey ?: return null
            val cert = ks.getCertificate(alias) as? X509Certificate ?: return null
            key to cert
        } catch (e: Exception) {
            AppLogger.e(TAG, "加载密钥失败", e)
            null
        } finally {
            if (tempFile.exists()) tempFile.delete()
        }
    }

    private fun sha256Base64(data: ByteArray): String {
        val digest = MessageDigest.getInstance(DIGEST_ALGORITHM).digest(data)
        return Base64.encodeToString(digest, Base64.NO_WRAP)
    }

    private fun sha256Base64Streamed(zipFile: ZipFile, entry: ZipEntry): String {
        val md = MessageDigest.getInstance(DIGEST_ALGORITHM)
        val buf = ByteArray(64 * 1024)
        zipFile.getInputStream(entry).use { input ->
            while (true) {
                val n = input.read(buf)
                if (n < 0) break
                md.update(buf, 0, n)
            }
        }
        return Base64.encodeToString(md.digest(), Base64.NO_WRAP)
    }

    private fun buildPkcs7Signature(
        signedData: ByteArray,
        privateKey: PrivateKey,
        certificate: X509Certificate
    ): ByteArray {

        val OID_SIGNED_DATA = decodeHex("06092A864886F70D010702")
        val OID_DATA = decodeHex("06092A864886F70D010701")
        val OID_SHA256 = decodeHex("0609608648016503040201")
        val OID_RSA_ENCRYPTION = decodeHex("06092A864886F70D010101")
        val NULL = byteArrayOf(0x05, 0x00)

        val sha256AlgId = derSequence(OID_SHA256 + NULL)
        val digestAlgorithms = derSet(sha256AlgId)

        val encapContentInfo = derSequence(OID_DATA)

        val certDer = certificate.encoded
        val certificatesSetContent = certDer
        val certificates = derImplicitTag(0xA0, certificatesSetContent)

        val issuerDer = certificate.issuerX500Principal.encoded
        val serial = certificate.serialNumber
        val serialDer = derInteger(serial.toByteArray())
        val issuerAndSerial = derSequence(issuerDer + serialDer)

        val signerVersion = derInteger(byteArrayOf(0x01))
        val signerDigestAlg = sha256AlgId
        val signerEncAlg = derSequence(OID_RSA_ENCRYPTION + NULL)

        val sig = Signature.getInstance(SIGNATURE_ALGORITHM)
        sig.initSign(privateKey)
        sig.update(signedData)
        val signatureBytes = sig.sign()
        val encryptedDigest = derOctetString(signatureBytes)

        val signerInfo = derSequence(
            signerVersion + issuerAndSerial + signerDigestAlg + signerEncAlg + encryptedDigest
        )
        val signerInfos = derSet(signerInfo)

        val signedDataVersion = derInteger(byteArrayOf(0x01))
        val signedDataInner = derSequence(
            signedDataVersion + digestAlgorithms + encapContentInfo + certificates + signerInfos
        )

        val explicitTag = derExplicitTag(0xA0, signedDataInner)
        val contentInfo = derSequence(OID_SIGNED_DATA + explicitTag)

        return contentInfo
    }

    private fun derLength(length: Int): ByteArray {
        return when {
            length < 0x80 -> byteArrayOf(length.toByte())
            length < 0x100 -> byteArrayOf(0x81.toByte(), length.toByte())
            length < 0x10000 -> byteArrayOf(
                0x82.toByte(),
                ((length ushr 8) and 0xFF).toByte(),
                (length and 0xFF).toByte()
            )
            length < 0x1000000 -> byteArrayOf(
                0x83.toByte(),
                ((length ushr 16) and 0xFF).toByte(),
                ((length ushr 8) and 0xFF).toByte(),
                (length and 0xFF).toByte()
            )
            else -> byteArrayOf(
                0x84.toByte(),
                ((length ushr 24) and 0xFF).toByte(),
                ((length ushr 16) and 0xFF).toByte(),
                ((length ushr 8) and 0xFF).toByte(),
                (length and 0xFF).toByte()
            )
        }
    }

    private fun derSequence(content: ByteArray): ByteArray =
        byteArrayOf(0x30) + derLength(content.size) + content

    private fun derSet(content: ByteArray): ByteArray =
        byteArrayOf(0x31) + derLength(content.size) + content

    private fun derInteger(bytes: ByteArray): ByteArray {

        var b = bytes
        if (b.isNotEmpty() && (b[0].toInt() and 0x80) != 0) {

            b = byteArrayOf(0x00) + b
        }
        return byteArrayOf(0x02) + derLength(b.size) + b
    }

    private fun derOctetString(bytes: ByteArray): ByteArray =
        byteArrayOf(0x04) + derLength(bytes.size) + bytes

    private fun derImplicitTag(tag: Int, content: ByteArray): ByteArray =
        byteArrayOf(tag.toByte()) + derLength(content.size) + content

    private fun derExplicitTag(tag: Int, content: ByteArray): ByteArray =
        byteArrayOf(tag.toByte()) + derLength(content.size) + content

    private fun decodeHex(hex: String): ByteArray {
        val out = ByteArray(hex.length / 2)
        for (i in out.indices) {
            out[i] = ((hex[i * 2].digitToInt(16) shl 4) or hex[i * 2 + 1].digitToInt(16)).toByte()
        }
        return out
    }

    private fun writeEntry(out: ZipOutputStream, name: String, data: ByteArray) {
        val entry = ZipEntry(name)
        out.putNextEntry(entry)
        out.write(data)
        out.closeEntry()
    }
}
