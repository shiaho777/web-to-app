package com.webtoapp.data.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.webtoapp.data.model.ActivationDialogConfig
import com.webtoapp.data.model.AdConfig
import com.webtoapp.data.model.Announcement
import com.webtoapp.data.model.ApkExportConfig
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.AutoStartConfig
import com.webtoapp.data.model.BgmConfig
import com.webtoapp.data.model.GalleryConfig
import com.webtoapp.data.model.HtmlConfig
import com.webtoapp.data.model.MediaConfig
import com.webtoapp.data.model.SplashConfig
import com.webtoapp.data.model.TranslateConfig
import com.webtoapp.data.model.WebViewConfig
import com.webtoapp.data.model.NodeJsConfig
import com.webtoapp.data.model.WordPressConfig
import com.webtoapp.data.model.PhpAppConfig
import com.webtoapp.data.model.PythonAppConfig
import com.webtoapp.data.model.GoAppConfig
import com.webtoapp.data.model.MultiWebConfig
import com.webtoapp.core.activation.ActivationCode

class Converters {

    companion object {

        /**
         * Enum handling for persisted JSON.
         *
         * Writing delegates to Gson, which emits the [SerializedName] value when a constant
         * declares one. Reading must therefore accept that same value: resolving by constant
         * name alone mapped `"web_api"` to no constant at all, so every load of a stored
         * [com.webtoapp.data.model.NotificationType.WEB_API] silently degraded to `NONE`.
         * Because [ApkExportConfig] round-trips through this Gson on every Room read/write,
         * the setting was lost as soon as it was saved.
         *
         * Values that match no constant still fall back to the first one, so JSON written by
         * a newer build stays loadable instead of nulling a non-null Kotlin field.
         */
        private val lenientEnumAdapterFactory = object : TypeAdapterFactory {
            override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
                val rawType = type.rawType
                val enumClass = when {
                    rawType.isEnum -> rawType
                    // Constants with a body compile to an anonymous subclass of the enum.
                    rawType.superclass?.isEnum == true -> rawType.superclass
                    else -> return null
                }

                val byExact = HashMap<String, Any>()
                val byLowercase = HashMap<String, Any>()
                enumClass.declaredFields.forEach { field ->
                    if (!field.isEnumConstant) return@forEach
                    val constant = runCatching { field.get(null) }.getOrNull() ?: return@forEach
                    val keys = mutableListOf((constant as Enum<*>).name)
                    field.getAnnotation(SerializedName::class.java)?.let { annotation ->
                        keys += annotation.value
                        keys += annotation.alternate
                    }
                    keys.forEach { key ->
                        byExact.putIfAbsent(key, constant)
                        byLowercase.putIfAbsent(key.lowercase(), constant)
                    }
                }
                val fallback = enumClass.enumConstants?.firstOrNull()
                val delegate = gson.getDelegateAdapter(this, type)

                return object : TypeAdapter<T>() {
                    override fun write(out: JsonWriter, value: T) = delegate.write(out, value)

                    override fun read(reader: JsonReader): T? {
                        if (reader.peek() == JsonToken.NULL) {
                            reader.nextNull()
                            return null
                        }
                        val raw = reader.nextString()
                        val resolved = byExact[raw] ?: byLowercase[raw.lowercase()] ?: fallback
                        @Suppress("UNCHECKED_CAST")
                        return resolved as T?
                    }
                }
            }
        }

        @PublishedApi
        internal val gson: Gson by lazy {
            GsonBuilder()
                .registerTypeAdapterFactory(lenientEnumAdapterFactory)
                .create()
        }

        fun <T> toJson(value: T?): String = gson.toJson(value)

        inline fun <reified T> fromJson(value: String): T? {
            return try {
                gson.fromJson(value, T::class.java)
            } catch (e: Exception) {
                com.webtoapp.core.logging.AppLogger.w("Converters", "JSON 反序列化失败: ${T::class.java.simpleName}, 原始数据长度=${value.length}", e)
                null
            }
        }

        @PublishedApi
        internal val defaultJsonCache = java.util.concurrent.ConcurrentHashMap<Class<*>, JsonElement>()

