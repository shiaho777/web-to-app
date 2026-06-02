package com.webtoapp.core.kernel

import com.webtoapp.data.model.UserAgentVersions

enum class KernelFlavor(
    val displayName: String,
    val engineFamily: String
) {
    SYSTEM_DEFAULT("System Default", "Native"),
    BLINK_CHROME("Chrome (Blink)", "Blink"),
    BLINK_EDGE("Edge (Blink)", "Blink"),
    BLINK_SAMSUNG("Samsung Internet (Blink)", "Blink"),
    GECKO_FIREFOX("Firefox (Gecko)", "Gecko"),
    WEBKIT_SAFARI("Safari (WebKit)", "WebKit");

    val profile: KernelFlavorProfile
        get() = KernelFlavorProfile.of(this)

    companion object {
        fun fromString(value: String?): KernelFlavor {
            if (value.isNullOrBlank()) return SYSTEM_DEFAULT
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: SYSTEM_DEFAULT
        }
    }
}

data class KernelBrand(
    val brand: String,
    val majorVersion: String,
    val fullVersion: String
)

data class KernelFlavorProfile(
    val flavor: KernelFlavor,
    val userAgent: String?,
    val vendor: String,
    val hasWindowChrome: Boolean,

    val supportsClientHints: Boolean,
    val brands: List<KernelBrand>,
    val mobile: Boolean,
    val platform: String,
    val platformVersion: String,
    val fullVersion: String,
    val architecture: String,
    val bitness: String,
    val model: String
) {
    val isNoOp: Boolean
        get() = flavor == KernelFlavor.SYSTEM_DEFAULT

    fun buildFlavorJs(): String {
        if (isNoOp) return ""

        val clientHintsJs = if (supportsClientHints && brands.isNotEmpty()) {
            val brandsArrayJs = brands.joinToString(",") { b ->
                "{brand:${jsString(b.brand)},version:${jsString(b.majorVersion)}}"
            }
            val fullVersionListJs = brands.joinToString(",") { b ->
                "{brand:${jsString(b.brand)},version:${jsString(b.fullVersion)}}"
            }
            """
            var uadBrands=[$brandsArrayJs];
            var uadFullList=[$fullVersionListJs];
            var uadObj={
                brands:uadBrands,
                mobile:$mobile,
                platform:${jsString(platform)},
                getHighEntropyValues:function(hints){
                    return Promise.resolve({
                        brands:uadBrands,
                        mobile:$mobile,
                        platform:${jsString(platform)},
                        platformVersion:${jsString(platformVersion)},
                        architecture:${jsString(architecture)},
                        bitness:${jsString(bitness)},
                        model:${jsString(model)},
                        uaFullVersion:${jsString(fullVersion)},
                        fullVersionList:uadFullList
                    });
                },
                toJSON:function(){return {brands:uadBrands,mobile:$mobile,platform:${jsString(platform)}};}
            };
            try{Object.defineProperty(navigator,'userAgentData',{get:function(){return uadObj;},configurable:true});}catch(e){}
            """
        } else {
            """
            try{Object.defineProperty(navigator,'userAgentData',{get:function(){return undefined;},configurable:true});}catch(e){}
            """
        }

        val windowChromeJs = if (hasWindowChrome) {
            """
            if(!window.chrome){window.chrome={};}
            if(!window.chrome.runtime){window.chrome.runtime={
                OnInstalledReason:{CHROME_UPDATE:'chrome_update',INSTALL:'install'},
                connect:function(){return{onDisconnect:{addListener:function(){}},postMessage:function(){},disconnect:function(){}};}
            };}
            if(!window.chrome.loadTimes){window.chrome.loadTimes=function(){return{commitLoadTime:Date.now()/1000,firstPaintTime:Date.now()/1000,navigationType:'Other'};};}
            if(!window.chrome.csi){window.chrome.csi=function(){return{onloadT:Date.now(),pageT:performance.now(),tran:15};};}
            """
        } else {
            """
            try{delete window.chrome;}catch(e){}
            try{Object.defineProperty(window,'chrome',{get:function(){return undefined;},configurable:true});}catch(e){}
            """
        }

        val uaJs = if (!userAgent.isNullOrBlank()) {
            "try{Object.defineProperty(navigator,'userAgent',{get:function(){return ${jsString(userAgent)};},configurable:true});}catch(e){}"
        } else {
            ""
        }

        return """(function(){'use strict';
            if(window.__wta_kernel_flavor__)return;
            window.__wta_kernel_flavor__='${flavor.name}';
            $uaJs
            try{Object.defineProperty(navigator,'vendor',{get:function(){return ${jsString(vendor)};},configurable:true});}catch(e){}
            $windowChromeJs
            $clientHintsJs
        })();""".trimIndent()
    }

    private fun jsString(value: String): String {
        val escaped = value
            .replace("\\", "\\\\")
            .replace("'", "\\'")
        return "'$escaped'"
    }

    companion object {
        fun of(flavor: KernelFlavor): KernelFlavorProfile = when (flavor) {
            KernelFlavor.SYSTEM_DEFAULT -> KernelFlavorProfile(
                flavor = flavor,
                userAgent = null,
                vendor = "Google Inc.",
                hasWindowChrome = true,
                supportsClientHints = true,
                brands = emptyList(),
                mobile = true,
                platform = "Android",
                platformVersion = "15.0.0",
                fullVersion = "",
                architecture = "",
                bitness = "64",
                model = ""
            )

            KernelFlavor.BLINK_CHROME -> KernelFlavorProfile(
                flavor = flavor,
                userAgent = "Mozilla/5.0 (Linux; Android 15; Pixel 9 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/${UserAgentVersions.CHROME}.0.0.0 Mobile Safari/537.36",
                vendor = "Google Inc.",
                hasWindowChrome = true,
                supportsClientHints = true,
                brands = listOf(
                    KernelBrand("Chromium", UserAgentVersions.CHROME, "${UserAgentVersions.CHROME}.0.0.0"),
                    KernelBrand("Google Chrome", UserAgentVersions.CHROME, "${UserAgentVersions.CHROME}.0.0.0"),
                    KernelBrand("Not_A Brand", "24", "24.0.0.0")
                ),
                mobile = true,
                platform = "Android",
                platformVersion = "15.0.0",
                fullVersion = "${UserAgentVersions.CHROME}.0.0.0",
                architecture = "",
                bitness = "64",
                model = "Pixel 9 Pro"
            )

            KernelFlavor.BLINK_EDGE -> KernelFlavorProfile(
                flavor = flavor,
                userAgent = "Mozilla/5.0 (Linux; Android 15; Pixel 9 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/${UserAgentVersions.CHROME}.0.0.0 Mobile Safari/537.36 EdgA/${UserAgentVersions.CHROME}.0.0.0",
                vendor = "Google Inc.",
                hasWindowChrome = true,
                supportsClientHints = true,
                brands = listOf(
                    KernelBrand("Chromium", UserAgentVersions.CHROME, "${UserAgentVersions.CHROME}.0.0.0"),
                    KernelBrand("Microsoft Edge", UserAgentVersions.CHROME, "${UserAgentVersions.CHROME}.0.0.0"),
                    KernelBrand("Not_A Brand", "24", "24.0.0.0")
                ),
                mobile = true,
                platform = "Android",
                platformVersion = "15.0.0",
                fullVersion = "${UserAgentVersions.CHROME}.0.0.0",
                architecture = "",
                bitness = "64",
                model = "Pixel 9 Pro"
            )

            KernelFlavor.BLINK_SAMSUNG -> KernelFlavorProfile(
                flavor = flavor,
                userAgent = "Mozilla/5.0 (Linux; Android 15; SM-S931B) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/27.0 Chrome/${UserAgentVersions.CHROME}.0.0.0 Mobile Safari/537.36",
                vendor = "Google Inc.",
                hasWindowChrome = true,
                supportsClientHints = true,
                brands = listOf(
                    KernelBrand("Chromium", UserAgentVersions.CHROME, "${UserAgentVersions.CHROME}.0.0.0"),
                    KernelBrand("Samsung Internet", "27", "27.0.0.0"),
                    KernelBrand("Not_A Brand", "24", "24.0.0.0")
                ),
                mobile = true,
                platform = "Android",
                platformVersion = "15.0.0",
                fullVersion = "${UserAgentVersions.CHROME}.0.0.0",
                architecture = "",
                bitness = "64",
                model = "SM-S931B"
            )

            KernelFlavor.GECKO_FIREFOX -> KernelFlavorProfile(
                flavor = flavor,
                userAgent = "Mozilla/5.0 (Android 15; Mobile; rv:${UserAgentVersions.FIREFOX}.0) Gecko/${UserAgentVersions.FIREFOX}.0 Firefox/${UserAgentVersions.FIREFOX}.0",
                vendor = "",
                hasWindowChrome = false,
                supportsClientHints = false,
                brands = emptyList(),
                mobile = true,
                platform = "Android",
                platformVersion = "15.0.0",
                fullVersion = "",
                architecture = "",
                bitness = "64",
                model = ""
            )

            KernelFlavor.WEBKIT_SAFARI -> KernelFlavorProfile(
                flavor = flavor,
                userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 18_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/${UserAgentVersions.SAFARI}.0 Mobile/15E148 Safari/604.1",
                vendor = "Apple Computer, Inc.",
                hasWindowChrome = false,
                supportsClientHints = false,
                brands = emptyList(),
                mobile = true,
                platform = "iOS",
                platformVersion = "18.0.0",
                fullVersion = "",
                architecture = "",
                bitness = "64",
                model = "iPhone"
            )
        }
    }
}
