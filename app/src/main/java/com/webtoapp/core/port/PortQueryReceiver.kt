package com.webtoapp.core.port

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.webtoapp.core.logging.AppLogger
import org.json.JSONArray
import org.json.JSONObject

class PortQueryReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_PORT_QUERY) return

        val pendingResult = goAsync()
        try {
            val allocations = PortManager.getAllAllocations()
            val arr = JSONArray()
            for ((port, alloc) in allocations) {
                val obj = JSONObject().apply {
                    put("port", port)
                    put("owner", alloc.owner)
                    put("range", alloc.range.name)
                    put("allocatedAt", alloc.allocatedAt)
                    put("pid", alloc.pid)
                    put("alive", PortManager.isProcessAlive(port))
                }
                arr.put(obj)
            }

            val bundle = Bundle().apply {
                putInt(EXTRA_PROTOCOL_VERSION, PROTOCOL_VERSION)
                putString(EXTRA_PACKAGE, context.packageName)
                putInt(EXTRA_PROCESS_PID, android.os.Process.myPid())
                putString(EXTRA_ALLOCATIONS, arr.toString())
            }

            pendingResult.resultCode = RESULT_CODE_OK
            pendingResult.resultData = arr.toString()

            pendingResult.setResultExtras(bundle)
        } catch (e: Exception) {
            AppLogger.w(TAG, "PortQueryReceiver failed: ${e.message}")
            pendingResult.resultCode = RESULT_CODE_ERROR
        } finally {
            pendingResult.finish()
        }
    }

    companion object {
        private const val TAG = "PortQueryReceiver"

        const val PROTOCOL_VERSION = 1

        const val ACTION_PORT_QUERY = "com.webtoapp.action.PORT_QUERY"

        const val EXTRA_PROTOCOL_VERSION = "wta.protocolVersion"
        const val EXTRA_PACKAGE = "wta.package"
        const val EXTRA_PROCESS_PID = "wta.pid"
        const val EXTRA_ALLOCATIONS = "wta.allocations"

        const val RESULT_CODE_OK = 1
        const val RESULT_CODE_ERROR = -1
    }
}
