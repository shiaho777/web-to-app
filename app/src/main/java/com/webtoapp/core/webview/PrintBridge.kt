package com.webtoapp.core.webview

import android.content.Context
import android.os.Build
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class PrintBridge(
    private val context: Context,
    private val scope: CoroutineScope,
    private val webViewProvider: () -> WebView? = { null }
) {
    companion object {
        const val JS_INTERFACE_NAME = "AndroidPrint"

        fun getInjectionScript(): String {
            return """
            (function() {
                if (window._wtaPrintBridgeHooked) return;
                window._wtaPrintBridgeHooked = true;

                if (!window.AndroidPrint) return;

                window.print = function() {
                    try {
                        if (window.AndroidPrint && window.AndroidPrint.printPage) {
                            window.AndroidPrint.printPage();
                        } else {
                            console.warn('[PrintBridge] printPage not available');
                        }
                    } catch(e) {
                        console.error('[PrintBridge] print() error:', e);
                    }
                };

                var originalWindowOpen = window.open;
                window.open = function(url, target, features) {
                    if (url && typeof url === 'string' && url.indexOf('blob:') === 0) {
                        try {
                            var blob = window.__wtaBlobMap && window.__wtaBlobMap.get(url);
                            if (blob) {
                                var isPdf = blob.type === 'application/pdf';
                                var hasPdfData = false;
                                if (!isPdf) {
                                    try {
                                        var u8 = new Uint8Array(blob.size > 2048 ? blob.slice(0, 2048).arrayBuffer() : blob.arrayBuffer());
                                        var header = '';
                                        for (var i = 0; i < Math.min(u8.length, 5); i++) {
                                            header += String.fromCharCode(u8[i]);
                                        }
                                        if (header.indexOf('%PDF') === 0) hasPdfData = true;
                                    } catch(e2) {}
                                }
                                if (isPdf || hasPdfData) {
                                    if (window.AndroidPrint && window.AndroidPrint.printBlob) {
                                        var fname = 'document.pdf';
                                        window.AndroidPrint.printBlob(url, fname);
                                        return null;
                                    }
                                }
                            }
                        } catch(e) {
                            console.error('[PrintBridge] blob print intercept error:', e);
                        }
                    }

                    if (url && typeof url === 'string' && url.indexOf('data:application/pdf') === 0) {
                        try {
                            if (window.AndroidPrint && window.AndroidPrint.printDataUrl) {
                                window.AndroidPrint.printDataUrl(url, 'document.pdf');
                                return null;
                            }
                        } catch(e) {
                            console.error('[PrintBridge] data URL print intercept error:', e);
                        }
                    }

                    if (originalWindowOpen) {
                        return originalWindowOpen.apply(window, arguments);
                    }
                    return null;
                };

                document.addEventListener('click', function(e) {
                    var target = e.target;
                    while (target && target.tagName !== 'A') {
                        target = target.parentElement;
                    }
                    if (!target || target.tagName !== 'A') return;

                    var href = target.href || '';
                    if (href.indexOf('blob:') !== 0) return;

                    try {
                        var blob = window.__wtaBlobMap && window.__wtaBlobMap.get(href);
                        if (!blob) return;

                        var isPdf = blob.type === 'application/pdf';
                        var hasPdfData = false;
                        if (!isPdf) {
                            try {
                                var u8 = new Uint8Array(blob.size > 2048 ? blob.slice(0, 2048).arrayBuffer() : blob.arrayBuffer());
                                var header = '';
                                for (var i = 0; i < Math.min(u8.length, 5); i++) {
                                    header += String.fromCharCode(u8[i]);
                                }
                                if (header.indexOf('%PDF') === 0) hasPdfData = true;
                            } catch(e2) {}
                        }

                        if ((isPdf || hasPdfData) && !target.hasAttribute('download') && window.AndroidPrint && window.AndroidPrint.printBlob) {
                            e.preventDefault();
                            e.stopPropagation();
                            var fname = target.getAttribute('download') || 'document.pdf';
                            window.AndroidPrint.printBlob(href, fname);
                            return false;
                        }
                    } catch(err) {
                        console.error('[PrintBridge] click intercept error:', err);
                    }
                }, true);

                console.log('[PrintBridge] Injection complete, window.print() hooked');
            })();
        """.trimIndent()
        }
    }

    @JavascriptInterface
    fun printPage() {
        scope.launch(Dispatchers.Main) {
            try {
                val webView = webViewProvider()
                if (webView == null) {
                    AppLogger.w("PrintBridge", "WebView not available for printing")
                    Toast.makeText(context, Strings.printNotAvailable, Toast.LENGTH_SHORT).show()
                    return@launch
                }

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    Toast.makeText(context, Strings.printNotAvailable, Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                if (printManager == null) {
                    Toast.makeText(context, Strings.printNotAvailable, Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val jobName = webView.title?.takeIf { it.isNotBlank() }
                    ?: webView.url?.substringAfterLast('/')?.takeIf { it.isNotBlank() }
                    ?: Strings.printDocument

                val printAdapter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    WebView.createPrintDocumentAdapter(webView, jobName)
                } else {
                    @Suppress("DEPRECATION")
                    webView.createPrintDocumentAdapter()
                }

                val printAttrs = PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.UNKNOWN_LANDSCAPE)
                    .setResolution(PrintAttributes.Resolution("default", "default", 300, 300))
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build()

                printManager.print(jobName, printAdapter, printAttrs)
                Toast.makeText(context, Strings.printStarted, Toast.LENGTH_SHORT).show()
                AppLogger.d("PrintBridge", "Print job started: $jobName")
            } catch (e: Exception) {
                AppLogger.e("PrintBridge", "Failed to start print job", e)
                Toast.makeText(context, Strings.printFailed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    @JavascriptInterface
    fun printBlob(blobUrl: String, filename: String) {
        scope.launch(Dispatchers.Main) {
            try {
                val webView = webViewProvider()
                if (webView == null) {
                    Toast.makeText(context, Strings.printNotAvailable, Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val safeBlobUrl = org.json.JSONObject.quote(blobUrl)
                val safeFilename = org.json.JSONObject.quote(filename)

                webView.evaluateJavascript("""
                    (function() {
                        try {
                            var blobUrl = $safeBlobUrl;
                            var filename = $safeFilename;

                            var blob = window.__wtaBlobMap && window.__wtaBlobMap.get(blobUrl);
                            if (!blob) {
                                fetch(blobUrl).then(function(r) { return r.blob(); }).then(function(b) {
                                    sendBlobForPrint(b, filename);
                                }).catch(function(err) {
                                    console.error('[PrintBridge] blob fetch failed:', err);
                                    if (window.AndroidPrint && window.AndroidPrint.showToast) {
                                        window.AndroidPrint.showToast('${Strings.printFailed}');
                                    }
                                });
                            } else {
                                sendBlobForPrint(blob, filename);
                            }

                            function sendBlobForPrint(blob, fname) {
                                var mimeType = blob.type || 'application/pdf';
                                var reader = new FileReader();
                                reader.onloadend = function() {
                                    var base64Data = reader.result.split(',')[1];
                                    if (window.AndroidPrint && window.AndroidPrint.printBase64Pdf) {
                                        window.AndroidPrint.printBase64Pdf(base64Data, fname, mimeType);
                                    }
                                };
                                reader.onerror = function() {
                                    console.error('[PrintBridge] FileReader error');
                                    if (window.AndroidPrint && window.AndroidPrint.showToast) {
                                        window.AndroidPrint.showToast('${Strings.printFailed}');
                                    }
                                };
                                reader.readAsDataURL(blob);
                            }
                        } catch(e) {
                            console.error('[PrintBridge] printBlob error:', e);
                        }
                    })();
                """.trimIndent(), null)

                Toast.makeText(context, Strings.printPreparing, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                AppLogger.e("PrintBridge", "Failed to print blob", e)
                Toast.makeText(context, Strings.printFailed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    @JavascriptInterface
    fun printDataUrl(dataUrl: String, filename: String) {
        try {
            val parts = dataUrl.split(",")
            val meta = parts[0]
            val base64Data = parts.getOrElse(1) { "" }
            val mimeMatch = Regex("data:([^;]+)").find(meta)
            val mimeType = mimeMatch?.groupValues?.get(1) ?: "application/pdf"
            printBase64Pdf(base64Data, filename, mimeType)
        } catch (e: Exception) {
            AppLogger.e("PrintBridge", "Failed to print data URL", e)
            scope.launch(Dispatchers.Main) {
                Toast.makeText(context, Strings.printFailed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    @JavascriptInterface
    fun printBase64Pdf(base64Data: String, filename: String, mimeType: String) {
        scope.launch(Dispatchers.IO) {
            var tempFile: File? = null
            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, Strings.printNotAvailable, Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                tempFile = File(context.cacheDir, "print_${System.currentTimeMillis()}.pdf")

                FileOutputStream(tempFile).use { fos ->
                    fos.write(decodedBytes)
                }

                withContext(Dispatchers.Main) {
                    val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                    if (printManager == null) {
                        Toast.makeText(context, Strings.printNotAvailable, Toast.LENGTH_SHORT).show()
                        return@withContext
                    }

                    val jobName = filename.ifBlank { Strings.printDocument }
                    val printAdapter = PdfPrintDocumentAdapter(tempFile!!.absolutePath)

                    val printAttrs = PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.UNKNOWN_LANDSCAPE)
                        .setResolution(PrintAttributes.Resolution("default", "default", 300, 300))
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build()

                    printManager.print(jobName, printAdapter, printAttrs)
                    Toast.makeText(context, Strings.printStarted, Toast.LENGTH_SHORT).show()
                    AppLogger.d("PrintBridge", "PDF print job started: $jobName (${decodedBytes.size} bytes)")
                }
            } catch (e: Exception) {
                AppLogger.e("PrintBridge", "Failed to print base64 PDF", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, Strings.printFailed, Toast.LENGTH_SHORT).show()
                }
            } finally {
                tempFile?.let { f ->
                    scope.launch(Dispatchers.IO) {
                        try {
                            Thread.sleep(60000)
                            if (f.exists()) f.delete()
                        } catch (e: Exception) {
                            AppLogger.w("PrintBridge", "Failed to cleanup temp print file", e)
                        }
                    }
                }
            }
        }
    }

    @JavascriptInterface
    fun showToast(message: String) {
        scope.launch(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
