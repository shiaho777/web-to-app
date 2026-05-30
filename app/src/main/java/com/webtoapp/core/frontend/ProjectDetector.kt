package com.webtoapp.core.frontend

import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import com.google.gson.JsonObject
import java.io.File

object ProjectDetector {

    private const val TAG = "ProjectDetector"
    private val gson = com.webtoapp.util.GsonProvider.gson

    private val frameworkDetectors = mapOf(
        "vue" to FrontendFramework.VUE,
        "@vue/cli" to FrontendFramework.VUE,
        "nuxt" to FrontendFramework.NUXT,
        "react" to FrontendFramework.REACT,
        "react-dom" to FrontendFramework.REACT,
        "next" to FrontendFramework.NEXT,
        "@angular/core" to FrontendFramework.ANGULAR,
        "svelte" to FrontendFramework.SVELTE
    )

    private val databaseDetectors = mapOf(
        "sqlite3" to DatabaseType.SQLITE,
        "better-sqlite3" to DatabaseType.SQLITE,
        "mysql" to DatabaseType.MYSQL,
        "mysql2" to DatabaseType.MYSQL,
        "pg" to DatabaseType.POSTGRESQL,
        "postgres" to DatabaseType.POSTGRESQL,
        "mongodb" to DatabaseType.MONGODB,
        "mongoose" to DatabaseType.MONGODB,
        "redis" to DatabaseType.REDIS,
        "ioredis" to DatabaseType.REDIS
    )

    private val categoryDetectors = mapOf(

        "element-ui" to DependencyCategory.UI_LIBRARY,
        "element-plus" to DependencyCategory.UI_LIBRARY,
        "ant-design-vue" to DependencyCategory.UI_LIBRARY,
        "antd" to DependencyCategory.UI_LIBRARY,
        "vuetify" to DependencyCategory.UI_LIBRARY,
        "@mui/material" to DependencyCategory.UI_LIBRARY,
        "tailwindcss" to DependencyCategory.UI_LIBRARY,
        "bootstrap" to DependencyCategory.UI_LIBRARY,

        "vuex" to DependencyCategory.STATE_MANAGEMENT,
        "pinia" to DependencyCategory.STATE_MANAGEMENT,
        "redux" to DependencyCategory.STATE_MANAGEMENT,
        "@reduxjs/toolkit" to DependencyCategory.STATE_MANAGEMENT,
        "mobx" to DependencyCategory.STATE_MANAGEMENT,
        "zustand" to DependencyCategory.STATE_MANAGEMENT,

        "vue-router" to DependencyCategory.ROUTER,
        "react-router" to DependencyCategory.ROUTER,
        "react-router-dom" to DependencyCategory.ROUTER,

        "axios" to DependencyCategory.HTTP_CLIENT,
        "ky" to DependencyCategory.HTTP_CLIENT,

        "vite" to DependencyCategory.BUILD_TOOL,
        "webpack" to DependencyCategory.BUILD_TOOL,
        "rollup" to DependencyCategory.BUILD_TOOL,
        "esbuild" to DependencyCategory.BUILD_TOOL,

        "jest" to DependencyCategory.TESTING,
        "vitest" to DependencyCategory.TESTING,
        "mocha" to DependencyCategory.TESTING,
        "cypress" to DependencyCategory.TESTING
    )

