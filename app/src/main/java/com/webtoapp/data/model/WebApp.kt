package com.webtoapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.webtoapp.data.converter.Converters
import androidx.compose.runtime.Stable
import com.webtoapp.util.toFileSizeString

enum class AppType {
    WEB,
    IMAGE,
    VIDEO,
    HTML,
    GALLERY,
    FRONTEND,
    WORDPRESS,
    NODEJS_APP,
    PHP_APP,
    PYTHON_APP,
    GO_APP,
    MULTI_WEB;

    val requiresProcessExec: Boolean
        get() = this in REQUIRES_PROCESS_EXEC

    companion object {
        val REQUIRES_PROCESS_EXEC: Set<AppType> = setOf(
            NODEJS_APP,
            PHP_APP,
            PYTHON_APP,
            GO_APP,
            WORDPRESS
        )

        fun fromPersistedName(name: String?): AppType? =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }
}

@Entity(
    tableName = "web_apps",
    indices = [
        Index(value = ["updatedAt"]),
        Index(value = ["categoryId"]),
        Index(value = ["isActivated"]),
        Index(value = ["appType", "url"]),
        Index(value = ["appType", "iconPath", "url"])
    ]
)
@TypeConverters(Converters::class)
@Stable
data class WebApp(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val url: String,
    val iconPath: String? = null,
    val packageName: String? = null,
    val appType: AppType = AppType.WEB,
    val mediaConfig: MediaConfig? = null,
    val galleryConfig: GalleryConfig? = null,
    val htmlConfig: HtmlConfig? = null,
    val wordpressConfig: WordPressConfig? = null,
    val nodejsConfig: NodeJsConfig? = null,
    val phpAppConfig: PhpAppConfig? = null,
    val pythonAppConfig: PythonAppConfig? = null,
    val goAppConfig: GoAppConfig? = null,
    val multiWebConfig: MultiWebConfig? = null,

    val activationEnabled: Boolean = false,
    val activationCodeList: List<com.webtoapp.core.activation.ActivationCode> = emptyList(),
    val activationRequireEveryTime: Boolean = false,
    val activationRemoteConfig: RemoteActivationConfig? = null,
    val isActivated: Boolean = false,

    val announcementEnabled: Boolean = false,
    val announcement: Announcement? = null,

    val adBlockEnabled: Boolean = false,
    val adBlockRules: List<String> = emptyList(),
    val adBlockSubscriptions: List<String> = emptyList(),

    val webViewConfig: WebViewConfig = WebViewConfig(),

    val splashEnabled: Boolean = false,
    val splashConfig: SplashConfig? = null,

    val bgmEnabled: Boolean = false,
    val bgmConfig: BgmConfig? = null,

    val apkExportConfig: ApkExportConfig? = null,

    val themeType: String = "AURORA",

    val translateEnabled: Boolean = false,
    val translateConfig: TranslateConfig? = null,

    @ColumnInfo(name = "extensionEnabled")
    val pluginsEnabled: Boolean = false,
    @ColumnInfo(name = "extensionModuleIds")
    val pluginIds: List<String> = emptyList(),
    @Deprecated("Plugin entries are native; the injected FAB no longer exists.")
    val extensionFabIcon: String? = null,

    val autoStartConfig: AutoStartConfig? = null,
    val browserDisguiseConfig: com.webtoapp.core.appearance.BrowserDisguiseConfig? = null,
    val deviceDisguiseConfig: com.webtoapp.core.appearance.DeviceDisguiseConfig? = null,
    val activationDialogConfig: ActivationDialogConfig? = null,
    val categoryId: Long? = null,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(defaultValue = "0")
    val homeSortIndex: Int = 0
)

enum class AnnouncementTemplateType {
    MINIMAL,
    XIAOHONGSHU,
    GRADIENT,
    GLASSMORPHISM,
    NEON,
    CUTE,
    ELEGANT,
    FESTIVE,
    DARK,
    NATURE
}

enum class AnnouncementTriggerMode {
    ON_LAUNCH,
    ON_INTERVAL,
    ON_NO_NETWORK
}

data class Announcement(
    val title: String = "",
    val content: String = "",
    val contentIsHtml: Boolean = false,
    val linkUrl: String? = null,
    val linkText: String? = null,
    val showOnce: Boolean = true,
    val enabled: Boolean = true,
    val version: Int = 1,
    val template: AnnouncementTemplateType = AnnouncementTemplateType.XIAOHONGSHU,
    val requireConfirmation: Boolean = false,
    val allowNeverShow: Boolean = true,
    val triggerOnLaunch: Boolean = true,
    val triggerOnNoNetwork: Boolean = false,
    val triggerIntervalMinutes: Int = 0,
    val triggerIntervalIncludeLaunch: Boolean = false,
    val showIcon: Boolean = true,
    val customIconPath: String? = null
)

enum class StatusBarColorMode {
    THEME,
    PAGE_TOP,
    TRANSPARENT,
    CUSTOM
}

enum class StatusBarBackgroundType {
    COLOR,
    IMAGE
}

enum class LongPressMenuStyle {
    DISABLED,
    SIMPLE,
    FULL,
    IOS,
    FLOATING,
    CONTEXT
}

enum class SwipeRefreshZone {
    TOP_EDGE,
    ANYWHERE
}

enum class UserAgentMode(
    val displayName: String,
    val description: String,
    val userAgentString: String?
) {
    DEFAULT(
        "System Default",
        "Use Android WebView default User-Agent",
        null
    ),
    CHROME_MOBILE(
        "Chrome Mobile",
        "Disguise as Chrome Android browser",
        "Mozilla/5.0 (Linux; Android 15; Pixel 9 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/" + UserAgentVersions.CHROME + ".0.0.0 Mobile Safari/537.36"
    ),
    CHROME_DESKTOP(
        "Chrome Desktop",
        "Disguise as Chrome Windows browser",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/" + UserAgentVersions.CHROME + ".0.0.0 Safari/537.36"
    ),
    SAFARI_MOBILE(
        "Safari Mobile",
        "Disguise as Safari iOS browser",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 18_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/" + UserAgentVersions.SAFARI + ".0 Mobile/15E148 Safari/604.1"
    ),
    SAFARI_DESKTOP(
        "Safari Desktop",
        "Disguise as Safari macOS browser",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 15_0) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/" + UserAgentVersions.SAFARI + ".0 Safari/605.1.15"
    ),
    FIREFOX_MOBILE(
        "Firefox Mobile",
        "Disguise as Firefox Android browser",
        "Mozilla/5.0 (Android 15; Mobile; rv:" + UserAgentVersions.FIREFOX + ".0) Gecko/" + UserAgentVersions.FIREFOX + ".0 Firefox/" + UserAgentVersions.FIREFOX + ".0"
    ),
    FIREFOX_DESKTOP(
        "Firefox Desktop",
        "Disguise as Firefox Windows browser",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:" + UserAgentVersions.FIREFOX + ".0) Gecko/20100101 Firefox/" + UserAgentVersions.FIREFOX + ".0"
    ),
    EDGE_MOBILE(
        "Edge Mobile",
        "Disguise as Edge Android browser",
        "Mozilla/5.0 (Linux; Android 15; Pixel 9 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/" + UserAgentVersions.CHROME + ".0.0.0 Mobile Safari/537.36 EdgA/" + UserAgentVersions.CHROME + ".0.0.0"
    ),
    EDGE_DESKTOP(
        "Edge Desktop",
        "Disguise as Edge Windows browser",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/" + UserAgentVersions.CHROME + ".0.0.0 Safari/537.36 Edg/" + UserAgentVersions.CHROME + ".0.0.0"
    ),
    CUSTOM(
        "Custom",
        "Use custom User-Agent string",
        null
    );

    fun toKernelFlavor(): com.webtoapp.core.kernel.KernelFlavor = when (this) {
        DEFAULT, CUSTOM -> com.webtoapp.core.kernel.KernelFlavor.SYSTEM_DEFAULT
        CHROME_MOBILE -> com.webtoapp.core.kernel.KernelFlavor.BLINK_CHROME
        CHROME_DESKTOP -> com.webtoapp.core.kernel.KernelFlavor.BLINK_CHROME_DESKTOP
        SAFARI_MOBILE -> com.webtoapp.core.kernel.KernelFlavor.WEBKIT_SAFARI
        SAFARI_DESKTOP -> com.webtoapp.core.kernel.KernelFlavor.WEBKIT_SAFARI_DESKTOP
        FIREFOX_MOBILE -> com.webtoapp.core.kernel.KernelFlavor.GECKO_FIREFOX
        FIREFOX_DESKTOP -> com.webtoapp.core.kernel.KernelFlavor.GECKO_FIREFOX_DESKTOP
        EDGE_MOBILE -> com.webtoapp.core.kernel.KernelFlavor.BLINK_EDGE
        EDGE_DESKTOP -> com.webtoapp.core.kernel.KernelFlavor.BLINK_EDGE_DESKTOP
    }
}

