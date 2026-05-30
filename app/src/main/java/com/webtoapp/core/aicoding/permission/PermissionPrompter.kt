package com.webtoapp.core.aicoding.permission

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PermissionPrompter {

    private val _requests = MutableSharedFlow<PermissionRequest>(replay = 0, extraBufferCapacity = 4)
    val requests: SharedFlow<PermissionRequest> = _requests.asSharedFlow()

    private val _choices = MutableSharedFlow<ChoiceRequest>(replay = 0, extraBufferCapacity = 4)
    val choices: SharedFlow<ChoiceRequest> = _choices.asSharedFlow()

    private val responseChannel = Channel<Pair<String, PermissionResponse>>(Channel.UNLIMITED)
    private val choiceResponseChannel = Channel<Pair<String, ChoiceResponse>>(Channel.UNLIMITED)

    private val singleFlight = Mutex()

    suspend fun request(req: PermissionRequest): PermissionResponse = singleFlight.withLock {
        _requests.emit(req)

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
        _choices.emit(req)
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
