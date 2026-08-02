package com.webtoapp.core.agent.permission

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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

        while (true) {
            val (id, resp) = responseChannel.receive()
            if (id == req.toolCallId) return resp
        }
        @Suppress("UNREACHABLE_CODE") PermissionResponse.Deny
    }

    fun respond(toolCallId: String, response: PermissionResponse) {
        responseChannel.trySend(toolCallId to response)
    }

    suspend fun askChoice(req: ChoiceRequest): ChoiceResponse = singleFlight.withLock {
        choiceChannel.send(req)
        while (true) {
            val (id, resp) = choiceResponseChannel.receive()
            if (id == req.id) return resp
        }
        @Suppress("UNREACHABLE_CODE") ChoiceResponse.Cancelled
    }

    fun respondChoice(requestId: String, response: ChoiceResponse) {
        choiceResponseChannel.trySend(requestId to response)
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