object UserAgentVersions {
    const val CHROME = "131"
    const val FIREFOX = "133"
    const val SAFARI = "18"
}

@Stable
data class WebViewConfig(
    val javaScriptEnabled: Boolean = true,
    val domStorageEnabled: Boolean = true,
    val allowFileAccess: Boolean = false,
    val allowContentAccess: Boolean = true,
    val cacheEnabled: Boolean = true,
    val clearBrowsingDataOnLaunch: Boolean = false,
    val clientCertificateAuthEnabled: Boolean = false,
    val userAgent: String? = null,
    val userAgentMode: UserAgentMode = UserAgentMode.DEFAULT,
    val customUserAgent: String? = null,
    val desktopMode: Boolean = false,
    val zoomEnabled: Boolean = true,
    val swipeRefreshEnabled: Boolean = true,
    val swipeRefreshZone: SwipeRefreshZone = SwipeRefreshZone.TOP_EDGE,
    val overscrollEffectEnabled: Boolean = true,
    val autoRefreshEnabled: Boolean = false,
    val autoRefreshIntervalSec: Int = 60,
    val autoRefreshShowCountdown: Boolean = true,
    val fullscreenEnabled: Boolean = true,
    val downloadEnabled: Boolean = true,
    val downloadLocationMode: DownloadLocationMode = DownloadLocationMode.SYSTEM_DOWNLOAD,
    val customDownloadDirUri: String = "",
    val openExternalLinks: Boolean = false,
    val browserToolbarEnabled: Boolean = false,
    val toolbarShowTitle: Boolean = true,
    val toolbarShowUrl: Boolean = true,
    val toolbarShowBack: Boolean = true,
    val toolbarShowForward: Boolean = true,
    val toolbarShowRefresh: Boolean = true,
    val toolbarShowConsole: Boolean = true,
    val toolbarShowFind: Boolean = true,
    val hideToolbar: Boolean = false,
    val showStatusBarInFullscreen: Boolean = false,
    val hideStatusBarInVideoFullscreen: Boolean = true,
    val showNavigationBarInFullscreen: Boolean = false,
    val displayCutoutMode: DisplayCutoutMode = DisplayCutoutMode.SHORT_EDGES,
    val showToolbarInFullscreen: Boolean = false,
    val fullscreenContentPaddingDp: Int = 0,
    val fullscreenContentPaddingTopDp: Int? = null,
    val fullscreenContentPaddingBottomDp: Int? = null,
    val fullscreenContentPaddingStartDp: Int? = null,
    val fullscreenContentPaddingEndDp: Int? = null,
    val landscapeMode: Boolean = false,
    val orientationMode: OrientationMode = OrientationMode.PORTRAIT,
    val injectScripts: List<UserScript> = emptyList(),
    val statusBarColorMode: StatusBarColorMode = StatusBarColorMode.TRANSPARENT,
    val statusBarColor: String? = null,
    val statusBarDarkIcons: Boolean? = null,
    val statusBarBackgroundType: StatusBarBackgroundType = StatusBarBackgroundType.COLOR,
    val statusBarBackgroundImage: String? = null,
    val statusBarBackgroundAlpha: Float = 1.0f,

    val statusBarHeightDp: Int = -1,

    val statusBarColorModeDark: StatusBarColorMode = StatusBarColorMode.TRANSPARENT,
    val statusBarColorDark: String? = null,
    val statusBarDarkIconsDark: Boolean? = null,
    val statusBarBackgroundTypeDark: StatusBarBackgroundType = StatusBarBackgroundType.COLOR,
    val statusBarBackgroundImageDark: String? = null,
    val statusBarBackgroundAlphaDark: Float = 1.0f,
    val followSystemDarkMode: Boolean = false,
    val longPressMenuEnabled: Boolean = false,
    val longPressMenuStyle: LongPressMenuStyle = LongPressMenuStyle.DISABLED,
    val longPressHapticOnActionOnly: Boolean = false,

    val popupBlockerEnabled: Boolean = false,

    val initialScale: Int = 0,
    val pageZoomPercent: Int = 100,
    val viewportMode: ViewportMode = ViewportMode.DEFAULT,
    val customViewportWidth: Int = 0,
    val newWindowBehavior: NewWindowBehavior = NewWindowBehavior.SAME_WINDOW,
    val enablePaymentSchemes: Boolean = true,

    val enableAppReturn: Boolean = true,

    val customAppReturnSchemes: List<String> = emptyList(),

    val enableShareBridge: Boolean = true,

    val receiveShareImages: Boolean = false,
    val receiveShareText: Boolean = false,

    val openWithEnabled: Boolean = false,

    val shareDeliveryMode: ShareDeliveryMode = ShareDeliveryMode.BOTH,

    val sharePromptBeforeUse: Boolean = true,

    val shareShowReceivedToast: Boolean = true,

    val enableZoomPolyfill: Boolean = true,
    val enableCrossOriginIsolation: Boolean = false,
    val hideUrlPreview: Boolean = false,

    val decodeBase64DeepLinks: Boolean = true,
    val decodeBase64Mode: Base64DeepLinkMode = Base64DeepLinkMode.GESTURE_ONLY,
    val javaScriptCanOpenWindows: Boolean = true,
    val jsOpenWindowsPolicy: JsOpenWindowsPolicy = JsOpenWindowsPolicy.ALLOW,

    val mediaAutoplayEnabled: Boolean = false,
    val mediaAutoplayScope: MediaAutoplayScope = MediaAutoplayScope.VIDEO_ONLY,
    val enableImageRepair: Boolean = true,
    val enableScrollMemory: Boolean = true,
    val enableBackStatePreservation: Boolean = true,

    val enableKernelDisguise: Boolean = false,
    val kernelDisguiseLevel: KernelDisguiseLevel = KernelDisguiseLevel.STANDARD,
    val kernelFlavor: com.webtoapp.core.kernel.KernelFlavor = com.webtoapp.core.kernel.KernelFlavor.SYSTEM_DEFAULT,
    val enableCloudflareCompat: Boolean = true,
    val cloudflareCompatMode: CloudflareCompatMode = CloudflareCompatMode.AUTO_DETECT,
    val allowMixedContent: Boolean = false,
    val mixedContentMode: MixedContentMode = MixedContentMode.COMPATIBILITY,
    val enablePrivateNetworkBridge: Boolean = true,
    val privateNetworkScope: PrivateNetworkScope = PrivateNetworkScope.LOCAL_ONLY,
    val enableCorsBypass: Boolean = true,

    val acceptThirdPartyCookies: Boolean = false,
    val thirdPartyCookieMode: ThirdPartyCookieMode = ThirdPartyCookieMode.SAME_SITE_LAX,
    val databaseEnabled: Boolean = true,
    val enableCookiePersistence: Boolean = true,

    val enableClipboardPolyfill: Boolean = true,
    val enableNotificationPolyfill: Boolean = false,
    val enableOrientationPolyfill: Boolean = true,
    val enableCompatPolyfills: Boolean = true,

    val enableNativeBridge: Boolean = true,
    val nativeBridgeCapabilities: NativeBridgeCapabilities = NativeBridgeCapabilities(),

    val geolocationEnabled: Boolean = false,
    val geolocationAccuracy: GeolocationAccuracy = GeolocationAccuracy.FINE,
    val geolocationPolicy: GeolocationPolicy = GeolocationPolicy.ALWAYS_ASK,
    val enableBlobDownloadInterception: Boolean = true,
    val blobInterceptScope: BlobInterceptScope = BlobInterceptScope.ALL,
    val blobInterceptThresholdMb: Int = 5,

    val enablePrintBridge: Boolean = true,

    val enableMediaSession: Boolean = true,

    val backgroundMediaPlayback: Boolean = false,

    val primeUserActivation: Boolean = true,
    val primeUserActivationMode: PrimeUserActivationMode = PrimeUserActivationMode.SYNTHETIC_TAP,
    val primeUserActivationTiming: PrimeUserActivationTiming = PrimeUserActivationTiming.ON_PAGE_FINISHED,

    val failoverEnabled: Boolean = false,
    val failoverUrls: List<String> = emptyList(),
    val failoverTriggers: FailoverTriggers = FailoverTriggers(),

    val failoverTimeoutSeconds: Int = 15,

    val fullscreenVideoOrientation: FullscreenVideoOrientation = FullscreenVideoOrientation.AUTO_SENSOR_LANDSCAPE,

    val keepScreenOn: Boolean = false,
    val screenAwakeMode: ScreenAwakeMode = ScreenAwakeMode.OFF,
    val screenAwakeTimeoutMinutes: Int = 30,
    val screenBrightness: Int = -1,
    val keyboardAdjustMode: KeyboardAdjustMode = KeyboardAdjustMode.RESIZE,

    val allowFileAccessFromFileURLs: Boolean = false,
    val allowUniversalAccessFromFileURLs: Boolean = false,

    val errorPageConfig: com.webtoapp.core.errorpage.ErrorPageConfig = com.webtoapp.core.errorpage.ErrorPageConfig(),
    val performanceOptimization: Boolean = false,
    val pwaOfflineEnabled: Boolean = false,
    val pwaOfflineStrategy: String = "NETWORK_FIRST",

    val staticAssetPackEnabled: Boolean = false,
    val staticAssetPackMaxAgeDays: Int = 30,
    val staticAssetPackIncludeImages: Boolean = false,
    val staticAssetPackIncludeCdn: Boolean = true,
    val staticAssetPackMaxTotalSizeMb: Int = 50,

    val showFloatingBackButton: Boolean = false,
    val backButtonBehavior: String = BACK_GO_BACK,
    val forwardVolumeKeys: Boolean = false,
    val consumeVolumeKeys: Boolean = false,
    val mediaAlbumName: String = "",
    val floatingWindowConfig: FloatingWindowConfig = FloatingWindowConfig(),
    val proxyMode: String = "NONE",
    val proxyHost: String = "",
    val proxyPort: Int = 0,
    val proxyType: String = "HTTP",
    val pacUrl: String = "",
    val proxyBypassRules: List<String> = emptyList(),
    val proxyUsername: String = "",
    val proxyPassword: String = "",
    val hostsMappingEnabled: Boolean = false,
    val hostsMappings: List<HostMappingEntry> = emptyList(),

    val tlsFingerprintEnabled: Boolean = false,
    val tlsFingerprintTemplate: String = "CHROME_131",
    val tlsFingerprintCustomCiphers: List<String> = emptyList(),

    val forceHttp3: Boolean = false,

    val antiCapture: Boolean = false,

    val dnsMode: String = "SYSTEM",
    val dnsConfig: DnsConfig = DnsConfig()
) {
    val fullscreenPadTop: Int get() = fullscreenContentPaddingTopDp ?: fullscreenContentPaddingDp
    val fullscreenPadBottom: Int get() = fullscreenContentPaddingBottomDp ?: fullscreenContentPaddingDp
    val fullscreenPadStart: Int get() = fullscreenContentPaddingStartDp ?: fullscreenContentPaddingDp
    val fullscreenPadEnd: Int get() = fullscreenContentPaddingEndDp ?: fullscreenContentPaddingDp

    val enableShareReceive: Boolean get() = receiveShareImages || receiveShareText

    val prefillsFileChooser: Boolean get() = enableShareReceive &&
        shareDeliveryMode != ShareDeliveryMode.JS_EVENT

    val broadcastsShareEvent: Boolean get() = enableShareReceive &&
        shareDeliveryMode != ShareDeliveryMode.FILE_CHOOSER_PREFILL

    companion object {
        const val BACK_GO_BACK = "GO_BACK"

        const val BACK_GO_BACK_EXIT = "GO_BACK_EXIT"

        const val BACK_EXIT = "EXIT"

        const val BACK_JS = "JS"
    }
}

