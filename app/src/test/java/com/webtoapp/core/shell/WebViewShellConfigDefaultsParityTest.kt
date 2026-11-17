package com.webtoapp.core.shell

import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.WebViewConfig
import org.junit.Test

class WebViewShellConfigDefaultsParityTest {

    @Test
    fun `shell config defaults match the model for every same-named boolean field`() {
        val model = WebViewConfig()
        val shell = WebViewShellConfig()
        val shellFields = WebViewShellConfig::class.java.declaredFields.associateBy { it.name }

        val mismatches = mutableListOf<String>()
        for (field in WebViewConfig::class.java.declaredFields) {
            if (field.type != java.lang.Boolean.TYPE) continue
            val shellField = shellFields[field.name] ?: continue
            if (shellField.type != java.lang.Boolean.TYPE) continue
            field.isAccessible = true
            shellField.isAccessible = true
            val modelDefault = field.getBoolean(model)
            val shellDefault = shellField.getBoolean(shell)
            if (modelDefault != shellDefault) {
                mismatches += "${field.name}: model=$modelDefault shell=$shellDefault"
            }
        }
        assertThat(mismatches).isEmpty()
    }

    @Test
    fun `geolocation accuracy default matches the model`() {
        assertThat(WebViewShellConfig().geolocationAccuracy).isEqualTo(WebViewConfig().geolocationAccuracy.name)
    }

    @Test
    fun `legacy json without keys deserializes to model-aligned defaults`() {
        val legacy = com.webtoapp.util.GsonProvider.gson.fromJson("{}", WebViewShellConfig::class.java)
        assertThat(legacy.enableScrollMemory).isTrue()
        assertThat(legacy.enableNativeBridge).isTrue()
        assertThat(legacy.primeUserActivation).isTrue()
        assertThat(legacy.nativeBridgeNotification).isFalse()
        assertThat(legacy.geolocationAccuracy).isEqualTo("FINE")
    }
}
