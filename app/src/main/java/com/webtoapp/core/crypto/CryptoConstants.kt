package com.webtoapp.core.crypto

object CryptoConstants {

    const val ENCRYPTED_HEADER_MAGIC = 0x57544145
    const val ENCRYPTED_HEADER_VERSION = 2

    const val AES_KEY_SIZE = 256
    const val AES_GCM_IV_SIZE = 12
    const val AES_GCM_TAG_SIZE = 128

    const val PBKDF2_ITERATIONS_PARANOID = 100000

    const val PBKDF2_ITERATIONS = PBKDF2_ITERATIONS_PARANOID

    const val ENCRYPTED_EXTENSION = ".enc"

    const val CONFIG_FILE = "app_config.json"
    const val ENCRYPTION_META_FILE = "encryption_meta.json"

    val HKDF_SALT = "WebToApp:KeyDerivation:v2".toByteArray()
    val HKDF_INFO_OBFUSCATION = "XOR:StringObfuscation".toByteArray()
}