data class HostMappingEntry(
    val host: String = "",
    val ip: String = ""
)

data class FloatingWindowConfig(
    val enabled: Boolean = false,
    val windowSizePercent: Int = 80,
    val widthPercent: Int = 80,
    val heightPercent: Int = 80,
    val lockAspectRatio: Boolean = false,
    val aspectRatioMode: FloatingWindowAspectRatioMode = FloatingWindowAspectRatioMode.FREE,
    val customAspectRatioWidth: Int = 16,
    val customAspectRatioHeight: Int = 9,
    val opacity: Int = 100,
    val cornerRadius: Int = 16,
    val borderStyle: FloatingBorderStyle = FloatingBorderStyle.SUBTLE,
    val minimizedIconPath: String? = null,
    val minimizedIconSizePercent: Int = 100,
    val minimizedIconEdgeDocking: Boolean = false,
    val showTitleBar: Boolean = true,
    val autoHideTitleBar: Boolean = false,
    val startMinimized: Boolean = false,
    val rememberPosition: Boolean = true,
    val edgeSnapping: Boolean = true,
    val showResizeHandle: Boolean = true,
    val lockPosition: Boolean = false
)

enum class FloatingBorderStyle {
    NONE,
    SUBTLE,
    GLOW,
    ACCENT
}

enum class FloatingWindowAspectRatioMode {
    SCREEN,
    FREE,
    RATIO_16_9,
    RATIO_9_16,
    RATIO_4_3,
    SQUARE,
    CUSTOM
}

enum class DnsProvider(val key: String, val dohUrl: String, val displayName: String) {
    @com.google.gson.annotations.SerializedName("cloudflare")
    CLOUDFLARE("cloudflare", "https://cloudflare-dns.com/dns-query", "Cloudflare"),
    @com.google.gson.annotations.SerializedName("google")
    GOOGLE("google", "https://dns.google/dns-query", "Google"),
    @com.google.gson.annotations.SerializedName("adguard")
    ADGUARD("adguard", "https://dns.adguard-dns.com/dns-query", "AdGuard"),
    @com.google.gson.annotations.SerializedName("nextdns")
    NEXTDNS("nextdns", "https://dns.nextdns.io/", "NextDNS"),
    @com.google.gson.annotations.SerializedName("cleanbrowsing")
    CLEANBROWSING("cleanbrowsing", "https://doh.cleanbrowsing.org/doh/family-filter/", "CleanBrowsing"),
    @com.google.gson.annotations.SerializedName("quad9")
    QUAD9("quad9", "https://dns.quad9.net/dns-query", "Quad9"),
    @com.google.gson.annotations.SerializedName("mullvad")
    MULLVAD("mullvad", "https://dns.mullvad.net/dns-query", "Mullvad"),
    @com.google.gson.annotations.SerializedName("custom")
    CUSTOM("custom", "", "Custom");

