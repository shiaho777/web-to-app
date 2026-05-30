package com.webtoapp.core.aicoding.permission

enum class PermissionMode { Default, AutoApprove, Plan, Dream }

enum class PermissionDecision { Allow, Deny }

data class PermissionRequest(
    val toolCallId: String,
    val toolName: String,
    val activity: String?,

    val argsPreview: Map<String, String>
)

sealed class PermissionResponse {
    object Allow : PermissionResponse()
    object Deny : PermissionResponse()

    object AlwaysAllow : PermissionResponse()
}
