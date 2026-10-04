package com.webtoapp.core.privacy

object IsolationScriptInjector {

    fun generateIsolationScript(
        config: IsolationConfig,
        fingerprint: GeneratedFingerprint
    ): String {
        if (!config.enabled) return ""

        val scripts = mutableListOf<String>()

        scripts.add(generateSeededPrng(fingerprint.canvasNoiseSeed))

        if (config.fingerprintConfig.randomize) {
            scripts.add(generateNavigatorScript(config, fingerprint))
        }

        if (config.protectCanvas) {
            scripts.add(generateCanvasProtectionScript(fingerprint))
        }

        if (config.protectWebGL) {
            scripts.add(generateWebGLProtectionScript(fingerprint))
        }

        if (config.protectAudio) {
            scripts.add(generateAudioProtectionScript(fingerprint))
        }

        if (config.blockWebRTC) {
            scripts.add(generateWebRTCProtectionScript())
        }

        if (config.protectFonts) {
            scripts.add(generateFontProtectionScript())
        }

        if (config.spoofLanguage && !config.fingerprintConfig.randomize) {
            scripts.add(generateLanguageSpoofScript(resolveLanguages(config, fingerprint)))
        }

        if (config.spoofScreen) {
            scripts.add(generateScreenSpoofScript(
                config.customScreenWidth ?: fingerprint.screenWidth,
                config.customScreenHeight ?: fingerprint.screenHeight,
                config.customDevicePixelRatio ?: 1.0f,
                fingerprint.colorDepth
            ))
        }

        if (config.spoofLanguage || config.spoofTimezone) {
            scripts.add(generateEnvSpoofScript(
                localeTag = if (config.spoofLanguage) resolveLanguages(config, fingerprint).first else null,
                timezone = if (config.spoofTimezone) (config.customTimezone ?: fingerprint.timezone) else null
            ))
        }

        if (config.protectCanvas) {
            scripts.add(generateClientRectsNoiseScript())
        }

        scripts.add(generatePerformanceTimingScript())

        if (config.fingerprintConfig.randomize) {
            scripts.add(generatePluginsSpoofScript(fingerprint))
        }

        scripts.add(generateBatteryProtectionScript())

        scripts.add(generateConnectionSpoofScript())

        scripts.add(generateMediaDevicesProtectionScript())

        scripts.add(generateStorageEstimateScript())

        scripts.add(generateHistoryProtectionScript())

        scripts.add(generatePermissionsProtectionScript())

        return """
            (function() {
                'use strict';
                if(window.__wta_isolation_v2__)return;
                window.__wta_isolation_v2__=true;
                try {
                    ${scripts.joinToString("\n\n")}
                } catch(e) {
                    console.error('[WebToApp] isolation script error:', e);
                }
            })();
        """.trimIndent()
    }

    private fun generateSeededPrng(seed: Long): String {
        return """
            // Seeded PRNG (mulberry32) — deterministic noise per session
            var __wta_seed__ = ${seed.toUInt()};
            function __wta_prng__() {
                __wta_seed__ |= 0; __wta_seed__ = __wta_seed__ + 0x6D2B79F5 | 0;
                var t = Math.imul(__wta_seed__ ^ __wta_seed__ >>> 15, 1 | __wta_seed__);
                t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t;
                return ((t ^ t >>> 14) >>> 0) / 4294967296;
            }
        """.trimIndent()
    }

    /** 解析 (primaryTag, languages[]) —— 语言伪装开启时优先用户选定标签，否则取随机指纹值。 */
    private fun resolveLanguages(config: IsolationConfig, fp: GeneratedFingerprint): Pair<String, List<String>> {
        val custom = config.customLanguage
        if (config.spoofLanguage && !custom.isNullOrBlank()) {
            val tag = custom.trim()
            return tag to IsolationPresets.languageList(tag)
        }
        val primary = fp.language.split(",").first().split(";").first().trim()
        val arr = fp.language.split(",").map { it.split(";").first().trim() }
        return primary to arr
    }

    /** 仅输出 JS 字符串字面量安全的字符，防止配置值破坏注入脚本。 */
    private fun jsStr(raw: String): String =
        raw.replace("\\", "\\\\").replace("'", "\\'").replace("\n", " ").replace("\r", " ")

