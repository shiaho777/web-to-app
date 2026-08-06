package com.webtoapp.core.extension

import com.webtoapp.core.logging.AppLogger
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

object DeclarativeNetRequestEngine {

    private const val TAG = "DNREngine"

    enum class ActionType(val wireName: String) {
        BLOCK("block"),
        ALLOW("allow"),
        REDIRECT("redirect"),
        MODIFY_HEADERS("modifyHeaders"),
        UPGRADE_SCHEME("upgradeScheme"),
        ALLOW_ALL_REQUESTS("allowAllRequests")
    }

    enum class ResourceType {
        MAIN_FRAME, SUB_FRAME, STYLESHEET, SCRIPT, IMAGE, FONT,
        OBJECT, XMLHTTPREQUEST, PING, CSP_REPORT, MEDIA, WEBSOCKET, OTHER;

        companion object {
            fun fromString(s: String): ResourceType? = try {
                valueOf(s.uppercase().replace("-", "_"))
            } catch (_: Exception) {
                null
            }
        }
    }

    data class DnrRule(
        val id: Int,
        val priority: Int,
        val action: ActionType,
        val redirectUrl: String?,
        val urlFilter: Pattern?,
        val regexFilter: Pattern?,
        val resourceTypes: Set<ResourceType>,
        val excludedResourceTypes: Set<ResourceType>,
        val domains: Set<String>,
        val excludedDomains: Set<String>,
        val requestDomains: Set<String> = emptySet(),
        val requestMethods: Set<String>,
        val excludedRequestMethods: Set<String>,
        val requestHeaders: List<HeaderOp> = emptyList(),
        val responseHeaders: List<HeaderOp> = emptyList()
    )

    data class HeaderOp(
        val header: String,
        val operation: String,
        val value: String?
    )

    data class HeaderModifications(
        val requestHeaders: List<HeaderOp>,
        val responseHeaders: List<HeaderOp>
    )

    data class StaticRuleset(
        val rulesetId: String,
        val path: String,
        val enabled: Boolean,
        val rules: List<DnrRule>
    )

    private val staticRulesets = ConcurrentHashMap<String, MutableMap<String, StaticRuleset>>()
    private val dynamicRules = ConcurrentHashMap<String, MutableList<DnrRule>>()
    private val sessionRules = ConcurrentHashMap<String, MutableList<DnrRule>>()

    /**
     * Immutable candidate index over all currently active rules, keyed by
     * hostname buckets. Rebuilt whenever any ruleset changes; read lock-free
     * (volatile snapshot) from the WebView request thread.
     */
    private data class RuleIndex(
        val domainBuckets: Map<String, List<DnrRule>>,
        val genericRules: List<DnrRule>
    )

    @Volatile
    private var ruleIndex: RuleIndex = RuleIndex(emptyMap(), emptyList())

    @Volatile
    var matchedCount: Long = 0
        private set

    @Volatile
    private var modifyHeaderRuleCount: Int = 0

    fun hasModifyHeaderRules(): Boolean = modifyHeaderRuleCount > 0

    private fun recomputeModifyHeaderFlag() {
        var count = 0
        for ((_, rules) in sessionRules) count += rules.count { it.action == ActionType.MODIFY_HEADERS }
        for ((_, rules) in dynamicRules) count += rules.count { it.action == ActionType.MODIFY_HEADERS }
        for ((_, rulesets) in staticRulesets) {
            rulesets.values.filter { it.enabled }.forEach { ruleset ->
                count += ruleset.rules.count { it.action == ActionType.MODIFY_HEADERS }
            }
        }
        modifyHeaderRuleCount = count
        rebuildIndex()
    }

