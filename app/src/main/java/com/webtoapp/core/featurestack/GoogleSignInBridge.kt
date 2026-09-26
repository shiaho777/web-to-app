package com.webtoapp.core.featurestack

import android.app.Activity
import com.webtoapp.core.featurestack.api.GoogleSignInStack
import com.webtoapp.core.featurestack.api.SignInCallback
import com.webtoapp.core.logging.AppLogger

/**
 * Facade for the optional `google_signin` feature stack (Credential Manager +
 * Google ID token). Returns false when the stack dex is absent so callers can
 * fail soft with an UNSUPPORTED payload.
 */
object GoogleSignInBridge {

    private const val TAG = "GoogleSignInBridge"

    fun signIn(
        activity: Activity,
        requestId: String,
        clientId: String,
        callback: SignInCallback
    ): Boolean {
        val stack = FeatureStackLoader.load<GoogleSignInStack>(
            activity, FeatureStackLoader.STACK_GOOGLE_SIGN_IN
        ) ?: return false
        return try {
            stack.signIn(activity, requestId, clientId, callback)
            true
        } catch (t: Throwable) {
            AppLogger.e(TAG, "Google sign-in stack invocation failed", t)
            false
        }
    }
}
