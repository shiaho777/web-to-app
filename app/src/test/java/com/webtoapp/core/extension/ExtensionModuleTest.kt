package com.webtoapp.core.extension

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ExtensionModuleTest {

    @Test
    fun `matchesUrl returns true when no rules configured`() {
        val module = ExtensionModule(
            name = "No Rules Module",
            code = "console.log('ok')"
        )

        assertThat(module.matchesUrl("https://example.com")).isTrue()
    }

    @Test
    fun `matchesUrl applies include and exclude rules`() {
        val module = ExtensionModule(
            name = "Url Rule Module",
            code = "console.log('ok')",
            urlMatches = listOf(
                UrlMatchRule(pattern = "*example.com*"),
                UrlMatchRule(pattern = "https://admin.example.com/*", exclude = true)
            )
        )

        assertThat(module.matchesUrl("https://shop.example.com/product")).isTrue()
        assertThat(module.matchesUrl("https://admin.example.com/panel")).isFalse()
        assertThat(module.matchesUrl("https://other.com")).isFalse()
    }

    @Test
    fun `invalid regex rule fails closed without crashing`() {
        val module = ExtensionModule(
            name = "Regex Module",
            code = "console.log('ok')",
            urlMatches = listOf(
                UrlMatchRule(pattern = "[invalid-regex", isRegex = true)
            )
        )

        assertThat(module.matchesUrl("https://example.com")).isFalse()
    }

    @Test
    fun `validate detects missing required fields`() {
        val module = ExtensionModule(
            name = "",
            code = "",
            cssCode = "",
            configItems = listOf(
                ModuleConfigItem(
                    key = "token",
                    name = "Token",
                    required = true
                )
            ),
            configValues = emptyMap()
        )

        val errors = module.validate()

        assertThat(errors).contains("模块名称不能为空")
        assertThat(errors).contains("代码内容不能为空")
        assertThat(errors).contains("配置项 'Token' 为必填项")
    }

    @Test
    fun `generateExecutableCode embeds module metadata and config`() {
        val module = ExtensionModule(
            id = "module-1",
            name = "Injector",
            icon = "🚀",
            code = "window.__flag = getConfig('flag', 'off')",
            cssCode = "body { background: #000; }",
            configValues = mapOf("flag" to "on"),
            uiConfig = ModuleUiConfig(type = ModuleUiType.FLOATING_BUTTON)
        )

        val executable = module.generateExecutableCode()

        assertThat(executable).contains("const __MODULE_CONFIG__")
        assertThat(executable).contains("\"flag\":\"on\"")
        assertThat(executable).contains("id: 'module-1'")
        assertThat(executable).contains("name: 'Injector'")
        assertThat(executable).contains("ext-module-module-1")
        assertThat(executable).contains("window.__flag = getConfig('flag', 'off')")
    }

    @Test
    fun `generateExecutableCode auto-registration carries url match state`() {
        val module = ExtensionModule(
            id = "module-2",
            name = "Reddit tweak",
            icon = "🧰",
            code = "console.log('hi')",
            urlMatches = listOf(
                UrlMatchRule(pattern = "https://*.reddit.com/*", isRegex = false, exclude = false),
                UrlMatchRule(pattern = "*://*.reddit.com/r/nsfw*", isRegex = false, exclude = true)
            )
        )

        val executable = module.generateExecutableCode()

        // The panel decides Active/Inactive from the module's url match state;
        // without it every module registered through the auto-register path was
        // shown as "Inactive / Does not match current page".
        assertThat(executable).contains("const __MODULE_URL_MATCHES__")
        assertThat(executable).contains("https://*.reddit.com/*")
        assertThat(executable).contains("\"exclude\":true")
        assertThat(executable).contains("function __moduleMatchesUrl__")
        assertThat(executable).contains("active: __moduleMatchesUrl__(),")
        assertThat(executable).contains("urlMatches: __MODULE_URL_MATCHES__,")
        // The JS matcher must escape regex metacharacters and anchor the pattern.
        assertThat(executable).contains("__escapeReChar__")
        assertThat(executable).contains("re += '${'$'}';")
    }

    @Test
    fun `sanitized coerces Gson-null fields back to defaults`() {
        // Gson allocates via Unsafe and leaves Kotlin non-null fields null when the JSON
        // omits them (Kotlin defaults are bypassed). sanitized() must restore the declared
        // defaults so consumers never observe null in a non-null field (regression: the
        // ModuleCard NullPointerException when rendering such a module).
        val module = com.google.gson.Gson().fromJson("{}", ExtensionModule::class.java)

        // Precondition: plain Gson left the non-null object fields as null (the bug condition).
        assertThat(module.name as String?).isNull()
        assertThat(module.description as String?).isNull()
        assertThat(module.storeIconPath as String?).isNull()

        val sanitized = module.sanitized()

        assertThat(sanitized.id).isNotEmpty()
        assertThat(sanitized.name).isEqualTo("")
        assertThat(sanitized.description).isEqualTo("")
        assertThat(sanitized.icon).isEqualTo("package")
        assertThat(sanitized.storeIconPath).isEqualTo("")
        assertThat(sanitized.world).isEqualTo("ISOLATED")
        assertThat(sanitized.category).isEqualTo(ModuleCategory.OTHER)
        assertThat(sanitized.tags).isEmpty()
        assertThat(sanitized.storeTags).isEmpty()
        assertThat(sanitized.version).isNotNull()
    }
}