    /** Rebuilds the immutable domain candidate index from all active rules. */
    private fun rebuildIndex() {
        val buckets = HashMap<String, MutableList<DnrRule>>()
        val generic = mutableListOf<DnrRule>()

        fun addRule(rule: DnrRule) {
            val keys = indexKeysFor(rule)
            if (keys.isEmpty()) {
                generic.add(rule)
                return
            }
            for (key in keys) {
                buckets.getOrPut(key) { mutableListOf() }.add(rule)
            }
        }

        for ((_, rules) in sessionRules) rules.forEach(::addRule)
        for ((_, rules) in dynamicRules) rules.forEach(::addRule)
        for ((_, rulesets) in staticRulesets) {
            rulesets.values.filter { it.enabled }.forEach { rs -> rs.rules.forEach(::addRule) }
        }

        ruleIndex = RuleIndex(
            domainBuckets = buckets.mapValues { it.value.toList() },
            genericRules = generic.toList()
        )
    }

    /**
     * Extracts the hostname bucket keys a rule should be indexed under.
     * `requestDomains` matches the request URL host (or subdomains), so each
     * entry becomes a key. Otherwise a stable host is extracted from the
     * urlFilter; rules that cannot be reliably keyed go to the generic pool.
     */
    private fun indexKeysFor(rule: DnrRule): Set<String> {
        if (rule.requestDomains.isNotEmpty()) {
            return rule.requestDomains
        }
        val filter = rule.urlFilter ?: return emptySet()
        return extractHostFromFilter(filter.pattern())?.let { setOf(it) } ?: emptySet()
    }

    private fun extractHostFromFilter(filter: String): String? {
        val cleaned = filter
            .replace("||", "")
            .replace("|", "")
            .replace("*", "")
            .replace("^", "")
            .replace("http://", "")
            .replace("https://", "")
            .replace("ws://", "")
            .replace("wss://", "")
            .trimStart('/')
            .substringBefore("/")
            .substringBefore("?")
            .substringBefore("#")
            .trim()
        if (cleaned.isEmpty()) return null
        if (!cleaned.contains(".")) return null
        if (cleaned.any { !it.isLetterOrDigit() && it != '.' && it != '-' }) return null
        return cleaned
    }

    /** Returns [host] and each parent domain, e.g. "a.b.example.com" → [a.b.example.com, b.example.com, example.com, com]. */
    private fun parentDomains(host: String): List<String> {
        if (host.isEmpty()) return emptyList()
        val parts = host.split(".")
        if (parts.size <= 1) return listOf(host)
        return (0 until parts.size).map { i -> parts.drop(i).joinToString(".") }
    }

    private fun matchesRequestDomain(requestHost: String, domains: Set<String>): Boolean {
        if (domains.isEmpty()) return true
        if (requestHost.isEmpty()) return false
        return domains.any { d -> requestHost == d || requestHost.endsWith(".$d") }
    }

    /** Candidate rules for a request: generic pool + hostname-bucket hits (host + parent domains). */
    private fun buildCandidates(index: RuleIndex, requestHost: String): List<DnrRule> {
        if (index.genericRules.isEmpty() && index.domainBuckets.isEmpty()) return emptyList()
        val buckets = index.domainBuckets
        val matched = ArrayList<DnrRule>(index.genericRules.size + 16)
        if (index.genericRules.isNotEmpty()) {
            matched.addAll(index.genericRules)
        }
        if (requestHost.isNotEmpty() && buckets.isNotEmpty()) {
            for (domain in parentDomains(requestHost)) {
                buckets[domain]?.let { matched.addAll(it) }
            }
        }
        return matched
    }

