package com.example.dynamic_fare.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream

fun saveQRCodeAsPDF(qrBitmap: Bitmap, context: Context) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(300, 300, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    canvas.drawBitmap(qrBitmap, 0f, 0f, null)
    pdfDocument.finishPage(page)

    val file = File(context.getExternalFilesDir(null), "QRCode.pdf")
    pdfDocument.writeTo(FileOutputStream(file))
    pdfDocument.close()
    Toast.makeText(context, "QR Code saved as PDF!", Toast.LENGTH_LONG).show()
}