    fun detectProject(projectPath: String): ProjectDetectionResult {
        val projectDir = File(projectPath)
        val issues = mutableListOf<ProjectIssue>()
        val suggestions = mutableListOf<String>()

        val distDir = findDistDirectory(projectDir)
        if (distDir != null) {
            suggestions.add(Strings.frontendDetectFoundDistDir.format(distDir.name))
        }

        val packageJsonFile = File(projectDir, "package.json")
        if (!packageJsonFile.exists()) {

            if (distDir != null || hasIndexHtml(projectDir)) {
                return createStaticResult(projectDir, distDir, issues, suggestions)
            }
            return createErrorResult(Strings.frontendDetectNoPackageJson, issues)
        }

        val packageJson = try {
            gson.fromJson(packageJsonFile.readText(), JsonObject::class.java)
        } catch (e: Exception) {
            AppLogger.d(TAG, "Failed to parse package.json", e)
            return createErrorResult(Strings.frontendDetectInvalidPackageJson.format(e.message ?: ""), issues)
        }

        val dependencies = parseDependencies(packageJson, "dependencies", false)
        val devDependencies = parseDependencies(packageJson, "devDependencies", true)
        val allDeps = dependencies + devDependencies

        val framework = detectFramework(allDeps, projectDir)
        val frameworkVersion = getFrameworkVersion(framework, allDeps)

        val packageManager = detectPackageManager(projectDir)

        val hasTypeScript = allDeps.any { it.name == "typescript" } ||
                           File(projectDir, "tsconfig.json").exists()

        val databases = detectDatabases(allDeps)

        val scripts = parseScripts(packageJson)

        val (buildCommand, outputDir) = determineBuildConfig(framework, scripts, projectDir)

        val devCommand = determineDevCommand(scripts)

        val hasDistFolder = distDir != null
        if (!hasDistFolder) {
            issues.add(ProjectIssue(
                severity = IssueSeverity.WARNING,
                type = IssueType.NO_DIST_FOLDER,
                message = Strings.frontendDetectNoDistFolder,
                suggestion = Strings.frontendDetectNoDistSuggestion
            ))
        }

        val runtimeRequirement = detectRuntimeRequirement(projectDir, framework, allDeps, scripts)

        generateSuggestions(framework, allDeps, databases, hasDistFolder, runtimeRequirement, suggestions)

        return ProjectDetectionResult(
            framework = framework,
            frameworkVersion = frameworkVersion,
            packageManager = packageManager,
            hasTypeScript = hasTypeScript,
            databases = databases,
            dependencies = dependencies,
            devDependencies = devDependencies,
            scripts = scripts,
            buildCommand = buildCommand,
            devCommand = devCommand,
            outputDir = distDir?.absolutePath ?: outputDir,
            issues = issues,
            suggestions = suggestions,
            runtimeRequirement = runtimeRequirement
        )
    }

    private fun findDistDirectory(projectDir: File): File? {
        val possibleDirs = listOf("dist", "build", "out", ".output/public", "public")
        for (dirName in possibleDirs) {
            val dir = File(projectDir, dirName)
            if (dir.exists() && dir.isDirectory && hasIndexHtml(dir)) {
                return dir
            }
        }
        return null
    }

    private fun hasIndexHtml(dir: File): Boolean {
        return File(dir, "index.html").exists()
    }

    private fun createStaticResult(
        projectDir: File,
        distDir: File?,
        issues: MutableList<ProjectIssue>,
        suggestions: MutableList<String>
    ): ProjectDetectionResult {
        val outputDir = distDir ?: projectDir

        if (!hasIndexHtml(outputDir)) {
            issues.add(ProjectIssue(
                severity = IssueSeverity.ERROR,
                type = IssueType.MISSING_CONFIG,
                message = Strings.frontendDetectNoIndexHtml
            ))
        }

        suggestions.add(Strings.frontendDetectStaticImport)

        return ProjectDetectionResult(
            framework = FrontendFramework.UNKNOWN,
            frameworkVersion = null,
            packageManager = PackageManager.NPM,
            hasTypeScript = false,
            databases = emptyList(),
            dependencies = emptyList(),
            devDependencies = emptyList(),
            scripts = emptyMap(),
            buildCommand = null,
            devCommand = null,
            outputDir = outputDir.absolutePath,
            issues = issues,
            suggestions = suggestions
        )
    }

    private fun parseDependencies(
        packageJson: JsonObject,
        key: String,
        isDevDependency: Boolean
    ): List<DependencyInfo> {
        val deps = packageJson.getAsJsonObject(key) ?: return emptyList()

        return deps.entrySet().map { (name, version) ->
            val category = categoryDetectors[name]
                ?: frameworkDetectors[name]?.let { DependencyCategory.FRAMEWORK }
                ?: databaseDetectors[name]?.let { DependencyCategory.DATABASE }
                ?: DependencyCategory.OTHER

            DependencyInfo(
                name = name,
                version = version.asString,
                isDevDependency = isDevDependency,
                category = category
            )
        }
    }