    private fun extractHost(url: String): String {
        return try {
            android.net.Uri.parse(url).host ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    fun loadStaticRules(
        extensionId: String,
        rulesetId: String,
        path: String,
        rulesJson: String,
        enabled: Boolean = true
    ) {
        try {
            val rules = parseRules(rulesJson)
            val rulesets = staticRulesets.getOrPut(extensionId) { mutableMapOf() }
            val safeRulesetId = rulesetId.ifBlank { path.ifBlank { "ruleset_${rulesets.size}" } }
            rulesets[safeRulesetId] = StaticRuleset(
                rulesetId = safeRulesetId,
                path = path,
                enabled = enabled,
                rules = rules
            )
            AppLogger.i(
                TAG,
                "Loaded ${rules.size} static DNR rules for $extensionId ruleset=$safeRulesetId enabled=$enabled"
            )
            recomputeModifyHeaderFlag()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to load static DNR rules for $extensionId ruleset=$rulesetId", e)
        }
    }

    fun updateEnabledStaticRulesets(
        extensionId: String,
        enableRulesetIdsJson: String,
        disableRulesetIdsJson: String
    ) {
        try {
            val rulesets = staticRulesets[extensionId] ?: return
            val enableIds = parseStringArray(enableRulesetIdsJson).toSet()
            val disableIds = parseStringArray(disableRulesetIdsJson).toSet()
            if (enableIds.isEmpty() && disableIds.isEmpty()) return

            val updated = rulesets.mapValues { (rulesetId, ruleset) ->
                when {
                    rulesetId in enableIds -> ruleset.copy(enabled = true)
                    rulesetId in disableIds -> ruleset.copy(enabled = false)
                    else -> ruleset
                }
            }
            staticRulesets[extensionId] = updated.toMutableMap()
            AppLogger.d(
                TAG,
                "Updated static rulesets for $extensionId: enable=${enableIds.size}, disable=${disableIds.size}"
            )
            recomputeModifyHeaderFlag()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to update static rulesets for $extensionId", e)
        }
    }

    fun getEnabledStaticRulesetIdsJson(extensionId: String): String {
        val ids = staticRulesets[extensionId]
            ?.values
            ?.filter { it.enabled }
            ?.map { it.rulesetId }
            .orEmpty()
        return ids.joinToString(prefix = "[", postfix = "]") { quoteJsonString(it) }
    }

    fun getAvailableStaticRuleCount(extensionId: String): Int {
        return staticRulesets[extensionId]?.values?.sumOf { it.rules.size } ?: 0
    }

    fun updateDynamicRules(
        extensionId: String,
        addRulesJson: String,
        removeRuleIdsJson: String
    ) {
        try {
            val removeIds = parseIntArray(removeRuleIdsJson).toSet()
            val addRules = parseRules(addRulesJson)

            val existing = dynamicRules.getOrPut(extensionId) { mutableListOf() }
            if (removeIds.isNotEmpty()) {
                existing.removeAll { it.id in removeIds }
            }
            if (addRules.isNotEmpty()) {
                val addIds = addRules.map { it.id }.toSet()
                existing.removeAll { it.id in addIds }
                existing.addAll(addRules)
            }
            AppLogger.d(
                TAG,
                "Updated dynamic DNR rules for $extensionId: removed=${removeIds.size}, added=${addRules.size}, total=${existing.size}"
            )
            recomputeModifyHeaderFlag()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to update dynamic DNR rules for $extensionId", e)
        }
    }

    fun updateSessionRules(
        extensionId: String,
        addRulesJson: String,
        removeRuleIdsJson: String
    ) {
        try {
            val removeIds = parseIntArray(removeRuleIdsJson).toSet()
            val addRules = parseRules(addRulesJson)

            val existing = sessionRules.getOrPut(extensionId) { mutableListOf() }
            if (removeIds.isNotEmpty()) {
                existing.removeAll { it.id in removeIds }
            }
            if (addRules.isNotEmpty()) {
                val addIds = addRules.map { it.id }.toSet()
                existing.removeAll { it.id in addIds }
                existing.addAll(addRules)
            }
            AppLogger.d(
                TAG,
                "Updated session DNR rules for $extensionId: removed=${removeIds.size}, added=${addRules.size}, total=${existing.size}"
            )
            recomputeModifyHeaderFlag()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to update session DNR rules for $extensionId", e)
        }
    }

    fun getDynamicRulesJson(extensionId: String): String {
        return rulesToJson(dynamicRules[extensionId].orEmpty())
    }

    fun getSessionRulesJson(extensionId: String): String {
        return rulesToJson(sessionRules[extensionId].orEmpty())
    }

    fun evaluate(
        url: String,
        resourceType: String = "",
        initiatorDomain: String = "",
        method: String = "GET"
    ): EvalResult? {
        val resType = ResourceType.fromString(resourceType)
        val requestHost = extractHost(url)
        val index = ruleIndex
        val candidates = buildCandidates(index, requestHost)
        if (candidates.isEmpty()) return null

        var bestMatch: Pair<DnrRule, Int>? = null

        for (rule in candidates) {
            if (!matchesRule(rule, url, resType, initiatorDomain, method, requestHost)) continue
            val effectivePriority = rule.priority
            if (bestMatch == null || effectivePriority > bestMatch.second) {
                bestMatch = rule to effectivePriority
            }
        }

        val matchedRule = bestMatch?.first ?: return null
        matchedCount++

        return when (matchedRule.action) {
            ActionType.BLOCK -> EvalResult(ActionType.BLOCK, null)
            ActionType.ALLOW, ActionType.ALLOW_ALL_REQUESTS -> EvalResult(ActionType.ALLOW, null)
            ActionType.REDIRECT -> EvalResult(ActionType.REDIRECT, matchedRule.redirectUrl)
            ActionType.UPGRADE_SCHEME -> {
                val upgraded = url.replaceFirst("http://", "https://")
                EvalResult(ActionType.REDIRECT, upgraded)
            }
            ActionType.MODIFY_HEADERS -> null
        }
    }

    data class EvalResult(
        val action: ActionType,
        val redirectUrl: String?
    )

    fun collectHeaderModifications(
        url: String,
        resourceType: String = "",
        initiatorDomain: String = "",
        method: String = "GET"
    ): HeaderModifications? {
        if (modifyHeaderRuleCount == 0) return null

        val resType = ResourceType.fromString(resourceType)
        val requestHost = extractHost(url)
        val index = ruleIndex
        val candidates = buildCandidates(index, requestHost)

        var maxAllowPriority = 0
        val matchedHeaderRules = mutableListOf<DnrRule>()
        for (rule in candidates) {
            if (!matchesRule(rule, url, resType, initiatorDomain, method, requestHost)) continue
            when (rule.action) {
                ActionType.ALLOW, ActionType.ALLOW_ALL_REQUESTS ->
                    if (rule.priority > maxAllowPriority) maxAllowPriority = rule.priority
                ActionType.MODIFY_HEADERS -> matchedHeaderRules.add(rule)
                else -> {}
            }
        }

        val effective = matchedHeaderRules
            .filter { it.priority > maxAllowPriority }
            .sortedByDescending { it.priority }
        if (effective.isEmpty()) return null

        val requestOps = effective.flatMap { it.requestHeaders }
        val responseOps = effective.flatMap { it.responseHeaders }
        if (requestOps.isEmpty() && responseOps.isEmpty()) return null

        matchedCount++
        return HeaderModifications(requestOps, responseOps)
    }

    fun clearExtension(extensionId: String) {
        staticRulesets.remove(extensionId)
        dynamicRules.remove(extensionId)
        sessionRules.remove(extensionId)
        recomputeModifyHeaderFlag()
    }

    fun clear() {
        staticRulesets.clear()
        dynamicRules.clear()
        sessionRules.clear()
        matchedCount = 0
        modifyHeaderRuleCount = 0
    }

    private fun matchesRule(
        rule: DnrRule,
        url: String,
        resType: ResourceType?,
        initiatorDomain: String,
        method: String,
        requestHost: String
    ): Boolean {
        val urlMatched = when {
            rule.urlFilter != null -> rule.urlFilter.matcher(url).find()
            rule.regexFilter != null -> rule.regexFilter.matcher(url).find()
            else -> true
        }
        if (!urlMatched) return false

        if (resType != null) {
            if (rule.resourceTypes.isNotEmpty() && resType !in rule.resourceTypes) return false
            if (resType in rule.excludedResourceTypes) return false
        }

        // requestDomains constrains the *request URL's* host (uBO filter lists rely on it).
        if (rule.requestDomains.isNotEmpty() && !matchesRequestDomain(requestHost, rule.requestDomains)) return false

        if (initiatorDomain.isNotEmpty()) {
            if (rule.domains.isNotEmpty() && !matchesDomain(initiatorDomain, rule.domains)) return false
            if (matchesDomain(initiatorDomain, rule.excludedDomains)) return false
        }

        val upperMethod = method.uppercase()
        if (rule.requestMethods.isNotEmpty() && upperMethod !in rule.requestMethods) return false
        if (upperMethod in rule.excludedRequestMethods) return false

        return true
    }

    private fun matchesDomain(domain: String, domainSet: Set<String>): Boolean {
        if (domainSet.isEmpty()) return false
        return domainSet.any { d -> domain == d || domain.endsWith(".$d") }
    }

    private fun parseRules(json: String): List<DnrRule> {
        val trimmed = json.trim()
        if (trimmed.isEmpty() || trimmed == "[]") return emptyList()

        val arr = JSONArray(trimmed)
        val rules = mutableListOf<DnrRule>()
        for (i in 0 until arr.length()) {
            try {
                val obj = arr.getJSONObject(i)
                parseRule(obj)?.let { rules.add(it) }
            } catch (e: Exception) {
                AppLogger.w(TAG, "Failed to parse DNR rule at index $i", e)
            }
        }
        return rules
    }

    private fun parseRule(obj: JSONObject): DnrRule? {
        val id = obj.optInt("id", -1)
        if (id < 0) return null

        val priority = obj.optInt("priority", 1)

        val actionObj = obj.optJSONObject("action") ?: return null
        val actionType = when (actionObj.optString("type", "")) {
            "block" -> ActionType.BLOCK
            "allow" -> ActionType.ALLOW
            "redirect" -> ActionType.REDIRECT
            "modifyHeaders" -> ActionType.MODIFY_HEADERS
            "upgradeScheme" -> ActionType.UPGRADE_SCHEME
            "allowAllRequests" -> ActionType.ALLOW_ALL_REQUESTS
            else -> return null
        }

        val redirectUrl = actionObj.optJSONObject("redirect")?.optString("url")
            ?: actionObj.optJSONObject("redirect")?.optString("extensionPath")

        val condition = obj.optJSONObject("condition")
        val urlFilter = condition?.optString("urlFilter", "")?.takeIf { it.isNotEmpty() }?.let {
            compileUrlFilter(it)
        }
        val regexFilter = condition?.optString("regexFilter", "")?.takeIf { it.isNotEmpty() }?.let {
            try {
                Pattern.compile(it, Pattern.CASE_INSENSITIVE)
            } catch (_: Exception) {
                null
            }
        }

        val resourceTypes = parseResourceTypes(condition?.optJSONArray("resourceTypes"))
        val excludedResourceTypes = parseResourceTypes(condition?.optJSONArray("excludedResourceTypes"))
        val domains = parseStringSet(condition?.optJSONArray("initiatorDomains")
            ?: condition?.optJSONArray("domains"))
        val excludedDomains = parseStringSet(condition?.optJSONArray("excludedInitiatorDomains")
            ?: condition?.optJSONArray("excludedDomains"))
        // requestDomains matches the *request URL's* host (uBO filter lists rely on this).
        val requestDomains = parseStringSet(condition?.optJSONArray("requestDomains"))
        val requestMethods = parseStringSet(condition?.optJSONArray("requestMethods")).map { it.uppercase() }.toSet()
        val excludedRequestMethods = parseStringSet(condition?.optJSONArray("excludedRequestMethods")).map { it.uppercase() }.toSet()

        val requestHeaderOps = if (actionType == ActionType.MODIFY_HEADERS)
            parseHeaderOps(actionObj.optJSONArray("requestHeaders")) else emptyList()
        val responseHeaderOps = if (actionType == ActionType.MODIFY_HEADERS)
            parseHeaderOps(actionObj.optJSONArray("responseHeaders")) else emptyList()

        return DnrRule(
            id = id,
            priority = priority,
            action = actionType,
            redirectUrl = redirectUrl,
            urlFilter = urlFilter,
            regexFilter = regexFilter,
            resourceTypes = resourceTypes,
            excludedResourceTypes = excludedResourceTypes,
            domains = domains,
            excludedDomains = excludedDomains,
            requestDomains = requestDomains,
            requestMethods = requestMethods,
            excludedRequestMethods = excludedRequestMethods,
            requestHeaders = requestHeaderOps,
            responseHeaders = responseHeaderOps
        )
    }

    private fun parseHeaderOps(arr: JSONArray?): List<HeaderOp> {
        if (arr == null || arr.length() == 0) return emptyList()
        val ops = mutableListOf<HeaderOp>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val header = obj.optString("header", "").trim()
            val operation = obj.optString("operation", "").trim().lowercase()
            if (header.isEmpty() || operation.isEmpty()) continue
            val value = obj.optString("value", "").takeIf { it.isNotEmpty() }
            ops.add(HeaderOp(header, operation, value))
        }
        return ops
    }

    private fun compileUrlFilter(filter: String): Pattern? {
        return try {
            val sb = StringBuilder()
            var i = 0
            val len = filter.length

            if (filter.startsWith("||")) {
                sb.append("^https?://([^/]*\\.)?")
                i = 2
            } else if (filter.startsWith("|")) {
                sb.append("^")
                i = 1
            }

            while (i < len) {
                val c = filter[i]
                when {
                    c == '*' -> sb.append(".*")
                    c == '^' -> sb.append("[^\\w\\-.~%]|$")
                    c == '|' && i == len - 1 -> sb.append("$")
                    c in ".+?{}()[]\\$" -> sb.append("\\").append(c)
                    else -> sb.append(c)
                }
                i++
            }

            Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE)
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to compile DNR urlFilter: $filter", e)
            null
        }
    }