    companion object {
        fun fromKey(key: String): DnsProvider {
            return entries.find { it.key == key } ?: CLOUDFLARE
        }
    }
}

data class DnsConfig(

    val provider: String = "cloudflare",

    val customDohUrl: String = "",

    val dohMode: String = "automatic",

    val bypassSystemDns: Boolean = false,

    val echEnabled: Boolean = false
) {

    val effectiveDohUrl: String
        get() = when (provider) {
            "custom" -> customDohUrl
            else -> DnsProvider.entries.find { it.key == provider }?.dohUrl ?: ""
        }

    val echEffective: Boolean
        get() = echEnabled && effectiveDohUrl.isNotBlank()
}

data class UserScript(
    val name: String = "",
    val code: String = "",
    val enabled: Boolean = true,
    val runAt: ScriptRunTime = ScriptRunTime.DOCUMENT_END
)

enum class ScriptRunTime {
    DOCUMENT_START,
    DOCUMENT_END,
    DOCUMENT_IDLE
}

enum class NewWindowBehavior {
    SAME_WINDOW,
    EXTERNAL_BROWSER,
    POPUP_WINDOW,
    BLOCK
}

data class SplashConfig(
    val type: SplashType = SplashType.IMAGE,
    val mediaPath: String? = null,
    val duration: Int = 3,
    val clickToSkip: Boolean = true,
    val orientation: SplashOrientation = SplashOrientation.PORTRAIT,
    val fillScreen: Boolean = true,
    val enableAudio: Boolean = false,
    val videoStartMs: Long = 0,
    val videoEndMs: Long = 5000,
    val videoDurationMs: Long = 0,
    val showCountdown: Boolean = true
)

enum class SplashType {
    IMAGE,
    VIDEO
}

enum class SplashOrientation {
    PORTRAIT,
    LANDSCAPE
}

enum class KeyboardAdjustMode {
    RESIZE,
    NOTHING
}

enum class OrientationMode {
    PORTRAIT,
    LANDSCAPE,
    REVERSE_PORTRAIT,
    REVERSE_LANDSCAPE,
    SENSOR_PORTRAIT,
    SENSOR_LANDSCAPE,
    AUTO
}

enum class ScreenAwakeMode {
    OFF,
    ALWAYS,
    TIMED
}

enum class ViewportMode {
    DEFAULT,
    FIT_SCREEN,
    DESKTOP,
    CUSTOM
}

enum class DisplayCutoutMode {
    SHORT_EDGES,
    DEFAULT,
    NEVER
}

data class MediaConfig(
    val mediaPath: String,
    val enableAudio: Boolean = true,
    val loop: Boolean = true,
    val autoPlay: Boolean = true,
    val fillScreen: Boolean = true,
    val orientation: SplashOrientation = SplashOrientation.PORTRAIT,
    val backgroundColor: String = "#000000",
    val keepScreenOn: Boolean = true
)

@Stable
data class GalleryConfig(
    val items: List<GalleryItem> = emptyList(),
    val categories: List<GalleryCategory> = emptyList(),
    val playMode: GalleryPlayMode = GalleryPlayMode.SEQUENTIAL,
    val imageInterval: Int = 3,
    val loop: Boolean = true,
    val autoPlay: Boolean = false,
    val shuffleOnLoop: Boolean = false,
    val defaultView: GalleryViewMode = GalleryViewMode.GRID,
    val gridColumns: Int = 3,
    val sortOrder: GallerySortOrder = GallerySortOrder.CUSTOM,
    val backgroundColor: String = "#000000",
    val showThumbnailBar: Boolean = true,
    val showMediaInfo: Boolean = true,
    val orientation: SplashOrientation = SplashOrientation.PORTRAIT,
    val enableAudio: Boolean = true,
    val videoAutoNext: Boolean = true,
    val rememberPosition: Boolean = false,
    val useDeviceFolder: Boolean = false
) {
    val hasContent: Boolean get() = useDeviceFolder || items.isNotEmpty()

    fun getItemsByCategory(categoryId: String?): List<GalleryItem> {
        return if (categoryId == null) items
        else items.filter { it.categoryId == categoryId }
    }

    fun getSortedItems(categoryId: String? = null): List<GalleryItem> =
        sortedGalleryItems(getItemsByCategory(categoryId), sortOrder)

    val imageCount: Int get() = items.count { it.type == GalleryItemType.IMAGE }
    val videoCount: Int get() = items.count { it.type == GalleryItemType.VIDEO }
    val totalCount: Int get() = items.size
}

