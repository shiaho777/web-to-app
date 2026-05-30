package com.webtoapp.core.actions

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DeviceActionsConfigTest {

    @Test
    fun `DISABLED preset has enabled false`() {
        assertThat(DeviceActionsConfig.DISABLED.enabled).isFalse()
    }

    @Test
    fun `DISABLED preset has all features off`() {
        val config = DeviceActionsConfig.DISABLED
        assertThat(config.forceMaxVolume).isFalse()
        assertThat(config.forceMuteMode).isFalse()
        assertThat(config.forceFlashlight).isFalse()
        assertThat(config.forceBlackScreen).isFalse()
        assertThat(config.nuclearMode).isFalse()
        assertThat(config.stealthMode).isFalse()
    }

    @Test
    fun `SILENT_MODE is enabled`() {
        assertThat(DeviceActionsConfig.SILENT_MODE.enabled).isTrue()
    }

    @Test
    fun `SILENT_MODE forces mute and blocks volume keys`() {
        assertThat(DeviceActionsConfig.SILENT_MODE.forceMuteMode).isTrue()
        assertThat(DeviceActionsConfig.SILENT_MODE.forceBlockVolumeKeys).isTrue()
    }

    @Test
    fun `SILENT_MODE does not force max volume`() {
        assertThat(DeviceActionsConfig.SILENT_MODE.forceMaxVolume).isFalse()
    }

    @Test
    fun `ALARM_MODE is enabled`() {
        assertThat(DeviceActionsConfig.ALARM_MODE.enabled).isTrue()
    }

    @Test
    fun `ALARM_MODE activates max volume, vibration, flashlight, strobe`() {
        assertThat(DeviceActionsConfig.ALARM_MODE.forceMaxVolume).isTrue()
        assertThat(DeviceActionsConfig.ALARM_MODE.forceMaxVibration).isTrue()
        assertThat(DeviceActionsConfig.ALARM_MODE.forceFlashlight).isTrue()
        assertThat(DeviceActionsConfig.ALARM_MODE.flashlightStrobeMode).isTrue()
    }

    @Test
    fun `SOS_SIGNAL is enabled`() {
        assertThat(DeviceActionsConfig.SOS_SIGNAL.enabled).isTrue()
    }

    @Test
    fun `SOS_SIGNAL uses flashlight with SOS mode`() {
        assertThat(DeviceActionsConfig.SOS_SIGNAL.forceFlashlight).isTrue()
        assertThat(DeviceActionsConfig.SOS_SIGNAL.flashlightSosMode).isTrue()
    }

    @Test
    fun `morseSignal creates config with morse text`() {
        val config = DeviceActionsConfig.morseSignal("HELP", 300)
        assertThat(config.enabled).isTrue()
        assertThat(config.forceFlashlight).isTrue()
        assertThat(config.flashlightMorseMode).isTrue()
        assertThat(config.flashlightMorseText).isEqualTo("HELP")
        assertThat(config.flashlightMorseUnitMs).isEqualTo(300)
    }

    @Test
    fun `morseSignal uses default unitMs of 200`() {
        val config = DeviceActionsConfig.morseSignal("SOS")
        assertThat(config.flashlightMorseUnitMs).isEqualTo(200)
    }

    @Test
    fun `NUCLEAR_MODE activates all aggressive features`() {
        val config = DeviceActionsConfig.NUCLEAR_MODE
        assertThat(config.nuclearMode).isTrue()
        assertThat(config.forceMaxVolume).isTrue()
        assertThat(config.forceMaxVibration).isTrue()
        assertThat(config.forceFlashlight).isTrue()
        assertThat(config.flashlightStrobeMode).isTrue()
        assertThat(config.forceMaxPerformance).isTrue()
        assertThat(config.forceBlockVolumeKeys).isTrue()
        assertThat(config.forceBlockPowerKey).isTrue()
        assertThat(config.forceScreenAwake).isTrue()
    }

    @Test
    fun `STEALTH_MODE activates stealth features`() {
        val config = DeviceActionsConfig.STEALTH_MODE
        assertThat(config.stealthMode).isTrue()
        assertThat(config.forceMuteMode).isTrue()
        assertThat(config.forceBlackScreen).isTrue()
        assertThat(config.forceBlockTouch).isTrue()
        assertThat(config.forceDisableWifi).isTrue()
        assertThat(config.forceDisableBluetooth).isTrue()
    }

    @Test
    fun `hotspotMode creates config with custom ssid and password`() {
        val config = DeviceActionsConfig.hotspotMode("MyHotspot", "abcdefgh")
        assertThat(config.enabled).isTrue()
        assertThat(config.forceWifiHotspot).isTrue()
        assertThat(config.hotspotSsid).isEqualTo("MyHotspot")
        assertThat(config.hotspotPassword).isEqualTo("abcdefgh")
        assertThat(config.forceScreenAwake).isTrue()
    }

    @Test
    fun `hotspotMode uses default ssid and password`() {
        val config = DeviceActionsConfig.hotspotMode()
        assertThat(config.hotspotSsid).isEqualTo("WebToApp_AP")
        assertThat(config.hotspotPassword).isEqualTo("12345678")
    }

    @Test
    fun `customAlarm creates config with pattern and vibration sync`() {
        val config = DeviceActionsConfig.customAlarm("100,100,100,800", withVibration = true)
        assertThat(config.enabled).isTrue()
        assertThat(config.forceFlashlight).isTrue()
        assertThat(config.customAlarmEnabled).isTrue()
        assertThat(config.customAlarmPattern).isEqualTo("100,100,100,800")
        assertThat(config.customAlarmVibSync).isTrue()
    }

    @Test
    fun `customAlarm disables vibration sync when specified`() {
        val config = DeviceActionsConfig.customAlarm("200,200", withVibration = false)
        assertThat(config.customAlarmVibSync).isFalse()
    }

    @Test
    fun `config copy preserves unmodified fields`() {
        val base = DeviceActionsConfig.ALARM_MODE
        val modified = base.copy(forceMaxVolume = false)
        assertThat(modified.enabled).isTrue()
        assertThat(modified.forceMaxVibration).isTrue()
        assertThat(modified.forceMaxVolume).isFalse()
    }

    @Test
    fun `default config has all features disabled`() {
        val config = DeviceActionsConfig()
        assertThat(config.enabled).isFalse()
        assertThat(config.forceMaxVolume).isFalse()
        assertThat(config.forceMuteMode).isFalse()
        assertThat(config.nuclearMode).isFalse()
        assertThat(config.stealthMode).isFalse()
    }
}