    private fun parseResourceTypes(arr: JSONArray?): Set<ResourceType> {
        if (arr == null || arr.length() == 0) return emptySet()
        return (0 until arr.length()).mapNotNull { i ->
            ResourceType.fromString(arr.optString(i, ""))
        }.toSet()
    }

    private fun parseStringSet(arr: JSONArray?): Set<String> {
        if (arr == null || arr.length() == 0) return emptySet()
        return (0 until arr.length()).mapNotNull { i ->
            arr.optString(i, "").takeIf { it.isNotEmpty() }
        }.toSet()
    }

    private fun parseIntArray(json: String): List<Int> {
        val trimmed = json.trim()
        if (trimmed.isEmpty() || trimmed == "[]") return emptyList()
        return try {
            val arr = JSONArray(trimmed)
            (0 until arr.length()).map { arr.getInt(it) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseStringArray(json: String): List<String> {
        val trimmed = json.trim()
        if (trimmed.isEmpty() || trimmed == "[]") return emptyList()
        return try {
            val arr = JSONArray(trimmed)
            (0 until arr.length()).mapNotNull { index ->
                arr.optString(index, "").takeIf { it.isNotBlank() }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun rulesToJson(rules: List<DnrRule>): String {
        return rules.joinToString(prefix = "[", postfix = "]") { rule ->
            ruleToJson(rule).toString()
        }
    }

    private fun quoteJsonString(value: String): String {
        return buildString {
            append('"')
            value.forEach { ch ->
                when (ch) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\b' -> append("\\b")
                    '\u000C' -> append("\\f")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> {
                        if (ch.code < 0x20) {
                            append("\\u")
                            append(ch.code.toString(16).padStart(4, '0'))
                        } else {
                            append(ch)
                        }
                    }
                }
            }
            append('"')
        }
    }

    private fun ruleToJson(rule: DnrRule): JSONObject {
        return JSONObject().apply {
            put("id", rule.id)
            put("priority", rule.priority)
            put("action", JSONObject().apply {
                put("type", rule.action.wireName)
                if (rule.redirectUrl != null) {
                    put("redirect", JSONObject().put("url", rule.redirectUrl))
                }
            })
        }
    }
}
