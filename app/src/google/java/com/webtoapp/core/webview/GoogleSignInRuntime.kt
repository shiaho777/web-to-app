package com.webtoapp.core.webview

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.webtoapp.core.logging.AppLogger

/**
 * Play Services Google sign-in. Only on the classpath when the build is not
 * `-Pfdroid=true`. The fdroid source set supplies a no-op with this same name.
 */
internal object GoogleSignInRuntime {
    suspend fun signIn(activity: Activity, serverClientId: String): GoogleSignInAttempt {
        return try {
            val credentialManager = CredentialManager.create(activity)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(activity, request)
            val credential = response.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                GoogleSignInAttempt.Ok(
                    idToken = googleCredential.idToken,
                    displayName = googleCredential.displayName ?: "",
                    profilePictureUri = googleCredential.profilePictureUri?.toString() ?: "",
                    googleUserId = googleCredential.id,
                )
            } else {
                GoogleSignInAttempt.Err(
                    "UNSUPPORTED_CREDENTIAL",
                    "Unexpected credential type: ${credential.type}",
                )
            }
        } catch (e: GetCredentialCancellationException) {
            GoogleSignInAttempt.Err("CANCELLED", "The user dismissed the account picker")
        } catch (e: GetCredentialException) {
            AppLogger.w("NativeBridge", "Google sign-in failed: ${e.type}", e)
            GoogleSignInAttempt.Err(
                "CREDENTIAL_ERROR",
                "Google sign-in failed (${e.type}): ${e.message ?: e::class.java.simpleName}",
            )
        } catch (e: Exception) {
            AppLogger.e("NativeBridge", "Google sign-in failed", e)
            GoogleSignInAttempt.Err("UNAVAILABLE", e.message ?: e::class.java.simpleName)
        }
    }
}
