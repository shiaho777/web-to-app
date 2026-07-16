package com.webtoapp.core.feature

import android.app.Application
import android.content.Context
import java.io.File
import java.io.InputStream

interface FeatureModule {
    val id: String
    val version: Int get() = 1
    fun install(context: FeatureContext)
    fun uninstall(context: FeatureContext) {}
}

class FeatureContext(
    val app: Application,
    val registry: FeatureRegistry,
    val config: FeatureConfigView,
    val files: FeatureFileAccess
)

interface FeatureConfigView {
    fun appType(): String
    fun engineType(): String
    fun rawJson(): String?
    fun <T> get(key: String, clazz: Class<T>): T?
}

interface FeatureFileAccess {
    fun featureRoot(featureId: String): File
    fun openAsset(path: String): InputStream
}

class FeatureRegistry {
    private val singles = LinkedHashMap<Class<*>, Any>()
    private val multi = LinkedHashMap<Class<*>, MutableList<Any>>()

    @Synchronized
    fun <T : Any> register(type: Class<T>, impl: T) {
        singles[type] = impl
        val list = multi.getOrPut(type) { mutableListOf() }
        if (impl !in list) list.add(impl)
    }

    @Synchronized
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(type: Class<T>): T? = singles[type] as? T

    @Synchronized
    fun <T : Any> require(type: Class<T>): T =
        get(type) ?: error("Feature SPI missing: ${type.name}")

    @Synchronized
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> all(type: Class<T>): List<T> =
        (multi[type] ?: emptyList()).map { it as T }
}

interface BrowserEngineProvider {
    val engineType: String
    fun create(context: Context): Any
}

interface ServerRuntimeProvider {
    val appTypes: Set<String>
    suspend fun start(context: Context, config: FeatureConfigView): ServerHandle
}

class ServerHandle(
    val localBaseUrl: String,
    val stop: () -> Unit
)

interface PushChannelProvider {
    val channelType: String
    fun start(context: Context, config: FeatureConfigView)
    fun stop(context: Context) {}
}

interface NetworkHardeningProvider {
    fun apply(context: Context, config: FeatureConfigView)
}

interface ExtensionRuntimeProvider {
    fun installModules(context: Context, config: FeatureConfigView)
}

interface ShellAddon {
    fun onShellReady(context: FeatureContext)
}