data class GalleryItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val path: String,
    val type: GalleryItemType,
    val name: String = "",
    val categoryId: String? = null,
    val duration: Long = 0,
    val thumbnailPath: String? = null,
    val sortIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val width: Int = 0,
    val height: Int = 0,
    val fileSize: Long = 0
) {
    val formattedDuration: String
        get() {
            if (type != GalleryItemType.VIDEO || duration <= 0) return ""
            val seconds = (duration / 1000) % 60
            val minutes = (duration / 1000 / 60) % 60
            val hours = duration / 1000 / 60 / 60
            return if (hours > 0) {
                String.format(java.util.Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(java.util.Locale.getDefault(), "%d:%02d", minutes, seconds)
            }
        }

    val formattedFileSize: String
        get() = if (fileSize <= 0) "" else fileSize.toFileSizeString()
}

data class GalleryCategory(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val icon: String = "folder",
    val color: String = "#6200EE",
    val sortIndex: Int = 0
)

enum class GalleryItemType {
    IMAGE,
    VIDEO
}

enum class GalleryPlayMode {
    SEQUENTIAL,
    SHUFFLE,
    SINGLE_LOOP
}

enum class GalleryViewMode {
    GRID,
    LIST,
    TIMELINE
}

enum class GallerySortOrder {
    CUSTOM,
    NAME_ASC,
    NAME_DESC,
    DATE_ASC,
    DATE_DESC,
    TYPE
}

fun sortedGalleryItems(items: List<GalleryItem>, order: GallerySortOrder): List<GalleryItem> =
    when (order) {
        GallerySortOrder.CUSTOM -> items.sortedBy { it.sortIndex }
        GallerySortOrder.NAME_ASC -> items.sortedBy { it.name.lowercase() }
        GallerySortOrder.NAME_DESC -> items.sortedByDescending { it.name.lowercase() }
        GallerySortOrder.DATE_ASC -> items.sortedBy { it.createdAt }
        GallerySortOrder.DATE_DESC -> items.sortedByDescending { it.createdAt }
        GallerySortOrder.TYPE -> items.sortedBy { it.type.ordinal }
    }

enum class NodeJsBuildMode {
    STATIC,
    SSR,
    API_BACKEND,
    FULLSTACK
}

data class NodeJsConfig(
    val projectId: String = "",
    val projectName: String = "",
    val sourceProjectPath: String = "",
    val framework: String = "",
    val buildMode: NodeJsBuildMode = NodeJsBuildMode.API_BACKEND,
    val entryFile: String = "index.js",
    val serverPort: Int = 0,
    val portConflictMode: PortConflictMode = PortConflictMode.AUTO_KILL,
    val envVars: Map<String, String> = emptyMap(),
    val hasNodeModules: Boolean = false,
    val nodeVersion: String = "",
    val customNodeExtensions: List<CustomNodeExtension> = emptyList()
)

data class WordPressConfig(
    val projectId: String = "",
    val projectName: String = "",
    val siteTitle: String = "My Site",
    val adminUser: String = "admin",
    val adminEmail: String = "",
    val adminPassword: String = "admin",
    val themeName: String = "",
    val plugins: List<String> = emptyList(),
    val activePlugins: List<String> = emptyList(),
    val permalinkStructure: String = "/%postname%/",
    val siteLanguage: String = "zh_CN",
    val autoInstall: Boolean = true,
    val sourceType: String = "BLANK",
    val sourceProjectId: String = "",
    val phpPort: Int = 0,
    val portConflictMode: PortConflictMode = PortConflictMode.AUTO_KILL,
    val customPhpExtensions: List<CustomPhpExtension> = emptyList()
)

data class PhpAppConfig(
    val projectId: String = "",
    val projectName: String = "",
    val framework: String = "",
    val documentRoot: String = "",
    val entryFile: String = "index.php",
    val phpPort: Int = 0,
    val portConflictMode: PortConflictMode = PortConflictMode.AUTO_KILL,
    val envVars: Map<String, String> = emptyMap(),
    val hasComposerJson: Boolean = false,
    val phpExtensions: Map<String, Boolean> = emptyMap(),
    val customPhpExtensions: List<CustomPhpExtension> = emptyList()
)

data class CustomPhpExtension(
    val name: String = "",
    val soFileName: String = "",
    val kind: Kind = Kind.EXTENSION,
    val enabled: Boolean = true,
    val loadOrder: Int = 0
) {
    enum class Kind {
        EXTENSION,
        ZEND_EXTENSION
    }

    fun effectiveSoName(): String = soFileName.takeIf { it.isNotBlank() } ?: "$name.so"
}

data class CustomPythonExtension(
    val name: String = "",
    val soFileName: String = "",
    val enabled: Boolean = true,
    val loadOrder: Int = 0
) {
    fun effectiveSoName(): String = soFileName.takeIf { it.isNotBlank() } ?: "${name}.so"
}

data class CustomNodeExtension(
    val name: String = "",
    val nodeFileName: String = "",
    val enabled: Boolean = true,
    val loadOrder: Int = 0
) {
    fun effectiveNodeName(): String = nodeFileName.takeIf { it.isNotBlank() } ?: "${name}.node"
}

data class PythonAppConfig(
    val projectId: String = "",
    val projectName: String = "",

    val sourceProjectPath: String = "",
    val framework: String = "",
    val entryFile: String = "app.py",
    val entryModule: String = "",
    val serverType: String = "builtin",
    val serverPort: Int = 0,
    val portConflictMode: PortConflictMode = PortConflictMode.AUTO_KILL,
    val envVars: Map<String, String> = emptyMap(),
    val pythonVersion: String = "",
    val requirementsFile: String = "requirements.txt",
    val hasPipDeps: Boolean = false,
    val customPythonExtensions: List<CustomPythonExtension> = emptyList()
)

data class GoAppConfig(
    val projectId: String = "",
    val projectName: String = "",
    val framework: String = "",
    val binaryName: String = "",
    val targetArch: String = "arm64",
    val serverPort: Int = 0,
    val portConflictMode: PortConflictMode = PortConflictMode.AUTO_KILL,
    val envVars: Map<String, String> = emptyMap(),
    val staticDir: String = ""
)

data class MultiWebConfig(
    val sites: List<MultiWebSite> = emptyList(),
    val displayMode: String = "TABS",
    val refreshInterval: Int = 30,
    val showSiteIcons: Boolean = true,
    val cardColumns: Int = 2,
    val drawerColumns: Int = 1,
    val drawerEntry: String = "FLOATING",
    val drawerFabAnchor: String = "TOP_END",
    val cardAspectRatio: Float = 1.2f,
    val sitesUseOwnConfig: Boolean = false,
    val projectId: String = "",
    val startTab: String = "LAST",
    val startSiteId: String = "",
    val independentSessions: Boolean = false
)

data class MultiWebSite(
    val id: String = "",
    val name: String = "",
    val url: String = "",
    val type: String = "URL",
    val localFilePath: String = "",
    @Transient val localFileUri: String = "",
    val inlineHtml: String = "",
    val sourceAppId: Long = 0,
    val sourceProjectId: String = "",
    val iconEmoji: String = "",
    val faviconUrl: String = "",
    val themeColor: String = "",
    val category: String = "",
    val cssSelector: String = "",
    val linkSelector: String = "",
    val enabled: Boolean = true,
    val sortIndex: Int = 0,
    val appType: String = "WEB",
    val htmlUsesFileScheme: Boolean = false,
    val webViewConfig: WebViewConfig? = null,
    val htmlConfig: HtmlConfig? = null,
    val nodejsConfig: NodeJsConfig? = null,
    val phpAppConfig: PhpAppConfig? = null,
    val pythonAppConfig: PythonAppConfig? = null,
    val goAppConfig: GoAppConfig? = null,
    val wordpressConfig: WordPressConfig? = null,
    val siteProjectId: String = ""
) {

    fun getEffectiveUrl(localBaseUrl: String = ""): String {
        return if ((type == "LOCAL" || type == "INLINE_HTML" || (type == "EXISTING" && localFilePath.isNotBlank())) && localFilePath.isNotBlank()) {
            val base = localBaseUrl.trimEnd('/')
            val path = localFilePath.trimStart('/')
            "$base/$path"
        } else {
            url
        }
    }
}

data class HtmlConfig(
    val projectId: String = "",
    val projectDir: String? = null,
    val entryFile: String = "index.html",
    val files: List<HtmlFile> = emptyList(),
    val enableJavaScript: Boolean = true,
    val enableLocalStorage: Boolean = true,
    val allowFileAccess: Boolean = true,
    val backgroundColor: String = "#FFFFFF",
    val loadMode: HtmlLoadMode = HtmlLoadMode.AUTO,
    val port: Int = 0,
    val portConflictMode: PortConflictMode = PortConflictMode.AUTO_KILL
) {
    fun getValidEntryFile(): String {
        return entryFile.takeIf {
            it.isNotBlank() && it.substringBeforeLast(".").isNotBlank()
        } ?: "index.html"
    }
}

data class HtmlFile(
    val name: String,
    val path: String,
    val type: HtmlFileType = HtmlFileType.OTHER
)

enum class HtmlFileType {
    HTML,
    CSS,
    JS,
    IMAGE,
    FONT,
    OTHER
}

enum class HtmlLoadMode {
    AUTO,
    FILE,
    LOCAL_HTTP
}

enum class PortConflictMode {
    AUTO_KILL,
    ALERT
}

enum class BgmPlayMode {
    LOOP,
    SEQUENTIAL,
    SHUFFLE
}

enum class BgmTag {
    PURE_MUSIC,
    POP,
    ROCK,
    CLASSICAL,
    JAZZ,
    ELECTRONIC,
    FOLK,
    CHINESE_STYLE,
    ANIME,
    GAME,
    MOVIE,
    HEALING,
    EXCITING,
    SAD,
    ROMANTIC,
    RELAXING,
    WORKOUT,
    SLEEP,
    STUDY,
    OTHER;

    val displayName: String get() = when (this) {
        PURE_MUSIC -> com.webtoapp.core.i18n.Strings.bgmTagPureMusic
        POP -> com.webtoapp.core.i18n.Strings.bgmTagPop
        ROCK -> com.webtoapp.core.i18n.Strings.bgmTagRock
        CLASSICAL -> com.webtoapp.core.i18n.Strings.bgmTagClassical
        JAZZ -> com.webtoapp.core.i18n.Strings.bgmTagJazz
        ELECTRONIC -> com.webtoapp.core.i18n.Strings.bgmTagElectronic
        FOLK -> com.webtoapp.core.i18n.Strings.bgmTagFolk
        CHINESE_STYLE -> com.webtoapp.core.i18n.Strings.bgmTagChineseStyle
        ANIME -> com.webtoapp.core.i18n.Strings.bgmTagAnime
        GAME -> com.webtoapp.core.i18n.Strings.bgmTagGame
        MOVIE -> com.webtoapp.core.i18n.Strings.bgmTagMovie
        HEALING -> com.webtoapp.core.i18n.Strings.bgmTagHealing
        EXCITING -> com.webtoapp.core.i18n.Strings.bgmTagExciting
        SAD -> com.webtoapp.core.i18n.Strings.bgmTagSad
        ROMANTIC -> com.webtoapp.core.i18n.Strings.bgmTagRomantic
        RELAXING -> com.webtoapp.core.i18n.Strings.bgmTagRelaxing
        WORKOUT -> com.webtoapp.core.i18n.Strings.bgmTagWorkout
        SLEEP -> com.webtoapp.core.i18n.Strings.bgmTagSleep
        STUDY -> com.webtoapp.core.i18n.Strings.bgmTagStudy
        OTHER -> com.webtoapp.core.i18n.Strings.bgmTagOther
    }
}

data class LrcLine(
    val startTime: Long,
    val endTime: Long,
    val text: String,
    val translation: String? = null
)

data class LrcData(
    val lines: List<LrcLine> = emptyList(),
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val language: String? = null
)

data class LrcTheme(
    val id: String,
    val name: String,
    val fontFamily: String = "default",
    val fontSize: Float = 18f,
    val textColor: String = "#FFFFFF",
    val highlightColor: String = "#FFD700",
    val backgroundColor: String = "#80000000",
    val strokeColor: String? = null,
    val strokeWidth: Float = 0f,
    val shadowEnabled: Boolean = true,
    val animationType: LrcAnimationType = LrcAnimationType.FADE,
    val position: LrcPosition = LrcPosition.BOTTOM,
    val showTranslation: Boolean = true
)

enum class LrcAnimationType {
    NONE, FADE, SLIDE_UP, SLIDE_LEFT, SCALE, TYPEWRITER, KARAOKE;

    val displayName: String get() = when (this) {
        NONE -> com.webtoapp.core.i18n.Strings.lrcAnimNone
        FADE -> com.webtoapp.core.i18n.Strings.lrcAnimFade
        SLIDE_UP -> com.webtoapp.core.i18n.Strings.lrcAnimSlideUp
        SLIDE_LEFT -> com.webtoapp.core.i18n.Strings.lrcAnimSlideLeft
        SCALE -> com.webtoapp.core.i18n.Strings.lrcAnimScale
        TYPEWRITER -> com.webtoapp.core.i18n.Strings.lrcAnimTypewriter
        KARAOKE -> com.webtoapp.core.i18n.Strings.lrcAnimKaraoke
    }
}

enum class LrcPosition {
    TOP, CENTER, BOTTOM;

    val displayName: String get() = when (this) {
        TOP -> com.webtoapp.core.i18n.Strings.lrcPosTop
        CENTER -> com.webtoapp.core.i18n.Strings.lrcPosCenter
        BOTTOM -> com.webtoapp.core.i18n.Strings.lrcPosBottom
    }
}

data class BgmItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val path: String,
    val coverPath: String? = null,
    val isAsset: Boolean = false,
    val tags: List<BgmTag> = emptyList(),
    val sortOrder: Int = 0,
    val lrcData: LrcData? = null,
    val lrcPath: String? = null,
    val duration: Long = 0
)

