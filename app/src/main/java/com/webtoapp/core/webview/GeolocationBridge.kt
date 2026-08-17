package com.webtoapp.core.webview

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.core.content.ContextCompat
import com.webtoapp.core.logging.AppLogger
import org.json.JSONObject

/**
 * Native navigator.geolocation backend for the System WebView engine (#581).
 *
 * Chromium's WebView implements navigator.geolocation exclusively through Google
 * Play Services' fused location provider, with no fallback: on devices where
 * Google location services are missing or unreachable (de-Googled ROMs, sanctioned
 * networks) every fix silently times out even with all permissions granted.
 * This bridge replaces navigator.geolocation with a LocationManager-backed
 * implementation (GPS / network / passive providers, no Google services involved).
 *
 * The shim script is injected at document start whenever webViewConfig.geolocationEnabled
 * is on; the permission flow is delegated to the host Activity through
 * [permissionRequester], which shows the same system dialog + location-services
 * dialog used by the Chromium prompt path.
 */
class GeolocationBridge(
    context: Context,
    private val policy: String,
    private val accuracy: String,
    private val webViewProvider: () -> WebView?,
    private val permissionRequester: (onResult: (Boolean) -> Unit) -> Unit,
    private val jsSink: ((String) -> Unit)? = null
) {

    companion object {
        private const val TAG = "GeolocationBridge"
        const val JS_INTERFACE_NAME = "WtaGeolocation"
        private const val ERR_PERMISSION = 1
        private const val ERR_UNAVAILABLE = 2
        private const val LAST_KNOWN_MAX_AGE_MS = 30_000L

        fun getShimScript(): String {
            return """
            (function() {
                if (window._wtaGeoShimmed) return;
                if (!window.WtaGeolocation) return;
                window._wtaGeoShimmed = true;

                var ERR_PERMISSION = 1, ERR_UNAVAILABLE = 2, ERR_TIMEOUT = 3;
                var pending = Object.create(null);
                var nextId = 1;
                var cache = null;

                function makeError(code, message) {
                    var e = new Error(message);
                    e.code = code;
                    e.PERMISSION_DENIED = ERR_PERMISSION;
                    e.POSITION_UNAVAILABLE = ERR_UNAVAILABLE;
                    e.TIMEOUT = ERR_TIMEOUT;
                    return e;
                }

                function makePosition(p) {
                    function orNull(v) { return (v === null || v === undefined || (typeof v === 'number' && isNaN(v))) ? null : v; }
                    return {
                        coords: {
                            latitude: p.latitude,
                            longitude: p.longitude,
                            accuracy: p.accuracy,
                            altitude: orNull(p.altitude),
                            altitudeAccuracy: null,
                            heading: orNull(p.heading),
                            speed: orNull(p.speed)
                        },
                        timestamp: p.timestamp
                    };
                }

                window.__wtaGeo = {
                    onResult: function(id, ok, payload, errCode, errMsg) {
                        var entry = pending[id];
                        if (!entry) return;
                        delete pending[id];
                        if (entry.timer !== null) { clearTimeout(entry.timer); entry.timer = null; }
                        if (ok && payload) {
                            cache = payload;
                            try { entry.success(makePosition(payload)); } catch (e) {}
                        } else {
                            var code = (errCode === ERR_PERMISSION || errCode === ERR_TIMEOUT) ? errCode : ERR_UNAVAILABLE;
                            try { entry.error && entry.error(makeError(code, errMsg || 'Location unavailable')); } catch (e) {}
                        }
                    }
                };

                function failEntry(id, entry, code, message) {
                    delete pending[id];
                    if (entry.timer !== null) { clearTimeout(entry.timer); entry.timer = null; }
                    try { entry.error && entry.error(makeError(code, message)); } catch (e) {}
                }

                function call(id, watch, success, error, options) {
                    var opts = options || {};
                    var highAccuracy = !!opts.enableHighAccuracy;
                    var maxAge = (typeof opts.maximumAge === 'number' && opts.maximumAge >= 0) ? opts.maximumAge : 0;
                    var entry = { success: success, error: error, timer: null };
                    pending[id] = entry;

                    if (cache && (Date.now() - cache.timestamp) <= maxAge) {
                        var cached = cache;
                        delete pending[id];
                        try { success(makePosition(cached)); } catch (e) {}
                        return id;
                    }

                    if (opts.timeout > 0) {
                        entry.timer = setTimeout(function() {
                            if (pending[id] === entry) failEntry(id, entry, ERR_TIMEOUT, 'Timed out');
                        }, opts.timeout);
                    }

                    try {
                        var origin = '';
                        try { origin = window.location.origin || ''; } catch (e0) {}
                        if (watch) window.WtaGeolocation.startWatch(id, origin, highAccuracy);
                        else window.WtaGeolocation.requestPosition(id, origin, highAccuracy);
                    } catch (e) {
                        if (pending[id] === entry) failEntry(id, entry, ERR_UNAVAILABLE, 'Bridge failure: ' + e.message);
                    }
                    return id;
                }

                var geolocationShim = {
                    getCurrentPosition: function(success, error, options) {
                        if (typeof success !== 'function') return;
                        call(String(nextId++), false, success, error, options);
                    },
                    watchPosition: function(success, error, options) {
                        if (typeof success !== 'function') return 0;
                        return call('w' + String(nextId++), true, success, error, options);
                    },
                    clearWatch: function(id) {
                        var key = String(id);
                        var entry = pending[key];
                        if (entry) {
                            delete pending[key];
                            if (entry.timer !== null) clearTimeout(entry.timer);
                        }
                        try { window.WtaGeolocation.stopWatch(key); } catch (e) {}
                    }
                };

                try {
                    Object.defineProperty(window.navigator, 'geolocation', {
                        value: geolocationShim, writable: true, configurable: true
                    });
                } catch (e) {
                    try { window.navigator.geolocation = geolocationShim; } catch (e2) {}
                }

                if (window.navigator.permissions && window.navigator.permissions.query) {
                    try {
                        var origQuery = window.navigator.permissions.query.bind(window.navigator.permissions);
                        window.navigator.permissions.query = function(desc) {
                            if (desc && desc.name === 'geolocation') {
                                var denied = false;
                                try { denied = !!window.WtaGeolocation.isDeniedByPolicy(); } catch (e1) {}
                                return Promise.resolve({ state: denied ? 'denied' : 'granted', onchange: null });
                            }
                            return origQuery(desc);
                        };
                    } catch (e) {}
                }
            })();
            """.trimIndent()
        }
    }

    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private val lm = appContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val pendingSingles = mutableSetOf<String>()
    private val watchers = mutableSetOf<String>()
    private var listenerRegistered = false
    @Volatile
    private var destroyed = false

    private val locationListener = LocationListener { location -> onLocation(location) }

    @JavascriptInterface
    fun isDeniedByPolicy(): Boolean = policy.equals("DENY_ALL", ignoreCase = true)

    @JavascriptInterface
    fun requestPosition(id: String, origin: String, highAccuracy: Boolean) {
        mainHandler.post { beginRequest(id, highAccuracy, watch = false) }
    }

    @JavascriptInterface
    fun startWatch(id: String, origin: String, highAccuracy: Boolean) {
        mainHandler.post { beginRequest(id, highAccuracy, watch = true) }
    }

    @JavascriptInterface
    fun stopWatch(id: String) {
        mainHandler.post {
            watchers.remove(id)
            maybeUnregisterListener()
        }
    }

    fun destroy() {
        mainHandler.post {
            destroyed = true
            runCatching { lm?.removeUpdates(locationListener) }
            listenerRegistered = false
            pendingSingles.clear()
            watchers.clear()
        }
    }

    private fun beginRequest(id: String, highAccuracy: Boolean, watch: Boolean) {
        if (destroyed) return
        if (isDeniedByPolicy()) {
            emitError(id, ERR_PERMISSION, "Geolocation denied by policy")
            return
        }
        if (hasAnyLocationPermission()) {
            startTracking(id, highAccuracy, watch)
            return
        }
        permissionRequester { granted ->
            mainHandler.post {
                if (destroyed) return@post
                if (!granted) {
                    emitError(id, ERR_PERMISSION, "Location permission denied")
                } else {
                    startTracking(id, highAccuracy, watch)
                }
            }
        }
    }

    private fun startTracking(id: String, highAccuracy: Boolean, watch: Boolean) {
        if (watch) watchers.add(id) else pendingSingles.add(id)

        bestLastKnown()?.let { lastKnown ->
            emitLocation(id, lastKnown)
            if (!watch) {
                pendingSingles.remove(id)
                maybeUnregisterListener()
                return
            }
        }

        if (listenerRegistered) return
        val providers = enabledProviders(highAccuracy)
        if (providers.isEmpty()) {
            failRequest(id, watch, ERR_UNAVAILABLE, "Location services are disabled")
            return
        }
        var registered = false
        providers.forEach { provider ->
            try {
                lm?.requestLocationUpdates(provider, 1000L, 0f, locationListener, mainHandler.looper)
                registered = true
            } catch (e: Exception) {
                AppLogger.w(TAG, "requestLocationUpdates failed for provider $provider: ${e.message}")
            }
        }
        if (!registered) {
            failRequest(id, watch, ERR_UNAVAILABLE, "Failed to register location updates")
            return
        }
        listenerRegistered = true
    }

    private fun enabledProviders(highAccuracy: Boolean): List<String> {
        val lm = this.lm ?: return emptyList()
        val fineGranted = ContextCompat.checkSelfPermission(
            appContext, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val providers = mutableListOf<String>()
        if (fineGranted && runCatching { lm.isProviderEnabled(LocationManager.GPS_PROVIDER) }.getOrDefault(false)) {
            providers += LocationManager.GPS_PROVIDER
        }
        if (runCatching { lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) }.getOrDefault(false)) {
            providers += LocationManager.NETWORK_PROVIDER
        }
        if (runCatching { lm.isProviderEnabled(LocationManager.PASSIVE_PROVIDER) }.getOrDefault(false)) {
            providers += LocationManager.PASSIVE_PROVIDER
        }
        if (!highAccuracy && providers.size > 1) {
            providers.remove(LocationManager.GPS_PROVIDER)
        }
        return providers
    }

    private fun onLocation(location: Location) {
        if (destroyed) return
        val ids = pendingSingles.toList() + watchers.toList()
        if (ids.isEmpty()) {
            maybeUnregisterListener()
            return
        }
        ids.forEach { emitLocation(it, location) }
        if (pendingSingles.isNotEmpty()) {
            pendingSingles.clear()
            maybeUnregisterListener()
        }
    }

    private fun maybeUnregisterListener() {
        if (pendingSingles.isEmpty() && watchers.isEmpty() && listenerRegistered) {
            runCatching { lm?.removeUpdates(locationListener) }
            listenerRegistered = false
        }
    }

    private fun failRequest(id: String, watch: Boolean, code: Int, message: String) {
        if (watch) watchers.remove(id) else pendingSingles.remove(id)
        emitError(id, code, message)
        maybeUnregisterListener()
    }

    private fun bestLastKnown(): Location? {
        val lm = this.lm ?: return null
        var best: Location? = null
        listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        ).forEach { provider ->
            val location = runCatching { lm.getLastKnownLocation(provider) }.getOrNull() ?: return@forEach
            if (System.currentTimeMillis() - location.time > LAST_KNOWN_MAX_AGE_MS) return@forEach
            if (best == null || location.time > best!!.time) best = location
        }
        return best
    }

    private fun hasAnyLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun emitLocation(id: String, location: Location) {
        val json = JSONObject().apply {
            put("latitude", location.latitude)
            put("longitude", location.longitude)
            put("accuracy", location.accuracy.toDouble())
            put("altitude", if (location.hasAltitude()) location.altitude else JSONObject.NULL)
            put("heading", if (location.hasBearing()) location.bearing.toDouble() else JSONObject.NULL)
            put("speed", if (location.hasSpeed()) location.speed.toDouble() else JSONObject.NULL)
            put("timestamp", location.time)
        }
        dispatch(id, json.toString())
    }

    private fun emitError(id: String, code: Int, message: String) = dispatch(id, null, code, message)

    private fun dispatch(id: String, payload: String?, code: Int = 0, message: String = "") {
        val ok = payload != null
        val js = buildString {
            append("window.__wtaGeo&&window.__wtaGeo.onResult(")
            append(JSONObject.quote(id)).append(',')
            append(ok).append(',')
            append(payload ?: "null").append(',')
            append(if (ok) 0 else code).append(',')
            append(JSONObject.quote(message))
            append(')')
        }
        jsSink?.let { sink ->
            sink(js)
            return
        }
        val webView = webViewProvider()
        if (webView == null) {
            AppLogger.w(TAG, "dispatch skipped, webView gone (id=$id ok=$ok)")
            return
        }
        webView.post { webView.evaluateJavascript(js, null) }
    }
}