    private fun generateNavigatorScript(config: IsolationConfig, fp: GeneratedFingerprint): String {
        val userAgent = jsStr(config.fingerprintConfig.customUserAgent ?: fp.userAgent)
        val platform = jsStr(config.fingerprintConfig.platform ?: fp.platform)
        val vendor = jsStr(config.fingerprintConfig.vendor ?: fp.vendor)
        val hardwareConcurrency = config.fingerprintConfig.hardwareConcurrency ?: fp.hardwareConcurrency
        val deviceMemory = config.fingerprintConfig.deviceMemory ?: fp.deviceMemory
        val (primaryLang, langArray) = resolveLanguages(config, fp)

        // Follow the fingerprint. A hardcoded false made every profile look
        // like a desktop browser, so sites rendered the desktop page.
        val clientHintsMobile = fp.chUaMobile.contains('1')
        val clientHintsJs = if (fp.chUa.isNotEmpty()) {
            """
            // navigator.userAgentData (Client Hints API)
            var uadBrands = [${fp.chUa.split(", ").joinToString(",") { brand ->
                val parts = brand.split(";v=")
                if (parts.size == 2) "{brand:${parts[0]},version:${parts[1]}}" else ""
            }}];
            var uadObj = {
                brands: uadBrands,
                mobile: $clientHintsMobile,
                platform: ${fp.chUaPlatform},
                getHighEntropyValues: function(hints) {
                    return Promise.resolve({
                        brands: uadBrands,
                        mobile: $clientHintsMobile,
                        platform: ${fp.chUaPlatform},
                        platformVersion: ${fp.chUaPlatformVersion},
                        architecture: ${fp.chUaArch},
                        bitness: ${fp.chUaBitness},
                        model: ${fp.chUaModel},
                        uaFullVersion: ${fp.chUaFullVersion},
                        fullVersionList: uadBrands
                    });
                },
                toJSON: function() {
                    return {brands:uadBrands,mobile:$clientHintsMobile,platform:${fp.chUaPlatform}};
                }
            };
            try{Object.defineProperty(navigator,'userAgentData',{get:function(){return uadObj;},configurable:true});}catch(e){/* expected */}
            """
        } else {
            """
            // Firefox/Safari: no userAgentData — delete it if present
            try{Object.defineProperty(navigator,'userAgentData',{get:function(){return undefined;},configurable:true});}catch(e){/* expected */}
            """
        }

        return """
            // Navigator property spoofing
            var navProps = {
                userAgent: '$userAgent',
                appVersion: '${fp.appVersion}',
                platform: '$platform',
                vendor: '$vendor',
                hardwareConcurrency: $hardwareConcurrency,
                deviceMemory: $deviceMemory,
                language: '${jsStr(primaryLang)}',
                languages: Object.freeze(${langArray.joinToString(",", "[", "]") { "'${jsStr(it)}'" }}),
                maxTouchPoints: ${fp.maxTouchPoints},
                webdriver: false,
                doNotTrack: '1',
                pdfViewerEnabled: true,
                cookieEnabled: true
            };
            Object.keys(navProps).forEach(function(p){
                try{Object.defineProperty(navigator,p,{get:function(){return navProps[p];},configurable:true});}catch(e){/* expected */}
            });

            // Remove automation markers
            try{
                delete window.cdc_adoQpoasnfa76pfcZLmcfl_Array;
                delete window.cdc_adoQpoasnfa76pfcZLmcfl_Promise;
                delete window.cdc_adoQpoasnfa76pfcZLmcfl_Symbol;
                delete window.__webdriver_evaluate;
                delete window.__selenium_evaluate;
                delete window.__fxdriver_evaluate;
                delete window.__driver_evaluate;
                delete window.__webdriver_unwrap;
                delete window.__selenium_unwrap;
                delete window.__fxdriver_unwrap;
                delete document.__webdriver_evaluate;
                delete document.__selenium_evaluate;
                delete document.__fxdriver_evaluate;
            }catch(e){/* expected */}

            $clientHintsJs
        """.trimIndent()
    }

    private fun generateCanvasProtectionScript(fp: GeneratedFingerprint): String {
        return """
            // Canvas fingerprint — deterministic seed-based noise
            var origToDataURL = HTMLCanvasElement.prototype.toDataURL;
            var origGetImageData = CanvasRenderingContext2D.prototype.getImageData;
            var origToBlob = HTMLCanvasElement.prototype.toBlob;

            function __wta_canvas_noise__(data) {
                // Save/restore PRNG state for determinism
                var saved = __wta_seed__;
                __wta_seed__ = ${fp.canvasNoiseSeed.toUInt()};
                for (var i = 0; i < data.length; i += 4) {
                    var r = __wta_prng__();
                    if (r < 0.1) {
                        var ch = (i % 3);
                        data[i + ch] = data[i + ch] ^ 1;
                    }
                }
                __wta_seed__ = saved;
            }

            HTMLCanvasElement.prototype.toDataURL = function() {
                try {
                    var ctx = this.getContext('2d');
                    if (ctx && this.width > 0 && this.height > 0 && this.width < 2000 && this.height < 2000) {
                        var imgData = origGetImageData.call(ctx, 0, 0, this.width, this.height);
                        __wta_canvas_noise__(imgData.data);
                        ctx.putImageData(imgData, 0, 0);
                    }
                } catch(e){ /* canvas noise injection failed */ }
                return origToDataURL.apply(this, arguments);
            };

            HTMLCanvasElement.prototype.toBlob = function() {
                try {
                    var ctx = this.getContext('2d');
                    if (ctx && this.width > 0 && this.height > 0 && this.width < 2000 && this.height < 2000) {
                        var imgData = origGetImageData.call(ctx, 0, 0, this.width, this.height);
                        __wta_canvas_noise__(imgData.data);
                        ctx.putImageData(imgData, 0, 0);
                    }
                } catch(e){ /* canvas noise injection failed */ }
                return origToBlob.apply(this, arguments);
            };

            CanvasRenderingContext2D.prototype.getImageData = function() {
                var imgData = origGetImageData.apply(this, arguments);
                try { __wta_canvas_noise__(imgData.data); } catch(e){ /* canvas noise failed */ }
                return imgData;
            };
        """.trimIndent()
    }