    private fun detectFramework(deps: List<DependencyInfo>, projectDir: File): FrontendFramework {
        val depNames = deps.map { it.name }.toSet()

        return when {
            "nuxt" in depNames || "nuxt3" in depNames -> FrontendFramework.NUXT
            "next" in depNames -> FrontendFramework.NEXT
            "@angular/core" in depNames -> FrontendFramework.ANGULAR
            "svelte" in depNames -> FrontendFramework.SVELTE
            "vue" in depNames -> {
                if ("vite" in depNames) FrontendFramework.VITE else FrontendFramework.VUE
            }
            "react" in depNames || "react-dom" in depNames -> {
                if ("vite" in depNames) FrontendFramework.VITE else FrontendFramework.REACT
            }
            "vite" in depNames -> FrontendFramework.VITE
            else -> {
                when {
                    File(projectDir, "vue.config.js").exists() -> FrontendFramework.VUE
                    File(projectDir, "vite.config.js").exists() ||
                    File(projectDir, "vite.config.ts").exists() -> FrontendFramework.VITE
                    File(projectDir, "next.config.js").exists() -> FrontendFramework.NEXT
                    File(projectDir, "nuxt.config.js").exists() ||
                    File(projectDir, "nuxt.config.ts").exists() -> FrontendFramework.NUXT
                    File(projectDir, "angular.json").exists() -> FrontendFramework.ANGULAR
                    else -> FrontendFramework.UNKNOWN
                }
            }
        }
    }

    private fun getFrameworkVersion(framework: FrontendFramework, deps: List<DependencyInfo>): String? {
        val frameworkPackage = when (framework) {
            FrontendFramework.VUE -> "vue"
            FrontendFramework.REACT -> "react"
            FrontendFramework.NEXT -> "next"
            FrontendFramework.NUXT -> "nuxt"
            FrontendFramework.ANGULAR -> "@angular/core"
            FrontendFramework.SVELTE -> "svelte"
            FrontendFramework.VITE -> "vite"
            else -> null
        }

        return frameworkPackage?.let { pkg ->
            deps.find { it.name == pkg }?.version?.removePrefix("^")?.removePrefix("~")
        }
    }

    fun detectPackageManager(projectDir: File): PackageManager {
        return when {
            File(projectDir, "pnpm-lock.yaml").exists() -> PackageManager.PNPM
            File(projectDir, "yarn.lock").exists() -> PackageManager.YARN
            File(projectDir, "bun.lockb").exists() -> PackageManager.BUN
            else -> PackageManager.NPM
        }
    }

    private fun detectDatabases(deps: List<DependencyInfo>): List<DatabaseType> {
        return deps.mapNotNull { dep ->
            databaseDetectors[dep.name]
        }.distinct()
    }

    private fun parseScripts(packageJson: JsonObject): Map<String, String> {
        val scripts = packageJson.getAsJsonObject("scripts") ?: return emptyMap()
        return scripts.entrySet().associate { it.key to it.value.asString }
    }

    private fun determineBuildConfig(
        framework: FrontendFramework,
        scripts: Map<String, String>,
        projectDir: File
    ): Pair<String?, String> {
        val buildCommand = when {
            "build" in scripts -> "build"
            "build:prod" in scripts -> "build:prod"
            "generate" in scripts -> "generate"
            else -> "build"
        }

        val outputDir = when (framework) {
            FrontendFramework.VUE -> "dist"
            FrontendFramework.REACT -> "build"
            FrontendFramework.NEXT -> "out"
            FrontendFramework.NUXT -> ".output/public"
            FrontendFramework.ANGULAR -> "dist"
            FrontendFramework.VITE -> "dist"
            else -> "dist"
        }

        return buildCommand to outputDir
    }

    private fun determineDevCommand(scripts: Map<String, String>): String? {
        return when {
            "dev" in scripts -> "dev"
            "serve" in scripts -> "serve"
            "start" in scripts -> "start"
            else -> null
        }
    }