        inline fun <reified T> fromJsonOrDefault(value: String, default: T): T {
            return try {
                val parsed = JsonParser.parseString(value)
                val clazz = T::class.java
                val defaultJson = defaultJsonCache.getOrPut(clazz) {
                    gson.toJsonTree(default)
                }
                val merged = mergeMissingDefaults(defaultJson, parsed)
                gson.fromJson(merged, clazz) ?: default
            } catch (e: Exception) {
                default
            }
        }

        @PublishedApi
        internal fun mergeMissingDefaults(defaults: JsonElement, current: JsonElement?): JsonElement {
            if (!defaults.isJsonObject) {
                return current?.deepCopy() ?: defaults.deepCopy()
            }

            val merged = JsonObject()
            val currentObj = if (current != null && current.isJsonObject) current.asJsonObject else JsonObject()

            currentObj.entrySet().forEach { (key, value) ->
                merged.add(key, value)
            }

            defaults.asJsonObject.entrySet().forEach { (key, defaultValue) ->
                val currentValue = if (merged.has(key)) merged.get(key) else null
                if (currentValue == null || currentValue.isJsonNull) {
                    merged.add(key, defaultValue.deepCopy())
                } else if (!isTypeCompatible(defaultValue, currentValue)) {

                    merged.add(key, defaultValue.deepCopy())
                } else {
                    merged.add(key, mergeMissingDefaults(defaultValue, currentValue))
                }
            }

            return merged
        }

