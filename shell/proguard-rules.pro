# WebToApp ProGuard Rules
#
# 策略：启用代码收缩（移除未使用代码）+ 资源压缩（移除未使用资源），
# 禁用混淆与激进优化。这是经过线上事故反复验证后的"稳定优先"配置，
# 减包体积仍能保留 30%+ 收益，但完全避免反射/泛型/ServiceLoader/JNI 类陷阱。
#
# 收益与权衡：
#   - shrink:    保留，移除未使用类/方法（约 25% 体积削减）
#   - obfuscate: 关闭（开源项目无意义，反而引入 Gson/Koin/反射 bug）
#   - optimize:  关闭部分激进优化（保留默认收缩，避免内联引发的 NPE）
#
# 出问题时排查方法：
#   1. ./gradlew :app:assembleRelease -PandroidProguardPrintUsage=true
#      会在 build/outputs/mapping/release/usage.txt 写出被 R8 删除的所有内容
#   2. ./gradlew :app:assembleRelease -PandroidProguardPrintSeeds=true
#      会写出被显式 keep 的所有内容
#   3. 把崩溃栈对照 build/outputs/mapping/release/mapping.txt 反推

# -dontobfuscate  # shell template: enable obfuscation for size

-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
-assumenosideeffects class com.webtoapp.core.logging.AppLogger {
    public static void v(...);
    public static void d(...);
    public static void i(...);
}

# 关掉一组容易破坏反射 / Compose / 协程语义的优化模式
# 这些是 R8 历史上反复出现 bug 的优化通道，关掉它们体积代价可忽略
-optimizationpasses 3

# ============================================================
# 调试 / 崩溃堆栈可读
# ============================================================
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleAnnotations,RuntimeInvisibleParameterAnnotations
-keepattributes RuntimeVisibleTypeAnnotations,RuntimeInvisibleTypeAnnotations
-keepattributes AnnotationDefault
-keepattributes MethodParameters

# 让崩溃栈打印 R8 重命名前的类名 / 行号

# ============================================================
# Android 组件 — Manifest 引用，必须保名
# ============================================================
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.accessibilityservice.AccessibilityService
-keep public class * extends android.app.Application

# ============================================================
# 通用反射 — Parcelable / Serializable / enum / native
# ============================================================
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# JNI native 方法
-keepclasseswithmembernames class * {
    native <methods>;
}

# WebView @JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# View 的 XML inflate / setOnClick 反射回调
-keepclassmembers class * extends android.view.View {
    void set*(***);
    *** get*();
}
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

# ============================================================
# Kotlin
# ============================================================
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings { <fields>; }
-keepclassmembers class kotlin.Lazy { public <methods>; }
-keep class kotlin.jvm.internal.DefaultConstructorMarker { *; }
-keepclassmembers class kotlin.coroutines.jvm.internal.** { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# Kotlin Coroutines — ServiceLoader 加载 Dispatchers.Main
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory
-keep class kotlinx.coroutines.android.AndroidDispatcherFactory { *; }
-keep class kotlinx.coroutines.android.AndroidExceptionPreHandler { *; }
-keep class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keep class kotlinx.coroutines.CoroutineExceptionHandler { *; }
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
# Coroutines 调试 / 异常 hooks
-keepnames class kotlinx.coroutines.flow.** { *; }

# ============================================================
# 项目自身代码 — 允许 R8 剔除未引用成员（LITE 体积）
# ============================================================
-keepclassmembers class com.webtoapp.data.model.** {
    <init>(...);
    <fields>;
}
-keepclassmembers class com.webtoapp.core.shell.** {
    <init>(...);
    <fields>;
}
-keep class com.webtoapp.core.appearance.** {
    <init>(...);
    <fields>;
}
-keepclassmembers enum com.webtoapp.core.appearance.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    <fields>;
}
-keep class com.webtoapp.core.actions.** {
    <init>(...);
    <fields>;
}
-keep class com.webtoapp.core.forcedrun.** {
    <init>(...);
    <fields>;
}
-keep class com.webtoapp.core.feature.** { *; }
-keep class * implements com.webtoapp.core.feature.FeatureModule { *; }
-keep class com.webtoapp.feature.compat.** { *; }
-keep class com.webtoapp.ui.shell.ShellActivity { *; }
-keep class com.webtoapp.core.i18n.Strings
-keep class com.webtoapp.core.i18n.ShellStringTable { *; }
-keep class com.webtoapp.core.i18n.AppLanguage { *; }
-keep class com.webtoapp.ui.shell.GeolocationPermissionsSingleton { *; }
-keep class com.webtoapp.core.engine.GeckoEngineAccess { *; }
-keep class com.webtoapp.core.webview.TlsMitmAccess { *; }
-keep class com.webtoapp.core.notification.FcmAccess { *; }
-keepclassmembers class com.webtoapp.core.engine.GeckoViewEngine { *; }
-keepclassmembers class com.webtoapp.core.webview.TlsMitmBridge { *; }
-keepclassmembers class com.webtoapp.core.webview.TlsMitmCaManager { *; }
-keepclassmembers class com.webtoapp.core.notification.NotificationFcmManager { *; }
-keepclassmembers enum com.webtoapp.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep interface com.webtoapp.core.feature.** { *; }

-dontwarn androidx.room.**

# ============================================================
# Gson — 序列化 / 反序列化通过反射
# ============================================================
-keep class sun.misc.Unsafe { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keep class * implements com.google.gson.InstanceCreator
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
    @com.google.gson.annotations.JsonAdapter <fields>;
    @com.google.gson.annotations.Expose <fields>;
}
-dontwarn com.google.gson.**

# Gson TypeToken — R8 full mode 会把 `object : TypeToken<List<...>>() {}`
# 匿名子类的 Signature 属性剥掉，运行时抛 IllegalStateException。
# 参考: https://github.com/google/gson/blob/main/Troubleshooting.md#r8
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

-dontwarn org.koin.**

# ============================================================
# OkHttp / Okio — Platform 反射检测 OS 安全栈
# ============================================================
-keep class okhttp3.internal.platform.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

-dontwarn coil.**

-dontwarn com.android.apksig.**

-dontwarn org.mozilla.geckoview.**
-dontwarn org.mozilla.gecko.**

-dontwarn com.google.zxing.**
-dontwarn com.journeyapps.**

-dontwarn com.patrykandpatrick.vico.**

-dontwarn com.android.billingclient.**
-dontwarn com.android.vending.billing.**

-dontwarn androidx.credentials.**
-dontwarn com.google.android.libraries.identity.**
-dontwarn com.google.android.gms.auth.api.identity.**

-dontwarn androidx.datastore.**

-dontwarn androidx.security.crypto.**
-dontwarn com.google.crypto.tink.**

-dontwarn org.apache.commons.compress.**
-dontwarn org.tukaani.xz.**
-dontwarn org.brotli.dec.**
-dontwarn org.yaml.snakeyaml.**

# ============================================================
# Compose Runtime — 已有 consumer rules，仅压制 warn
# ============================================================
-dontwarn androidx.compose.**

# ============================================================
# Material / AppCompat — 已有 consumer rules，仅兜底
# ============================================================
-dontwarn com.google.android.material.**

-dontwarn androidx.browser.**

-dontwarn dev.chrisbanes.haze.**

-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**


-keep class com.webtoapp.core.feature.** { *; }
-keep class * implements com.webtoapp.core.feature.FeatureModule { *; }
-keep class com.webtoapp.feature.compat.** { *; }