    private fun detectRuntimeRequirement(
        projectDir: File,
        framework: FrontendFramework,
        deps: List<DependencyInfo>,
        scripts: Map<String, String>
    ): ProjectRuntimeRequirement {
        val depNames = deps.map { it.name }.toSet()

        val backendFramework = when {
            "express" in depNames -> BackendFramework.EXPRESS
            "fastify" in depNames -> BackendFramework.FASTIFY
            "koa" in depNames -> BackendFramework.KOA
            "@nestjs/core" in depNames || "@nestjs/common" in depNames -> BackendFramework.NESTJS
            "@hapi/hapi" in depNames -> BackendFramework.HAPI
            else -> BackendFramework.NONE
        }

        val serverDirs = listOf("server", "api", "backend", "src/server", "src/api")
        val hasServerDir = serverDirs.any { File(projectDir, it).exists() }

        val hasServerScript = scripts.keys.any { key ->
            key.contains("server", ignoreCase = true) ||
            key.contains("api", ignoreCase = true) ||
            key.contains("backend", ignoreCase = true)
        }

        val needsBackend = backendFramework != BackendFramework.NONE || hasServerDir || hasServerScript

        val isSSR = when (framework) {
            FrontendFramework.NEXT -> {

                val nextConfig = File(projectDir, "next.config.js")
                val canStaticExport = if (nextConfig.exists()) {
                    val content = nextConfig.readText()
                    content.contains("output: 'export'") || content.contains("output:'export'")
                } else false
                !canStaticExport
            }
            FrontendFramework.NUXT -> {

                val nuxtConfig = File(projectDir, "nuxt.config.js")
                val canStaticExport = if (nuxtConfig.exists()) {
                    val content = nuxtConfig.readText()
                    content.contains("ssr: false") || scripts.containsKey("generate")
                } else false
                !canStaticExport
            }
            else -> false
        }

        val envVarHints = mutableMapOf<String, String>()
        val envExample = File(projectDir, ".env.example")
        if (envExample.exists()) {
            envExample.readLines().forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                    val parts = trimmed.split("=", limit = 2)
                    if (parts.size >= 1) {
                        val key = parts[0].trim()

                        envVarHints[key] = Strings.frontendDetectConfigureEnvVar.format(key)
                    }
                }
            }
        }

        val backendEntryFile = serverDirs.firstNotNullOfOrNull { dir ->
            val serverIndex = File(projectDir, "$dir/index.js")
            if (serverIndex.exists()) "$dir/index.js" else null
        } ?: run {
            val serverJs = File(projectDir, "server.js")
            if (serverJs.exists()) "server.js" else null
        }

        return ProjectRuntimeRequirement(
            needsNodeRuntime = needsBackend || isSSR,
            backendFramework = backendFramework,
            backendEntryFile = backendEntryFile,
            isSSR = isSSR,
            canStaticExport = !isSSR && !needsBackend,
            envVarHints = envVarHints
        )
    }

    private fun generateSuggestions(
        framework: FrontendFramework,
        deps: List<DependencyInfo>,
        databases: List<DatabaseType>,
        hasDistFolder: Boolean,
        runtimeRequirement: ProjectRuntimeRequirement,
        suggestions: MutableList<String>
    ) {
        if (runtimeRequirement.needsNodeRuntime) {

            if (runtimeRequirement.backendFramework != BackendFramework.NONE) {
                val fwName = runtimeRequirement.backendFramework.name.lowercase()
                    .replaceFirstChar { it.uppercase() }
                suggestions.add("🚀 ${Strings.frontendSuggestBackendDetected.format(fwName)}")
            }
            if (runtimeRequirement.isSSR) {
                suggestions.add("⚠️ ${Strings.frontendSuggestSsrDetected}")
            }
            if (runtimeRequirement.backendEntryFile != null) {
                suggestions.add("📄 ${Strings.frontendSuggestBackendEntry.format(runtimeRequirement.backendEntryFile)}")
            }
        } else if (!hasDistFolder) {
            suggestions.add("💡 ${Strings.frontendSuggestRunBuild}")
            suggestions.add("💡 ${Strings.frontendSuggestSelectDist}")
        }

        if (runtimeRequirement.envVarHints.isNotEmpty()) {
            suggestions.add("🔑 ${Strings.frontendSuggestEnvVars.format(runtimeRequirement.envVarHints.size)}")
        }

        if (databases.isNotEmpty()) {
            if (runtimeRequirement.needsNodeRuntime) {
                suggestions.add("🗃️ ${Strings.frontendSuggestDbWithNode}")
            } else {
                suggestions.add("⚠️ ${Strings.frontendSuggestDbStaticOnly}")
            }
        }
    }

    private fun createErrorResult(message: String, issues: MutableList<ProjectIssue>): ProjectDetectionResult {
        issues.add(ProjectIssue(
            severity = IssueSeverity.ERROR,
            type = IssueType.MISSING_CONFIG,
            message = message
        ))

        return ProjectDetectionResult(
            framework = FrontendFramework.UNKNOWN,
            frameworkVersion = null,
            packageManager = PackageManager.NPM,
            hasTypeScript = false,
            databases = emptyList(),
            dependencies = emptyList(),
            devDependencies = emptyList(),
            scripts = emptyMap(),
            buildCommand = null,
            devCommand = null,
            outputDir = "dist",
            issues = issues,
            suggestions = emptyList()
        )
    }
}