    private fun generateWebGLProtectionScript(fp: GeneratedFingerprint): String {
        return """
            // WebGL fingerprint spoofing
            var glParamHandler = {
                apply: function(target, thisArg, args) {
                    var p = args[0];
                    if (p === 37445) return '${fp.webglVendor}';
                    if (p === 37446) return '${fp.webglRenderer}';
                    return target.apply(thisArg, args);
                }
            };
            var origGlGetParam = WebGLRenderingContext.prototype.getParameter;
            WebGLRenderingContext.prototype.getParameter = new Proxy(origGlGetParam, glParamHandler);
            if (typeof WebGL2RenderingContext !== 'undefined') {
                var origGl2GetParam = WebGL2RenderingContext.prototype.getParameter;
                WebGL2RenderingContext.prototype.getParameter = new Proxy(origGl2GetParam, glParamHandler);
            }

            // getExtension — ensure WEBGL_debug_renderer_info returns correct constants
            var origGetExt = WebGLRenderingContext.prototype.getExtension;
            WebGLRenderingContext.prototype.getExtension = function(name) {
                if (name === 'WEBGL_debug_renderer_info') {
                    return { UNMASKED_VENDOR_WEBGL: 37445, UNMASKED_RENDERER_WEBGL: 37446 };
                }
                return origGetExt.apply(this, arguments);
            };
            if (typeof WebGL2RenderingContext !== 'undefined') {
                var origGetExt2 = WebGL2RenderingContext.prototype.getExtension;
                WebGL2RenderingContext.prototype.getExtension = function(name) {
                    if (name === 'WEBGL_debug_renderer_info') {
                        return { UNMASKED_VENDOR_WEBGL: 37445, UNMASKED_RENDERER_WEBGL: 37446 };
                    }
                    return origGetExt2.apply(this, arguments);
                };
            }
        """.trimIndent()
    }

    private fun generateAudioProtectionScript(fp: GeneratedFingerprint): String {
        return """
            // AudioContext fingerprint — deterministic noise
            var origGetFloatFreq = AnalyserNode.prototype.getFloatFrequencyData;
            var origGetByteFreq = AnalyserNode.prototype.getByteFrequencyData;
            var origGetFloatTime = AnalyserNode.prototype.getFloatTimeDomainData;
            var origGetByteTime = AnalyserNode.prototype.getByteTimeDomainData;

            function __wta_audio_noise_float__(arr) {
                var saved = __wta_seed__;
                __wta_seed__ = ${fp.audioNoiseSeed.toUInt()};
                for (var i = 0; i < arr.length; i++) {
                    arr[i] += (__wta_prng__() - 0.5) * 0.0001;
                }
                __wta_seed__ = saved;
            }
            function __wta_audio_noise_byte__(arr) {
                var saved = __wta_seed__;
                __wta_seed__ = ${fp.audioNoiseSeed.toUInt()};
                for (var i = 0; i < arr.length; i++) {
                    if (__wta_prng__() < 0.05) arr[i] = (arr[i] + 1) & 0xFF;
                }
                __wta_seed__ = saved;
            }

            AnalyserNode.prototype.getFloatFrequencyData = function(a) {
                origGetFloatFreq.call(this, a); __wta_audio_noise_float__(a);
            };
            AnalyserNode.prototype.getByteFrequencyData = function(a) {
                origGetByteFreq.call(this, a); __wta_audio_noise_byte__(a);
            };
            AnalyserNode.prototype.getFloatTimeDomainData = function(a) {
                origGetFloatTime.call(this, a); __wta_audio_noise_float__(a);
            };
            AnalyserNode.prototype.getByteTimeDomainData = function(a) {
                origGetByteTime.call(this, a); __wta_audio_noise_byte__(a);
            };

            // Spoof AudioContext.destination.channelCount
            try {
                var origACtx = window.AudioContext || window.webkitAudioContext;
                if (origACtx) {
                    var origCreateOsc = origACtx.prototype.createOscillator;
                    var origCreateDynComp = origACtx.prototype.createDynamicsCompressor;
                    // Wrap createOscillator to add noise to oscillator output
                    origACtx.prototype.createOscillator = function() {
                        var osc = origCreateOsc.apply(this, arguments);
                        // Slightly vary frequency to alter audio fingerprint
                        var origFreq = osc.frequency.value;
                        osc.frequency.value = origFreq + (__wta_prng__() - 0.5) * 0.01;
                        return osc;
                    };
                }
            } catch(e){ /* AudioContext spoofing failed */ }
        """.trimIndent()
    }

