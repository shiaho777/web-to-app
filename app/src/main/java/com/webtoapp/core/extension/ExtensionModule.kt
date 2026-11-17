package com.webtoapp.core.extension

import com.google.gson.annotations.SerializedName
import com.webtoapp.core.i18n.Strings
import com.webtoapp.util.GsonProvider
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.text.RegexOption

private const val REGEX_TIMEOUT_MS = 200L

private val regexExecutor by lazy {
    Executors.newSingleThreadExecutor { r ->
        Thread(r, "SafeRegexMatcher").apply { isDaemon = true }
    }
}

private val regexCache = object : LinkedHashMap<String, Regex>(32, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Regex>?) = size > 64
}

private fun safeRegexMatch(pattern: String, input: String): Boolean {
    return try {
        val compiledRegex = synchronized(regexCache) {
            regexCache.getOrPut(pattern) { Regex(pattern) }
        }
        val future = regexExecutor.submit<Boolean> { compiledRegex.containsMatchIn(input) }
        future.get(REGEX_TIMEOUT_MS, TimeUnit.MILLISECONDS)
    } catch (e: TimeoutException) {
        false
    } catch (e: Exception) {
        false
    }
}

/**
 * Cache of compiled glob→regex translations for [ExtensionModule.matchRule]. URL
 * matching runs per page load per injection phase per rule, so compiling a fresh
 * Regex each time is measurable jank; reuses [regexCache] so the compiled pattern
 * from [safeRegexMatch] and glob translation live in one bounded LRU.
 */
private fun cachedRegex(pattern: String, ignoreCase: Boolean): Regex? {
    val key = if (ignoreCase) "i:$pattern" else "c:$pattern"
    return synchronized(regexCache) {
        regexCache.getOrPut(key) {
            try {
                Regex(pattern, if (ignoreCase) setOf(RegexOption.IGNORE_CASE) else emptySet())
            } catch (e: Exception) {
                null
            } ?: return@synchronized null
        }
    }
}

enum class ModuleCategory(val icon: String) {
    CONTENT_FILTER("block"),
    CONTENT_ENHANCE("auto_awesome"),
    STYLE_MODIFIER("palette"),
    THEME("rainbow"),
    FUNCTION_ENHANCE("bolt"),
    AUTOMATION("smart_toy"),
    NAVIGATION("explore"),
    DATA_EXTRACT("analytics"),
    DATA_SAVE("save"),
    INTERACTION("mouse"),
    ACCESSIBILITY("accessibility"),
    MEDIA("movie"),
    VIDEO("videocam"),
    IMAGE("image"),
    AUDIO("music_note"),
    SECURITY("lock"),
    ANTI_TRACKING("person_search"),
    SOCIAL("chat"),
    SHOPPING("shopping_cart"),
    READING("book"),
    TRANSLATE("globe"),
    DEVELOPER("wrench"),
    OTHER("package");

    fun getDisplayName(): String = when (this) {
        CONTENT_FILTER -> Strings.catContentFilter
        CONTENT_ENHANCE -> Strings.catContentEnhance
        STYLE_MODIFIER -> Strings.catStyleModifier
        THEME -> Strings.catTheme
        FUNCTION_ENHANCE -> Strings.catFunctionEnhance
        AUTOMATION -> Strings.catAutomation
        NAVIGATION -> Strings.catNavigation
        DATA_EXTRACT -> Strings.catDataExtract
        DATA_SAVE -> Strings.catDataSave
        INTERACTION -> Strings.catInteraction
        ACCESSIBILITY -> Strings.catAccessibility
        MEDIA -> Strings.catMedia
        VIDEO -> Strings.catVideo
        IMAGE -> Strings.catImage
        AUDIO -> Strings.catAudio
        SECURITY -> Strings.catSecurity
        ANTI_TRACKING -> Strings.catAntiTracking
        SOCIAL -> Strings.catSocial
        SHOPPING -> Strings.catShopping
        READING -> Strings.catReading
        TRANSLATE -> Strings.catTranslate
        DEVELOPER -> Strings.catDeveloper
        OTHER -> Strings.catOther
    }

}

