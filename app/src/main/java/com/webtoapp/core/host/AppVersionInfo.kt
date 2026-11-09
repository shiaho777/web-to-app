package com.webtoapp.core.host

import android.content.Context
import android.content.pm.PackageManager

object AppVersionInfo {

    data class Info(val name: String, val code: Long)

    @Volatile
    private var memo: Info? = null

    fun peek(): Info? = memo

    fun read(context: Context): Info = memo ?: synchronized(this) {
        memo ?: load(context.applicationContext).also { memo = it }
    }

    fun warm(context: Context) {
        read(context)
    }

    private fun load(context: Context): Info {
        return try {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            val code = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                info.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                info.versionCode.toLong()
            }
            Info(info.versionName ?: "", code)
        } catch (_: PackageManager.NameNotFoundException) {
            Info("", 0L)
        }
    }
}