    private fun generateWebRTCProtectionScript(): String {
        return """
            // WebRTC IP leak protection (keeps functionality, masks local IPs)
            if (typeof RTCPeerConnection !== 'undefined') {
                var OrigRTC = RTCPeerConnection;

                var WtaRTC = function(config, constraints) {
                    // Keep TURN servers (relay), remove STUN (IP discovery)
                    if (config && config.iceServers) {
                        config.iceServers = config.iceServers.filter(function(s) {
                            var urls = s.urls || s.url || '';
                            if (typeof urls === 'string') urls = [urls];
                            return urls.some(function(u) { return u.indexOf('turn:') === 0 || u.indexOf('turns:') === 0; });
                        });
                    }
                    var pc = new OrigRTC(config, constraints);

                    // Filter ICE candidates — remove host candidates (local IPs)
                    var origAddEvent = pc.addEventListener.bind(pc);
                    pc.addEventListener = function(type, fn, opts) {
                        if (type === 'icecandidate') {
                            var wrapped = function(e) {
                                if (e.candidate && e.candidate.candidate) {
                                    var c = e.candidate.candidate;
                                    // Block host candidates (contain local IP)
                                    if (c.indexOf('typ host') !== -1) {
                                        // Fire event with null candidate (empty)
                                        var fakeEvt = new Event('icecandidate');
                                        fakeEvt.candidate = null;
                                        fn(fakeEvt);
                                        return;
                                    }
                                }
                                fn(e);
                            };
                            return origAddEvent(type, wrapped, opts);
                        }
                        return origAddEvent(type, fn, opts);
                    };

                    // Also filter onicecandidate setter
                    var origOnIce = Object.getOwnPropertyDescriptor(OrigRTC.prototype, 'onicecandidate');
                    if (origOnIce && origOnIce.set) {
                        Object.defineProperty(pc, 'onicecandidate', {
                            set: function(fn) {
                                if (typeof fn !== 'function') { origOnIce.set.call(pc, fn); return; }
                                origOnIce.set.call(pc, function(e) {
                                    if (e.candidate && e.candidate.candidate && e.candidate.candidate.indexOf('typ host') !== -1) {
                                        var fakeEvt = new Event('icecandidate');
                                        fakeEvt.candidate = null;
                                        fn(fakeEvt);
                                        return;
                                    }
                                    fn(e);
                                });
                            },
                            get: function() { return origOnIce.get ? origOnIce.get.call(pc) : undefined; },
                            configurable: true
                        });
                    }

                    return pc;
                };
                WtaRTC.prototype = OrigRTC.prototype;
                WtaRTC.generateCertificate = OrigRTC.generateCertificate;
                window.RTCPeerConnection = WtaRTC;
                if (window.webkitRTCPeerConnection) window.webkitRTCPeerConnection = WtaRTC;
            }
        """.trimIndent()
    }

    private fun generateFontProtectionScript(): String {
        return """
            // Font fingerprint protection — normalize metrics + FontFace API
            var commonFonts = new Set([
                'Arial','Arial Black','Comic Sans MS','Courier New','Georgia',
                'Impact','Times New Roman','Trebuchet MS','Verdana','Helvetica',
                'Lucida Console','Palatino Linotype','Tahoma','Segoe UI',
                'Microsoft YaHei','SimSun','SimHei','PingFang SC','Hiragino Sans GB'
            ]);

            // 1. Override document.fonts.check() — always return true for common, false for others
            if (document.fonts && document.fonts.check) {
                var origFontsCheck = document.fonts.check.bind(document.fonts);
                document.fonts.check = function(font, text) {
                    // Extract font family name
                    var match = font.match(/['"](.*?)['"]/);
                    if (!match) match = font.match(/\d+(?:px|pt|em|rem)\s+(.*)/);
                    var family = match ? match[1].trim() : font;
                    if (commonFonts.has(family)) return true;
                    // For non-common fonts, randomize to hide real set
                    return __wta_prng__() > 0.5;
                };
            }

            // 2. Normalize font measurement probes
            // Font detection works by creating an element with a specific font, measuring width/height,
            // and comparing to a fallback font. We add small deterministic noise to defeat exact comparison.
            var origGetBCR = Element.prototype.getBoundingClientRect;
            var origOffsetW = Object.getOwnPropertyDescriptor(HTMLElement.prototype, 'offsetWidth');
            var origOffsetH = Object.getOwnPropertyDescriptor(HTMLElement.prototype, 'offsetHeight');

            function isFontProbe(el) {
                if (!el || !el.style) return false;
                var s = el.style;
                return (s.position === 'absolute' && (s.left === '-9999px' || s.top === '-9999px' || s.visibility === 'hidden')) ||
                       (el.parentNode && el.parentNode.style && el.parentNode.style.position === 'absolute' && el.parentNode.style.left === '-9999px');
            }

            if (origOffsetW && origOffsetW.get) {
                Object.defineProperty(HTMLElement.prototype, 'offsetWidth', {
                    get: function() {
                        var w = origOffsetW.get.call(this);
                        if (isFontProbe(this)) return w + ((__wta_prng__() < 0.3) ? 1 : 0);
                        return w;
                    }, configurable: true
                });
            }
            if (origOffsetH && origOffsetH.get) {
                Object.defineProperty(HTMLElement.prototype, 'offsetHeight', {
                    get: function() {
                        var h = origOffsetH.get.call(this);
                        if (isFontProbe(this)) return h + ((__wta_prng__() < 0.3) ? 1 : 0);
                        return h;
                    }, configurable: true
                });
            }
        """.trimIndent()
    }