        private fun isTypeCompatible(default: JsonElement, current: JsonElement): Boolean {
            if (default.isJsonObject && !current.isJsonObject) return false
            if (default.isJsonArray && !current.isJsonArray) return false
            if (default.isJsonPrimitive && current.isJsonObject) return false
            if (default.isJsonPrimitive && current.isJsonArray) return false
            return true
        }
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value ?: emptyList<String>())
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            val parsed = JsonParser.parseString(value)
            if (!parsed.isJsonArray) return emptyList()

            parsed.asJsonArray.mapNotNull { element ->
                try {
                    if (element.isJsonNull) null else element.asString
                } catch (_: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromAdConfig(value: AdConfig?): String = toJson(value)

    @TypeConverter
    fun toAdConfig(value: String): AdConfig? = fromJson(value)

    @TypeConverter
    fun fromAnnouncement(value: Announcement?): String = toJson(value)

    @TypeConverter
    fun toAnnouncement(value: String): Announcement? = fromJson(value)

    @TypeConverter
    fun fromWebViewConfig(value: WebViewConfig): String = toJson(value)

    @TypeConverter
    fun toWebViewConfig(value: String): WebViewConfig {
        val config = fromJsonOrDefault(value, WebViewConfig())

        return if (config.landscapeMode && config.orientationMode == com.webtoapp.data.model.OrientationMode.PORTRAIT) {
            config.copy(orientationMode = com.webtoapp.data.model.OrientationMode.LANDSCAPE)
        } else {
            config
        }
    }

    @TypeConverter
    fun fromSplashConfig(value: SplashConfig?): String = toJson(value)

    @TypeConverter
    fun toSplashConfig(value: String): SplashConfig? = fromJson(value)

    @TypeConverter
    fun fromAppType(value: AppType): String = value.name

    @TypeConverter
    fun toAppType(value: String): AppType = try {
        AppType.valueOf(value)
    } catch (e: Exception) {
        AppType.WEB
    }

    @TypeConverter
    fun fromMediaConfig(value: MediaConfig?): String = toJson(value)

    @TypeConverter
    fun toMediaConfig(value: String): MediaConfig? = fromJson(value)

    @TypeConverter
    fun fromGalleryConfig(value: GalleryConfig?): String = toJson(value)

    @TypeConverter
    fun toGalleryConfig(value: String): GalleryConfig? = fromJson(value)

    @TypeConverter
    fun fromBgmConfig(value: BgmConfig?): String = toJson(value)

    @TypeConverter
    fun toBgmConfig(value: String): BgmConfig? = fromJson(value)

    @TypeConverter
    fun fromHtmlConfig(value: HtmlConfig?): String = toJson(value)

    @TypeConverter
    fun toHtmlConfig(value: String): HtmlConfig? = fromJson(value)

    @TypeConverter
    fun fromApkExportConfig(value: ApkExportConfig?): String = toJson(value)

    @TypeConverter
    fun toApkExportConfig(value: String): ApkExportConfig? = fromJson(value)

    @TypeConverter
    fun fromTranslateConfig(value: TranslateConfig?): String = toJson(value)

    @TypeConverter
    fun toTranslateConfig(value: String): TranslateConfig? = fromJson(value)

    @TypeConverter
    fun fromActivationCodeList(value: List<ActivationCode>?): String {
        val safeList = value ?: emptyList()
        return gson.toJson(safeList.map { gson.toJsonTree(it) })
    }

    @TypeConverter
    fun toActivationCodeList(value: String): List<ActivationCode> {
        return try {
            val parsed = JsonParser.parseString(value)
            if (!parsed.isJsonArray) return emptyList()

            parsed.asJsonArray.mapNotNull { element ->
                try {
                    gson.fromJson(element, ActivationCode::class.java)
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromActivationDialogConfig(value: ActivationDialogConfig?): String = toJson(value)

    @TypeConverter
    fun toActivationDialogConfig(value: String): ActivationDialogConfig? = fromJson(value)

    @TypeConverter
    fun fromRemoteActivationConfig(value: com.webtoapp.data.model.RemoteActivationConfig?): String = toJson(value)

    @TypeConverter
    fun toRemoteActivationConfig(value: String): com.webtoapp.data.model.RemoteActivationConfig? = fromJson(value)

    @TypeConverter
    fun fromAppLockConfig(value: com.webtoapp.data.model.AppLockConfig?): String = toJson(value)

    @TypeConverter
    fun toAppLockConfig(value: String): com.webtoapp.data.model.AppLockConfig? = fromJson(value)

    @TypeConverter
    fun fromAutoStartConfig(value: AutoStartConfig?): String = toJson(value)

    @TypeConverter
    fun toAutoStartConfig(value: String): AutoStartConfig? = fromJson(value)

    @TypeConverter
    fun fromBrowserDisguiseConfig(value: com.webtoapp.core.appearance.BrowserDisguiseConfig?): String = toJson(value)

    @TypeConverter
    fun toBrowserDisguiseConfig(value: String): com.webtoapp.core.appearance.BrowserDisguiseConfig? = fromJson(value)

    @TypeConverter
    fun fromDeviceDisguiseConfig(value: com.webtoapp.core.appearance.DeviceDisguiseConfig?): String = toJson(value)

    @TypeConverter
    fun toDeviceDisguiseConfig(value: String): com.webtoapp.core.appearance.DeviceDisguiseConfig? = fromJson(value)

    @TypeConverter
    fun fromWordPressConfig(value: WordPressConfig?): String = toJson(value)

    @TypeConverter
    fun toWordPressConfig(value: String): WordPressConfig? = fromJson(value)

    @TypeConverter
    fun fromNodeJsConfig(value: NodeJsConfig?): String = toJson(value)

    @TypeConverter
    fun toNodeJsConfig(value: String): NodeJsConfig? = fromJson(value)

    @TypeConverter
    fun fromPhpAppConfig(value: PhpAppConfig?): String = toJson(value)

    @TypeConverter
    fun toPhpAppConfig(value: String): PhpAppConfig? = fromJson(value)

    @TypeConverter
    fun fromPythonAppConfig(value: PythonAppConfig?): String = toJson(value)

    @TypeConverter
    fun toPythonAppConfig(value: String): PythonAppConfig? = fromJson(value)

    @TypeConverter
    fun fromGoAppConfig(value: GoAppConfig?): String = toJson(value)

    @TypeConverter
    fun toGoAppConfig(value: String): GoAppConfig? = fromJson(value)

    @TypeConverter
    fun fromMultiWebConfig(value: MultiWebConfig?): String = toJson(value)

    @TypeConverter
    fun toMultiWebConfig(value: String): MultiWebConfig? = fromJson(value)

}
