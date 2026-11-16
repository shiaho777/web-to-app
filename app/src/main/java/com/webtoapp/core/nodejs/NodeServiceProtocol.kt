package com.webtoapp.core.nodejs

object NodeServiceProtocol {

    const val MAX_HEALTH_CHECK_RETRIES = 60
    const val HEALTH_CHECK_INTERVAL_MS = 500L
    const val HEALTH_PROBE_TIMEOUT_MS = 500

    const val SERVER_READY_BUDGET_MS =
        MAX_HEALTH_CHECK_RETRIES * (2L * HEALTH_PROBE_TIMEOUT_MS + HEALTH_CHECK_INTERVAL_MS)

    const val START_REPLY_TIMEOUT_MS = SERVER_READY_BUDGET_MS + 15_000L

    const val MSG_START_SERVER = 1

    const val MSG_STOP_SERVER = 2

    const val MSG_KILL_ENGINE = 3

    const val MSG_QUERY_STATUS = 4

    const val MSG_SERVER_STARTED = 100

    const val MSG_SERVER_FAILED = 101

    const val MSG_SERVER_STOPPED = 102

    const val MSG_STATUS = 103

    object Keys {

        const val PROJECT_DIR = "projectDir"
        const val ENTRY_FILE = "entryFile"
        const val PORT_PREF = "portPref"
        const val PORT_CONFLICT_MODE = "portConflictMode"
        const val ENV_VARS = "envVars"
        const val REQUEST_ID = "requestId"

        const val ACTUAL_PORT = "actualPort"
        const val SERVER_URL = "serverUrl"
        const val ERROR_MESSAGE = "errorMessage"
        const val V8_STARTED = "v8Started"
        const val SERVER_RUNNING = "serverRunning"
    }
}
