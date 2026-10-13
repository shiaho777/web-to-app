package com.webtoapp.core.webview

/**
 * Result of a native Google sign-in attempt. The Play Services implementation
 * and the F-Droid no-op live in different source sets and share this type.
 */
internal sealed class GoogleSignInAttempt {
    data class Ok(
        val idToken: String,
        val displayName: String,
        val profilePictureUri: String,
        val googleUserId: String,
    ) : GoogleSignInAttempt()

    data class Err(val code: String, val message: String) : GoogleSignInAttempt()
}