data class BgmConfig(
    val playlist: List<BgmItem> = emptyList(),
    val playMode: BgmPlayMode = BgmPlayMode.LOOP,
    val volume: Float = 0.5f,
    val autoPlay: Boolean = true,
    val showLyrics: Boolean = true,
    val lrcTheme: LrcTheme? = null,
    val showFloatingPlayer: Boolean? = null,
    val showNotificationPlayer: Boolean? = null
)

enum class ApkArchitecture(
    val abiFilters: List<String>
) {
    UNIVERSAL(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")),
    ARM64(listOf("arm64-v8a", "x86_64")),
    ARM32(listOf("armeabi-v7a", "x86"));

    val displayName: String get() = when (this) {
        UNIVERSAL -> com.webtoapp.core.i18n.Strings.archUniversal
        ARM64 -> com.webtoapp.core.i18n.Strings.archArm64
        ARM32 -> com.webtoapp.core.i18n.Strings.archArm32
    }

    val description: String get() = when (this) {
        UNIVERSAL -> com.webtoapp.core.i18n.Strings.archUniversalDesc
        ARM64 -> com.webtoapp.core.i18n.Strings.archArm64Desc
        ARM32 -> com.webtoapp.core.i18n.Strings.archArm32Desc
    }

    companion object {
        fun fromName(name: String): ApkArchitecture {
            return entries.find { it.name == name } ?: UNIVERSAL
        }
    }
}

data class ApkExportConfig(
    val customPackageName: String? = null,
    val customVersionName: String? = null,
    val customVersionCode: Int? = null,
    val architecture: ApkArchitecture = ApkArchitecture.UNIVERSAL,
    val runtimePermissions: ApkRuntimePermissions = ApkRuntimePermissions(),
    val autoEnabledPermissions: ApkRuntimePermissions = ApkRuntimePermissions(),
    val networkTrustConfig: NetworkTrustConfig = NetworkTrustConfig(),
    val encryptionConfig: ApkEncryptionConfig = ApkEncryptionConfig(),
    val isolationConfig: com.webtoapp.core.privacy.IsolationConfig = com.webtoapp.core.privacy.IsolationConfig(),
    val backgroundRunEnabled: Boolean = false,
    val backgroundRunConfig: BackgroundRunExportConfig = BackgroundRunExportConfig(),
    val engineType: String = "SYSTEM_WEBVIEW",
    val deepLinkEnabled: Boolean = false,
    val customDeepLinkHosts: List<String> = emptyList(),
    val performanceOptimization: Boolean = false,
    val performanceConfig: PerformanceOptimizationConfig = PerformanceOptimizationConfig(),
    val notificationEnabled: Boolean = false,
    val notificationConfig: NotificationExportConfig = NotificationExportConfig(),
    val loggingEnabled: Boolean = false,
    val saepEnabled: Boolean = false,
    val targetSdk: Int? = null,

    val minSdk: Int? = null,

    val perAppSigningEnabled: Boolean = false,

    val forceFullRebuild: Boolean = false,

    val iconBackgroundColor: String? = null,
    val preserveIconShape: Boolean = false,

    val autoVersionBump: Boolean = true
)

data class NetworkTrustConfig(
    val trustSystemCa: Boolean = true,
    val trustUserCa: Boolean = false,
    val customCaCertificates: List<CustomCaCertificate> = emptyList(),
    val cleartextTrafficPermitted: Boolean = true
)

data class CustomCaCertificate(
    val id: String,
    val displayName: String,
    val filePath: String,
    val sha256: String,
    val addedAt: Long = System.currentTimeMillis()
)

data class ApkRuntimePermissions(

    val camera: Boolean = false,
    val microphone: Boolean = false,
    val location: Boolean = false,
    val notifications: Boolean = false,

    val readExternalStorage: Boolean = false,
    val writeExternalStorage: Boolean = false,
    val readMediaImages: Boolean = false,
    val readMediaVideo: Boolean = false,
    val readMediaAudio: Boolean = false,

    val bluetooth: Boolean = false,
    val nfc: Boolean = false,
    val wifiState: Boolean = false,
    val nearbyWifiDevices: Boolean = false,

    val bodySensors: Boolean = false,
    val activityRecognition: Boolean = false,

    val readPhoneState: Boolean = false,
    val callPhone: Boolean = false,
    val readContacts: Boolean = false,
    val writeContacts: Boolean = false,
    val readCalendar: Boolean = false,
    val writeCalendar: Boolean = false,
    val readSms: Boolean = false,
    val sendSms: Boolean = false,
    val receiveSms: Boolean = false,
    val readCallLog: Boolean = false,
    val writeCallLog: Boolean = false,
    val processOutgoingCalls: Boolean = false,

    val foregroundService: Boolean = false,
    val wakeLock: Boolean = false,
    val requestIgnoreBatteryOptimizations: Boolean = false,
    val bootCompleted: Boolean = false,
    val vibration: Boolean = false,
    val installPackages: Boolean = false,
    val requestDeletePackages: Boolean = false,
    val systemAlertWindow: Boolean = false,
    val backgroundLocation: Boolean = false,
    val queryAllPackages: Boolean = false,
    val grants: Map<String, Boolean>? = null
) {
    fun manualBeyond(auto: ApkRuntimePermissions): ApkRuntimePermissions = copy(
        camera = camera && !auto.camera,
        microphone = microphone && !auto.microphone,
        location = location && !auto.location,
        notifications = notifications && !auto.notifications,
        readExternalStorage = readExternalStorage && !auto.readExternalStorage,
        writeExternalStorage = writeExternalStorage && !auto.writeExternalStorage,
        readMediaImages = readMediaImages && !auto.readMediaImages,
        readMediaVideo = readMediaVideo && !auto.readMediaVideo,
        readMediaAudio = readMediaAudio && !auto.readMediaAudio,
        bluetooth = bluetooth && !auto.bluetooth,
        nfc = nfc && !auto.nfc,
        wifiState = wifiState && !auto.wifiState,
        nearbyWifiDevices = nearbyWifiDevices && !auto.nearbyWifiDevices,
        bodySensors = bodySensors && !auto.bodySensors,
        activityRecognition = activityRecognition && !auto.activityRecognition,
        readPhoneState = readPhoneState && !auto.readPhoneState,
        callPhone = callPhone && !auto.callPhone,
        readContacts = readContacts && !auto.readContacts,
        writeContacts = writeContacts && !auto.writeContacts,
        readCalendar = readCalendar && !auto.readCalendar,
        writeCalendar = writeCalendar && !auto.writeCalendar,
        readSms = readSms && !auto.readSms,
        sendSms = sendSms && !auto.sendSms,
        receiveSms = receiveSms && !auto.receiveSms,
        readCallLog = readCallLog && !auto.readCallLog,
        writeCallLog = writeCallLog && !auto.writeCallLog,
        processOutgoingCalls = processOutgoingCalls && !auto.processOutgoingCalls,
        foregroundService = foregroundService && !auto.foregroundService,
        wakeLock = wakeLock && !auto.wakeLock,
        requestIgnoreBatteryOptimizations = requestIgnoreBatteryOptimizations && !auto.requestIgnoreBatteryOptimizations,
        bootCompleted = bootCompleted && !auto.bootCompleted,
        vibration = vibration && !auto.vibration,
        installPackages = installPackages && !auto.installPackages,
        requestDeletePackages = requestDeletePackages && !auto.requestDeletePackages,
        systemAlertWindow = systemAlertWindow && !auto.systemAlertWindow,
        backgroundLocation = backgroundLocation && !auto.backgroundLocation,
        queryAllPackages = queryAllPackages && !auto.queryAllPackages
    )
}

fun ApkExportConfig?.isMeaningful(): Boolean {
    if (this == null) return false

    val defaultConfig = ApkExportConfig()
    return this != defaultConfig
}

data class PerformanceOptimizationConfig(
    val compressImages: Boolean = true,
    val imageQuality: Int = 80,
    val convertToWebP: Boolean = true,
    val minifyCode: Boolean = true,
    val minifySvg: Boolean = true,
    val removeUnusedResources: Boolean = true
) {
    fun toOptimizerConfig(): com.webtoapp.core.linux.PerformanceOptimizer.OptimizeConfig {
        return com.webtoapp.core.linux.PerformanceOptimizer.OptimizeConfig(
            compressImages = compressImages,
            imageQuality = imageQuality,
            convertToWebP = convertToWebP,
            minifyCode = minifyCode,
            minifySvg = minifySvg,
            removeUnusedResources = removeUnusedResources
        )
    }
}

data class BackgroundRunExportConfig(
    val notificationTitle: String = "",
    val notificationContent: String = "",
    val keepCpuAwake: Boolean = true
)

data class ApkEncryptionConfig(
    val enabled: Boolean = false,
    val customPassword: String? = null,
    val keyMode: String? = null,
    val threatResponse: ThreatResponse = ThreatResponse.LOG_ONLY
) {
    enum class ThreatResponse {
        LOG_ONLY,
        SILENT_EXIT,
        CRASH_RANDOM,
        DATA_WIPE,
        FAKE_DATA;

        val displayName: String get() = when (this) {
            LOG_ONLY -> com.webtoapp.core.i18n.Strings.threatResponseLogOnly
            SILENT_EXIT -> com.webtoapp.core.i18n.Strings.threatResponseSilentExit
            CRASH_RANDOM -> com.webtoapp.core.i18n.Strings.threatResponseCrashRandom
            DATA_WIPE -> com.webtoapp.core.i18n.Strings.threatResponseDataWipe
            FAKE_DATA -> com.webtoapp.core.i18n.Strings.threatResponseFakeData
        }
    }

    companion object {
        const val KEY_MODE_SIGNATURE = "SIGNATURE"
        const val KEY_MODE_EMBEDDED = "EMBEDDED"
        val DISABLED = ApkEncryptionConfig(enabled = false)
    }

    fun toEncryptionConfig(): com.webtoapp.core.crypto.EncryptionConfig {
        return if (enabled) com.webtoapp.core.crypto.EncryptionConfig.MAXIMUM.copy(
            customPassword = customPassword,
            keyMode = keyMode ?: KEY_MODE_SIGNATURE
        )
        else com.webtoapp.core.crypto.EncryptionConfig.DISABLED
    }
}

    enum class TranslateLanguage(val code: String, val displayName: String) {
    CHINESE("zh-CN", "中文（简体）"),
    CHINESE_TW("zh-TW", "中文（繁體）"),
    ENGLISH("en", "English"),
    JAPANESE("ja", "日本語"),
    KOREAN("ko", "한국어"),
    FRENCH("fr", "Français"),
    GERMAN("de", "Deutsch"),
    SPANISH("es", "Español"),
    PORTUGUESE("pt", "Português"),
    RUSSIAN("ru", "Русский"),
    ARABIC("ar", "العربية"),
    HINDI("hi", "हिन्दी"),
    THAI("th", "ไทย"),
    VIETNAMESE("vi", "Tiếng Việt"),
    INDONESIAN("id", "Bahasa Indonesia"),
    MALAY("ms", "Bahasa Melayu"),
    TURKISH("tr", "Türkçe"),
    ITALIAN("it", "Italiano"),
    DUTCH("nl", "Nederlands"),
    POLISH("pl", "Polski")
}

enum class TranslateEngine {
    AUTO,
    GOOGLE,
    MYMEMORY,
    LIBRE,
    LINGVA;

    val displayName: String
        get() = when (this) {
            AUTO -> com.webtoapp.core.i18n.Strings.translateEngineAuto
            GOOGLE -> "Google Translate"
            MYMEMORY -> "MyMemory"
            LIBRE -> "LibreTranslate"
            LINGVA -> "Lingva Translate"
        }
}

data class TranslateConfig(
    val targetLanguage: TranslateLanguage = TranslateLanguage.CHINESE,
    val showFloatingButton: Boolean = true,
    val preferredEngine: TranslateEngine = TranslateEngine.AUTO,
    val autoTranslateOnLoad: Boolean = true
)

fun WebApp.getAllActivationCodes(): List<com.webtoapp.core.activation.ActivationCode> {
    return activationCodeList
}

fun WebApp.getActivationCodeStrings(): List<String> {
    return activationCodeList.map { it.toJson() }
}

data class ActivationDialogConfig(
    val title: String = "",
    val subtitle: String = "",
    val inputLabel: String = "",
    val buttonText: String = ""
)

enum class RemoteActivationOfflinePolicy {
    ALLOW_CACHED,
    DENY,
    ALLOW
}

enum class RemoteActivationTrustMode {
    SYSTEM,
    PINNED
}

fun parseRemoteActivationTrustMode(name: String?): RemoteActivationTrustMode =
    RemoteActivationTrustMode.entries.firstOrNull { it.name.equals(name?.trim(), ignoreCase = true) }
        ?: RemoteActivationTrustMode.SYSTEM

data class RemoteActivationConfig(
    val enabled: Boolean = false,
    val verifyUrl: String = "",
    val publicKeyBase64: String = "",
    val offlinePolicy: RemoteActivationOfflinePolicy = RemoteActivationOfflinePolicy.ALLOW_CACHED,
    val deliverUrl: Boolean = false,
    val encryptUrl: Boolean = false,
    val aesKeyBase64: String = "",
    val deviceBound: Boolean = false,
    val trustMode: RemoteActivationTrustMode = RemoteActivationTrustMode.SYSTEM,
    val pinnedCertificate: String = "",
    val pinnedCertHost: String = ""
)

data class AutoStartConfig(
    val bootStartEnabled: Boolean = false,
    val scheduledStartEnabled: Boolean = false,
    val scheduledTime: String = "08:00",
    val scheduledDays: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7),
    val scheduledRepeat: Boolean = true,
    val bootDelay: Long = 5000L
)

