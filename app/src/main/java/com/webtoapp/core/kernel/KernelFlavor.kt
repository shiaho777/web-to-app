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
    WEBKIT_SAFARI("Safari (WebKit)", "WebKit"),

    BLINK_CHROME_DESKTOP("Chrome Desktop (Blink)", "Blink"),
    BLINK_EDGE_DESKTOP("Edge Desktop (Blink)", "Blink"),
    GECKO_FIREFOX_DESKTOP("Firefox Desktop (Gecko)", "Gecko"),
    WEBKIT_SAFARI_DESKTOP("Safari Desktop (WebKit)", "WebKit");

    /** Desktop variants present themselves as a desktop browser instead of a mobile one. */
    val isDesktop: Boolean
        get() = this in DESKTOP_FLAVORS

    val profile: KernelFlavorProfile
        get() = KernelFlavorProfile.of(this)

    companion object {

        val DESKTOP_FLAVORS: Set<KernelFlavor> = setOf(
            BLINK_CHROME_DESKTOP,
            BLINK_EDGE_DESKTOP,
            GECKO_FIREFOX_DESKTOP,
            WEBKIT_SAFARI_DESKTOP
        )

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

    /**
     * JS the page (and its iframes) must see before their own scripts run.
     *
     * Derived identities from device disguise keep [flavor] = [KernelFlavor.SYSTEM_DEFAULT]
     * but still carry a Chrome UA. Those used to emit nothing, so a challenge iframe saw a
     * Chrome User-Agent with a WebView `window.chrome` / `navigator.webdriver`.
     */
    fun buildFlavorJs(): String {
        if (isNoOp && userAgent.isNullOrBlank()) return ""

        val formFactorsJs = if (mobile) "['Mobile']" else "['Desktop']"
        val productSub = if (hasWindowChrome) "20030107" else "20100101"
        val uaLiteral = userAgent?.takeIf { it.isNotBlank() }?.let { jsString(it) }
        val appVersionLiteral = userAgent?.takeIf { it.isNotBlank() }
            ?.removePrefix("Mozilla/")
            ?.let { jsString(it) }

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
                getHighEntropyValues:_mn(function(hints){
                    return Promise.resolve({
                        brands:uadBrands,
                        mobile:$mobile,
                        platform:${jsString(platform)},
                        platformVersion:${jsString(platformVersion)},
                        architecture:${jsString(architecture)},
                        bitness:${jsString(bitness)},
                        model:${jsString(model)},
                        uaFullVersion:${jsString(fullVersion)},
                        fullVersionList:uadFullList,
                        wow64:false,
                        formFactors:$formFactorsJs
                    });
                }),
                toJSON:_mn(function(){return {brands:uadBrands,mobile:$mobile,platform:${jsString(platform)}};} )
            };
            _proto(Navigator,'userAgentData',function(){return uadObj;});
            """
        } else {
            """
            _proto(Navigator,'userAgentData',function(){return undefined;});
            """
        }

        val windowChromeJs = if (hasWindowChrome) {
            """
            if(!window.chrome)window.chrome={};
            if(!window.chrome.app)window.chrome.app={
                isInstalled:false,
                InstallState:{DISABLED:'disabled',INSTALLED:'installed',NOT_INSTALLED:'not_installed'},
                RunningState:{CANNOT_RUN:'cannot_run',READY_TO_RUN:'ready_to_run',RUNNING:'running'},
                getDetails:_mn(function(){return null;}),
                getIsInstalled:_mn(function(){return false;}),
                installState:_mn(function(cb){if(cb)cb('not_installed');return 'not_installed';})
            };
            if(!window.chrome.runtime)window.chrome.runtime={
                OnInstalledReason:{CHROME_UPDATE:'chrome_update',INSTALL:'install',SHARED_MODULE_UPDATE:'shared_module_update',UPDATE:'update'},
                OnRestartRequiredReason:{APP_UPDATE:'app_update',OS_UPDATE:'os_update',PERIODIC:'periodic'},
                PlatformArch:{ARM:'arm',ARM64:'arm64',MIPS:'mips',MIPS64:'mips64',X86_32:'x86-32',X86_64:'x86-64'},
                PlatformOs:{ANDROID:'android',CROS:'cros',LINUX:'linux',MAC:'mac',WIN:'win'},
                connect:_mn(function(){return{onDisconnect:{addListener:function(){}},onMessage:{addListener:function(){}},postMessage:function(){},disconnect:function(){}};}),
                sendMessage:_mn(function(){}),
                id:undefined
            };
            if(!window.chrome.loadTimes){
                window.chrome.loadTimes=_mn(function(){
                    var n=performance.now()/1000;
                    return{requestTime:n-0.3,startLoadTime:n-0.25,commitLoadTime:n-0.1,
                        finishDocumentLoadTime:n-0.05,finishLoadTime:n,firstPaintTime:n-0.08,
                        firstPaintAfterLoadTime:0,navigationType:'Other',
                        wasFetchedViaSpdy:true,wasNpnNegotiated:true,npnNegotiatedProtocol:'h2',
                        wasAlternateProtocolAvailable:false,connectionInfo:'h2'};
                });
            }
            if(!window.chrome.csi){
                window.chrome.csi=_mn(function(){return{onloadT:Date.now(),startE:Date.now()-300,pageT:performance.now(),tran:15};});
            }
            _proto(Navigator,'pdfViewerEnabled',function(){return true;});
            _proto(Navigator,'productSub',function(){return '$productSub';});
            $chromePluginSpoofJs
            """
        } else {
            """
            try{delete window.chrome;}catch(e){}
            _proto(Window,'chrome',function(){return undefined;});
            """
        }

        val uaJs = if (uaLiteral != null && appVersionLiteral != null) {
            """
            _proto(Navigator,'userAgent',function(){return $uaLiteral;});
            _proto(Navigator,'appVersion',function(){return $appVersionLiteral;});
            """
        } else {
            ""
        }

        return """(function(){'use strict';
            var KEY=Symbol.for('wta.kf');
            if(window[KEY])return;
            try{Object.defineProperty(window,KEY,{value:1,enumerable:false,configurable:false});}catch(e){window[KEY]=1;}

            var _ots=Function.prototype.toString;
            var _ht=typeof WeakSet==='function'?new WeakSet():null;
            var _nativeToString=function(){
                if(_ht&&_ht.has(this))return'function '+(this.name||'')+'() { [native code] }';
                return _ots.call(this);
            };
            if(_ht)_ht.add(_nativeToString);
            try{Object.defineProperty(Function.prototype,'toString',{value:_nativeToString,writable:true,configurable:true});}catch(e){}
            function _mn(fn){if(_ht)_ht.add(fn);return fn;}
            function _proto(ctor,prop,getter){
                var g=_mn(getter);
                try{Object.defineProperty(ctor.prototype,prop,{get:g,enumerable:true,configurable:true});return;}catch(e){}
                try{Object.defineProperty(ctor===Navigator?navigator:ctor===Window?window:navigator,prop,{get:g,enumerable:true,configurable:true});}catch(e2){}
            }

            _proto(Navigator,'webdriver',function(){return false;});
            try{delete window.__selenium_unwrapped;delete window.__webdriver_evaluate;delete window.__webdriver_script_function;delete window.domAutomation;delete window.domAutomationController;}catch(e){}
            $uaJs
            _proto(Navigator,'vendor',function(){return ${jsString(vendor)};});
            $windowChromeJs
            $clientHintsJs
            if(!window.outerWidth){_proto(Window,'outerWidth',function(){return window.innerWidth;});}
            if(!window.outerHeight){_proto(Window,'outerHeight',function(){return window.innerHeight;});}
        })();""".trimIndent()
    }

    private val chromePluginSpoofJs: String
        get() = """
            (function(){
                var pdf={type:'application/pdf',suffixes:'pdf',description:'Portable Document Format',enabledPlugin:null};
                function mkPlugin(name){
                    var p={name:name,description:'Portable Document Format',filename:'internal-pdf-viewer',length:1,0:pdf};
                    p[pdf.type]=pdf;
                    if(typeof Plugin!=='undefined')try{Object.setPrototypeOf(p,Plugin.prototype);}catch(e){}
                    return p;
                }
                var list=[mkPlugin('PDF Viewer'),mkPlugin('Chrome PDF Viewer'),mkPlugin('Chromium PDF Viewer')];
                pdf.enabledPlugin=list[0];
                var plugins={length:list.length,item:_mn(function(i){return list[i]||null;}),namedItem:_mn(function(n){for(var i=0;i<list.length;i++)if(list[i].name===n)return list[i];return null;}),refresh:_mn(function(){})};
                var mimes={length:1,item:_mn(function(i){return i===0?pdf:null;}),namedItem:_mn(function(n){return n==='application/pdf'?pdf:null;})};
                for(var i=0;i<list.length;i++)plugins[i]=list[i];
                mimes[0]=pdf;mimes['application/pdf']=pdf;
                if(typeof PluginArray!=='undefined')try{Object.setPrototypeOf(plugins,PluginArray.prototype);}catch(e){}
                if(typeof MimeTypeArray!=='undefined')try{Object.setPrototypeOf(mimes,MimeTypeArray.prototype);}catch(e){}
                _proto(Navigator,'plugins',function(){return plugins;});
                _proto(Navigator,'mimeTypes',function(){return mimes;});
            })();
        """.trimIndent()

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
                    KernelBrand("Not_A Brand", "24", "24.0.0.0"),
                    KernelBrand("Chromium", UserAgentVersions.CHROME, "${UserAgentVersions.CHROME}.0.0.0"),
                    KernelBrand("Google Chrome", UserAgentVersions.CHROME, "${UserAgentVersions.CHROME}.0.0.0")
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
                    KernelBrand("Not_A Brand", "24", "24.0.0.0"),
                    KernelBrand("Chromium", UserAgentVersions.CHROME, "${UserAgentVersions.CHROME}.0.0.0"),
                    KernelBrand("Microsoft Edge", UserAgentVersions.CHROME, "${UserAgentVersions.CHROME}.0.0.0")
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
                    KernelBrand("Not_A Brand", "24", "24.0.0.0"),
                    KernelBrand("Chromium", UserAgentVersions.CHROME, "${UserAgentVersions.CHROME}.0.0.0"),
                    KernelBrand("Samsung Internet", "27", "27.0.0.0")
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

            KernelFlavor.BLINK_CHROME_DESKTOP -> KernelFlavorProfile(
                flavor = flavor,
                userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/${UserAgentVersions.CHROME}.0.0.0 Safari/537.36",
                vendor = "Google Inc.",
                hasWindowChrome = true,
                supportsClientHints = true,
                brands = listOf(
                    KernelBrand("Not_A Brand", "24", "24.0.0.0"),
                    KernelBrand("Chromium", UserAgentVersions.CHROME, "${UserAgentVersions.CHROME}.0.0.0"),
                    KernelBrand("Google Chrome", UserAgentVersions.CHROME, "${UserAgentVersions.CHROME}.0.0.0")
                ),
                mobile = false,
                platform = "Windows",
                platformVersion = "10.0.0",
                fullVersion = "${UserAgentVersions.CHROME}.0.0.0",
                architecture = "x86",
                bitness = "64",
                model = ""
            )

            KernelFlavor.BLINK_EDGE_DESKTOP -> KernelFlavorProfile(
                flavor = flavor,
                userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/${UserAgentVersions.CHROME}.0.0.0 Safari/537.36 Edg/${UserAgentVersions.CHROME}.0.0.0",
                vendor = "Google Inc.",
                hasWindowChrome = true,
                supportsClientHints = true,
                brands = listOf(
                    KernelBrand("Not_A Brand", "24", "24.0.0.0"),
                    KernelBrand("Chromium", UserAgentVersions.CHROME, "${UserAgentVersions.CHROME}.0.0.0"),
                    KernelBrand("Microsoft Edge", UserAgentVersions.CHROME, "${UserAgentVersions.CHROME}.0.0.0")
                ),
                mobile = false,
                platform = "Windows",
                platformVersion = "10.0.0",
                fullVersion = "${UserAgentVersions.CHROME}.0.0.0",
                architecture = "x86",
                bitness = "64",
                model = ""
            )

            KernelFlavor.GECKO_FIREFOX_DESKTOP -> KernelFlavorProfile(
                flavor = flavor,
                userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:${UserAgentVersions.FIREFOX}.0) Gecko/20100101 Firefox/${UserAgentVersions.FIREFOX}.0",
                vendor = "",
                hasWindowChrome = false,
                supportsClientHints = false,
                brands = emptyList(),
                mobile = false,
                platform = "Windows",
                platformVersion = "10.0.0",
                fullVersion = "",
                architecture = "",
                bitness = "64",
                model = ""
            )

            KernelFlavor.WEBKIT_SAFARI_DESKTOP -> KernelFlavorProfile(
                flavor = flavor,
                userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 15_0) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/${UserAgentVersions.SAFARI}.0 Safari/605.1.15",
                vendor = "Apple Computer, Inc.",
                hasWindowChrome = false,
                supportsClientHints = false,
                brands = emptyList(),
                mobile = false,
                platform = "macOS",
                platformVersion = "15.0.0",
                fullVersion = "",
                architecture = "",
                bitness = "64",
                model = ""
            )
        }
    }
}