enum class ModuleRunTime(val jsEvent: String) {
    DOCUMENT_START(""),
    DOCUMENT_END("DOMContentLoaded"),
    DOCUMENT_IDLE("load"),
    CONTEXT_MENU("contextmenu"),
    BEFORE_UNLOAD("beforeunload");
}

enum class ModulePermission(val dangerous: Boolean = false) {
    DOM_ACCESS,
    DOM_OBSERVE,
    CSS_INJECT,
    STORAGE,
    COOKIE(true),
    INDEXED_DB(true),
    CACHE,
    NETWORK(true),
    WEBSOCKET(true),
    FETCH_INTERCEPT(true),
    CLIPBOARD(true),
    NOTIFICATION,
    ALERT,
    KEYBOARD,
    MOUSE,
    TOUCH,
    LOCATION(true),
    CAMERA(true),
    MICROPHONE(true),
    DEVICE_INFO,
    MEDIA,
    FULLSCREEN,
    PICTURE_IN_PICTURE,
    SCREEN_CAPTURE(true),
    DOWNLOAD,
    FILE_ACCESS(true),
    EVAL(true),
    IFRAME(true),
    WINDOW_OPEN,
    HISTORY,
    NAVIGATION;

}

enum class ConfigItemType {
    TEXT, TEXTAREA, NUMBER, BOOLEAN,
    SELECT, MULTI_SELECT, RADIO, CHECKBOX,
    COLOR, URL, EMAIL, PASSWORD,
    REGEX, CSS_SELECTOR, JAVASCRIPT, JSON,
    RANGE, DATE, TIME, DATETIME,
    FILE, IMAGE;

}

data class ModuleConfigItem(
    @SerializedName("key")
    val key: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String = "",
    @SerializedName("type")
    val type: ConfigItemType = ConfigItemType.TEXT,
    @SerializedName("defaultValue")
    val defaultValue: String = "",
    @SerializedName("options")
    val options: List<String> = emptyList(),
    @SerializedName("required")
    val required: Boolean = false,
    @SerializedName("placeholder")
    val placeholder: String = "",
    @SerializedName("validation")
    val validation: String? = null
)

data class ModuleAuthor(
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("url")
    val url: String? = null,
    @SerializedName("qq")
    val qq: String? = null
)

data class ModuleVersion(
    @SerializedName("code")
    val code: Int = 1,
    @SerializedName("name")
    val name: String = "1.0.0",
    @SerializedName("changelog")
    val changelog: String = ""
)

data class UrlMatchRule(
    @SerializedName("pattern")
    val pattern: String,
    @SerializedName("isRegex")
    val isRegex: Boolean = false,
    @SerializedName("exclude")
    val exclude: Boolean = false
)

enum class ModuleSourceType {
    CUSTOM,
    USERSCRIPT,
    CHROME_EXTENSION,
    GREASYFORK
}

enum class ModuleRunMode {
    INTERACTIVE,
    AUTO;

}

