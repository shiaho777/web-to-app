package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.playstore.aab.axml.AxmlToProtoXml
import com.android.aapt.Resources.XmlElement
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Verifies [AxmlRebuilder.mergeManifestFragment] grafts a curated text
 * `<fragment>` document into an existing binary manifest (issue #1115).
 *
 * Fixture `axml/base_manifest.axml` is a real aapt2-compiled manifest for
 * `com.spike.base` carrying INTERNET, one `<application>` and one launcher
 * activity — the same shape the admob stack fragment must merge into.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AxmlRebuilderFragmentTest {

    private val rebuilder = AxmlRebuilder()

    private fun fixture(): ByteArray =
        javaClass.classLoader.getResourceAsStream("axml/base_manifest.axml")!!.readBytes()

    private fun merged(fragment: String, vars: Map<String, String> = emptyMap()): XmlElement {
        val node = AxmlToProtoXml.convert(
            rebuilder.mergeManifestFragment(fixture(), fragment.toByteArray(), vars)
        )
        assertThat(node.hasElement()).isTrue()
        return node.element
    }

    private fun children(el: XmlElement, name: String) =
        el.childList.filter { it.hasElement() && it.element.name == name }.map { it.element }

    private fun XmlElement.attr(name: String) =
        attributeList.firstOrNull { it.name == name }

    @Test
    fun `uses-permission is added once and existing ones are not duplicated`() {
        val manifest = merged(
            """<fragment xmlns:android="http://schemas.android.com/apk/res/android">
                <uses-permission android:name="com.google.android.gms.permission.AD_ID" />
                <uses-permission android:name="android.permission.INTERNET" />
            </fragment>"""
        )
        val perms = children(manifest, "uses-permission")
            .map { it.attr("name")!!.value }
        assertThat(perms).containsExactly(
            "com.google.android.gms.permission.AD_ID",
            "android.permission.INTERNET"
        )
    }

    @Test
    fun `queries intent subtree is grafted with typed attribute values`() {
        val manifest = merged(
            """<fragment xmlns:android="http://schemas.android.com/apk/res/android">
                <queries>
                    <intent>
                        <action android:name="android.intent.action.VIEW" />
                        <data android:scheme="https" />
                    </intent>
                </queries>
            </fragment>"""
        )
        val queries = children(manifest, "queries").single()
        val intent = children(queries, "intent").single()
        assertThat(children(intent, "action").single().attr("name")!!.value)
            .isEqualTo("android.intent.action.VIEW")
        assertThat(children(intent, "data").single().attr("scheme")!!.value)
            .isEqualTo("https")
    }

    @Test
    fun `application children land inside the existing application element`() {
        val manifest = merged(
            """<fragment xmlns:android="http://schemas.android.com/apk/res/android">
                <application>
                    <provider
                        android:name="com.google.android.gms.ads.MobileAdsInitProvider"
                        android:authorities="${'$'}{applicationId}.mobileadsinitprovider"
                        android:exported="false"
                        android:initOrder="100" />
                    <meta-data
                        android:name="com.google.android.gms.ads.APPLICATION_ID"
                        android:value="${'$'}{admobAppId}" />
                </application>
            </fragment>""",
            vars = mapOf(
                "applicationId" to "com.example.app",
                "admobAppId" to "ca-app-pub-123~456"
            )
        )
        val app = children(manifest, "application").single()

        val provider = children(app, "provider").single()
        assertThat(provider.attr("name")!!.value)
            .isEqualTo("com.google.android.gms.ads.MobileAdsInitProvider")
        assertThat(provider.attr("authorities")!!.value)
            .isEqualTo("com.example.app.mobileadsinitprovider")
        assertThat(provider.attr("exported")!!.compiledItem.prim.booleanValue).isFalse()
        assertThat(provider.attr("initOrder")!!.compiledItem.prim.intDecimalValue).isEqualTo(100)

        val meta = children(app, "meta-data")
            .single { it.attr("name")!!.value == "com.google.android.gms.ads.APPLICATION_ID" }
        assertThat(meta.attr("value")!!.value).isEqualTo("ca-app-pub-123~456")

        // Original activity survives alongside injected components.
        assertThat(children(app, "activity").map { it.attr("name")!!.value })
            .contains("com.spike.base.MainActivity")
    }

    @Test
    fun `framework resource refs and flag attrs encode as typed values`() {
        val manifest = merged(
            """<fragment xmlns:android="http://schemas.android.com/apk/res/android">
                <application>
                    <activity
                        android:name="com.google.android.gms.ads.AdActivity"
                        android:configChanges="keyboard|orientation|screenSize"
                        android:exported="false"
                        android:theme="@android:style/Theme.Translucent" />
                </application>
            </fragment>"""
        )
        val activity = children(children(manifest, "application").single(), "activity")
            .single { it.attr("name")!!.value == "com.google.android.gms.ads.AdActivity" }

        val theme = activity.attr("theme")!!
        assertThat(theme.compiledItem.hasRef()).isTrue()
        assertThat(theme.compiledItem.ref.id).isEqualTo(0x0103000f)

        val config = activity.attr("configChanges")!!.compiledItem
        assertThat(config.prim.intHexadecimalValue)
            .isEqualTo(0x0010 or 0x0080 or 0x0400)
    }

    @Test
    fun `invalid or non-fragment input leaves bytes untouched`() {
        val base = fixture()
        assertThat(rebuilder.mergeManifestFragment(base, "<oops/>".toByteArray()))
            .isEqualTo(base)
        assertThat(
            rebuilder.mergeManifestFragment(base, "not xml at all <<<".toByteArray())
        ).isEqualTo(base)
    }
}
