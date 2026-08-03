package com.webtoapp.core.agent.permission

import com.webtoapp.core.logging.AppLogger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

class PermissionPrompter {

    // Requests/choices are delivered to the UI through Channels (exposed as flows via
    // receiveAsFlow) rather than replay-0 SharedFlows. A Channel guarantees each emitted
    // request is received by the UI collector (buffered until consumed), so a request can
    // never be lost to a subscribe/emit race — which previously left request()/askChoice()
    // suspended forever on the response channel, freezing the agent loop after a prompt.
    private val requestChannel = Channel<PermissionRequest>(Channel.UNLIMITED)
    val requests: Flow<PermissionRequest> = requestChannel.receiveAsFlow()

    private val choiceChannel = Channel<ChoiceRequest>(Channel.UNLIMITED)
    val choices: Flow<ChoiceRequest> = choiceChannel.receiveAsFlow()

    private val responseChannel = Channel<Pair<String, PermissionResponse>>(Channel.UNLIMITED)
    private val choiceResponseChannel = Channel<Pair<String, ChoiceResponse>>(Channel.UNLIMITED)

    private val singleFlight = Mutex()

    suspend fun request(req: PermissionRequest): PermissionResponse = singleFlight.withLock {
        requestChannel.send(req)

        // Guard against the UI never responding (e.g. it was destroyed mid-prompt and the
        // pending request was lost). Without this the engine would hang forever waiting on
        // the response channel. On timeout we deny the tool so the turn can continue/end.
        val resp = withTimeoutOrNull(PROMPT_TIMEOUT_MS) {
            while (true) {
                val (id, r) = responseChannel.receive()
                if (id == req.toolCallId) return@withTimeoutOrNull r
            }
            @Suppress("UNREACHABLE_CODE") PermissionResponse.Deny
        }
        resp ?: run {
            AppLogger.w(TAG, "permission request '${req.toolCallId}' timed out after ${PROMPT_TIMEOUT_MS}ms — denying")
            PermissionResponse.Deny
        }
    }

    fun respond(toolCallId: String, response: PermissionResponse) {
        responseChannel.trySend(toolCallId to response)
    }

    suspend fun askChoice(req: ChoiceRequest): ChoiceResponse = singleFlight.withLock {
        choiceChannel.send(req)
        // Same rationale as request(): if the choice sheet is never shown or answered,
        // cancel after a grace period so the agent loop doesn't hang indefinitely.
        val resp = withTimeoutOrNull(PROMPT_TIMEOUT_MS) {
            while (true) {
                val (id, r) = choiceResponseChannel.receive()
                if (id == req.id) return@withTimeoutOrNull r
            }
            @Suppress("UNREACHABLE_CODE") ChoiceResponse.Cancelled
        }
        resp ?: run {
            AppLogger.w(TAG, "choice request '${req.id}' timed out after ${PROMPT_TIMEOUT_MS}ms — cancelling")
            ChoiceResponse.Cancelled
        }
    }

    fun respondChoice(requestId: String, response: ChoiceResponse) {
        choiceResponseChannel.trySend(requestId to response)
    }

    companion object {
        private const val TAG = "PermissionPrompter"
        // 10 minutes — generous enough for a user who steps away, but finite so a lost
        // UI prompt can never wedge the agent loop forever.
        private const val PROMPT_TIMEOUT_MS = 10L * 60 * 1000
    }
}

data class ChoiceRequest(
    val id: String,
    val questions: List<Question>
) {
    data class Question(
        val text: String,
        val options: List<Option>,
        val multiSelect: Boolean = false,
        val allowOther: Boolean = true
    )

    data class Option(
        val label: String,
        val description: String = ""
    )
}

sealed class ChoiceResponse {

    data class Answered(val answers: List<List<String>>) : ChoiceResponse()
    object Cancelled : ChoiceResponse()
}
