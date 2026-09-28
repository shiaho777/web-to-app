package com.webtoapp.core.engine

import com.google.common.truth.Truth.assertWithMessage
import java.io.File
import org.junit.Test

/**
 * Issue #1090: GeckoView-powered generated APKs crashed on launch with
 * `ExceptionInInitializerError` inside `DebugConfig.fromFile`. R8 repackaged
 * `org.yaml.snakeyaml.TypeDescription` into the root package, where
 * `Class.getPackage()` returns null and its static initializer's
 * `getPackage().getName()` dereference threw. SnakeYAML class names must stay
 * un-obfuscated in every minified artifact that ships GeckoView.
 */
class ShellProguardRulesTest {

    @Test
    fun `shell keeps snakeyaml class names so getPackage stays non-null`() {
        assertKeepsSnakeYaml("shell/proguard-rules.pro", "proguard-rules.pro")
    }

    @Test
    fun `app keeps snakeyaml class names so getPackage stays non-null`() {
        assertKeepsSnakeYaml("app/proguard-rules.pro", "proguard-rules.pro")
    }

    private fun assertKeepsSnakeYaml(rootPath: String, modulePath: String) {
        val file = listOf(rootPath, modulePath).asSequence()
            .map(::File).firstOrNull(File::isFile)
            ?: error("Cannot locate proguard rules at $rootPath or $modulePath")
        val text = file.readText()
        assertWithMessage("${file.path} must keep org.yaml.snakeyaml class names; " +
            "repackaging them into the root package makes TypeDescription's " +
            "static initializer NPE and kills GeckoRuntime.create")
            .that(text).contains("-keep class org.yaml.snakeyaml.**")
    }
}
