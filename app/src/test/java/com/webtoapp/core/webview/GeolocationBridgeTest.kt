package com.webtoapp.core.webview

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class GeolocationBridgeTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun shadowLocationManager() =
        shadowOf(context.getSystemService(Context.LOCATION_SERVICE) as LocationManager)

    private fun idleMain() {
        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun denyLocationPermissions() {
        shadowOf(ApplicationProvider.getApplicationContext<android.app.Application>()).denyPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    private fun grantLocationPermissions() {
        shadowOf(ApplicationProvider.getApplicationContext<android.app.Application>()).grantPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    private fun freshLocation(lat: Double, lon: Double): Location =
        Location(LocationManager.NETWORK_PROVIDER).apply {
            latitude = lat
            longitude = lon
            accuracy = 10f
            time = System.currentTimeMillis()
        }

    private fun newBridge(
        policy: String = "ALWAYS_ASK",
        js: MutableList<String> = mutableListOf(),
        requester: ((Boolean) -> Unit) -> Unit = { it(false) }
    ) = GeolocationBridge(
        context = context,
        policy = policy,
        accuracy = "FINE",
        webViewProvider = { null },
        permissionRequester = requester,
        jsSink = { js.add(it) }
    ) to js

    @Test
    fun denyAllPolicyReportsPermissionDeniedWithoutRequesting() {
        val requesterCalls = mutableListOf<(Boolean) -> Unit>()
        val (bridge, js) = newBridge(
            policy = "DENY_ALL",
            requester = { cb -> requesterCalls.add(cb) }
        )

        bridge.requestPosition("1", "https://example.com", true)
        idleMain()

        assertTrue(requesterCalls.isEmpty())
        assertEquals(1, js.size)
        assertTrue(js[0].contains("onResult(\"1\",false,null,1,"))
    }

    @Test
    fun deniedAndroidPermissionSettlesAsPermissionDenied() {
        denyLocationPermissions()
        var captured: ((Boolean) -> Unit)? = null
        val (bridge, js) = newBridge(requester = { cb -> captured = cb })

        bridge.requestPosition("1", "https://example.com", true)
        idleMain()
        assertTrue(captured != null)

        captured?.invoke(false)
        idleMain()

        assertEquals(1, js.size)
        assertTrue(js[0].contains("onResult(\"1\",false,null,1,"))
    }

    @Test
    fun grantedAccessWithFreshLastKnownEmitsPositionImmediately() {
        grantLocationPermissions()
        val (bridge, js) = newBridge()
        shadowLocationManager().apply {
            setProviderEnabled(LocationManager.GPS_PROVIDER, false)
            setProviderEnabled(LocationManager.NETWORK_PROVIDER, true)
            setLastKnownLocation(LocationManager.NETWORK_PROVIDER, freshLocation(35.7, 51.4))
        }

        bridge.requestPosition("7", "https://example.com", false)
        idleMain()

        assertEquals(1, js.size)
        assertTrue(js[0].contains("onResult(\"7\",true,"))
        assertTrue(js[0].contains("\"latitude\":35.7"))
        assertTrue(js[0].contains("\"longitude\":51.4"))
    }

    @Test
    fun noEnabledProvidersReportsUnavailable() {
        denyLocationPermissions()
        var captured: ((Boolean) -> Unit)? = null
        val (bridge, js) = newBridge(requester = { cb -> captured = cb })
        shadowLocationManager().apply {
            setProviderEnabled(LocationManager.GPS_PROVIDER, false)
            setProviderEnabled(LocationManager.NETWORK_PROVIDER, false)
            setProviderEnabled(LocationManager.PASSIVE_PROVIDER, false)
        }

        bridge.requestPosition("1", "https://example.com", true)
        idleMain()
        captured?.invoke(true)
        idleMain()

        assertEquals(1, js.size)
        assertTrue(js[0].contains("onResult(\"1\",false,null,2,"))
        assertTrue(js[0].contains("Location services are disabled"))
    }

    @Test
    fun watchEmitsUpdatesAndStopsAfterClearWatch() {
        grantLocationPermissions()
        val (bridge, js) = newBridge()
        val shadowLm = shadowLocationManager()
        shadowLm.setProviderEnabled(LocationManager.NETWORK_PROVIDER, true)

        bridge.startWatch("w1", "https://example.com", false)
        idleMain()

        shadowLm.simulateLocation(freshLocation(35.71, 51.41))
        idleMain()
        val emittedAfterUpdate = js.size
        assertTrue(emittedAfterUpdate >= 1)
        assertTrue(js.any { it.contains("onResult(\"w1\",true,") && it.contains("\"latitude\":35.71") })

        bridge.stopWatch("w1")
        idleMain()
        val sizeAfterStop = js.size

        shadowLm.simulateLocation(freshLocation(35.72, 51.42))
        idleMain()

        assertEquals(sizeAfterStop, js.size)
    }

    @Test
    fun destroyStopsEmitting() {
        grantLocationPermissions()
        val (bridge, js) = newBridge()
        val shadowLm = shadowLocationManager()
        shadowLm.setProviderEnabled(LocationManager.NETWORK_PROVIDER, true)

        bridge.startWatch("w1", "https://example.com", false)
        idleMain()
        bridge.destroy()
        idleMain()

        val sizeAfterDestroy = js.size
        shadowLm.simulateLocation(freshLocation(35.73, 51.43))
        idleMain()

        assertEquals(sizeAfterDestroy, js.size)
    }

    @Test
    fun shimScriptOverridesNavigatorGeolocationAndQueriesPermissions() {
        val script = GeolocationBridge.getShimScript()
        assertTrue(script.contains("window._wtaGeoShimmed"))
        assertTrue(script.contains("getCurrentPosition"))
        assertTrue(script.contains("watchPosition"))
        assertTrue(script.contains("clearWatch"))
        assertTrue(script.contains("permissions.query"))
        assertFalse(script.contains("\${"))
    }
}
