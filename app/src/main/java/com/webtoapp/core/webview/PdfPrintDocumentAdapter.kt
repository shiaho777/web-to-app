package com.webtoapp.core.webview

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import androidx.annotation.RequiresApi
import com.webtoapp.core.logging.AppLogger
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@RequiresApi(Build.VERSION_CODES.KITKAT)
internal class PdfPrintDocumentAdapter(
    private val pdfFilePath: String
) : PrintDocumentAdapter() {

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback?,
        extras: Bundle?
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback?.onLayoutCancelled()
            return
        }

        val file = File(pdfFilePath)
        if (!file.exists()) {
            callback?.onLayoutFailed("PDF file not found: $pdfFilePath")
            return
        }

        val info = PrintDocumentInfo.Builder("document.pdf")
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .build()

        callback?.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<out PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback?
    ) {
        if (destination == null) {
            callback?.onWriteFailed("Destination is null")
            return
        }

        try {
            val file = File(pdfFilePath)
            if (!file.exists()) {
                callback?.onWriteFailed("PDF file not found")
                return
            }

            FileInputStream(file).use { input ->
                FileOutputStream(destination.fileDescriptor).use { output ->
                    input.copyTo(output, bufferSize = 64 * 1024)
                }
            }

            callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: Exception) {
            AppLogger.e("PdfPrintDocumentAdapter", "Failed to write PDF for printing", e)
            callback?.onWriteFailed(e.message ?: "Write failed")
        }
    }
}
