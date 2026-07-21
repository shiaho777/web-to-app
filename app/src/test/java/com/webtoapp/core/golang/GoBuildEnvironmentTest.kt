package com.webtoapp.core.golang

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GoBuildEnvironmentTest {

    @Test
    fun `looksLikeNoSpace detects device full errors`() {
        assertThat(
            GoBuildEnvironment.looksLikeNoSpace(
                "golang.org/x/text/unicode/bidi: mkdir /data/user/0/com.webtoapp/cache/go-build1/b254/: no space left on device"
            )
        ).isTrue()
        assertThat(GoBuildEnvironment.looksLikeNoSpace("ENOSPC")).isTrue()
        assertThat(GoBuildEnvironment.looksLikeNoSpace("compile error: undefined: Foo")).isFalse()
    }
}