data class ExtensionModule(

    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String = "",
    @SerializedName("icon")
    val icon: String = "package",

    @SerializedName("category")
    val category: ModuleCategory = ModuleCategory.OTHER,
    @SerializedName("tags")
    val tags: List<String> = emptyList(),

    @SerializedName("version")
    val version: ModuleVersion = ModuleVersion(),
    @SerializedName("author")
    val author: ModuleAuthor? = null,

    @SerializedName("code")
    val code: String = "",
    @SerializedName("cssCode")
    val cssCode: String = "",
    @SerializedName("panelHtml")
    val panelHtml: String = "",

    @SerializedName("codeFiles")
    val codeFiles: Map<String, String> = emptyMap(),

    @SerializedName("runAt")
    val runAt: ModuleRunTime = ModuleRunTime.DOCUMENT_END,
    @SerializedName("urlMatches")
    val urlMatches: List<UrlMatchRule> = emptyList(),

    @SerializedName("permissions")
    val permissions: List<ModulePermission> = emptyList(),

    @SerializedName("configItems")
    val configItems: List<ModuleConfigItem> = emptyList(),
    @SerializedName("configValues")
    val configValues: Map<String, String> = emptyMap(),

    @SerializedName("dependencies")
    val dependencies: List<String> = emptyList(),

    @SerializedName("enabled")
    val enabled: Boolean = true,
    @SerializedName("builtIn")
    val builtIn: Boolean = false,

    @SerializedName("runMode")
    val runMode: ModuleRunMode = ModuleRunMode.INTERACTIVE,

    @SerializedName("sourceType")
    val sourceType: ModuleSourceType = ModuleSourceType.CUSTOM,

    @SerializedName("chromeExtId")
    val chromeExtId: String = "",
    @SerializedName("storeIconPath")
    val storeIconPath: String = "",
    @SerializedName("storeTags")
    val storeTags: List<String> = emptyList(),
    @SerializedName("world")
    val world: String = "ISOLATED",
    @SerializedName("backgroundScript")
    val backgroundScript: String = "",
    @SerializedName("popupPath")
    val popupPath: String = "",
    @SerializedName("optionsPagePath")
    val optionsPagePath: String = "",
    @SerializedName("manifestJson")
    val manifestJson: String = "",

    @SerializedName("gmGrants")
    val gmGrants: List<String> = emptyList(),
    @SerializedName("requireUrls")
    val requireUrls: List<String> = emptyList(),
    @SerializedName("resources")
    val resources: Map<String, String> = emptyMap(),
    @SerializedName("noframes")
    val noframes: Boolean = false,

    @SerializedName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),
    @SerializedName("updatedAt")
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        private val gson get() = GsonProvider.gson


        fun fromJson(json: String): ExtensionModule? {
            return try {
                gson.fromJson(json, ExtensionModule::class.java)?.sanitized()
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Gson allocates instances via Unsafe and leaves Kotlin non-null fields null when the
     * JSON omits them (Kotlin default values are bypassed). Coerce every object-typed field
     * back to its declared default so consumers never observe null in a non-null field
     * (fixes the ModuleCard NullPointerException when rendering such a module). Primitive
     * fields (enabled/builtIn/noframes/createdAt/updatedAt) cannot be null and are left as-is.
     */
    fun sanitized(): ExtensionModule = copy(
        id = (id as String?)?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString(),
        name = (name as String?) ?: "",
        description = (description as String?) ?: "",
        icon = (icon as String?) ?: "package",
        category = (category as ModuleCategory?) ?: ModuleCategory.OTHER,
        tags = (tags as List<String>?) ?: emptyList(),
        version = (version as ModuleVersion?) ?: ModuleVersion(),
        code = (code as String?) ?: "",
        cssCode = (cssCode as String?) ?: "",
        panelHtml = (panelHtml as String?) ?: "",
        codeFiles = (codeFiles as Map<String, String>?) ?: emptyMap(),
        runAt = (runAt as ModuleRunTime?) ?: ModuleRunTime.DOCUMENT_END,
        urlMatches = (urlMatches as List<UrlMatchRule>?) ?: emptyList(),
        permissions = (permissions as List<ModulePermission>?) ?: emptyList(),
        configItems = (configItems as List<ModuleConfigItem>?) ?: emptyList(),
        configValues = (configValues as Map<String, String>?) ?: emptyMap(),
        dependencies = (dependencies as List<String>?) ?: emptyList(),
        runMode = (runMode as ModuleRunMode?) ?: ModuleRunMode.INTERACTIVE,
        sourceType = (sourceType as ModuleSourceType?) ?: ModuleSourceType.CUSTOM,
        chromeExtId = (chromeExtId as String?) ?: "",
        storeIconPath = (storeIconPath as String?) ?: "",
        storeTags = (storeTags as List<String>?) ?: emptyList(),
        world = (world as String?) ?: "ISOLATED",
        backgroundScript = (backgroundScript as String?) ?: "",
        popupPath = (popupPath as String?) ?: "",
        optionsPagePath = (optionsPagePath as String?) ?: "",
        manifestJson = (manifestJson as String?) ?: "",
        gmGrants = (gmGrants as List<String>?) ?: emptyList(),
        requireUrls = (requireUrls as List<String>?) ?: emptyList(),
        resources = (resources as Map<String, String>?) ?: emptyMap(),
    )


    fun matchesUrl(url: String): Boolean {
        if (urlMatches.isEmpty()) return true

        var hasInclude = false
        for (rule in urlMatches) {
            if (rule.exclude) {
                if (matchRule(url, rule)) return false
            } else {
                hasInclude = true
            }
        }

        if (!hasInclude) return true

        for (rule in urlMatches) {
            if (!rule.exclude && matchRule(url, rule)) return true
        }
        return false
    }

    private fun matchRule(url: String, rule: UrlMatchRule): Boolean {
        if (rule.isRegex) return safeRegexMatch(rule.pattern, url)

        val pattern = rule.pattern
        if (pattern == "*" || pattern == "<all_urls>") return true

        if (pattern.contains("://")) return matchChromePattern(pattern, url)

        val regexPattern = buildString {
            append("^")
            var i = 0
            while (i < pattern.length) {
                val c = pattern[i]
                when {
                    c == '*' -> {
                        append(".*")
                        i++
                    }
                    c in ".+?^\${}()|[]\\/" -> {
                        append("\\")
                        append(c)
                        i++
                    }
                    else -> {
                        append(c)
                        i++
                    }
                }
            }
            append("$")
        }
        return try {
            val compiled = cachedRegex(regexPattern, ignoreCase = true)
                ?: return url.contains(pattern, ignoreCase = true)
            compiled.matches(url)
        } catch (e: Exception) {
            url.contains(pattern, ignoreCase = true)
        }
    }

    private fun matchChromePattern(pattern: String, url: String): Boolean {
        val p = splitMatchUrl(pattern) ?: return false
        val u = splitMatchUrl(url) ?: return false

        val schemeOk = when (p.scheme) {
            "*" -> u.scheme == "http" || u.scheme == "https"
            else -> p.scheme == u.scheme
        }
        if (!schemeOk) return false
        if (!hostComponentMatches(p.host, u.host)) return false
        return pathComponentMatches(p.path, u.path)
    }

    private data class MatchUrlParts(val scheme: String, val host: String, val path: String)

    private fun splitMatchUrl(raw: String): MatchUrlParts? {
        val sep = raw.indexOf("://")
        if (sep <= 0) return null
        val scheme = raw.substring(0, sep).lowercase()
        val rest = raw.substring(sep + 3)
        if (rest.isEmpty()) return null
        val pathStart = rest.indexOf('/')
        val authority = if (pathStart >= 0) rest.substring(0, pathStart) else rest
        val path = if (pathStart >= 0) rest.substring(pathStart) else "/"
        val host = authority.substringBefore('@').substringBefore(':').lowercase()
        if (host.isEmpty()) return null
        return MatchUrlParts(scheme, host, path)
    }

    private fun hostComponentMatches(patternHost: String, host: String): Boolean {
        if (patternHost == "*") return true
        if (patternHost == host) return true
        if (patternHost.startsWith("*.")) {
            val suffix = patternHost.substring(1)
            return host.endsWith(suffix) || host == suffix.removePrefix(".")
        }
        return false
    }

    private fun pathComponentMatches(patternPath: String, path: String): Boolean {
        if (patternPath == "/*" || patternPath == "*" || patternPath == path) return true
        if (!patternPath.contains('*')) return false
        val regex = buildString {
            append('^')
            patternPath.forEach { ch ->
                when (ch) {
                    '*' -> append(".*")
                    else -> {
                        if ("\\.[]{}()+?^$|".contains(ch)) append('\\')
                        append(ch)
                    }
                }
            }
            append('$')
        }
        return runCatching { Regex(regex).matches(path) }.getOrDefault(false)
    }
}