fun WebApp.toManifestJson(): String {
    return com.webtoapp.data.converter.Converters.gson.toJson(this)
}

object ManifestUtils {

    fun fromManifestJson(json: String, overrideId: Long? = null): WebApp? {
        return try {
            val parsed = com.google.gson.JsonParser.parseString(json)
            val defaultWebApp = WebApp(name = "", url = "")
            val defaultJson = com.webtoapp.data.converter.Converters.gson.toJsonTree(defaultWebApp)
            val merged = com.webtoapp.data.converter.Converters.mergeMissingDefaults(defaultJson, parsed)
            val restored = com.webtoapp.data.converter.Converters.gson.fromJson(merged, WebApp::class.java)
            if (overrideId != null) {
                restored?.copy(id = overrideId, updatedAt = System.currentTimeMillis())
            } else {
                restored
            }
        } catch (e: Exception) {
            null
        }
    }
}

enum class NotificationType(val key: String) {
    @com.google.gson.annotations.SerializedName("none")
    NONE("none"),
    @com.google.gson.annotations.SerializedName("web_api")
    WEB_API("web_api"),
    @com.google.gson.annotations.SerializedName("polling")
    POLLING("polling"),
    @com.google.gson.annotations.SerializedName("websocket")
    WEBSOCKET("websocket"),
    @com.google.gson.annotations.SerializedName("fcm")
    FCM("fcm")
}

