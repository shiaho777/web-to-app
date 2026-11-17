package com.webtoapp.core.activation

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.security.MessageDigest

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ActivationHashedCodeTest {

    private lateinit var manager: ActivationManager
    private lateinit var context: android.content.Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        manager = ActivationManager(context)
    }

    private fun digest(code: String): String {
        val normalized = code.replace("-", "").replace(" ", "").uppercase().trim()
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest((normalized + "WebToApp_Salt_2024").toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    @Test
    fun `sha256 stored lowercase digest verifies`() = runTest {
        val stored = ActivationCode(code = digest("SECRET-CODE"), type = ActivationCodeType.PERMANENT)
        val result = manager.verifyActivationCodeWithObjects(9001L, "SECRET-CODE", listOf(stored))
        assertThat(result).isInstanceOf(ActivationResult.Success::class.java)
    }

    @Test
    fun `sha256 stored uppercase digest verifies`() = runTest {
        val stored = ActivationCode(code = digest("SECRET-CODE").uppercase(), type = ActivationCodeType.PERMANENT)
        val result = manager.verifyActivationCodeWithObjects(9002L, "SECRET-CODE", listOf(stored))
        assertThat(result).isInstanceOf(ActivationResult.Success::class.java)
    }

    @Test
    fun `wrong code against hashed store fails`() = runTest {
        val stored = ActivationCode(code = digest("SECRET-CODE"), type = ActivationCodeType.PERMANENT)
        val result = manager.verifyActivationCodeWithObjects(9003L, "WRONG-CODE", listOf(stored))
        assertThat(result).isInstanceOf(ActivationResult.Invalid::class.java)
    }

    @Test
    fun `plaintext stored code still verifies`() = runTest {
        val stored = ActivationCode(code = "PLAIN-CODE", type = ActivationCodeType.PERMANENT)
        val result = manager.verifyActivationCodeWithObjects(9004L, "PLAIN-CODE", listOf(stored))
        assertThat(result).isInstanceOf(ActivationResult.Success::class.java)
    }
}
