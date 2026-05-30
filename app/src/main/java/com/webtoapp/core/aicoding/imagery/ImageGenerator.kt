package com.webtoapp.core.aicoding.imagery

import com.webtoapp.data.model.ApiKeyConfig
import com.webtoapp.data.model.SavedModel

interface ImageGenerator {

    suspend fun generate(
        request: Request,
        model: SavedModel,
        apiKey: ApiKeyConfig
    ): Result

    fun supports(model: SavedModel): Boolean

    data class Request(
        val prompt: String,
        val negativePrompt: String? = null,
        val style: String? = null,

        val size: String = "1024",

        val referenceImage: ReferenceImage? = null
    )

    data class ReferenceImage(val bytes: ByteArray, val mimeType: String) {
        override fun equals(other: Any?): Boolean =
            other is ReferenceImage && mimeType == other.mimeType && bytes.contentEquals(other.bytes)

        override fun hashCode(): Int = 31 * mimeType.hashCode() + bytes.contentHashCode()
    }

    sealed class Result {
        data class Ok(val bytes: ByteArray, val mimeType: String = "image/png") : Result()
        data class Error(val message: String) : Result()
    }
}

class ImageGeneratorRegistry(private val generators: List<ImageGenerator>) {
    fun generatorFor(model: SavedModel): ImageGenerator? =
        generators.firstOrNull { it.supports(model) }

    fun isEmpty(): Boolean = generators.isEmpty()
}
