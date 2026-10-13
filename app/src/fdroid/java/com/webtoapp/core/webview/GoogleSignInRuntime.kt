package com.webtoapp.core.webview

import android.app.Activity

/** F-Droid builds do not ship Play Services. */
internal object GoogleSignInRuntime {
    @Suppress("UNUSED_PARAMETER")
    suspend fun signIn(activity: Activity, serverClientId: String): GoogleSignInAttempt =
        GoogleSignInAttempt.Err(
            "UNAVAILABLE",
            "Google sign-in is not included in this build",
        )
}
