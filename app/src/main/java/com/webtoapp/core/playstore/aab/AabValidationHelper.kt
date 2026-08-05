package com.webtoapp.core.playstore.aab

import com.android.aapt.Resources
import com.google.protobuf.InvalidProtocolBufferException
import com.webtoapp.core.logging.AppLogger
import java.io.File
import java.util.zip.ZipFile

/**
 * AAB validation helper to verify resource integrity before signing and uploading.
 * This prevents Google Play Console rejection due to missing resource files.
 */
object AabValidationHelper {

    private const val TAG = "AabValidationHelper"

    /** Extract all file references from the proto table. */
    private fun Resources.ResourceTable.fileReferences(): Set<String> {
        val out = mutableSetOf<String>()
        for (pkg in packageList) {
            for (type in pkg.typeList) {
                for (entry in type.entryList) {
                    for (cv in entry.configValueList) {
                        if (cv.value.item.hasFile()) {
                            out.add(cv.value.item.file.path)
                        }
                    }
                }
            }
        }
        return out
    }

    /**
     * Validate the AAB file for:
     * 1. All resources referenced in resources.pb exist as actual files
     * 2. No orphaned resource references
     * 
     * @return true if validation passes, false otherwise
     */
    fun validateAab(aabFile: File): ValidationResult {
        AppLogger.d(TAG, "Validating AAB: ${aabFile.name}")

        val issues = mutableListOf<String>()

        try {
            ZipFile(aabFile).use { zip ->
                // Check if resources.pb exists
                val resourcesPbEntry = zip.getEntry("base/resources.pb")
                    ?: run {
                        issues.add("Missing base/resources.pb (Android App Bundle format error)")
                        return@validateAab ValidationResult(false, issues)
                    }

                // Parse resources.pb and get all referenced resource paths
                val resourcesBytes = zip.getInputStream(resourcesPbEntry).readBytes()
                
                try {
                    val protoTable = Resources.ResourceTable.parseFrom(resourcesBytes)
                    
                    // Collect all file references from the resource table
                    val referencedResources = protoTable.fileReferences()
                    AppLogger.d(TAG, "Found ${referencedResources.size} resource references in resources.pb")
                    AppLogger.d(TAG, "All references validated against AAB contents")

                    // Verify each referenced resource exists in the AAB
                    for (resourcePath in referencedResources) {
                        if (zip.getEntry("base/$resourcePath") == null &&
                            zip.getEntry(resourcePath) == null &&
                            zip.getEntry("res/$resourcePath") == null) {
                            
                            issues.add(
                                "Referenced resource not found in AAB: $resourcePath\n" +
                                    "This will cause bundletool to reject the bundle with:" +
                                    "\"Resource table of module 'base' contains references to non-existing files\""
                            )
                        }
                    }

                } catch (e: InvalidProtocolBufferException) {
                    issues.add("Failed to parse base/resources.pb: ${e.message}")
                    AppLogger.e(TAG, "Invalid resources.pb", e)
                }
            }
        } catch (e: Exception) {
            issues.add("Validation error: ${e.message}")
            AppLogger.e(TAG, "AAB validation failed", e)
        }

        if (issues.isNotEmpty()) {
            AppLogger.w(
                TAG,
                "AAB validation failed with ${issues.size} issue(s):\n" +
                    issues.joinToString("\n  - ") { "  - $it" }
            )
        } else {
            AppLogger.d(TAG, "AAB validation passed successfully")
        }

        return ValidationResult(issues.isEmpty(), issues)
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val issues: List<String>
)
