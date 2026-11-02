package com.webtoapp.core.appmodifier

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ClonePackageNamesTest {

    @Test
    fun `generated name is valid and differs from the original`() {
        val original = "com.example.app"
        val generated = ClonePackageNames.generate(original)

        assertThat(generated).isNotEqualTo(original)
        assertThat(ClonePackageNames.isValid(generated)).isTrue()
    }

    @Test
    fun `accepts a user package with at least two segments`() {
        assertThat(ClonePackageNames.isValid("com.example.app")).isTrue()
        assertThat(ClonePackageNames.isValid("com.example.app.clone")).isTrue()
        assertThat(ClonePackageNames.isValid("c.x1")).isTrue()
    }

    @Test
    fun `rejects malformed and reserved names`() {
        assertThat(ClonePackageNames.isValid("single")).isFalse()
        assertThat(ClonePackageNames.isValid("Com.Example.App")).isFalse()
        assertThat(ClonePackageNames.isValid("com..app")).isFalse()
        assertThat(ClonePackageNames.isValid("com.example.app-1")).isFalse()
        assertThat(ClonePackageNames.isValid("com.example.1app")).isFalse()
        assertThat(ClonePackageNames.isValid("android.permission")).isFalse()
        assertThat(ClonePackageNames.isValid("com.android.foo")).isFalse()
        assertThat(ClonePackageNames.isValid("")).isFalse()
    }
}
