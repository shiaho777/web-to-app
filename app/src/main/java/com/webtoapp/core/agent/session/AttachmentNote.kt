package com.webtoapp.core.agent.session

import java.io.File

/**
 * Builds the `<user-attachments>` block appended to a user message before it
 * goes to the model. Attachments are PATH REFERENCES, never inlined content:
 * the note lists where each file landed in the session sandbox (with size /
 * entry count) and leaves the model to inspect them with its own tools — a
 * 1GB upload must cost one line of context, not 1GB of prompt.
 */
object AttachmentNote {

    /**
     * @param statFile resolves a sandbox-relative path to a real file, used only
     *   as a fallback for attachments persisted before [UserAttachment.sizeBytes]
     *   existed. May return null for missing paths.
     */
    fun build(attachments: List<UserAttachment>, statFile: (String) -> File?): String {
        if (attachments.isEmpty()) return ""
        val sb = StringBuilder("\n\n<user-attachments>\n")
        sb.append(
            "Files the user attached from their device, copied into the session " +
                "sandbox (paths are session-relative):\n"
        )
        attachments.forEach { att ->
            sb.append("- ").append(att.path)
            if (att.path.endsWith("/")) {
                sb.append(" — folder")
                if (att.entryCount > 0) sb.append(", ").append(att.entryCount).append(" files")
                val bytes = att.sizeBytes
                if (bytes > 0) sb.append(", ").append(formatSize(bytes))
            } else {
                val size = if (att.sizeBytes > 0) att.sizeBytes
                    else statFile(att.path)?.takeIf { it.isFile }?.length() ?: 0L
                if (size > 0) sb.append(" — ").append(formatSize(size))
                if (att.mimeType.isNotEmpty()) sb.append(" — ").append(att.mimeType)
            }
            sb.append('\n')
        }
        sb.append("</user-attachments>\n")
        sb.append(
            "Contents are NOT inlined above — inspect them with tools before relying " +
                "on them: ListFiles/Glob for listings, Read with offset/limit for text " +
                "(binary files refuse), Grep for search, ViewImage for images when available."
        )
        return sb.toString()
    }

    private fun formatSize(bytes: Long): String = when {
        bytes < 1024 -> "${bytes}B"
        bytes < 1024 * 1024 -> "${bytes / 1024}KB"
        bytes < 1024L * 1024 * 1024 -> "${bytes / (1024 * 1024)}MB"
        else -> "%.1fGB".format(bytes / (1024.0 * 1024 * 1024))
    }
}
