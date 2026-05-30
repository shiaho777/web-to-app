package com.webtoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FontDownload
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.webtoapp.core.extension.ExtensionModule
import com.webtoapp.core.extension.ModuleSourceType
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.theme.LocalIsDarkTheme
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionSourceBrowserDialog(
    module: ExtensionModule,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isChromeExt = module.sourceType == ModuleSourceType.CHROME_EXTENSION && module.chromeExtId.isNotEmpty()

    var selectedFilePath by remember { mutableStateOf<String?>(null) }
    var selectedFileContent by remember { mutableStateOf("") }
    var selectedFileName by remember { mutableStateOf("") }

    val fileTree = remember(module.id) {
        if (isChromeExt) buildExtensionFileTree(context, module) else null
    }

    val expandedDirs = remember { mutableStateMapOf<String, Boolean>() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(WtaRadius.Card))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                if (selectedFilePath != null) selectedFileName else module.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (selectedFilePath != null) {
                                Text(
                                    selectedFilePath ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (selectedFilePath != null) {
                                selectedFilePath = null
                            } else {
                                onDismiss()
                            }
                        }) {
                            Icon(
                                if (selectedFilePath != null) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Close,
                                contentDescription = null
                            )
                        }
                    }
                )

                if (selectedFilePath != null) {
                    FileContentView(
                        content = selectedFileContent,
                        fileName = selectedFileName,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (isChromeExt && fileTree != null) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        fileTree.children.sortedWith(compareBy({ !it.isDirectory }, { it.name })).forEach { node ->
                            fileTreeItems(
                                node = node,
                                depth = 0,
                                expandedDirs = expandedDirs,
                                onFileClick = { path, name ->
                                    val content = readExtensionFile(context, module, path)
                                    selectedFileContent = content ?: Strings.cannotReadFile
                                    selectedFileName = name
                                    selectedFilePath = path
                                }
                            )
                        }
                    }
                } else {
                    FileContentView(
                        content = module.code,
                        fileName = if (module.sourceType == ModuleSourceType.CHROME_EXTENSION) "content.js"
                            else "${module.name}.user.js",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

private data class FileNode(
    val name: String,
    val relativePath: String,
    val isDirectory: Boolean,
    val size: Long = 0,
    val children: MutableList<FileNode> = mutableListOf()
)

private fun LazyListScope.fileTreeItems(
    node: FileNode,
    depth: Int,
    expandedDirs: MutableMap<String, Boolean>,
    onFileClick: (path: String, name: String) -> Unit
) {
    val isExpanded = expandedDirs[node.relativePath] ?: (depth == 0)

    item(key = node.relativePath) {
        FileTreeRow(
            node = node,
            depth = depth,
            isExpanded = isExpanded,
            onClick = {
                if (node.isDirectory) {
                    expandedDirs[node.relativePath] = !isExpanded
                } else {
                    onFileClick(node.relativePath, node.name)
                }
            }
        )
    }

    if (node.isDirectory && isExpanded) {
        node.children.sortedWith(compareBy({ !it.isDirectory }, { it.name })).forEach { child ->
            fileTreeItems(child, depth + 1, expandedDirs, onFileClick)
        }
    }
}

@Composable
private fun FileTreeRow(
    node: FileNode,
    depth: Int,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = (16 + depth * 20).dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (node.isDirectory) {
            Icon(
                if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        } else {
            Icon(
                getFileIcon(node.name),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = getFileIconColor(node.name)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            node.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (node.isDirectory) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(weight = 1f, fill = true)
        )

        if (!node.isDirectory && node.size > 0) {
            Text(
                formatFileSize(node.size),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        if (node.isDirectory) {
            Icon(
                if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FileContentView(
    content: String,
    fileName: String,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isBinary = content.any { it < ' ' && it != '\n' && it != '\r' && it != '\t' }
    val isImage = fileName.lowercase().let {
        it.endsWith(".png") || it.endsWith(".jpg") || it.endsWith(".jpeg") ||
            it.endsWith(".gif") || it.endsWith(".svg") || it.endsWith(".webp") || it.endsWith(".ico")
    }

    Column(modifier = modifier.padding(horizontal = 12.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(WtaRadius.Button))
                .background(
                    if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.10f)
                    else Color.White.copy(alpha = 0.72f)
                )
                .padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    getFileIcon(fileName),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = getFileIconColor(fileName)
                )
                Text(
                    fileName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.weight(weight = 1f, fill = true))
                Text(
                    "${content.length} chars",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        if (isImage) {
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "🖼️ ${Strings.imageFile}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (isBinary) {
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    Strings.binaryFile,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val lines = content.lines()
            val lineNumWidth = lines.size.toString().length

            Text(
                buildLineNumberedAnnotated(lines, lineNumWidth),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            )
        }
    }
}

private fun buildLineNumberedAnnotated(lines: List<String>, lineNumWidth: Int): AnnotatedString {
    return buildAnnotatedString {
        val maxLines = 10000
        lines.take(maxLines).forEachIndexed { index, line ->
            val lineNum = (index + 1).toString().padStart(lineNumWidth)
            pushStyle(SpanStyle(color = Color.Gray))
            append("$lineNum  ")
            pop()
            append(line)
            if (index < lines.size - 1) append("\n")
        }
        if (lines.size > maxLines) {
            append("\n\n... (${lines.size} lines total)")
        }
    }
}

@Composable
private fun getFileIcon(fileName: String): ImageVector = when {
    fileName.endsWith(".js") || fileName.endsWith(".mjs") -> Icons.Outlined.Code
    fileName.endsWith(".ts") || fileName.endsWith(".tsx") -> Icons.Outlined.Code
    fileName.endsWith(".css") || fileName.endsWith(".scss") -> Icons.Outlined.Palette
    fileName.endsWith(".html") || fileName.endsWith(".htm") -> Icons.Outlined.Language
    fileName.endsWith(".json") -> Icons.Outlined.DataObject
    fileName.endsWith(".md") || fileName.endsWith(".txt") -> Icons.Outlined.Description
    fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".svg") ||
        fileName.endsWith(".gif") || fileName.endsWith(".webp") || fileName.endsWith(".ico") -> Icons.Outlined.Image
    fileName.endsWith(".woff") || fileName.endsWith(".woff2") || fileName.endsWith(".ttf") -> Icons.Outlined.FontDownload
    fileName == "manifest.json" -> Icons.Outlined.Settings
    fileName == "LICENSE" || fileName.startsWith("LICENSE") -> Icons.Outlined.Gavel
    else -> Icons.Outlined.InsertDriveFile
}

@Composable
private fun getFileIconColor(fileName: String): Color = when {
    fileName.endsWith(".js") || fileName.endsWith(".mjs") -> MaterialTheme.colorScheme.tertiary
    fileName.endsWith(".ts") || fileName.endsWith(".tsx") -> MaterialTheme.colorScheme.primary
    fileName.endsWith(".css") || fileName.endsWith(".scss") -> MaterialTheme.colorScheme.secondary
    fileName.endsWith(".html") || fileName.endsWith(".htm") -> MaterialTheme.colorScheme.error
    fileName.endsWith(".json") -> MaterialTheme.colorScheme.primary
    fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".svg") ||
        fileName.endsWith(".gif") -> MaterialTheme.colorScheme.tertiary
    fileName == "manifest.json" -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

private fun formatFileSize(bytes: Long): String = when {
    bytes < 1024 -> "${bytes}B"
    bytes < 1024 * 1024 -> "${"%.1f".format(bytes / 1024.0)}KB"
    else -> "${"%.1f".format(bytes / (1024.0 * 1024.0))}MB"
}

private fun buildExtensionFileTree(
    context: android.content.Context,
    module: ExtensionModule
): FileNode? {
    val extId = module.chromeExtId
    if (extId.isEmpty()) return null

    return if (module.builtIn) {
        buildAssetFileTree(context, "extensions/$extId", extId)
    } else {
        val extDir = File(context.filesDir, "extensions/$extId")
        if (extDir.exists() && extDir.isDirectory) buildFileSystemTree(extDir, "") else null
    }
}

private fun buildAssetFileTree(
    context: android.content.Context,
    assetPath: String,
    name: String
): FileNode {
    val root = FileNode(name = name, relativePath = "", isDirectory = true)

    fun walkAssets(currentPath: String, parent: FileNode) {
        try {
            val children = context.assets.list(currentPath) ?: return
            for (child in children) {
                val childPath = "$currentPath/$child"
                val relativePath = childPath.removePrefix("extensions/$name/")
                val subChildren = context.assets.list(childPath)

                if (subChildren != null && subChildren.isNotEmpty()) {
                    val dirNode = FileNode(name = child, relativePath = relativePath, isDirectory = true)
                    walkAssets(childPath, dirNode)
                    parent.children.add(dirNode)
                } else {
                    val size = try {
                        context.assets.open(childPath).use { it.available().toLong() }
                    } catch (e: Exception) {
                        0L
                    }
                    parent.children.add(
                        FileNode(name = child, relativePath = relativePath, isDirectory = false, size = size)
                    )
                }
            }
        } catch (_: Exception) {

        }
    }

    walkAssets(assetPath, root)
    return root
}

private fun buildFileSystemTree(dir: File, relativePath: String): FileNode {
    val root = FileNode(name = dir.name, relativePath = relativePath, isDirectory = true)

    dir.listFiles()?.forEach { file ->
        val childRelative = if (relativePath.isEmpty()) file.name else "$relativePath/${file.name}"
        if (file.isDirectory) {
            root.children.add(buildFileSystemTree(file, childRelative))
        } else {
            root.children.add(
                FileNode(name = file.name, relativePath = childRelative, isDirectory = false, size = file.length())
            )
        }
    }

    return root
}

private fun readExtensionFile(
    context: android.content.Context,
    module: ExtensionModule,
    relativePath: String
): String? {
    val extId = module.chromeExtId

    return try {
        if (module.builtIn) {
            context.assets.open("extensions/$extId/$relativePath").bufferedReader().use { it.readText() }
        } else {
            val file = File(context.filesDir, "extensions/$extId/$relativePath")
            if (file.exists()) file.readText() else null
        }
    } catch (_: Exception) {
        null
    }
}
