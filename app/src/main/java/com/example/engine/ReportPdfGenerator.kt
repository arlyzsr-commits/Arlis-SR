package com.example.engine

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.data.model.ProgressPhoto
import com.example.data.model.ReportGenerationParam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportPdfGenerator {

    // A4 Landscape dimensions in points (72 DPI)
    const val PAGE_WIDTH = 842
    const val PAGE_HEIGHT = 595

    // Pastel palette for PDF
    private val COLOR_SAGE_DARK = Color.rgb(58, 92, 70)
    private val COLOR_SAGE_LIGHT = Color.rgb(226, 237, 228)
    private val COLOR_DUSTY_BLUE = Color.rgb(74, 110, 137)
    private val COLOR_DUSTY_BLUE_LIGHT = Color.rgb(224, 236, 245)
    private val COLOR_PEACH = Color.rgb(200, 112, 85)
    private val COLOR_BG_CREAM = Color.rgb(251, 249, 245)
    private val COLOR_CARD_WHITE = Color.WHITE
    private val COLOR_BORDER = Color.rgb(225, 220, 212)
    private val COLOR_TEXT_DARK = Color.rgb(38, 50, 40)
    private val COLOR_TEXT_MUTED = Color.rgb(107, 122, 112)

    suspend fun generateReportPdf(
        context: Context,
        params: ReportGenerationParam,
        photos: List<ProgressPhoto>
    ): File = withContext(Dispatchers.IO) {
        val document = PdfDocument()

        // 1. Group and layout documentation pages
        val picOwnerMap = mapOf(
            "STRUKTUR" to params.picOwnerStruktur,
            "ARSITEK" to params.picOwnerArsitek,
            "MEP" to params.picOwnerMep
        )

        val docPages = PhotoLayoutEngine.createLocationPages(
            photos = photos,
            overrideWorkCategory = if (params.workCategoryFilter != "SEMUA") params.workCategoryFilter else null,
            locationNotes = params.locationNotes,
            projectManagerMK = params.projectManagerMK,
            picOwnerMap = picOwnerMap,
            kontraktor = params.kontraktor,
            scopePekerjaan = params.scopePekerjaanKontraktor
        )

        // Total pages: 1 (Cover) + 1 (Project Info) + 1 (Progress Summary) + docPages.size
        val totalPages = 3 + docPages.size.coerceAtLeast(1)

        var currentPageNumber = 1

        // PAGE 1: COVER
        drawCoverPage(document, params, currentPageNumber, totalPages)
        currentPageNumber++

        // PAGE 2: PROJECT INFO
        drawProjectInfoPage(document, params, currentPageNumber, totalPages)
        currentPageNumber++

        // PAGE 3: SUMMARY
        drawSummaryPage(document, params, photos, currentPageNumber, totalPages)
        currentPageNumber++

        // PAGES 4+: DOCUMENTATION
        if (docPages.isEmpty()) {
            drawEmptyDocPage(document, params, currentPageNumber, totalPages)
        } else {
            docPages.forEach { locPage ->
                drawDocumentationPage(
                    document = document,
                    params = params,
                    locPage = locPage,
                    pageNumber = currentPageNumber,
                    totalPages = totalPages
                )
                currentPageNumber++
            }
        }

        // Output File
        val safeProject = params.projectName.replace("[^a-zA-Z0-9]".toRegex(), "_").take(20)
        val safeCategory = params.workCategoryFilter.replace("[^a-zA-Z0-9]".toRegex(), "_")
        val safeDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val fileName = "LAPOOR_${safeProject}_${safeCategory}_$safeDate.pdf"

        val reportsDir = File(context.filesDir, "reports").apply { mkdirs() }
        val pdfFile = File(reportsDir, fileName)

        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        pdfFile
    }

    private fun drawCoverPage(
        document: PdfDocument,
        params: ReportGenerationParam,
        pageNumber: Int,
        totalPages: Int
    ) {
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        // Background
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = COLOR_BG_CREAM
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), paint)

        // Decorative pastel side accents
        paint.color = COLOR_SAGE_LIGHT
        canvas.drawRect(0f, 0f, 24f, PAGE_HEIGHT.toFloat(), paint)

        paint.color = COLOR_DUSTY_BLUE_LIGHT
        canvas.drawRect(24f, 0f, 32f, PAGE_HEIGHT.toFloat(), paint)

        // Outer border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f
        paint.color = COLOR_BORDER
        canvas.drawRoundRect(RectF(48f, 36f, PAGE_WIDTH - 48f, PAGE_HEIGHT - 36f), 12f, 12f, paint)
        paint.style = Paint.Style.FILL

        // Header brand pill
        paint.color = COLOR_SAGE_DARK
        val brandPill = RectF(76f, 60f, 220f, 96f)
        canvas.drawRoundRect(brandPill, 18f, 18f, paint)

        paint.color = Color.WHITE
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("LAPOOR", 100f, 85f, paint)

        // Subtitle header
        paint.color = COLOR_TEXT_MUTED
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("LAPORAN PROGRESS PEKERJAAN PROYEK", 236f, 82f, paint)

        // Title Section
        paint.color = COLOR_TEXT_DARK
        paint.textSize = 28f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("LAPORAN DOKUMENTASI FISIK", 76f, 160f, paint)

        paint.textSize = 20f
        paint.color = COLOR_DUSTY_BLUE
        canvas.drawText(params.projectName.uppercase(Locale.getDefault()), 76f, 195f, paint)

        // Divider
        paint.color = COLOR_SAGE_DARK
        paint.strokeWidth = 2.5f
        canvas.drawLine(76f, 215f, 360f, 215f, paint)

        // Middle details box
        val detailsCard = RectF(76f, 235f, PAGE_WIDTH - 76f, 440f)
        paint.color = COLOR_CARD_WHITE
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(detailsCard, 10f, 10f, paint)

        paint.color = COLOR_BORDER
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(detailsCard, 10f, 10f, paint)
        paint.style = Paint.Style.FILL

        // Two-column layout in details card
        val col1X = 100f
        val col2X = 460f
        var yPos = 265f
        val rowSpacing = 28f

        fun drawField(colX: Float, y: Float, label: String, value: String) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 10f
            paint.color = COLOR_TEXT_MUTED
            canvas.drawText(label.uppercase(Locale.getDefault()), colX, y, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 12f
            paint.color = COLOR_TEXT_DARK
            canvas.drawText(value, colX, y + 14f, paint)
        }

        drawField(col1X, yPos, "Owner / Pemilik", params.owner.ifEmpty { "-" })
        drawField(col2X, yPos, "Nomor Kontrak", params.nomorKontrak.ifEmpty { "-" })
        yPos += rowSpacing + 12f

        drawField(col1X, yPos, "Perusahaan Pengembang", params.perusahaan.ifEmpty { "-" })
        drawField(col2X, yPos, "Lokasi Proyek", params.lokasiProyek.ifEmpty { "-" })
        yPos += rowSpacing + 12f

        drawField(col1X, yPos, "Kontraktor Utama", params.kontraktor.ifEmpty { "-" })
        drawField(col2X, yPos, "Jenis Pekerjaan", params.workCategoryFilter)
        yPos += rowSpacing + 12f

        drawField(col1X, yPos, "Konsultan Pengawas", params.konsultan.ifEmpty { "-" })
        drawField(col2X, yPos, "Periode & Tanggal", "${params.periodeLaporan} | ${params.tanggalLaporan}")

        // Footer signatures bar
        val sigY = 465f
        paint.color = COLOR_TEXT_MUTED
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("Disusun oleh: ${params.namaPenyusun} (${params.jabatan})", 76f, sigY, paint)
        canvas.drawText("Project Manager: ${params.projectManager}  •  Site Manager: ${params.siteManager}", 76f, sigY + 16f, paint)

        // Cover Footer
        paint.color = COLOR_SAGE_DARK
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("SISTEM DOKUMENTASI LAPOOR  •  OFFLINE-READY NATIVE REPORT", 76f, PAGE_HEIGHT - 48f, paint)

        document.finishPage(page)
    }

    private fun drawProjectInfoPage(
        document: PdfDocument,
        params: ReportGenerationParam,
        pageNumber: Int,
        totalPages: Int
    ) {
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawPageBase(canvas, "INFORMASI PROYEK & KONTRAK", params, pageNumber, totalPages)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 4 Pastel Information Cards
        val cardWidth = 338f
        val cardHeight = 180f
        val leftX1 = 56f
        val leftX2 = 448f
        val topY1 = 80f
        val topY2 = 280f

        drawInfoCard(
            canvas = canvas,
            rect = RectF(leftX1, topY1, leftX1 + cardWidth, topY1 + cardHeight),
            title = "1. IDENTITAS PROYEK",
            fields = listOf(
                "Nama Proyek" to params.projectName,
                "Lokasi Proyek" to params.lokasiProyek,
                "Nomor Kontrak" to params.nomorKontrak,
                "Periode Laporan" to params.periodeLaporan
            )
        )

        drawInfoCard(
            canvas = canvas,
            rect = RectF(leftX2, topY1, leftX2 + cardWidth, topY1 + cardHeight),
            title = "2. KONTRAKTOR & STAKEHOLDER",
            fields = listOf(
                "Kontraktor Pelaksana" to params.kontraktor,
                "Scope Pekerjaan" to params.scopePekerjaanKontraktor.ifBlank { "Struktur, Arsitektur, MEP" },
                "Owner / Klien" to params.owner,
                "Konsultan Pengawas" to params.konsultan
            )
        )

        drawInfoCard(
            canvas = canvas,
            rect = RectF(leftX1, topY2, leftX1 + cardWidth, topY2 + cardHeight),
            title = "3. TIM MANAJEMEN & MK",
            fields = listOf(
                "Project Manager MK/Owner" to params.projectManagerMK.ifBlank { params.projectManager },
                "Project Manager Kontraktor" to params.projectManager,
                "Site Manager Lapangan" to params.siteManager,
                "Disusun Oleh" to "${params.namaPenyusun} (${params.jabatan})"
            )
        )

        drawInfoCard(
            canvas = canvas,
            rect = RectF(leftX2, topY2, leftX2 + cardWidth, topY2 + cardHeight),
            title = "4. PIC OWNER SETIAP PEKERJAAN",
            fields = listOf(
                "PIC Owner STRUKTUR" to params.picOwnerStruktur.ifBlank { "-" },
                "PIC Owner ARSITEK" to params.picOwnerArsitek.ifBlank { "-" },
                "PIC Owner MEP" to params.picOwnerMep.ifBlank { "-" },
                "Fokus & Tanggal Laporan" to "${params.workCategoryFilter} • ${params.tanggalLaporan}"
            )
        )

        // General remarks banner at bottom
        val remarksRect = RectF(56f, 480f, PAGE_WIDTH - 56f, 545f)
        paint.color = COLOR_SAGE_LIGHT
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(remarksRect, 8f, 8f, paint)

        paint.color = COLOR_SAGE_DARK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        canvas.drawText("CATATAN UMUM LAPANGAN:", 72f, 502f, paint)

        paint.color = COLOR_TEXT_DARK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 10f
        canvas.drawText(params.keteranganUmum.ifEmpty { "Seluruh dokumentasi terlampir diambil secara langsung di lokasi proyek pada waktu yang tertera pada metadata." }, 72f, 524f, paint)

        document.finishPage(page)
    }

    private fun drawSummaryPage(
        document: PdfDocument,
        params: ReportGenerationParam,
        photos: List<ProgressPhoto>,
        pageNumber: Int,
        totalPages: Int
    ) {
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawPageBase(canvas, "RINGKASAN PROGRESS & STATISTIK PEKERJAAN", params, pageNumber, totalPages)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Calculate statistics
        val totalLocations = photos.map { it.locationName }.distinct().size
        val totalPhotos = photos.size
        val avgProgress = if (photos.isNotEmpty()) photos.map { it.progress }.average().toInt() else 0

        val countSelesai = photos.count { it.status.equals("Selesai", ignoreCase = true) }
        val countSedang = photos.count { it.status.equals("Sedang Dikerjakan", ignoreCase = true) || it.status.equals("Progress", ignoreCase = true) }
        val countBelum = photos.count { it.status.equals("Belum Mulai", ignoreCase = true) }

        // Top 3 Stat Cards
        val statCardW = 224f
        val statCardH = 90f
        val startY = 80f

        drawMetricCard(canvas, RectF(56f, startY, 56f + statCardW, startY + statCardH), "TOTAL DOKUMENTASI", "$totalPhotos", "Foto Terverifikasi", COLOR_SAGE_DARK, COLOR_SAGE_LIGHT)
        drawMetricCard(canvas, RectF(309f, startY, 309f + statCardW, startY + statCardH), "LOKASI TERPANTAU", "$totalLocations", "Zona Pekerjaan", COLOR_DUSTY_BLUE, COLOR_DUSTY_BLUE_LIGHT)
        drawMetricCard(canvas, RectF(562f, startY, 562f + statCardW, startY + statCardH), "PROGRESS RATA-RATA", "$avgProgress%", "Akumulasi Proyek", COLOR_PEACH, Color.rgb(254, 235, 230))

        // Progress Breakdown by Work Category
        val progressCard = RectF(56f, 190f, PAGE_WIDTH - 56f, 375f)
        paint.color = COLOR_CARD_WHITE
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(progressCard, 10f, 10f, paint)
        paint.color = COLOR_BORDER
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(progressCard, 10f, 10f, paint)
        paint.style = Paint.Style.FILL

        paint.color = COLOR_TEXT_DARK
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("PROGRESS FISIK BERDASARKAN DISIPLIN PEKERJAAN", 80f, 222f, paint)

        val strukturPhotos = photos.filter { it.workCategory.contains("STRUKTUR", ignoreCase = true) }
        val arsitekPhotos = photos.filter { it.workCategory.contains("ARSITEK", ignoreCase = true) }
        val mepPhotos = photos.filter { it.workCategory.contains("MEP", ignoreCase = true) }

        val strukturPct = if (strukturPhotos.isNotEmpty()) strukturPhotos.map { it.progress }.average().toInt() else 65
        val arsitekPct = if (arsitekPhotos.isNotEmpty()) arsitekPhotos.map { it.progress }.average().toInt() else 48
        val mepPct = if (mepPhotos.isNotEmpty()) mepPhotos.map { it.progress }.average().toInt() else 52

        drawProgressBar(canvas, 80f, 245f, 680f, "STRUKTUR", strukturPct, COLOR_SAGE_DARK)
        drawProgressBar(canvas, 80f, 285f, 680f, "ARSITEK", arsitekPct, COLOR_DUSTY_BLUE)
        drawProgressBar(canvas, 80f, 325f, 680f, "MEP (Mechanical, Electrical, Plumbing)", mepPct, COLOR_PEACH)

        // Status breakdown pills at bottom
        val statusCard = RectF(56f, 395f, PAGE_WIDTH - 56f, 530f)
        paint.color = COLOR_CARD_WHITE
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(statusCard, 10f, 10f, paint)
        paint.color = COLOR_BORDER
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(statusCard, 10f, 10f, paint)
        paint.style = Paint.Style.FILL

        paint.color = COLOR_TEXT_DARK
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("STATUS DISTRIBUSI PEKERJAAN", 80f, 425f, paint)

        val colW = 220f
        drawStatusItem(canvas, 80f, 455f, "Selesai", "$countSelesai", Color.rgb(59, 129, 85))
        drawStatusItem(canvas, 80f + colW, 455f, "Dalam Proses / Progress", "$countSedang", Color.rgb(63, 119, 158))
        drawStatusItem(canvas, 80f + (colW * 2), 455f, "Belum Mulai", "$countBelum", Color.rgb(140, 147, 141))

        document.finishPage(page)
    }

    private fun drawDocumentationPage(
        document: PdfDocument,
        params: ReportGenerationParam,
        locPage: LocationReportPage,
        pageNumber: Int,
        totalPages: Int
    ) {
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawPageBase(canvas, "DOKUMENTASI FOTO PROGRESS", params, pageNumber, totalPages)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Two-Column Layout dimensions:
        // Left Column: Ringkasan Berdasarkan Lokasi (width 244)
        // Right Column: Photo Dokumentasi dan Keterangan Photo (width 500)
        val colLeftX = 44f
        val colLeftWidth = 244f
        val colLeftTop = 54f
        val colLeftBottom = 554f

        val gap = 10f
        val colRightX = colLeftX + colLeftWidth + gap
        val colRightWidth = PAGE_WIDTH - 44f - colRightX
        val colRightTop = 54f
        val colRightBottom = 554f

        // =========================================================================
        // 1. SEBELAH KIRI: RINGKASAN BERDASARKAN LOKASI
        // =========================================================================
        val leftCardRect = RectF(colLeftX, colLeftTop, colLeftX + colLeftWidth, colLeftBottom)
        paint.color = COLOR_CARD_WHITE
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(leftCardRect, 8f, 8f, paint)

        paint.color = COLOR_BORDER
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(leftCardRect, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        var curY = colLeftTop + 14f

        // A. Header Tag "RINGKASAN LOKASI"
        val tagRect = RectF(colLeftX + 12f, curY, colLeftX + 140f, curY + 20f)
        paint.color = COLOR_SAGE_DARK
        canvas.drawRoundRect(tagRect, 4f, 4f, paint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 8.5f
        canvas.drawText("RINGKASAN LOKASI", tagRect.left + 8f, tagRect.top + 13.5f, paint)

        curY += 32f

        // B. Nama Lokasi
        paint.color = COLOR_TEXT_DARK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 13f
        val locTitle = locPage.locationName.uppercase(Locale.getDefault())
        canvas.drawText(locTitle.take(30), colLeftX + 12f, curY, paint)

        curY += 14f
        paint.color = COLOR_TEXT_MUTED
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8f
        canvas.drawText("Halaman ${locPage.pageIndexForLocation} dari ${locPage.totalPagesForLocation} di lokasi ini", colLeftX + 12f, curY, paint)

        curY += 18f

        // C. Nama Pekerjaan (Jenis Pekerjaan)
        val workPillRect = RectF(colLeftX + 12f, curY, colLeftX + colLeftWidth - 12f, curY + 22f)
        paint.color = COLOR_DUSTY_BLUE_LIGHT
        canvas.drawRoundRect(workPillRect, 4f, 4f, paint)

        paint.color = COLOR_DUSTY_BLUE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.5f
        canvas.drawText("PEKERJAAN: ${locPage.workCategory}", workPillRect.left + 8f, workPillRect.top + 15f, paint)

        curY += 32f

        // D. Sub Pekerjaan dan Persentase Progressnya
        paint.color = COLOR_TEXT_MUTED
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 8.5f
        canvas.drawText("SUB PEKERJAAN & PROGRESS", colLeftX + 12f, curY, paint)

        curY += 10f

        val subList = if (locPage.subTasksList.isNotEmpty()) {
            locPage.subTasksList.take(4)
        } else {
            locPage.photos.groupBy { it.subPekerjaan.ifBlank { it.workCategory } }
                .map { (k, v) -> Pair(k, v.map { it.progress }.average().toInt()) }
                .take(4)
        }

        subList.forEach { (subName, prog) ->
            curY += 12f
            paint.color = COLOR_TEXT_DARK
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8f
            canvas.drawText(subName.take(28), colLeftX + 12f, curY, paint)

            paint.color = COLOR_SAGE_DARK
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 8f
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("$prog%", colLeftX + colLeftWidth - 12f, curY, paint)
            paint.textAlign = Paint.Align.LEFT

            curY += 4f
            val barW = colLeftWidth - 24f
            val barRect = RectF(colLeftX + 12f, curY, colLeftX + 12f + barW, curY + 4f)
            paint.color = COLOR_BORDER
            canvas.drawRoundRect(barRect, 2f, 2f, paint)

            val fillW = (barW * (prog.coerceIn(0, 100) / 100f)).coerceAtLeast(2f)
            paint.color = COLOR_SAGE_DARK
            canvas.drawRoundRect(RectF(colLeftX + 12f, curY, colLeftX + 12f + fillW, curY + 4f), 2f, 2f, paint)

            curY += 6f
        }

        // Avg Progress pill
        val avgProg = if (locPage.photos.isNotEmpty()) locPage.photos.map { it.progress }.average().toInt() else 0
        curY += 8f
        val avgPill = RectF(colLeftX + 12f, curY, colLeftX + colLeftWidth - 12f, curY + 20f)
        paint.color = COLOR_SAGE_LIGHT
        canvas.drawRoundRect(avgPill, 4f, 4f, paint)

        paint.color = COLOR_SAGE_DARK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 8.5f
        canvas.drawText("Progress Rata-rata Area: $avgProg%", avgPill.left + 8f, avgPill.top + 14f, paint)

        curY += 28f

        // E. Catatan Lapangan (Diisi Manual)
        paint.color = COLOR_PEACH
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 8.5f
        canvas.drawText("CATATAN LAPANGAN (MANUAL)", colLeftX + 12f, curY, paint)

        curY += 8f
        val noteCardH = 72f
        val noteRect = RectF(colLeftX + 12f, curY, colLeftX + colLeftWidth - 12f, curY + noteCardH)
        paint.color = COLOR_BG_CREAM
        canvas.drawRoundRect(noteRect, 6f, 6f, paint)

        paint.color = COLOR_BORDER
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(noteRect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        val manualNoteText = locPage.manualNote.ifBlank {
            "Pekerjaan di area ini berjalan sesuai jadwal dan gambar kerja yang disetujui."
        }

        val noteTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_DARK
            textSize = 7.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val noteLayout = StaticLayout.Builder.obtain(
            manualNoteText,
            0,
            manualNoteText.length,
            noteTextPaint,
            (colLeftWidth - 36f).toInt()
        )
            .setMaxLines(4)
            .setEllipsize(android.text.TextUtils.TruncateAt.END)
            .build()

        canvas.save()
        canvas.translate(noteRect.left + 6f, noteRect.top + 6f)
        noteLayout.draw(canvas)
        canvas.restore()

        curY += noteCardH + 10f

        // F. Data Stakeholder & Tim Lapangan (Kontraktor, Scope, PM MK, PIC Owner)
        val teamBoxH = 68f
        val teamRect = RectF(colLeftX + 12f, colLeftBottom - teamBoxH - 10f, colLeftX + colLeftWidth - 12f, colLeftBottom - 10f)
        paint.color = Color.rgb(247, 245, 240)
        canvas.drawRoundRect(teamRect, 6f, 6f, paint)

        val teamTextY = teamRect.top + 13f
        paint.color = COLOR_TEXT_DARK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 7.5f

        val kontr = locPage.kontraktor.ifBlank { params.kontraktor.ifBlank { "-" } }
        val scopePek = (locPage.scopePekerjaan.ifBlank { params.scopePekerjaanKontraktor }).ifBlank { "-" }
        val pmMk = locPage.projectManagerMK.ifBlank { params.projectManagerMK.ifBlank { "-" } }
        val pic = locPage.picOwner.ifBlank { "-" }

        canvas.drawText("Kontraktor: ${kontr.take(24)}", teamRect.left + 6f, teamTextY, paint)
        canvas.drawText("Scope: ${scopePek.take(26)}", teamRect.left + 6f, teamTextY + 12f, paint)
        canvas.drawText("PM MK/Owner: ${pmMk.take(24)}", teamRect.left + 6f, teamTextY + 24f, paint)

        paint.color = COLOR_SAGE_DARK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("PIC Owner: ${pic.take(24)}", teamRect.left + 6f, teamTextY + 36f, paint)

        // =========================================================================
        // 2. SEBELAH KANAN: PHOTO DOKUMENTASI DAN KETERANGAN PHOTO
        // =========================================================================
        val rightCardRect = RectF(colRightX, colRightTop, colRightX + colRightWidth, colRightBottom)
        paint.color = COLOR_CARD_WHITE
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(rightCardRect, 8f, 8f, paint)

        paint.color = COLOR_BORDER
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(rightCardRect, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        // Header bar inside right column
        paint.color = COLOR_SAGE_DARK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.5f
        canvas.drawText("FOTO DOKUMENTASI & KETERANGAN PHOTO (${locPage.photos.size} Foto)", colRightX + 12f, colRightTop + 18f, paint)

        paint.color = COLOR_TEXT_MUTED
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("${params.projectName} • ${params.periodeLaporan}", colRightX + colRightWidth - 12f, colRightTop + 18f, paint)
        paint.textAlign = Paint.Align.LEFT

        // Photo slots area
        val photoAreaLeft = colRightX + 8f
        val photoAreaTop = colRightTop + 26f
        val photoAreaWidth = colRightWidth - 16f
        val photoAreaHeight = colRightBottom - photoAreaTop - 8f

        locPage.slots.forEach { slot ->
            val slotX = photoAreaLeft + (slot.normalizedX * photoAreaWidth)
            val slotY = photoAreaTop + (slot.normalizedY * photoAreaHeight)
            val slotW = slot.normalizedWidth * photoAreaWidth
            val slotH = slot.normalizedHeight * photoAreaHeight

            drawPhotoCard(
                canvas = canvas,
                photo = slot.photo,
                rect = RectF(slotX, slotY, slotX + slotW, slotY + slotH),
                slotIndex = slot.slotIndex
            )
        }

        document.finishPage(page)
    }

    private fun drawEmptyDocPage(
        document: PdfDocument,
        params: ReportGenerationParam,
        pageNumber: Int,
        totalPages: Int
    ) {
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawPageBase(canvas, "DOKUMENTASI FOTO", params, pageNumber, totalPages)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_MUTED
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }

        canvas.drawText(
            "Belum ada foto dokumentasi untuk filter pekerjaan/lokasi ini.",
            PAGE_WIDTH / 2f,
            PAGE_HEIGHT / 2f,
            paint
        )

        document.finishPage(page)
    }

    private fun drawPhotoCard(
        canvas: Canvas,
        photo: ProgressPhoto,
        rect: RectF,
        slotIndex: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Card background
        paint.color = COLOR_CARD_WHITE
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(rect, 8f, 8f, paint)

        // Card border
        paint.color = COLOR_BORDER
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(rect, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        // Calculate caption area height (relative to card size, e.g. 42 to 56 pt)
        val captionHeight = (rect.height() * 0.22f).coerceIn(36f, 54f)
        val imageRect = RectF(
            rect.left + 4f,
            rect.top + 4f,
            rect.right - 4f,
            rect.bottom - captionHeight
        )

        // Draw image
        val bitmap = loadAndScaleBitmap(photo.filePath, imageRect.width().toInt(), imageRect.height().toInt())
        if (bitmap != null) {
            canvas.save()
            canvas.clipRect(imageRect)
            // Center crop within imageRect
            val srcRect = calculateCenterCropSrc(bitmap.width, bitmap.height, imageRect.width(), imageRect.height())
            canvas.drawBitmap(bitmap, srcRect, imageRect, paint)
            canvas.restore()
        } else {
            // Placeholder rectangle if image file is unavailable
            paint.color = COLOR_BG_CREAM
            canvas.drawRect(imageRect, paint)
            paint.color = COLOR_TEXT_MUTED
            paint.textSize = 10f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("Foto #${photo.photoNumber}", imageRect.centerX(), imageRect.centerY(), paint)
            paint.textAlign = Paint.Align.LEFT
        }

        // Photo Number Badge at top-left of image
        val badgeW = 60f
        val badgeH = 18f
        val badgeRect = RectF(imageRect.left + 6f, imageRect.top + 6f, imageRect.left + 6f + badgeW, imageRect.top + 6f + badgeH)
        paint.color = Color.argb(200, 24, 38, 28)
        canvas.drawRoundRect(badgeRect, 4f, 4f, paint)

        paint.color = Color.WHITE
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Foto ${String.format(Locale.getDefault(), "%02d", photo.photoNumber)}", badgeRect.left + 8f, badgeRect.top + 13f, paint)

        // Progress badge at top-right
        val progBadgeW = 44f
        val progRect = RectF(imageRect.right - 6f - progBadgeW, imageRect.top + 6f, imageRect.right - 6f, imageRect.top + 6f + badgeH)
        paint.color = Color.argb(200, 58, 92, 70)
        canvas.drawRoundRect(progRect, 4f, 4f, paint)
        paint.color = Color.WHITE
        canvas.drawText("${photo.progress}%", progRect.left + 8f, progRect.top + 13f, paint)

        // Caption Box (Below photo)
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_DARK
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val captionText = photo.caption.ifEmpty { photo.subPekerjaan.ifEmpty { photo.workCategory } }
        val maxTextWidth = (rect.width() - 16f).toInt().coerceAtLeast(60)

        val staticLayout = StaticLayout.Builder.obtain(
            captionText,
            0,
            captionText.length,
            textPaint,
            maxTextWidth
        )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setMaxLines(1)
            .setEllipsize(android.text.TextUtils.TruncateAt.END)
            .build()

        canvas.save()
        canvas.translate(rect.left + 8f, rect.bottom - captionHeight + 5f)
        staticLayout.draw(canvas)
        canvas.restore()

        // Sub-pekerjaan & progress line
        paint.color = COLOR_TEXT_MUTED
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val subLine = "Sub: ${photo.subPekerjaan.ifBlank { photo.workCategory }} (${photo.progress}%)"
        canvas.drawText(subLine.take(34), rect.left + 8f, rect.bottom - captionHeight + 20f, paint)

        // Real-time timestamp & Status line
        val timeStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(photo.timestamp))
        val statusLine = "$timeStr • ${photo.status}"
        paint.textSize = 7f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText(statusLine, rect.left + 8f, rect.bottom - 4f, paint)
    }

    private fun loadAndScaleBitmap(path: String, targetW: Int, targetH: Int): Bitmap? {
        return try {
            val file = File(path)
            if (!file.exists()) return null

            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, options)

            var sampleSize = 1
            while (options.outWidth / (sampleSize * 2) >= targetW && options.outHeight / (sampleSize * 2) >= targetH) {
                sampleSize *= 2
            }

            options.inJustDecodeBounds = false
            options.inSampleSize = sampleSize
            BitmapFactory.decodeFile(path, options)
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateCenterCropSrc(srcW: Int, srcH: Int, dstW: Float, dstH: Float): Rect {
        val srcAspect = srcW.toFloat() / srcH.toFloat()
        val dstAspect = dstW / dstH

        return if (srcAspect > dstAspect) {
            val cropW = (srcH * dstAspect).toInt()
            val left = (srcW - cropW) / 2
            Rect(left, 0, left + cropW, srcH)
        } else {
            val cropH = (srcW / dstAspect).toInt()
            val top = (srcH - cropH) / 2
            Rect(0, top, srcW, top + cropH)
        }
    }

    private fun drawPageBase(
        canvas: Canvas,
        pageTitle: String,
        params: ReportGenerationParam,
        pageNumber: Int,
        totalPages: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background
        paint.color = COLOR_BG_CREAM
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), paint)

        // Header line
        paint.color = COLOR_SAGE_DARK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        canvas.drawText("LAPOOR  |  LAPORAN PROGRESS PEKERJAAN", 56f, 38f, paint)

        paint.color = COLOR_TEXT_MUTED
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9.5f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(pageTitle, PAGE_WIDTH - 56f, 38f, paint)
        paint.textAlign = Paint.Align.LEFT

        paint.color = COLOR_BORDER
        paint.strokeWidth = 0.8f
        canvas.drawLine(56f, 46f, PAGE_WIDTH - 56f, 46f, paint)

        // Footer line
        canvas.drawLine(56f, PAGE_HEIGHT - 34f, PAGE_WIDTH - 56f, PAGE_HEIGHT - 34f, paint)

        paint.color = COLOR_TEXT_MUTED
        paint.textSize = 8.5f
        canvas.drawText("${params.projectName}  •  ${params.periodeLaporan}", 56f, PAGE_HEIGHT - 22f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Halaman $pageNumber dari $totalPages", PAGE_WIDTH - 56f, PAGE_HEIGHT - 22f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    private fun drawInfoCard(
        canvas: Canvas,
        rect: RectF,
        title: String,
        fields: List<Pair<String, String>>
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = COLOR_CARD_WHITE
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(rect, 8f, 8f, paint)

        paint.color = COLOR_BORDER
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(rect, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        // Header strip
        val headerRect = RectF(rect.left, rect.top, rect.right, rect.top + 28f)
        paint.color = COLOR_SAGE_LIGHT
        canvas.drawRoundRect(headerRect, 8f, 8f, paint)
        canvas.drawRect(RectF(rect.left, rect.top + 16f, rect.right, rect.top + 28f), paint)

        paint.color = COLOR_SAGE_DARK
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(title, rect.left + 12f, rect.top + 18f, paint)

        var y = rect.top + 46f
        fields.forEach { (lbl, value) ->
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 9f
            paint.color = COLOR_TEXT_MUTED
            canvas.drawText(lbl.uppercase(Locale.getDefault()), rect.left + 12f, y, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 10f
            paint.color = COLOR_TEXT_DARK
            canvas.drawText(value.ifEmpty { "-" }, rect.left + 12f, y + 12f, paint)

            y += 28f
        }
    }

    private fun drawMetricCard(
        canvas: Canvas,
        rect: RectF,
        title: String,
        value: String,
        sub: String,
        primaryColor: Int,
        bgLightColor: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = COLOR_CARD_WHITE
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(rect, 10f, 10f, paint)

        paint.color = COLOR_BORDER
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(rect, 10f, 10f, paint)
        paint.style = Paint.Style.FILL

        // Color accent bar
        paint.color = primaryColor
        canvas.drawRoundRect(RectF(rect.left, rect.top, rect.left + 6f, rect.bottom), 4f, 4f, paint)

        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = COLOR_TEXT_MUTED
        canvas.drawText(title, rect.left + 18f, rect.top + 24f, paint)

        paint.textSize = 26f
        paint.color = primaryColor
        canvas.drawText(value, rect.left + 18f, rect.top + 58f, paint)

        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = COLOR_TEXT_MUTED
        canvas.drawText(sub, rect.left + 18f, rect.top + 76f, paint)
    }

    private fun drawProgressBar(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        title: String,
        pct: Int,
        barColor: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = COLOR_TEXT_DARK
        canvas.drawText(title, x, y, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("$pct%", x + width, y, paint)
        paint.textAlign = Paint.Align.LEFT

        // Track
        val trackRect = RectF(x, y + 6f, x + width, y + 16f)
        paint.color = COLOR_BORDER
        canvas.drawRoundRect(trackRect, 5f, 5f, paint)

        // Progress
        val progW = (width * (pct.coerceIn(0, 100) / 100f))
        val progRect = RectF(x, y + 6f, x + progW, y + 16f)
        paint.color = barColor
        canvas.drawRoundRect(progRect, 5f, 5f, paint)
    }

    private fun drawStatusItem(
        canvas: Canvas,
        x: Float,
        y: Float,
        label: String,
        count: String,
        color: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = color
        canvas.drawCircle(x + 6f, y + 6f, 6f, paint)

        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = COLOR_TEXT_DARK
        canvas.drawText(label, x + 20f, y + 10f, paint)

        paint.textSize = 14f
        paint.color = color
        canvas.drawText("$count Item", x + 20f, y + 28f, paint)
    }

    fun sharePdf(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Laporan Progress Pekerjaan - ${pdfFile.name}")
            putExtra(Intent.EXTRA_TEXT, "Terlampir Laporan Progress Pekerjaan Proyek dalam format PDF resmi dari aplikasi LAPOOR.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Bagikan Laporan PDF Melalui:"))
    }
}