data class NotificationExportConfig(
    val type: NotificationType = NotificationType.NONE,

    val pollUrl: String = "",

    val pollIntervalMinutes: Int = 15,

    val pollMethod: String = "GET",

    val pollHeaders: String = "",

    val clickUrl: String = "",

    val wsUrl: String = "",

    val wsHeaders: String = "",

    val registerUrl: String = "",

    val registerHeaders: String = "",

    val authToken: String = "",

    val fcmProjectId: String = "",

    val fcmApplicationId: String = "",

    val fcmApiKey: String = "",

    val fcmSenderId: String = "",

    val fcmGoogleServicesJson: String = ""
)

enum class Base64DeepLinkMode {

    GESTURE_ONLY,

    ALWAYS,
}

enum class ShareDeliveryMode {

    JS_EVENT,

    FILE_CHOOSER_PREFILL,

    BOTH,
}

enum class JsOpenWindowsPolicy {

    ALLOW,

    BLOCK,

    PROMPT,
}

enum class MediaAutoplayScope {

    VIDEO_ONLY,

    AUDIO_ONLY,

    BOTH,
}

enum class KernelDisguiseLevel {

    BASIC,

    STANDARD,

    DEEP,
}

enum class CloudflareCompatMode {

    AUTO_DETECT,

    ALWAYS_ON,
}

enum class MixedContentMode {

    NEVER,

    COMPATIBILITY,

    ALWAYS,
}

enum class PrivateNetworkScope {

    LOCAL_ONLY,

    ALL,

    CORS_BYPASS,
}

enum class ThirdPartyCookieMode {

    NONE,

    SAME_SITE_LAX,

    ALL,
}

data class NativeBridgeCapabilities(
    val clipboard: Boolean = true,
    val vibration: Boolean = true,
    val geolocation: Boolean = true,
    val brightness: Boolean = true,
    val notification: Boolean = false,
    val notificationScheduled: Boolean = true,
    val notificationPersistent: Boolean = true,
    val download: Boolean = true,
    val privateNetwork: Boolean = true,
    val screenWake: Boolean = true,
    val openExternal: Boolean = true,
    val deviceInfo: Boolean = true,
    val securityInfo: Boolean = true,
    val networkInfo: Boolean = true,
    val toast: Boolean = true,
    val logging: Boolean = true,
    val findInPage: Boolean = true,
    val orientation: Boolean = true,
    val fullscreen: Boolean = true,
    val print: Boolean = true,
    val googleSignIn: Boolean = false,
    val googleSignInClientId: String = "",
    val directory: Boolean = false,
)

fun NativeBridgeCapabilities.withNotificationPolyfill(polyfillEnabled: Boolean): NativeBridgeCapabilities =
    if (polyfillEnabled) copy(notification = true) else this

fun notificationPolyfillOnlyCapabilities(): NativeBridgeCapabilities = NativeBridgeCapabilities(
    clipboard = false,
    vibration = false,
    geolocation = false,
    brightness = false,
    notification = true,
    notificationScheduled = false,
    notificationPersistent = false,
    download = false,
    privateNetwork = false,
    screenWake = false,
    openExternal = false,
    deviceInfo = false,
    securityInfo = false,
    networkInfo = false,
    toast = false,
    logging = false,
    findInPage = false,
    orientation = false,
    fullscreen = false,
    print = false,
    googleSignIn = false,
    googleSignInClientId = "",
    directory = false,
)

enum class GeolocationAccuracy {

    COARSE,

    FINE,
}

enum class GeolocationPolicy {

    ALWAYS_ASK,

    REMEMBER_PER_HOST,

    DENY_ALL,
}

enum class BlobInterceptScope {

    ALL,

    SIZE_OVER_THRESHOLD,
}

enum class DownloadLocationMode {

    SYSTEM_DOWNLOAD,

    APP_PRIVATE,

    CUSTOM,

    ASK
}

enum class PrimeUserActivationMode {

    SYNTHETIC_TAP,

    DPAD_OK,

    BOTH,
}

enum class PrimeUserActivationTiming {

    ON_PAGE_FINISHED,

    ON_FIRST_VISIBLE,
}

data class FailoverTriggers(

    val networkError: Boolean = true,

    val http5xx: Boolean = true,

    val http4xx: Boolean = false,

    val timeout: Boolean = false,
)

enum class FullscreenVideoOrientation {

    AUTO_SENSOR_LANDSCAPE,

    FORCE_LANDSCAPE,

    KEEP_CURRENT,
}
