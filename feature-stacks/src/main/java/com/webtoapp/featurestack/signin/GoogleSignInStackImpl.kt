package com.webtoapp.featurestack.signin

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.webtoapp.core.featurestack.api.FeatureRuntime
import com.webtoapp.core.featurestack.api.GoogleSignInStack
import com.webtoapp.core.featurestack.api.SignInCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Native Google sign-in via Jetpack Credential Manager — same flow the host
 * NativeBridge used to run inline, moved into the `google_signin` feature dex.
 */
class GoogleSignInStackImpl : GoogleSignInStack {

    private lateinit var runtime: FeatureRuntime
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun init(context: Context, runtime: FeatureRuntime) {
        this.runtime = runtime
    }

    override fun signIn(
        activity: Activity,
        requestId: String,
        clientId: String,
        callback: SignInCallback
    ) {
        scope.launch {
            try {
                val credentialManager = CredentialManager.create(activity)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(clientId)
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
                    val payload = JSONObject().apply {
                        put("ok", true)
                        put("idToken", googleCredential.idToken)
                        put("displayName", googleCredential.displayName ?: "")
                        put("profilePictureUri", googleCredential.profilePictureUri?.toString() ?: "")
                        put("googleUserId", googleCredential.id)
                    }
                    callback.onResult(requestId, payload)
                } else {
                    callback.onResult(
                        requestId,
                        error("UNSUPPORTED_CREDENTIAL", "Unexpected credential type: ${credential.type}")
                    )
                }
            } catch (e: GetCredentialCancellationException) {
                callback.onResult(requestId, error("CANCELLED", "The user dismissed the account picker"))
            } catch (e: GetCredentialException) {
                runtime.log(
                    FeatureRuntime.LOG_WARN, TAG,
                    "Google sign-in failed: ${e.type}", e
                )
                callback.onResult(
                    requestId,
                    error(
                        "CREDENTIAL_ERROR",
                        "Google sign-in failed (${e.type}): ${e.message ?: e.javaClass.simpleName}"
                    )
                )
            } catch (e: Exception) {
                runtime.log(FeatureRuntime.LOG_ERROR, TAG, "Google sign-in failed", e)
                callback.onResult(
                    requestId,
                    error("UNAVAILABLE", e.message ?: e.javaClass.simpleName)
                )
            }
        }
    }

    private fun error(code: String, message: String): JSONObject =
        JSONObject().apply {
            put("ok", false)
            put("error", code)
            put("message", message)
        }

    private companion object {
        const val TAG = "GoogleSignInStack"
    }
}