    private fun generateScreenSpoofScript(
        width: Int,
        height: Int,
        dpr: Float,
        colorDepth: Int
    ): String {
        val availHeight = height - 40
        val innerHeight = height - 120
        val orientation = if (width >= height) "landscape-primary" else "portrait-primary"
        val dprStr = if (dpr == dpr.toLong().toFloat()) dpr.toLong().toString() else dpr.toString()
        return """
            // Screen/window dimension spoofing — coherent screen + viewport + media queries
            var __wta_scrW__ = $width, __wta_scrH__ = $height, __wta_dpr__ = $dprStr;
            var __wta_innerH__ = $innerHeight, __wta_availH__ = $availHeight;
            var screenProps = {
                width: __wta_scrW__, height: __wta_scrH__,
                availWidth: __wta_scrW__, availHeight: __wta_availH__,
                availLeft: 0, availTop: 0,
                colorDepth: $colorDepth, pixelDepth: $colorDepth,
                isExtended: false
            };
            Object.keys(screenProps).forEach(function(p){
                try{Object.defineProperty(screen,p,{get:function(){return screenProps[p];},configurable:true});}catch(e){/* expected */}
            });
            try{
                Object.defineProperty(screen,'orientation',{get:function(){
                    return {type:'$orientation',angle:0,onchange:null,
                        lock:function(){return Promise.resolve();},unlock:function(){},
                        addEventListener:function(){},removeEventListener:function(){}};
                },configurable:true});
            }catch(e){/* expected */}

            var winProps = {
                innerWidth: __wta_scrW__, innerHeight: __wta_innerH__,
                outerWidth: __wta_scrW__, outerHeight: __wta_scrH__,
                devicePixelRatio: __wta_dpr__,
                screenX: 0, screenY: 0, screenLeft: 0, screenTop: 0,
                orientation: 0
            };
            Object.keys(winProps).forEach(function(p){
                try{Object.defineProperty(window,p,{get:function(){return winProps[p];},configurable:true});}catch(e){/* expected */}
            });
            try{
                var fakeVV = {
                    width: __wta_scrW__, height: __wta_innerH__, scale: 1,
                    offsetLeft: 0, offsetTop: 0, pageLeft: 0, pageTop: 0,
                    onresize: null, onscroll: null,
                    addEventListener: function(){}, removeEventListener: function(){}
                };
                Object.defineProperty(window,'visualViewport',{get:function(){return fakeVV;},configurable:true});
            }catch(e){/* expected */}

            // matchMedia — evaluate dimension/resolution/orientation queries against fake metrics
            var __wta_origMM__ = window.matchMedia ? window.matchMedia.bind(window) : null;
            function __wta_mmMetric__(name) {
                switch (name) {
                    case 'width': case 'device-width': return __wta_scrW__;
                    case 'height': case 'device-height': return __wta_scrH__;
                    case 'resolution': return __wta_dpr__;
                    case 'aspect-ratio': return __wta_scrW__ / __wta_scrH__;
                }
                return null;
            }
            function __wta_mmValue__(raw, feature) {
                raw = raw.trim();
                if (feature === 'orientation') return raw;
                if (feature === 'aspect-ratio') {
                    var ab = raw.split('/');
                    return ab.length === 2 ? (parseFloat(ab[0]) / parseFloat(ab[1])) : NaN;
                }
                if (feature === 'resolution') {
                    var r = parseFloat(raw);
                    if (raw.indexOf('dpi') !== -1) return r / 96;
                    if (raw.indexOf('dpcm') !== -1) return r / 37.795;
                    return r; // dppx / x
                }
                var v = parseFloat(raw);
                if (raw.indexOf('em') !== -1 || raw.indexOf('rem') !== -1) v *= 16;
                return v;
            }
            function __wta_mmEval__(query) {
                var orParts = query.split(',');
                var seen = false;
                for (var oi = 0; oi < orParts.length; oi++) {
                    var clause = orParts[oi].trim().toLowerCase();
                    // Strip media type / modifiers: (not|only)? <type>? and ...
                    clause = clause.replace(/^\s*(only|not)\s+/, '').replace(/^[a-z-]+\s+and\s+/, '');
                    var negate = /^not\s+/.test(orParts[oi].trim().toLowerCase());
                    var conds = clause.match(/\([^)]*\)/g);
                    if (!conds || conds.length === 0) { if (clause === 'all' || clause === 'screen') { return null; } return null; }
                    var clauseOk = true;
                    for (var ci = 0; ci < conds.length; ci++) {
                        var body = conds[ci].slice(1, -1);
                        var kv = body.split(':');
                        var feature = kv[0].trim();
                        var base = feature.replace(/^(min|max)-/, '');
                        var metric = __wta_mmMetric__(base);
                        if (metric === null) return null; // unknown feature → delegate
                        var dir = feature.indexOf('min-') === 0 ? 'min' : (feature.indexOf('max-') === 0 ? 'max' : 'eq');
                        var ok;
                        if (kv.length === 1) { ok = metric !== 0; }
                        else {
                            var want = __wta_mmValue__(kv.slice(1).join(':'), base);
                            if (base === 'orientation') {
                                ok = (want === 'landscape') === (__wta_scrW__ >= __wta_scrH__);
                            } else if (typeof metric === 'number' && typeof want === 'number' && !isNaN(want)) {
                                ok = dir === 'min' ? metric >= want : (dir === 'max' ? metric <= want : Math.abs(metric - want) < 0.001);
                            } else { return null; }
                        }
                        if (!ok) { clauseOk = false; break; }
                    }
                    if (negate) clauseOk = !clauseOk;
                    if (clauseOk) return true;
                    seen = true;
                }
                return seen ? false : false;
            }
            if (__wta_origMM__) {
                window.matchMedia = function(q) {
                    try {
                        var res = __wta_mmEval__(String(q));
                        if (res === null) return __wta_origMM__(q);
                        return {
                            matches: res, media: q, onchange: null,
                            addListener: function(){}, removeListener: function(){},
                            addEventListener: function(){}, removeEventListener: function(){},
                            dispatchEvent: function(){ return false; }
                        };
                    } catch(e) { return __wta_origMM__(q); }
                };
            }
        """.trimIndent()
    }

