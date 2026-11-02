package com.webtoapp.core.appmodifier

import com.webtoapp.util.AppConstants

/**
 * Package-name helpers for App Modifier clones.
 *
 * A clone is re-signed with the builder key, so it cannot reuse an installed
 * app's package name: the system would treat it as an update with a different
 * signature. The default is therefore a unique value; the user can replace it
 * with any other valid, non-installed package.
 */
object ClonePackageNames {

    private const val MAX_LENGTH = 200

    /** Prefixes the platform reserves for itself or system packages. */
    private val RESERVED_PREFIXES = listOf("android", "com.android")

    /**
     * A unique, valid package name derived from the current time and a random
     * suffix. The original package is only used to bound the total length.
     */
    fun generate(originalPackageName: String): String {
        val maxLen = originalPackageName.length.coerceAtMost(128)
        val timestamp = System.currentTimeMillis()
        val random = (0..9999).random()
        val uniqueId = ((timestamp xor random.toLong()) and 0x7FFFFFFF).toString(36)

        val suffixLen = (maxLen - 2).coerceAtLeast(1)
        val rawSuffix = if (uniqueId.length >= suffixLen) {
            uniqueId.take(suffixLen)
        } else {
            uniqueId.padEnd(suffixLen, '0')
        }

        return "c.${normalizeSegment(rawSuffix)}"
    }

    fun isValid(packageName: String): Boolean {
        val name = packageName.trim()
        if (name.isEmpty() || name.length > MAX_LENGTH) return false
        if (!AppConstants.PACKAGE_NAME_REGEX.matches(name)) return false
        return RESERVED_PREFIXES.none { name == it || name.startsWith("$it.") }
    }

    private fun normalizeSegment(segment: String): String {
        if (segment.isEmpty()) return "a"
        val chars = segment.lowercase().toCharArray()
        chars[0] = when {
            chars[0] in 'a'..'z' -> chars[0]
            chars[0] in '0'..'9' -> ('a' + (chars[0] - '0'))
            else -> 'a'
        }
        return String(chars)
    }
}
