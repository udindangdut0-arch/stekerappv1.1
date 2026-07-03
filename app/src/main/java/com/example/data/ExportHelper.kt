package com.example.data

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

data class PaymentReportItem(
    val nomor: Int,
    val name: String,
    val mandatoryPaid: Double,
    val voluntaryPaid: Double,
    val totalPaid: Double,
    val lastPaymentDateStr: String,
    val statusStr: String
)

data class SpecialReportSummary(
    val numMandatoryPayers: Int,
    val numVoluntaryPayers: Int,
    val totalMandatoryCollected: Double,
    val totalVoluntaryCollected: Double,
    val totalFundsReceived: Double
)

object ExportHelper {

    private fun formatRupiah(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return format.format(amount).replace("Rp", "Rp ").replace(",00", "")
    }

    // 1. Export Special Payment Report to PDF
    fun exportSpecialReportToPdf(
        context: Context,
        monthName: String,
        year: Int,
        reportItems: List<PaymentReportItem>,
        summary: SpecialReportSummary,
        shareToWhatsApp: Boolean = false
    ) {
        try {
            val pdfDocument = PdfDocument()
            val paint = Paint()
            val boldPaint = Paint().apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            // Page dimensions (A4 size: 595 x 842 points)
            val pageWidth = 595
            val pageHeight = 842
            var pageNumber = 1
            
            var currentPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var currentPage = pdfDocument.startPage(currentPageInfo)
            var canvas = currentPage.canvas

            var y = 40f

            fun drawHeader() {
                // Header Background
                paint.color = Color.parseColor("#1C1C1C")
                canvas.drawRect(0f, 0f, pageWidth.toFloat(), 105f, paint)

                // App Title
                paint.color = Color.parseColor("#C91D2E")
                paint.textSize = 20f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("STEKER HITAM", 30f, 42f, paint)

                // Subtitle
                paint.color = Color.WHITE
                paint.textSize = 10f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText("LAPORAN KHUSUS ANGGOTA YANG TELAH MEMBAYAR IURAN", 30f, 60f, paint)
                
                // Period
                paint.color = Color.parseColor("#E0E0E0")
                paint.textSize = 11f
                boldPaint.color = Color.WHITE
                boldPaint.textSize = 11f
                canvas.drawText("Periode: $monthName $year", 30f, 80f, boldPaint)

                y = 130f
            }

            drawHeader()

            // Draw Rekap/Summary Box
            paint.color = Color.parseColor("#F5F5F5")
            canvas.drawRect(30f, y, (pageWidth - 30).toFloat(), y + 105f, paint)
            
            paint.color = Color.parseColor("#1C1C1C")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRect(30f, y, (pageWidth - 30).toFloat(), y + 105f, paint)
            paint.style = Paint.Style.FILL

            paint.color = Color.parseColor("#C91D2E")
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("REKAPITULASI DANA MASUK", 40f, y + 20f, paint)

            paint.color = Color.BLACK
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            
            canvas.drawText("Jumlah Anggota Bayar Iuran Wajib: ${summary.numMandatoryPayers}", 40f, y + 42f, paint)
            canvas.drawText("Jumlah Anggota Bayar Iuran Sukarela: ${summary.numVoluntaryPayers}", 40f, y + 58f, paint)

            val xCol2 = 300f
            canvas.drawText("Total Iuran Wajib Terkumpul: ${formatRupiah(summary.totalMandatoryCollected)}", xCol2, y + 42f, paint)
            canvas.drawText("Total Iuran Sukarela Terkumpul: ${formatRupiah(summary.totalVoluntaryCollected)}", xCol2, y + 58f, paint)

            // Horizontal line in box
            paint.color = Color.parseColor("#CCCCCC")
            canvas.drawLine(40f, y + 70f, (pageWidth - 40).toFloat(), y + 70f, paint)

            paint.color = Color.BLACK
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("TOTAL DANA DITERIMA:", 40f, y + 90f, paint)
            
            paint.color = Color.parseColor("#C91D2E")
            canvas.drawText(formatRupiah(summary.totalFundsReceived), xCol2, y + 90f, paint)

            y += 125f

            // Draw Table Title
            paint.color = Color.parseColor("#1C1C1C")
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("DAFTAR ANGGOTA", 30f, y, paint)
            
            y += 15f

            // Draw Table Headers
            // Columns: No (30), Nama Anggota (130), Iuran Wajib (80), Iuran Sukarela (80), Total (80), Tgl Terakhir (85), Status (50)
            val colX = floatArrayOf(30f, 60f, 190f, 270f, 350f, 430f, 515f)
            
            paint.color = Color.parseColor("#1C1C1C")
            canvas.drawRect(30f, y, (pageWidth - 30).toFloat(), y + 25f, paint)

            paint.color = Color.WHITE
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            
            canvas.drawText("No", colX[0] + 5f, y + 16f, paint)
            canvas.drawText("Nama Anggota", colX[1] + 5f, y + 16f, paint)
            canvas.drawText("Iuran Wajib", colX[2] + 5f, y + 16f, paint)
            canvas.drawText("Iuran Sukarela", colX[3] + 5f, y + 16f, paint)
            canvas.drawText("Total Bayar", colX[4] + 5f, y + 16f, paint)
            canvas.drawText("Tgl Terakhir", colX[5] + 5f, y + 16f, paint)
            canvas.drawText("Status", colX[6] + 5f, y + 16f, paint)

            y += 25f

            // Draw Table Rows
            paint.color = Color.BLACK
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8f

            var isAlternate = false

            for (item in reportItems) {
                // Check if page overflow
                if (y > pageHeight - 100) {
                    // Draw Footer of current page
                    paint.color = Color.GRAY
                    paint.textSize = 8f
                    canvas.drawText("Halaman $pageNumber | Steker App v1.1", 30f, (pageHeight - 35).toFloat(), paint)

                    pdfDocument.finishPage(currentPage)
                    
                    pageNumber++
                    currentPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    currentPage = pdfDocument.startPage(currentPageInfo)
                    canvas = currentPage.canvas
                    
                    drawHeader()
                    
                    // Redraw Table Headers on new page
                    paint.color = Color.parseColor("#1C1C1C")
                    canvas.drawRect(30f, y, (pageWidth - 30).toFloat(), y + 25f, paint)

                    paint.color = Color.WHITE
                    paint.textSize = 9f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText("No", colX[0] + 5f, y + 16f, paint)
                    canvas.drawText("Nama Anggota", colX[1] + 5f, y + 16f, paint)
                    canvas.drawText("Iuran Wajib", colX[2] + 5f, y + 16f, paint)
                    canvas.drawText("Iuran Sukarela", colX[3] + 5f, y + 16f, paint)
                    canvas.drawText("Total Bayar", colX[4] + 5f, y + 16f, paint)
                    canvas.drawText("Tgl Terakhir", colX[5] + 5f, y + 16f, paint)
                    canvas.drawText("Status", colX[6] + 5f, y + 16f, paint)

                    y += 25f
                    paint.color = Color.BLACK
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.textSize = 8f
                }

                // Row Background
                if (isAlternate) {
                    paint.color = Color.parseColor("#FDFDFD")
                } else {
                    paint.color = Color.parseColor("#F5F5F5")
                }
                canvas.drawRect(30f, y, (pageWidth - 30).toFloat(), y + 20f, paint)
                isAlternate = !isAlternate

                // Row Borders
                paint.color = Color.parseColor("#E0E0E0")
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 0.5f
                canvas.drawRect(30f, y, (pageWidth - 30).toFloat(), y + 20f, paint)
                paint.style = Paint.Style.FILL

                // Cell Text
                paint.color = Color.BLACK
                canvas.drawText(item.nomor.toString(), colX[0] + 5f, y + 13f, paint)
                canvas.drawText(item.name, colX[1] + 5f, y + 13f, paint)
                canvas.drawText(formatRupiah(item.mandatoryPaid), colX[2] + 5f, y + 13f, paint)
                canvas.drawText(formatRupiah(item.voluntaryPaid), colX[3] + 5f, y + 13f, paint)
                
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(formatRupiah(item.totalPaid), colX[4] + 5f, y + 13f, paint)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

                canvas.drawText(item.lastPaymentDateStr, colX[5] + 5f, y + 13f, paint)
                canvas.drawText(item.statusStr, colX[6] + 5f, y + 13f, paint)

                y += 20f
            }

            // Draw Transparency Note & Legal note at bottom
            if (y > pageHeight - 110) {
                // Draw Footer of current page
                paint.color = Color.GRAY
                paint.textSize = 8f
                canvas.drawText("Halaman $pageNumber | Steker App v1.1", 30f, (pageHeight - 35).toFloat(), paint)

                pdfDocument.finishPage(currentPage)
                
                pageNumber++
                currentPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                currentPage = pdfDocument.startPage(currentPageInfo)
                canvas = currentPage.canvas
                drawHeader()
            }

            y += 25f
            paint.color = Color.parseColor("#555555")
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            canvas.drawText("Catatan:", 30f, y, paint)
            canvas.drawText("Laporan ini dibuat sebagai bentuk transparansi pengelolaan dana Steker Hitam.", 30f, y + 15f, paint)

            // Final page footer
            paint.color = Color.GRAY
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Halaman $pageNumber | Steker App v1.1", 30f, (pageHeight - 35).toFloat(), paint)

            pdfDocument.finishPage(currentPage)

            // Save File to internal cache
            val fileName = "Laporan_Pembayaran_Steker_${monthName}_${year}.pdf"
            val file = File(context.externalCacheDir, fileName)
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            pdfDocument.close()
            fos.close()

            // Trigger Share Intent
            shareFile(context, file, "application/pdf", shareToWhatsApp)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal mengekspor PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    // 2. Export Special Payment Report to Excel-Compatible Semicolon CSV
    fun exportSpecialReportToExcel(
        context: Context,
        monthName: String,
        year: Int,
        reportItems: List<PaymentReportItem>,
        summary: SpecialReportSummary,
        shareToWhatsApp: Boolean = false
    ) {
        try {
            val sb = StringBuilder()
            // Add UTF-8 BOM so Microsoft Excel can read Indonesian accents and characters properly
            sb.append('\ufeff')

            // Excel Title Block
            sb.append("LAPORAN KHUSUS ANGGOTA YANG TELAH MEMBAYAR IURAN\n")
            sb.append("ORGANISASI STEKER HITAM\n")
            sb.append("Periode: $monthName $year\n\n")

            // Rekap Block
            sb.append("REKAPITULASI DANA MASUK\n")
            sb.append("Jumlah Anggota Bayar Iuran Wajib;${summary.numMandatoryPayers}\n")
            sb.append("Jumlah Anggota Bayar Iuran Sukarela;${summary.numVoluntaryPayers}\n")
            sb.append("Total Iuran Wajib Terkumpul;${summary.totalMandatoryCollected.toLong()}\n")
            sb.append("Total Iuran Sukarela Terkumpul;${summary.totalVoluntaryCollected.toLong()}\n")
            sb.append("TOTAL DANA DITERIMA;${summary.totalFundsReceived.toLong()}\n\n")

            // Table Headers
            sb.append("No;Nama Anggota;Iuran Wajib;Iuran Sukarela;Total Pembayaran;Tanggal Pembayaran Terakhir;Status Pembayaran\n")

            // Table Rows
            for (item in reportItems) {
                sb.append("${item.nomor};${item.name};${item.mandatoryPaid.toLong()};${item.voluntaryPaid.toLong()};${item.totalPaid.toLong()};${item.lastPaymentDateStr};${item.statusStr}\n")
            }

            sb.append("\nCatatan:\n")
            sb.append("Laporan ini dibuat sebagai bentuk transparansi pengelolaan dana Steker Hitam.\n")

            // Save File to external cache
            val fileName = "Laporan_Pembayaran_Steker_${monthName}_${year}.csv"
            val file = File(context.externalCacheDir, fileName)
            val fos = FileOutputStream(file)
            fos.write(sb.toString().toByteArray(Charsets.UTF_8))
            fos.close()

            // Trigger Share Intent
            shareFile(context, file, "text/csv", shareToWhatsApp)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal mengekspor Excel: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    // Helper to share files
    private fun shareFile(context: Context, file: File, mimeType: String, shareToWhatsApp: Boolean) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, file.name.replace(".pdf", "").replace(".csv", "").replace("_", " "))
            putExtra(Intent.EXTRA_TEXT, "Halo, berikut kami bagikan file laporan keuangan Steker Hitam untuk transparansi bersama.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (shareToWhatsApp) {
            intent.setPackage("com.whatsapp")
        }

        val chooserIntent = Intent.createChooser(intent, "Bagikan Laporan")
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    }
}