    /** 语言伪装（navigator.language/languages 独立块，指纹随机化关闭时也可用）。 */
    private fun generateLanguageSpoofScript(langs: Pair<String, List<String>>): String {
        val (primary, arr) = langs
        return """
            // Language spoofing (standalone navigator override)
            try{Object.defineProperty(navigator,'language',{get:function(){return '${jsStr(primary)}';},configurable:true});}catch(e){/* expected */}
            try{Object.defineProperty(navigator,'languages',{get:function(){return Object.freeze(${arr.joinToString(",", "[", "]") { "'${jsStr(it)}'" }});},configurable:true});}catch(e){/* expected */}
        """.trimIndent()
    }

    /**
     * 环境伪装核心块：Intl 语言环境注入 + 完整时区伪装。
     * 时区通过 Intl.formatToParts 按时刻动态求偏移（自动处理夏令时），
     * 并补齐 Date 本地时间 getter / toString 家族 / 构造函数参数解释，
     * 使 new Date().toString() 与 getHours() 等全部落在目标时区。
     */
    private fun generateEnvSpoofScript(localeTag: String?, timezone: String?): String {
        val localeLine = localeTag?.let { "var __wta_locale__ = '${jsStr(it)}';" } ?: "var __wta_locale__ = null;"
        val tzLine = timezone?.let { "var __wta_tz__ = '${jsStr(it)}';" } ?: "var __wta_tz__ = null;"

        val localeJs = if (localeTag != null) """
            // Locale spoofing — Intl constructors default to spoofed locale
            var __wta_origNF__ = Intl.NumberFormat;
            var WtaNF = function(locales, options) {
                return new __wta_origNF__(locales === undefined ? __wta_locale__ : locales, options);
            };
            WtaNF.prototype = __wta_origNF__.prototype;
            WtaNF.supportedLocalesOf = __wta_origNF__.supportedLocalesOf;
            Intl.NumberFormat = WtaNF;
            if (Intl.Collator) {
                var __wta_origCol__ = Intl.Collator;
                var WtaCol = function(locales, options) {
                    return new __wta_origCol__(locales === undefined ? __wta_locale__ : locales, options);
                };
                WtaCol.prototype = __wta_origCol__.prototype;
                WtaCol.supportedLocalesOf = __wta_origCol__.supportedLocalesOf;
                Intl.Collator = WtaCol;
            }
        """ else ""

        val timezoneJs = if (timezone != null) """
            // Timezone spoofing — DST-aware complete Date patching
            var __wta_dtf__ = __wta_origDTF__('en-US', {
                timeZone: __wta_tz__, hourCycle: 'h23', weekday: 'short',
                year: 'numeric', month: '2-digit', day: '2-digit',
                hour: '2-digit', minute: '2-digit', second: '2-digit'
            });
            var __wta_dtfStr__ = __wta_origDTF__('en-US', {
                timeZone: __wta_tz__, hourCycle: 'h23', weekday: 'short',
                year: 'numeric', month: 'short', day: '2-digit',
                hour: '2-digit', minute: '2-digit', second: '2-digit',
                timeZoneName: 'short'
            });
            var __wta_wdmap__ = {Sun:0,Mon:1,Tue:2,Wed:3,Thu:4,Fri:5,Sat:6};
            var __wta_monmap__ = {Jan:0,Feb:1,Mar:2,Apr:3,May:4,Jun:5,Jul:6,Aug:7,Sep:8,Oct:9,Nov:10,Dec:11};

            function __wta_tzparts__(ms) {
                var p = {};
                __wta_dtf__.formatToParts(ms).forEach(function(x){ p[x.type] = x.value; });
                return p;
            }
            function __wta_tzstrparts__(ms) {
                var p = {};
                __wta_dtfStr__.formatToParts(ms).forEach(function(x){ p[x.type] = x.value; });
                return p;
            }
            // 目标时区墙钟 - UTC，单位毫秒（对具体时刻求值 → 自动含 DST）
            function __wta_tzoff__(ms) {
                var p = __wta_tzparts__(ms);
                var h = +p.hour === 24 ? 0 : +p.hour;
                var wallAsUtc = __wta_OrigDate__.UTC(+p.year, +p.month - 1, +p.day, h, +p.minute, +p.second);
                return wallAsUtc - Math.floor(ms / 1000) * 1000;
            }
            function __wta_gmtstr__(ms) {
                var offMin = Math.round(__wta_tzoff__(ms) / 60000);
                var abs = Math.abs(offMin), sign = offMin < 0 ? '-' : '+';
                return 'GMT' + sign + ('0' + Math.floor(abs / 60)).slice(-2) + ('0' + (abs % 60)).slice(-2);
            }

            var __wta_OrigDate__ = Date;
            Date.prototype.getTimezoneOffset = function() {
                return -Math.round(__wta_tzoff__(this.getTime()) / 60000);
            };
            Date.prototype.getFullYear = function() { return +__wta_tzparts__(this.getTime()).year; };
            Date.prototype.getYear = function() { return +__wta_tzparts__(this.getTime()).year - 1900; };
            Date.prototype.getMonth = function() { return +__wta_tzparts__(this.getTime()).month - 1; };
            Date.prototype.getDate = function() { return +__wta_tzparts__(this.getTime()).day; };
            Date.prototype.getDay = function() { return __wta_wdmap__[__wta_tzparts__(this.getTime()).weekday]; };
            Date.prototype.getHours = function() { var h = +__wta_tzparts__(this.getTime()).hour; return h === 24 ? 0 : h; };
            Date.prototype.getMinutes = function() { return +__wta_tzparts__(this.getTime()).minute; };
            Date.prototype.getSeconds = function() { return +__wta_tzparts__(this.getTime()).second; };
            Date.prototype.toString = function() {
                var ms = this.getTime(), p = __wta_tzstrparts__(ms);
                var h = +p.hour === 24 ? 0 : +p.hour;
                return p.weekday + ' ' + p.month + ' ' + p.day + ' ' + p.year + ' ' +
                    ('0' + h).slice(-2) + ':' + p.minute + ':' + p.second + ' ' +
                    __wta_gmtstr__(ms) + ' (' + p.timeZoneName + ')';
            };
            Date.prototype.toDateString = function() {
                var p = __wta_tzstrparts__(this.getTime());
                return p.weekday + ' ' + p.month + ' ' + p.day + ' ' + p.year;
            };
            Date.prototype.toTimeString = function() {
                var ms = this.getTime(), p = __wta_tzstrparts__(ms);
                var h = +p.hour === 24 ? 0 : +p.hour;
                return ('0' + h).slice(-2) + ':' + p.minute + ':' + p.second + ' ' +
                    __wta_gmtstr__(ms) + ' (' + p.timeZoneName + ')';
            };

            // new Date(y, m, d, ...) 的参数按目标时区墙钟解释
            var WtaDate = function(y, mo, d, h, mi, s, msec) {
                if (!(this instanceof WtaDate)) return __wta_OrigDate__.apply(null, arguments);
                var argc = arguments.length;
                if (argc === 0) return new __wta_OrigDate__();
                if (argc === 1) return new __wta_OrigDate__(y);
                var guess = __wta_OrigDate__.UTC(y, mo, d === undefined ? 1 : d, h || 0, mi || 0, s || 0, msec || 0);
                var t = guess - __wta_tzoff__(guess);
                t = guess - __wta_tzoff__(t); // 迭代一次消除 DST 边界误差
                return new __wta_OrigDate__(t);
            };
            WtaDate.prototype = __wta_OrigDate__.prototype;
            WtaDate.now = __wta_OrigDate__.now;
            WtaDate.UTC = __wta_OrigDate__.UTC;
            WtaDate.parse = __wta_OrigDate__.parse;
            __wta_OrigDate__.prototype.constructor = WtaDate;
            window.Date = WtaDate;
        """ else ""

        return """
            // Environment spoofing — coherent Intl locale + timezone
            $localeLine
            $tzLine
            var __wta_origDTF__ = Intl.DateTimeFormat;
            var WtaDTF = function(locales, options) {
                var loc = locales;
                if (loc === undefined && __wta_locale__) loc = __wta_locale__;
                var opts = Object.assign({}, options || {});
                if (__wta_tz__) opts.timeZone = __wta_tz__;
                return new __wta_origDTF__(loc, opts);
            };
            WtaDTF.prototype = __wta_origDTF__.prototype;
            WtaDTF.supportedLocalesOf = __wta_origDTF__.supportedLocalesOf;
            Intl.DateTimeFormat = WtaDTF;

            $localeJs

            $timezoneJs
        """.trimIndent()
    }

