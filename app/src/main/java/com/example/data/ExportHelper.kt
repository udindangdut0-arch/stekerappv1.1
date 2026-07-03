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
        month: Int,
        year: Int,
        monthName: String,
        reportItems: List<PaymentReportItem>,
        summary: SpecialReportSummary,
        mandatoryPayments: List<MandatoryDuesPaymentEntity>,
        voluntaryPayments: List<VoluntaryDuesPaymentEntity>,
        otherIncomes: List<OtherIncomeEntity>,
        expenses: List<ExpenseEntity>,
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
            
            // ----------------------------------------------------
            // HALAMAN 1: LAPORAN KESELURUHAN KEUANGAN
            // ----------------------------------------------------
            var pageNumber = 1
            var pageInfo1 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page1 = pdfDocument.startPage(pageInfo1)
            var canvas = page1.canvas

            // Helper to check if date matches selected period
            fun isDateInPeriod(dateMs: Long, targetMonth: Int, targetYear: Int): Boolean {
                val cal = Calendar.getInstance()
                cal.timeInMillis = dateMs
                return (cal.get(Calendar.MONTH) + 1) == targetMonth && cal.get(Calendar.YEAR) == targetYear
            }

            // Calculation of Financial Balances
            val calLimit = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val limitTime = calLimit.timeInMillis

            // Prior totals for Saldo Awal
            val priorMandatory = mandatoryPayments.filter { !it.isCancelled && it.paymentDate < limitTime }.sumOf { it.amountPaid }
            val priorVoluntary = voluntaryPayments.filter { !it.isCancelled && it.paymentDate < limitTime }.sumOf { it.amountPaid }
            val priorOther = otherIncomes.filter { !it.isCancelled && it.paymentDate < limitTime }.sumOf { it.amount }
            val priorExpenses = expenses.filter { !it.isCancelled && it.expenseDate < limitTime }.sumOf { it.amount }
            val saldoAwal = priorMandatory + priorVoluntary + priorOther - priorExpenses

            // Current period calculations
            val currentPeriodMandatory = mandatoryPayments.filter { !it.isCancelled && it.month == month && it.year == year }
            val currentPeriodVoluntary = voluntaryPayments.filter { !it.isCancelled && isDateInPeriod(it.paymentDate, month, year) }
            val currentPeriodOther = otherIncomes.filter { !it.isCancelled && isDateInPeriod(it.paymentDate, month, year) }
            val currentPeriodExpenses = expenses.filter { !it.isCancelled && isDateInPeriod(it.expenseDate, month, year) }

            val totalMandatoryIn = currentPeriodMandatory.sumOf { it.amountPaid }
            val totalVoluntaryIn = currentPeriodVoluntary.sumOf { it.amountPaid }
            val totalOtherIn = currentPeriodOther.sumOf { it.amount }
            val totalIncome = totalMandatoryIn + totalVoluntaryIn + totalOtherIn
            val totalExpense = currentPeriodExpenses.sumOf { it.amount }
            val saldoAkhir = saldoAwal + totalIncome - totalExpense

            // Page 1 Header
            paint.color = Color.parseColor("#1C1C1C")
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 105f, paint)

            paint.color = Color.parseColor("#C91D2E")
            paint.textSize = 18f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("STEKER HITAM", 30f, 38f, paint)

            paint.color = Color.WHITE
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("LAPORAN KEUANGAN BULANAN", 30f, 56f, paint)

            paint.color = Color.parseColor("#E0E0E0")
            paint.textSize = 10f
            canvas.drawText("Periode: $monthName $year", 30f, 75f, paint)

            // financial Summary Box (Halaman 1)
            var y1 = 125f
            paint.color = Color.parseColor("#F5F5F5")
            canvas.drawRect(30f, y1, (pageWidth - 30).toFloat(), y1 + 175f, paint)

            paint.color = Color.parseColor("#1C1C1C")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRect(30f, y1, (pageWidth - 30).toFloat(), y1 + 175f, paint)
            paint.style = Paint.Style.FILL

            paint.color = Color.parseColor("#C91D2E")
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("IKHTISAR KEUANGAN BULANAN", 45f, y1 + 22f, paint)

            paint.color = Color.BLACK
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            canvas.drawText("Saldo Awal Kas:", 45f, y1 + 45f, paint)
            canvas.drawText(formatRupiah(saldoAwal), 380f, y1 + 45f, paint)

            canvas.drawText("Total Pemasukan Iuran Wajib:", 45f, y1 + 63f, paint)
            canvas.drawText(formatRupiah(totalMandatoryIn), 380f, y1 + 63f, paint)

            canvas.drawText("Total Pemasukan Iuran Sukarela:", 45f, y1 + 81f, paint)
            canvas.drawText(formatRupiah(totalVoluntaryIn), 380f, y1 + 81f, paint)

            canvas.drawText("Total Pemasukan Lain-lain:", 45f, y1 + 99f, paint)
            canvas.drawText(formatRupiah(totalOtherIn), 380f, y1 + 99f, paint)

            // Divider
            paint.color = Color.parseColor("#CCCCCC")
            canvas.drawLine(45f, y1 + 110f, (pageWidth - 45).toFloat(), y1 + 110f, paint)

            paint.color = Color.BLACK
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Total Seluruh Pemasukan:", 45f, y1 + 125f, paint)
            canvas.drawText(formatRupiah(totalIncome), 380f, y1 + 125f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Total Pengeluaran Bulanan:", 45f, y1 + 142f, paint)
            canvas.drawText(formatRupiah(totalExpense), 380f, y1 + 142f, paint)

            paint.color = Color.parseColor("#2E7D32")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("SALDO AKHIR KAS:", 45f, y1 + 160f, paint)
            canvas.drawText(formatRupiah(saldoAkhir), 380f, y1 + 160f, paint)

            // GRAPHIC CHART SECTION
            y1 += 195f
            paint.color = Color.parseColor("#1C1C1C")
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("GRAFIK PERBANDINGAN KEUANGAN", 30f, y1, paint)

            val chartStartY = y1 + 15f
            paint.color = Color.parseColor("#E0E0E0")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.5f
            canvas.drawRect(30f, chartStartY, (pageWidth - 30).toFloat(), chartStartY + 100f, paint)
            paint.style = Paint.Style.FILL

            val maxVal = maxOf(totalIncome, totalExpense, saldoAkhir, 10000.0)
            val bar1Height = ((totalIncome / maxVal) * 80f).toFloat()
            val bar2Height = ((totalExpense / maxVal) * 80f).toFloat()
            val bar3Height = ((saldoAkhir / maxVal) * 80f).toFloat()

            // Draw Bar 1 (Green) - Pemasukan
            paint.color = Color.parseColor("#2E7D32")
            canvas.drawRect(80f, chartStartY + 90f - bar1Height, 150f, chartStartY + 90f, paint)

            // Draw Bar 2 (Red) - Pengeluaran
            paint.color = Color.parseColor("#C62828")
            canvas.drawRect(240f, chartStartY + 90f - bar2Height, 310f, chartStartY + 90f, paint)

            // Draw Bar 3 (Blue) - Saldo Akhir
            paint.color = Color.parseColor("#1565C0")
            canvas.drawRect(400f, chartStartY + 90f - bar3Height, 470f, chartStartY + 90f, paint)

            // Values and Labels above bars
            paint.color = Color.BLACK
            paint.textSize = 7.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(formatRupiah(totalIncome), 70f, chartStartY + 90f - bar1Height - 4f, paint)
            canvas.drawText(formatRupiah(totalExpense), 230f, chartStartY + 90f - bar2Height - 4f, paint)
            canvas.drawText(formatRupiah(saldoAkhir), 390f, chartStartY + 90f - bar3Height - 4f, paint)

            paint.textSize = 8.5f
            canvas.drawText("Pemasukan", 90f, chartStartY + 108f, paint)
            canvas.drawText("Pengeluaran", 250f, chartStartY + 108f, paint)
            canvas.drawText("Saldo Akhir", 410f, chartStartY + 108f, paint)

            // TRANSACTION SUMMARY TABLE SECTION (EXPENSES)
            y1 += 135f
            paint.color = Color.parseColor("#1C1C1C")
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("RINGKASAN TRANSAKSI PENGELUARAN BULANAN", 30f, y1, paint)

            y1 += 12f
            val expColX = floatArrayOf(30f, 60f, 160f, 290f, 470f)
            paint.color = Color.parseColor("#1C1C1C")
            canvas.drawRect(30f, y1, (pageWidth - 30).toFloat(), y1 + 18f, paint)

            paint.color = Color.WHITE
            paint.textSize = 8.5f
            canvas.drawText("No", expColX[0] + 5f, y1 + 12f, paint)
            canvas.drawText("Kategori", expColX[1] + 5f, y1 + 12f, paint)
            canvas.drawText("Penerima", expColX[2] + 5f, y1 + 12f, paint)
            canvas.drawText("Keterangan", expColX[3] + 5f, y1 + 12f, paint)
            canvas.drawText("Jumlah", expColX[4] + 5f, y1 + 12f, paint)

            y1 += 18f
            paint.color = Color.BLACK
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8f

            if (currentPeriodExpenses.isEmpty()) {
                canvas.drawText("Tidak ada transaksi pengeluaran pada periode ini.", 45f, y1 + 15f, paint)
                y1 += 25f
            } else {
                currentPeriodExpenses.take(8).forEachIndexed { idx, exp ->
                    paint.color = if (idx % 2 == 0) Color.parseColor("#F5F5F5") else Color.parseColor("#FDFDFD")
                    canvas.drawRect(30f, y1, (pageWidth - 30).toFloat(), y1 + 16f, paint)

                    paint.color = Color.BLACK
                    canvas.drawText((idx + 1).toString(), expColX[0] + 5f, y1 + 11f, paint)
                    canvas.drawText(exp.category, expColX[1] + 5f, y1 + 11f, paint)
                    canvas.drawText(exp.recipient, expColX[2] + 5f, y1 + 11f, paint)

                    val noteLimit = if (exp.note.length > 32) exp.note.take(30) + ".." else exp.note
                    canvas.drawText(noteLimit, expColX[3] + 5f, y1 + 11f, paint)
                    canvas.drawText(formatRupiah(exp.amount), expColX[4] + 5f, y1 + 11f, paint)

                    y1 += 16f
                }
            }

            // transparency Note at bottom Page 1
            paint.color = Color.parseColor("#555555")
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            canvas.drawText("Catatan:", 30f, pageHeight - 65f, paint)
            canvas.drawText("Laporan ini dibuat sebagai bentuk transparansi pengelolaan dana Steker Hitam.", 30f, pageHeight - 52f, paint)

            paint.color = Color.GRAY
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Halaman $pageNumber | Steker App v1.3", 30f, (pageHeight - 35).toFloat(), paint)

            pdfDocument.finishPage(page1)

            // ----------------------------------------------------
            // HALAMAN 2: LAPORAN IURAN ANGGOTA (Daftar & Rekap)
            // ----------------------------------------------------
            pageNumber++
            var currentPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var currentPage = pdfDocument.startPage(currentPageInfo)
            canvas = currentPage.canvas

            var y = 130f

            fun drawHeaderHalaman2() {
                paint.color = Color.parseColor("#1C1C1C")
                canvas.drawRect(0f, 0f, pageWidth.toFloat(), 105f, paint)

                paint.color = Color.parseColor("#C91D2E")
                paint.textSize = 18f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("STEKER HITAM", 30f, 38f, paint)

                paint.color = Color.WHITE
                paint.textSize = 11f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText("LAPORAN IURAN ANGGOTA", 30f, 56f, paint)

                paint.color = Color.parseColor("#E0E0E0")
                paint.textSize = 10f
                canvas.drawText("Periode: $monthName $year", 30f, 75f, paint)

                y = 130f
            }

            drawHeaderHalaman2()

            // Draw Table Title
            paint.color = Color.parseColor("#1C1C1C")
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("DAFTAR PEMBAYARAN ANGGOTA", 30f, y, paint)
            
            y += 14f

            // Columns layout for Member payments
            val colX = floatArrayOf(30f, 60f, 190f, 270f, 350f, 430f, 515f)
            
            // Header row
            paint.color = Color.parseColor("#1C1C1C")
            canvas.drawRect(30f, y, (pageWidth - 30).toFloat(), y + 22f, paint)

            paint.color = Color.WHITE
            paint.textSize = 8.5f
            canvas.drawText("No", colX[0] + 5f, y + 14f, paint)
            canvas.drawText("Nama Anggota", colX[1] + 5f, y + 14f, paint)
            canvas.drawText("Iuran Wajib", colX[2] + 5f, y + 14f, paint)
            canvas.drawText("Iuran Sukarela", colX[3] + 5f, y + 14f, paint)
            canvas.drawText("Total Bayar", colX[4] + 5f, y + 14f, paint)
            canvas.drawText("Tgl Terakhir", colX[5] + 5f, y + 14f, paint)
            canvas.drawText("Status", colX[6] + 5f, y + 14f, paint)

            y += 22f

            paint.color = Color.BLACK
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8f

            var isAlternate = false

            for (item in reportItems) {
                // If overflow table limit on page 2
                if (y > pageHeight - 160) {
                    paint.color = Color.GRAY
                    paint.textSize = 8f
                    canvas.drawText("Halaman $pageNumber | Steker App v1.3", 30f, (pageHeight - 35).toFloat(), paint)

                    pdfDocument.finishPage(currentPage)
                    
                    pageNumber++
                    currentPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    currentPage = pdfDocument.startPage(currentPageInfo)
                    canvas = currentPage.canvas
                    
                    drawHeaderHalaman2()

                    paint.color = Color.parseColor("#1C1C1C")
                    canvas.drawRect(30f, y, (pageWidth - 30).toFloat(), y + 22f, paint)

                    paint.color = Color.WHITE
                    paint.textSize = 8.5f
                    canvas.drawText("No", colX[0] + 5f, y + 14f, paint)
                    canvas.drawText("Nama Anggota", colX[1] + 5f, y + 14f, paint)
                    canvas.drawText("Iuran Wajib", colX[2] + 5f, y + 14f, paint)
                    canvas.drawText("Iuran Sukarela", colX[3] + 5f, y + 14f, paint)
                    canvas.drawText("Total Bayar", colX[4] + 5f, y + 14f, paint)
                    canvas.drawText("Tgl Terakhir", colX[5] + 5f, y + 14f, paint)
                    canvas.drawText("Status", colX[6] + 5f, y + 14f, paint)

                    y += 22f
                    paint.color = Color.BLACK
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.textSize = 8f
                }

                // Alternate bg rows
                paint.color = if (isAlternate) Color.parseColor("#FDFDFD") else Color.parseColor("#F5F5F5")
                canvas.drawRect(30f, y, (pageWidth - 30).toFloat(), y + 18f, paint)
                isAlternate = !isAlternate

                paint.color = Color.parseColor("#E0E0E0")
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 0.5f
                canvas.drawRect(30f, y, (pageWidth - 30).toFloat(), y + 18f, paint)
                paint.style = Paint.Style.FILL

                paint.color = Color.BLACK
                canvas.drawText(item.nomor.toString(), colX[0] + 5f, y + 12f, paint)
                canvas.drawText(item.name, colX[1] + 5f, y + 12f, paint)
                canvas.drawText(formatRupiah(item.mandatoryPaid), colX[2] + 5f, y + 12f, paint)
                canvas.drawText(formatRupiah(item.voluntaryPaid), colX[3] + 5f, y + 12f, paint)
                
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(formatRupiah(item.totalPaid), colX[4] + 5f, y + 12f, paint)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

                canvas.drawText(item.lastPaymentDateStr, colX[5] + 5f, y + 12f, paint)
                canvas.drawText(item.statusStr, colX[6] + 5f, y + 12f, paint)

                y += 18f
            }

            // If summary box would overflow page height
            if (y > pageHeight - 160) {
                paint.color = Color.GRAY
                paint.textSize = 8f
                canvas.drawText("Halaman $pageNumber | Steker App v1.3", 30f, (pageHeight - 35).toFloat(), paint)

                pdfDocument.finishPage(currentPage)
                
                pageNumber++
                currentPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                currentPage = pdfDocument.startPage(currentPageInfo)
                canvas = currentPage.canvas
                
                drawHeaderHalaman2()
            }

            // Draw Rekap Box at bottom
            y += 15f
            paint.color = Color.parseColor("#F5F5F5")
            canvas.drawRect(30f, y, (pageWidth - 30).toFloat(), y + 90f, paint)

            paint.color = Color.parseColor("#1C1C1C")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRect(30f, y, (pageWidth - 30).toFloat(), y + 90f, paint)
            paint.style = Paint.Style.FILL

            paint.color = Color.parseColor("#C91D2E")
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("REKAPITULASI IURAN ANGGOTA", 40f, y + 18f, paint)

            paint.color = Color.BLACK
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            canvas.drawText("Anggota Bayar Iuran Wajib: ${summary.numMandatoryPayers} orang", 40f, y + 36f, paint)
            canvas.drawText("Anggota Bayar Iuran Sukarela: ${summary.numVoluntaryPayers} orang", 40f, y + 52f, paint)
            
            val numBothPayers = reportItems.count { it.mandatoryPaid > 0.0 && it.voluntaryPaid > 0.0 }
            canvas.drawText("Anggota Bayar Keduanya: $numBothPayers orang", 40f, y + 68f, paint)

            val xCol2 = 310f
            canvas.drawText("Total Iuran Wajib: ${formatRupiah(summary.totalMandatoryCollected)}", xCol2, y + 36f, paint)
            canvas.drawText("Total Iuran Sukarela: ${formatRupiah(summary.totalVoluntaryCollected)}", xCol2, y + 52f, paint)
            canvas.drawText("Total Dana Iuran Anggota: ${formatRupiah(summary.totalFundsReceived)}", xCol2, y + 68f, paint)

            // Final page footer
            paint.color = Color.GRAY
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Halaman $pageNumber | Steker App v1.3", 30f, (pageHeight - 35).toFloat(), paint)

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
