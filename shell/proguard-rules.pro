# WebToApp ProGuard Rules
#
# 策略：启用代码收缩（移除未使用代码）+ 资源压缩（移除未使用资源）
# + 混淆重命名（dex 约 8% 收益），仍关闭激进优化。
#
# 混淆安全性说明：
#   - com.webtoapp.** 仅允许混淆、不允许收缩；字段名单独固定，
#     Gson/JSON 反射按名取字段不受影响（@SerializedName 成员规则兜底）。
#   - JNI 回调类（NodeJniOutputBridge 等）与 manifest 组件继续显式 keep。
#   - 崩溃栈仍能拿到行号（LineNumberTable 保留），类名需 mapping.txt 还原。
#
# 出问题时排查方法：
#   1. ./gradlew :shell:assembleRelease -PandroidProguardPrintUsage=true
#      会在 build/outputs/mapping/release/usage.txt 写出被 R8 删除的所有内容
#   2. ./gradlew :shell:assembleRelease -PandroidProguardPrintSeeds=true
#      会写出被显式 keep 的所有内容
#   3. 把崩溃栈对照 build/outputs/mapping/release/mapping.txt 反推

# 关掉一组容易破坏反射 / Compose / 协程语义的优化模式
# 这些是 R8 历史上反复出现 bug 的优化通道，关掉它们体积代价可忽略
-optimizations !class/merging/*,!field/*,!method/marking/static,!method/inlining/*,!code/allocation/variable
-optimizationpasses 1

# ============================================================
# 调试 / 崩溃堆栈可读
# ============================================================
-keepattributes SourceFile,LineNumberTable
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
-renamesourcefileattribute SourceFile

# ============================================================
# Android 组件 — Manifest 引用，必须保名
# ============================================================
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.accessibilityservice.AccessibilityService
-keep public class * extends androidx.work.ListenableWorker
-keep public class * extends android.app.Application
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends androidx.lifecycle.ViewModel
-keep public class * extends androidx.lifecycle.AndroidViewModel

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
-keep class kotlin.reflect.** { *; }
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
# 项目自身代码 — 防收缩，但允许混淆重命名
# （开源 + 重反射：类全量保留；字段名固定以保护未注解的 Gson 字段）
# ============================================================
# Feature-stack bridge layer: classes in feature_stacks/*.dex are loaded by
# DexClassLoader and reference this contract plus MainFeatureRuntime BY NAME —
# it must stay fully unrenamed and unshrunk or dex calls resolve to nothing.
-keep class com.webtoapp.core.featurestack.** { *; }
-keep,allowobfuscation class com.webtoapp.** { *; }
-keepclassmembers class com.webtoapp.** { <fields>; }

# Feature dexes reference kotlin-stdlib / coroutines by their source names
# (e.g. kotlin.jvm.internal.Intrinsics, kotlinx.coroutines.Dispatchers). Pin the
# names of whatever survives shrinking in the main dex so cross-dex calls
# resolve; unused classes may still shrink.
-keepnames class kotlin.**
-keepnames class kotlinx.**

# data class 的合成构造器（含默认参数）— Gson 反序列化必须
-keepclassmembers class com.webtoapp.data.model.** {
    <init>(...);
}

# ============================================================
# Room — KSP 生成的 _Impl 类已经在 com.webtoapp.** 范围内
# ============================================================
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-dontwarn androidx.room.paging.**

# ============================================================
# Gson — 序列化 / 反序列化通过反射
# ============================================================
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }
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

# Gson TypeToken — R8 full mode 会把 `object : TypeToken<List<...>>() {}`
# 匿名子类的 Signature 属性剥掉，运行时抛 IllegalStateException。
# 参考: https://github.com/google/gson/blob/main/Troubleshooting.md#r8
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

# ============================================================
# OkHttp / Okio — Platform 反射检测 OS 安全栈
# keepnames: 允许收缩未用类；存活类保留类名（Platform 按名反射探测）
# ============================================================
-keepnames class okhttp3.internal.platform.**
-keepnames class okhttp3.internal.publicsuffix.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ============================================================
# Coil — fetcher / decoder / mapper 走组件注册，允许收缩未用实现
# ============================================================
-keepnames class coil.util.**
-keepnames class coil.fetch.**
-keepnames class coil.decode.**
-keepnames class coil.map.**
-dontwarn coil.**

# ============================================================
# GeckoView — 大量 JNI / 注解反射
# ============================================================
-keep class org.mozilla.geckoview.** { *; }
-keep class org.mozilla.gecko.** { *; }
-keepclassmembers class * extends org.mozilla.geckoview.GeckoSession$* { *; }
-dontwarn org.mozilla.geckoview.**

# ============================================================
# ZXing — shell 未依赖，仅兜底
# ============================================================
-dontwarn com.google.zxing.**

# ============================================================
# Credentials API + GoogleId — 反射解析 ID Token
# keepnames: AAR 自带 consumer rules 管成员级反射，这里只保类名并允许收缩
# ============================================================
-keepnames class androidx.credentials.**
-keepnames class com.google.android.libraries.identity.**
-keepnames class com.google.android.gms.auth.api.identity.**
-dontwarn androidx.credentials.**
-dontwarn com.google.android.libraries.identity.**

# ============================================================
# DataStore Preferences — 无反射，允许收缩未用类
# ============================================================
-keepnames class androidx.datastore.**
-dontwarn androidx.datastore.**

# ============================================================
# Compress / xz — 按名解析格式的类名保留；未用到的归档格式实现允许收缩
# ============================================================
-keepnames class org.apache.commons.compress.**
-keepnames class org.tukaani.xz.**
-dontwarn org.apache.commons.compress.**
-dontwarn org.tukaani.xz.**
-dontwarn org.brotli.dec.**
# snakeyaml rides in transitively via GeckoView; its java.beans introspection
# references don't exist on Android but those code paths are never hit.
-dontwarn org.yaml.snakeyaml.**

# ============================================================
# Compose Runtime — 已有 consumer rules，仅压制 warn
# ============================================================
-dontwarn androidx.compose.**

# Node.js JNI output bridge (R8 may rename onOutput otherwise)
-keep class com.webtoapp.core.nodejs.NodeBridge { *; }
-keep class com.webtoapp.core.nodejs.NodeJniOutputBridge {
    <init>(...);
    public void onOutput(java.lang.String, boolean);
}

# Firebase / FCM
# keepnames 而非全量 keep：gms 全家桶（auth/fido/common.api 等 ~4MB dex）
# 此前被该规则整包锚定为 shrink 种子；改为存活类保名、死类可收缩。
# FCM / Credential 的反射面由各库自带 consumer rules 兜底。
-keepnames class com.google.firebase.**
-keepnames class com.google.android.gms.**
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ============================================================
# Cronet (forced HTTP/3 upstream)
# ============================================================
# Cronet's native code reaches its Java classes via JNI and ServiceLoader
# (org.chromium.net.impl.** / org.chromium.base.**); R8 must not rename or
# strip them. The AAR ships consumer rules, this is the belt-and-braces copy
# for the shell template path.
-keep class org.chromium.net.** { *; }
-keep class org.chromium.base.** { *; }
-keep class org.chromium.components.** { *; }
-dontwarn org.chromium.**
-keepdirectories META-INF/services/**