    private fun generateClientRectsNoiseScript(): String {
        return """
            // ClientRects / DOMRect noise — defeats rect-based fingerprinting
            var origGetBCR = Element.prototype.getBoundingClientRect;
            Element.prototype.getBoundingClientRect = function() {
                var r = origGetBCR.call(this);
                var n = (__wta_prng__() - 0.5) * 0.5;
                return new DOMRect(r.x + n, r.y + n, r.width + n, r.height + n);
            };
            var origGetCR = Element.prototype.getClientRects;
            Element.prototype.getClientRects = function() {
                var rects = origGetCR.call(this);
                var result = [];
                for (var i = 0; i < rects.length; i++) {
                    var r = rects[i];
                    var n = (__wta_prng__() - 0.5) * 0.5;
                    result.push(new DOMRect(r.x + n, r.y + n, r.width + n, r.height + n));
                }
                // Return array-like with item() method
                result.item = function(idx) { return result[idx] || null; };
                return result;
            };
        """.trimIndent()
    }

    private fun generatePerformanceTimingScript(): String {
        return """
            // performance.now() — reduce precision to 100μs to defeat timing attacks
            var origPerfNow = performance.now.bind(performance);
            performance.now = function() {
                return Math.round(origPerfNow() * 10) / 10;
            };
        """.trimIndent()
    }

