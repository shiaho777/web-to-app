package com.webtoapp.core.nodejs

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NodeLinkerPathTest {

    @Test
    fun `libnode directory is searched before the app lib dir`() {
        assertThat(nodeLinkerPath("/data/node/arm64-v8a", "/data/app/lib", ""))
            .isEqualTo("/data/node/arm64-v8a:/data/app/lib")
    }

    @Test
    fun `duplicate directories are kept once`() {
        assertThat(nodeLinkerPath("/data/app/lib", "/data/app/lib", "/data/app/lib:/opt"))
            .isEqualTo("/data/app/lib:/opt")
    }
}
