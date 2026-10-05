package com.example.ui.screens

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ProgressPhoto
import com.example.data.model.ReportGenerationParam
import com.example.engine.LocationReportPage
import com.example.engine.ReportPdfGenerator
import com.example.ui.components.StatusBadge
import com.example.ui.theme.PastelBorderColor
import com.example.ui.theme.PastelCharcoalText
import com.example.ui.theme.PastelCreamBackground
import com.example.ui.theme.PastelDustyBlue
import com.example.ui.theme.PastelDustyBlueContainer
import com.example.ui.theme.PastelMutedPeach
import com.example.ui.theme.PastelMutedPeachContainer
import com.example.ui.theme.PastelMutedText
import com.example.ui.theme.PastelSageContainer
import com.example.ui.theme.PastelSageOnContainer
import com.example.ui.theme.PastelSagePrimary
import com.example.ui.theme.PastelSurfaceCard
import com.example.ui.viewmodel.LapoorViewModel
import java.io.File
import java.util.Locale

@Composable
fun ReportPreviewScreen(
    viewModel: LapoorViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val params by viewModel.reportParams.collectAsState()
    val previewPages by viewModel.previewPages.collectAsState()
    val isGenerating by viewModel.isGeneratingPdf.collectAsState()
    val generatedPdf by viewModel.generatedPdfFile.collectAsState()
    val photos by viewModel.photos.collectAsState()

    BackHandler { onBack() }

    // Page indexes:
    // 0 -> Cover
    // 1 -> Project Info
    // 2 -> Summary
    // 3+ -> Documentation Pages
    var selectedPageIndex by remember { mutableIntStateOf(0) }
    val totalPreviewPages = 3 + previewPages.size.coerceAtLeast(1)

    val currentParams = params ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFEFECE6))
            .testTag("report_preview_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "PREVIEW LAPORAN PDF",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelSagePrimary
                    )
                    Text(
                        text = "A4 Landscape (Halaman ${selectedPageIndex + 1} dari $totalPreviewPages)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelCharcoalText
                    )
                }
            }

            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Edit Form", fontSize = 11.sp)
            }
        }

        // Horizontal Page Selector Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(bottom = 10.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.width(16.dp))
            val pageLabels = mutableListOf("1. Cover", "2. Info Proyek", "3. Ringkasan Progress")
            previewPages.forEachIndexed { i, page ->
                pageLabels.add("${4 + i}. ${page.locationName}")
            }
            if (previewPages.isEmpty()) {
                pageLabels.add("4. Dokumentasi Kosong")
            }

            pageLabels.forEachIndexed { idx, lbl ->
                FilterChip(
                    selected = selectedPageIndex == idx,
                    onClick = { selectedPageIndex = idx },
                    label = { Text(lbl, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PastelSagePrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
        }

        // Main A4 Landscape Canvas Container
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // A4 Landscape Aspect Ratio is 842 / 595 ≈ 1.415
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.415f)
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PastelCreamBackground)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when (selectedPageIndex) {
                        0 -> PreviewCoverPage(params = currentParams)
                        1 -> PreviewProjectInfoPage(params = currentParams)
                        2 -> PreviewSummaryPage(params = currentParams, photos = photos)
                        else -> {
                            val docPageIndex = selectedPageIndex - 3
                            if (docPageIndex in previewPages.indices) {
                                PreviewDocPage(
                                    page = previewPages[docPageIndex],
                                    params = currentParams,
                                    pageNum = selectedPageIndex + 1,
                                    totalPage = totalPreviewPages
                                )
                            } else {
                                PreviewEmptyDocPage(params = currentParams)
                            }
                        }
                    }
                }
            }
        }

        // Bottom Action Bar: GENERATE PDF | SHARE PDF | CETAK
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // GENERATE PDF
                    Button(
                        onClick = {
                            viewModel.generatePdf { file ->
                                // PDF ready
                            }
                        },
                        enabled = !isGenerating,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PastelSagePrimary),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_generate_pdf_action")
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Membuat PDF...", fontSize = 12.sp)
                        } else {
                            Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("GENERATE PDF", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // SHARE PDF (Active when PDF is generated or generates on the fly)
                    Button(
                        onClick = {
                            val pdf = generatedPdf
                            if (pdf != null && pdf.exists()) {
                                ReportPdfGenerator.sharePdf(context, pdf)
                            } else {
                                viewModel.generatePdf { file ->
                                    ReportPdfGenerator.sharePdf(context, file)
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PastelDustyBlue),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_share_pdf_action")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SHARE PDF", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    // CETAK / PRINT
                    OutlinedButton(
                        onClick = {
                            val pdf = generatedPdf
                            if (pdf != null && pdf.exists()) {
                                ReportPdfGenerator.sharePdf(context, pdf)
                            } else {
                                viewModel.generatePdf { file ->
                                    ReportPdfGenerator.sharePdf(context, file)
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("CETAK", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewCoverPage(params: ReportGenerationParam) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PastelCreamBackground)
            .padding(16.dp)
    ) {
        // Decorative left border strip
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(6.dp)
                .background(PastelSagePrimary)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PastelSagePrimary
                ) {
                    Text(
                        text = "LAPOOR",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "LAPORAN PROGRESS PEKERJAAN",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = PastelMutedText
                )
            }

            // Title
            Column {
                Text(
                    text = "LAPORAN DOKUMENTASI FISIK",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PastelCharcoalText
                )
                Text(
                    text = params.projectName.uppercase(Locale.getDefault()),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PastelDustyBlue
                )
            }

            // Two-column table card
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("OWNER: ${params.owner.ifEmpty { "-" }}", fontSize = 9.sp, color = PastelCharcoalText)
                        Text("KONTRAKTOR: ${params.kontraktor.ifEmpty { "-" }}", fontSize = 9.sp, color = PastelCharcoalText)
                        Text("PERIODE: ${params.periodeLaporan}", fontSize = 9.sp, color = PastelCharcoalText)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("LOKASI: ${params.lokasiProyek.ifEmpty { "-" }}", fontSize = 9.sp, color = PastelCharcoalText)
                        Text("KONSULTAN: ${params.konsultan.ifEmpty { "-" }}", fontSize = 9.sp, color = PastelCharcoalText)
                        Text("TANGGAL: ${params.tanggalLaporan}", fontSize = 9.sp, color = PastelCharcoalText)
                    }
                }
            }

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Disusun: ${params.namaPenyusun} (${params.jabatan})", fontSize = 8.5.sp, color = PastelMutedText)
                Text("LAPOOR • HALAMAN 1", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = PastelSagePrimary)
            }
        }
    }
}

@Composable
private fun PreviewProjectInfoPage(params: ReportGenerationParam) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Page Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("LAPOOR | INFORMASI PROYEK & KONTRAK", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PastelSagePrimary)
            Text("HALAMAN 2", fontSize = 9.sp, color = PastelMutedText)
        }

        // 4 Cards Grid
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MiniInfoCard(
                    title = "IDENTITAS PROYEK",
                    items = listOf(
                        "Proyek" to params.projectName,
                        "Kontrak" to params.nomorKontrak,
                        "Lokasi" to params.lokasiProyek,
                        "Periode" to params.periodeLaporan
                    ),
                    modifier = Modifier.weight(1f)
                )
                MiniInfoCard(
                    title = "KONTRAKTOR & STAKEHOLDER",
                    items = listOf(
                        "Kontraktor" to params.kontraktor,
                        "Scope" to params.scopePekerjaanKontraktor.ifBlank { "Struktur, Arsitektur, MEP" },
                        "Owner" to params.owner,
                        "Konsultan" to params.konsultan
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MiniInfoCard(
                    title = "MANAJEMEN & MK",
                    items = listOf(
                        "PM MK/Owner" to params.projectManagerMK.ifBlank { params.projectManager },
                        "PM Kontraktor" to params.projectManager,
                        "Site Mgr" to params.siteManager,
                        "Penyusun" to "${params.namaPenyusun} (${params.jabatan})"
                    ),
                    modifier = Modifier.weight(1f)
                )
                MiniInfoCard(
                    title = "PIC OWNER SETIAP PEKERJAAN",
                    items = listOf(
                        "PIC Struktur" to params.picOwnerStruktur.ifBlank { "-" },
                        "PIC Arsitek" to params.picOwnerArsitek.ifBlank { "-" },
                        "PIC MEP" to params.picOwnerMep.ifBlank { "-" },
                        "Fokus" to params.workCategoryFilter
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Page Footer
        Text("${params.projectName} • ${params.periodeLaporan}", fontSize = 8.5.sp, color = PastelMutedText)
    }
}

@Composable
private fun MiniInfoCard(title: String, items: List<Pair<String, String>>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(title, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = PastelSagePrimary)
            Spacer(modifier = Modifier.height(4.dp))
            items.forEach { (lbl, v) ->
                Text("$lbl: ${v.ifEmpty { "-" }}", fontSize = 8.sp, color = PastelCharcoalText, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun PreviewSummaryPage(params: ReportGenerationParam, photos: List<ProgressPhoto>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Page Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("LAPOOR | RINGKASAN PROGRESS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PastelSagePrimary)
            Text("HALAMAN 3", fontSize = 9.sp, color = PastelMutedText)
        }

        val totalPhotos = photos.size
        val avgProgress = if (photos.isNotEmpty()) photos.map { it.progress }.average().toInt() else 0

        // 3 Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = PastelSageContainer)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("TOTAL FOTO", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = PastelSageOnContainer)
                    Text("$totalPhotos", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = PastelSageOnContainer)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = PastelDustyBlueContainer)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("LOKASI", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = PastelDustyBlue)
                    Text("${photos.map { it.locationName }.distinct().size}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = PastelDustyBlue)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = PastelMutedPeachContainer)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("PROGRESS", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = PastelMutedPeach)
                    Text("$avgProgress%", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = PastelMutedPeach)
                }
            }
        }

        // Bars
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("PROGRESS BERDASARKAN DISIPLIN", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = PastelCharcoalText)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("STRUKTUR", fontSize = 8.sp, modifier = Modifier.width(60.dp))
                    LinearProgressIndicator(progress = { 0.65f }, modifier = Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(3.dp)), color = PastelSagePrimary)
                    Text(" 65%", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("ARSITEK", fontSize = 8.sp, modifier = Modifier.width(60.dp))
                    LinearProgressIndicator(progress = { 0.48f }, modifier = Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(3.dp)), color = PastelDustyBlue)
                    Text(" 48%", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("MEP", fontSize = 8.sp, modifier = Modifier.width(60.dp))
                    LinearProgressIndicator(progress = { 0.52f }, modifier = Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(3.dp)), color = PastelMutedPeach)
                    Text(" 52%", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Text("${params.projectName} • ${params.periodeLaporan}", fontSize = 8.5.sp, color = PastelMutedText)
    }
}

@Composable
private fun PreviewDocPage(
    page: LocationReportPage,
    params: ReportGenerationParam,
    pageNum: Int,
    totalPage: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        // Page Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = PastelSagePrimary
                ) {
                    Text(
                        text = "LAPOOR",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "DOKUMENTASI FOTO PROGRESS",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = PastelMutedText
                )
            }
            Text(
                text = "${params.projectName} • ${params.periodeLaporan}",
                fontSize = 7.5.sp,
                color = PastelMutedText
            )
        }

        // Two-Column Body:
        // Left Column: Ringkasan Berdasarkan Lokasi (33% width)
        // Right Column: Photo Dokumentasi dan Keterangan Photo (67% width)
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // =========================================================================
            // 1. SEBELAH KIRI: RINGKASAN BERDASARKAN LOKASI
            // =========================================================================
            Card(
                modifier = Modifier
                    .weight(0.33f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Header Tag
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = PastelSagePrimary
                    ) {
                        Text(
                            text = "RINGKASAN LOKASI",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Nama Lokasi
                    Column {
                        Text(
                            text = page.locationName.uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PastelCharcoalText,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Halaman ${page.pageIndexForLocation} dari ${page.totalPagesForLocation} di lokasi ini",
                            fontSize = 7.sp,
                            color = PastelMutedText
                        )
                    }

                    // Nama Pekerjaan (Jenis Pekerjaan)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = PastelDustyBlueContainer
                    ) {
                        Text(
                            text = "PEKERJAAN: ${page.workCategory}",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelDustyBlue,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    // Sub Pekerjaan & Progress
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = "SUB PEKERJAAN & PROGRESS",
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelMutedText
                        )
                        val subList = if (page.subTasksList.isNotEmpty()) {
                            page.subTasksList.take(4)
                        } else {
                            page.photos.groupBy { it.subPekerjaan.ifBlank { it.workCategory } }
                                .map { (k, v) -> Pair(k, v.map { it.progress }.average().toInt()) }
                                .take(4)
                        }

                        subList.forEach { (sub, prog) ->
                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = sub,
                                        fontSize = 7.sp,
                                        color = PastelCharcoalText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "$prog%",
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PastelSagePrimary
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { prog / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = PastelSagePrimary,
                                    trackColor = Color(0xFFEFECE6)
                                )
                            }
                        }
                    }

                    // Average progress pill
                    val avgProg = if (page.photos.isNotEmpty()) page.photos.map { it.progress }.average().toInt() else 0
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = PastelSageContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Progress Area: $avgProg%",
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelSageOnContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Catatan Manual Lapangan
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "CATATAN LAPANGAN (MANUAL)",
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelMutedPeach
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PastelCreamBackground,
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, PastelBorderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = page.manualNote.ifBlank { "Pekerjaan sesuai dengan gambar teknis dan instruksi kerja." },
                                fontSize = 7.sp,
                                color = PastelCharcoalText,
                                modifier = Modifier.padding(5.dp),
                                lineHeight = 9.sp,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Tim & PIC Owner
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFF7F5F0))
                            .padding(5.dp),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        val kontr = page.kontraktor.ifBlank { params.kontraktor.ifBlank { "-" } }
                        val scopePek = (page.scopePekerjaan.ifBlank { params.scopePekerjaanKontraktor }).ifBlank { "-" }
                        val pmMk = page.projectManagerMK.ifBlank { params.projectManagerMK.ifBlank { "-" } }
                        val pic = page.picOwner.ifBlank { "-" }

                        Text(text = "Kontraktor: $kontr", fontSize = 6.5.sp, color = PastelCharcoalText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = "Scope: $scopePek", fontSize = 6.5.sp, color = PastelMutedText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = "PM MK/Owner: $pmMk", fontSize = 6.5.sp, color = PastelCharcoalText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = "PIC Owner: $pic", fontSize = 6.5.sp, fontWeight = FontWeight.Bold, color = PastelSagePrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            // =========================================================================
            // 2. SEBELAH KANAN: PHOTO DOKUMENTASI DAN KETERANGAN PHOTO
            // =========================================================================
            Card(
                modifier = Modifier
                    .weight(0.67f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                ) {
                    // Right column header bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FOTO DOKUMENTASI & KETERANGAN PHOTO (${page.photos.size} Foto)",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelSagePrimary
                        )
                        Text(
                            text = "Halaman ${page.pageIndexForLocation} / ${page.totalPagesForLocation}",
                            fontSize = 7.sp,
                            color = PastelMutedText
                        )
                    }

                    // Photo slots area
                    BoxWithConstraints(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        val totalW = maxWidth
                        val totalH = maxHeight

                        page.slots.forEach { slot ->
                            val slotX = totalW * slot.normalizedX
                            val slotY = totalH * slot.normalizedY
                            val slotW = totalW * slot.normalizedWidth
                            val slotH = totalH * slot.normalizedHeight

                            Card(
                                modifier = Modifier
                                    .offset(x = slotX, y = slotY)
                                    .size(width = slotW, height = slotH),
                                shape = RoundedCornerShape(6.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Image
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .background(Color(0xFFE5E0D8))
                                    ) {
                                        AsyncImage(
                                            model = File(slot.photo.filePath),
                                            contentDescription = slot.photo.caption,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        // Photo number badge
                                        Box(
                                            modifier = Modifier
                                                .padding(3.dp)
                                                .align(Alignment.TopStart)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(Color.Black.copy(alpha = 0.65f))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Foto #${slot.photo.photoNumber}",
                                                fontSize = 7.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        // Progress badge
                                        Box(
                                            modifier = Modifier
                                                .padding(3.dp)
                                                .align(Alignment.TopEnd)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(PastelSagePrimary.copy(alpha = 0.85f))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${slot.photo.progress}%",
                                                fontSize = 7.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Keterangan Photo Area (Caption, Sub, Timestamp, Status)
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFFAF9F6))
                                            .padding(horizontal = 4.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = slot.photo.caption.ifBlank { slot.photo.subPekerjaan.ifBlank { slot.photo.workCategory } },
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = PastelCharcoalText
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Sub: ${slot.photo.subPekerjaan}",
                                                fontSize = 6.5.sp,
                                                color = PastelMutedText,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = slot.photo.status,
                                                fontSize = 6.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = PastelSagePrimary
                                            )
                                        }
                                        val timeStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(slot.photo.timestamp))
                                        Text(
                                            text = timeStr,
                                            fontSize = 6.sp,
                                            color = PastelMutedText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Page Footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${params.projectName} • Disusun: ${params.namaPenyusun}", fontSize = 7.5.sp, color = PastelMutedText)
            Text("Halaman $pageNum dari $totalPage", fontSize = 7.5.sp, color = PastelMutedText)
        }
    }
}

@Composable
private fun PreviewEmptyDocPage(params: ReportGenerationParam) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Belum ada foto dokumentasi untuk filter ini.", fontSize = 11.sp, color = PastelMutedText)
    }
}