    private fun generatePluginsSpoofScript(fp: GeneratedFingerprint): String {
        val isChromium = fp.browserType == "CHROME" || fp.browserType == "EDGE"
        return if (isChromium) {
            """
            // Plugins — Chromium default (PDF Viewer + Chrome PDF Viewer)
            try {
                var fakePlugins = [
                    {name:'PDF Viewer',filename:'internal-pdf-viewer',description:'Portable Document Format',length:1,
                     0:{type:'application/pdf',suffixes:'pdf',description:'Portable Document Format'}},
                    {name:'Chrome PDF Viewer',filename:'internal-pdf-viewer',description:'Portable Document Format',length:1,
                     0:{type:'application/pdf',suffixes:'pdf',description:'Portable Document Format'}},
                    {name:'Chromium PDF Viewer',filename:'internal-pdf-viewer',description:'Portable Document Format',length:1,
                     0:{type:'application/pdf',suffixes:'pdf',description:'Portable Document Format'}},
                    {name:'Microsoft Edge PDF Viewer',filename:'internal-pdf-viewer',description:'Portable Document Format',length:1,
                     0:{type:'application/pdf',suffixes:'pdf',description:'Portable Document Format'}},
                    {name:'WebKit built-in PDF',filename:'internal-pdf-viewer',description:'Portable Document Format',length:1,
                     0:{type:'application/pdf',suffixes:'pdf',description:'Portable Document Format'}}
                ];
                fakePlugins.item = function(i){return fakePlugins[i]||null;};
                fakePlugins.namedItem = function(n){return fakePlugins.find(function(p){return p.name===n;})||null;};
                fakePlugins.refresh = function(){};
                Object.defineProperty(navigator,'plugins',{get:function(){return fakePlugins;},configurable:true});
                // mimeTypes
                var fakeMime = [{type:'application/pdf',suffixes:'pdf',description:'Portable Document Format',enabledPlugin:fakePlugins[0]}];
                fakeMime.item = function(i){return fakeMime[i]||null;};
                fakeMime.namedItem = function(n){return fakeMime.find(function(m){return m.type===n;})||null;};
                Object.defineProperty(navigator,'mimeTypes',{get:function(){return fakeMime;},configurable:true});
            }catch(e){/* expected */}
            """.trimIndent()
        } else {
            """
            // Firefox/Safari: empty plugins (real behavior)
            try {
                Object.defineProperty(navigator,'plugins',{get:function(){var a=[];a.item=function(){return null;};a.namedItem=function(){return null;};a.refresh=function(){};return a;},configurable:true});
                Object.defineProperty(navigator,'mimeTypes',{get:function(){var a=[];a.item=function(){return null;};a.namedItem=function(){return null;};return a;},configurable:true});
            }catch(e){/* expected */}
            """.trimIndent()
        }
    }

    private fun generateBatteryProtectionScript(): String {
        return """
            // Battery API — return consistent fake values (always "plugged in, full")
            if (navigator.getBattery) {
                var fakeBattery = {
                    charging: true, chargingTime: 0, dischargingTime: Infinity, level: 1,
                    addEventListener: function(){}, removeEventListener: function(){},
                    dispatchEvent: function(){return true;},
                    onchargingchange: null, onchargingtimechange: null,
                    ondischargingtimechange: null, onlevelchange: null
                };
                navigator.getBattery = function() { return Promise.resolve(fakeBattery); };
            }
        """.trimIndent()
    }

    private fun generateConnectionSpoofScript(): String {
        return """
            // navigator.connection — normalize to common broadband profile
            try {
                var fakeConn = {
                    downlink: 10, effectiveType: '4g', rtt: 50, saveData: false,
                    type: 'wifi', downlinkMax: Infinity,
                    addEventListener: function(){}, removeEventListener: function(){},
                    dispatchEvent: function(){return true;},
                    onchange: null, ontypechange: null
                };
                Object.defineProperty(navigator,'connection',{get:function(){return fakeConn;},configurable:true});
            }catch(e){/* expected */}
        """.trimIndent()
    }

    private fun generateMediaDevicesProtectionScript(): String {
        return """
            // MediaDevices.enumerateDevices — return generic device list
            if (navigator.mediaDevices && navigator.mediaDevices.enumerateDevices) {
                var origEnum = navigator.mediaDevices.enumerateDevices.bind(navigator.mediaDevices);
                navigator.mediaDevices.enumerateDevices = function() {
                    return origEnum().then(function(devices) {
                        // Strip labels and deviceIds to prevent fingerprinting
                        return devices.map(function(d, i) {
                            return {
                                deviceId: 'device' + i,
                                groupId: 'group' + Math.floor(i/2),
                                kind: d.kind,
                                label: '',
                                toJSON: function() { return {deviceId:this.deviceId,groupId:this.groupId,kind:this.kind,label:''}; }
                            };
                        });
                    });
                };
            }
        """.trimIndent()
    }

    private fun generateStorageEstimateScript(): String {
        return """
            // StorageManager.estimate — return normalized values
            if (navigator.storage && navigator.storage.estimate) {
                navigator.storage.estimate = function() {
                    return Promise.resolve({ quota: 2147483648, usage: 0 });
                };
            }
        """.trimIndent()
    }

    private fun generateHistoryProtectionScript(): String {
        return """
            // history.length — fixed at 1 to prevent history-based tracking
            try{Object.defineProperty(history,'length',{get:function(){return 1;},configurable:true});}catch(e){/* expected */}
        """.trimIndent()
    }

    private fun generatePermissionsProtectionScript(): String {
        return """
            // Permissions API — normalize responses
            if (navigator.permissions && navigator.permissions.query) {
                var origQuery = navigator.permissions.query.bind(navigator.permissions);
                navigator.permissions.query = function(desc) {
                    return origQuery(desc).catch(function() {
                        return { state: 'prompt', onchange: null, addEventListener: function(){}, removeEventListener: function(){} };
                    });
                };
            }
        """.trimIndent()
    }
}
