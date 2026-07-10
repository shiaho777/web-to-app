package com.webtoapp.core.aicoding.tool

data class ToolResult(
    val text: String,
    val isError: Boolean = false,
    val images: List<ImageAttachment> = emptyList(),
    val fileChange: FileChange? = null,
    val planReviewPath: String? = null
) {
    val isMultimodal: Boolean get() = images.isNotEmpty()

    companion object {
        fun ok(text: String, fileChange: FileChange? = null): ToolResult =
            ToolResult(text = text, isError = false, fileChange = fileChange)

        fun okPlanReview(text: String, planPath: String): ToolResult =
            ToolResult(text = text, isError = false, planReviewPath = planPath)

        fun error(text: String): ToolResult =
            ToolResult(text = text, isError = true)

        fun multimodal(
            text: String,
            images: List<ImageAttachment>,
            fileChange: FileChange? = null
        ): ToolResult = ToolResult(
            text = text,
            isError = false,
            images = images,
            fileChange = fileChange
        )
    }
}

data class ImageAttachment(
    val bytes: ByteArray,
    val mimeType: String = "image/png",

    val path: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ImageAttachment) return false
        return mimeType == other.mimeType && path == other.path && bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + (path?.hashCode() ?: 0)
        return result
    }
}

data class FileChange(
    val path: String,
    val kind: Kind,

    val newContent: String? = null
) {
    enum class Kind { WRITE, EDIT, DELETE }
}
