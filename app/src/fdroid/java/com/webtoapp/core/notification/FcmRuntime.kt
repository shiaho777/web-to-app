package com.webtoapp.core.notification

import android.content.Context
import com.webtoapp.core.logging.AppLogger

/** F-Droid builds do not ship Firebase. */
internal object FcmRuntime {
    @Suppress("UNUSED_PARAMETER")
    fun start(context: Context, config: NotificationFcmManager.FcmConfig) {
        AppLogger.i("FcmRuntime", "FCM is not included in this build")
    }
}
