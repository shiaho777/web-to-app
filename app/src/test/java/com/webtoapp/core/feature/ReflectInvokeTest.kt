package com.webtoapp.core.feature

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ReflectInvokeTest {
    object SampleObject {
        fun greet(name: String): String = "hi-$name"
        fun flag(on: Boolean): String = if (on) "on" else "off"
    }

    class SampleClass {
        companion object {
            fun create(id: String): String = "id-$id"
        }
    }

    @Test
    fun `invokes kotlin object methods via INSTANCE`() {
        assertThat(ReflectInvoke.call(SampleObject::class.java, "greet", "wta")).isEqualTo("hi-wta")
        assertThat(ReflectInvoke.call(SampleObject::class.java, "flag", true)).isEqualTo("on")
    }

    @Test
    fun `invokes companion methods via Companion`() {
        assertThat(ReflectInvoke.call(SampleClass::class.java, "create", "42")).isEqualTo("id-42")
    }

    @Test
    fun `call by class name resolves object methods`() {
        assertThat(
            ReflectInvoke.call(
                "com.webtoapp.core.feature.ReflectInvokeTest\$SampleObject",
                "greet",
                "pack"
            )
        ).isEqualTo("hi-pack")
    }

    @Test
    fun `missing method returns null`() {
        assertThat(ReflectInvoke.call(SampleObject::class.java, "missing")).isNull()
    }
}
