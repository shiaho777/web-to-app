package com.webtoapp.core.crypto

data class EncryptionConfig(
    val enabled: Boolean = false,
    val customPassword: String? = null
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
