package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.engine.ReportPdfGenerator
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
import com.example.ui.viewmodel.LapoorViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: LapoorViewModel,
    onOpenPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeProject by viewModel.activeProject.collectAsState()
    val locations by viewModel.locations.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val reports by viewModel.reports.collectAsState()

    var selectedWorkFilter by remember { mutableStateOf("SEMUA") }
    var selectedLocationFilter by remember { mutableStateOf("SEMUA") }
    var periodePreset by remember { mutableStateOf("Mingguan") }
    var customPeriodeText by remember { mutableStateOf("Periode Minggu ke-4 (Oktober 2026)") }
    var tanggalLaporanText by remember { mutableStateOf(SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())) }
    var namaPenyusun by remember { mutableStateOf("Site Inspector") }
    var jabatan by remember { mutableStateOf("Quality Control / Supervisor") }
    var keteranganUmum by remember { mutableStateOf("Dokumentasi progress fisik mingguan pekerjaan proyek di lapangan.") }

    var showAddSubTaskDialog by remember { mutableStateOf(false) }
    var selectedCatForNewSub by remember { mutableStateOf(com.example.data.model.WorkCategory.STRUKTUR) }
    var newSubTaskInputText by remember { mutableStateOf("") }

    if (showAddSubTaskDialog) {
        AlertDialog(
            onDismissRequest = { showAddSubTaskDialog = false },
            title = { Text("Tambah Sub Pekerjaan", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Pilih Jenis Pekerjaan:", fontSize = 12.sp, color = PastelMutedText)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        com.example.data.model.WorkCategory.entries.forEach { cat ->
                            val isSel = selectedCatForNewSub == cat
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedCatForNewSub = cat },
                                label = { Text(cat.name, fontSize = 10.5.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PastelSagePrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Text("Nama Sub Pekerjaan Baru:", fontSize = 12.sp, color = PastelMutedText)
                    OutlinedTextField(
                        value = newSubTaskInputText,
                        onValueChange = { newSubTaskInputText = it },
                        placeholder = { Text("Contoh: Pembesian Balok Lantai 2") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSubTaskInputText.isNotBlank()) {
                            viewModel.addCustomSubTask(selectedCatForNewSub, newSubTaskInputText.trim())
                            newSubTaskInputText = ""
                            showAddSubTaskDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PastelSagePrimary)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubTaskDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PastelCreamBackground)
            .testTag("report_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PastelSagePrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "CETAK LAPORAN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelSagePrimary
                        )
                        Text(
                            text = "Konfigurasi & Generate PDF",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelCharcoalText
                        )
                    }
                }

                Button(
                    onClick = { showAddSubTaskDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PastelDustyBlue),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("+ Sub Pekerjaan", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Form Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "PARAMETER LAPORAN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelCharcoalText
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Project Info Readonly
                    Text("Proyek:", fontSize = 11.sp, color = PastelMutedText, fontWeight = FontWeight.Bold)
                    Text(activeProject?.namaProject ?: "-", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PastelCharcoalText)
                    Text("${activeProject?.perusahaan ?: ""} • ${activeProject?.lokasi ?: ""}", fontSize = 12.sp, color = PastelMutedText)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Disiplin Pekerjaan Filter
                    Text("Pilihan Disiplin Pekerjaan:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PastelCharcoalText)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("SEMUA", "STRUKTUR", "ARSITEK", "MEP").forEach { opt ->
                            val isSel = selectedWorkFilter == opt
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedWorkFilter = opt },
                                label = { Text(opt, fontSize = 11.5.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PastelSageContainer,
                                    selectedLabelColor = PastelSageOnContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Lokasi Filter
                    Text("Pilihan Lokasi:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PastelCharcoalText)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedLocationFilter == "SEMUA",
                            onClick = { selectedLocationFilter = "SEMUA" },
                            label = { Text("Semua Lokasi", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PastelDustyBlueContainer,
                                selectedLabelColor = PastelDustyBlue
                            )
                        )
                        locations.forEach { loc ->
                            FilterChip(
                                selected = selectedLocationFilter == loc.namaLokasi,
                                onClick = { selectedLocationFilter = loc.namaLokasi },
                                label = { Text(loc.namaLokasi, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PastelDustyBlueContainer,
                                    selectedLabelColor = PastelDustyBlue
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Periode Presets
                    Text("Pilihan Periode:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PastelCharcoalText)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Hari Ini", "Mingguan", "Bulanan", "Custom").forEach { preset ->
                            val isSel = periodePreset == preset
                            FilterChip(
                                selected = isSel,
                                onClick = {
                                    periodePreset = preset
                                    customPeriodeText = when (preset) {
                                        "Hari Ini" -> "Laporan Harian (${SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())})"
                                        "Mingguan" -> "Periode Minggu ke-4 (${SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())})"
                                        "Bulanan" -> "Laporan Bulanan ${SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())}"
                                        else -> customPeriodeText
                                    }
                                },
                                label = { Text(preset, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = customPeriodeText,
                        onValueChange = { customPeriodeText = it },
                        label = { Text("Keterangan Periode Laporan", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = tanggalLaporanText,
                        onValueChange = { tanggalLaporanText = it },
                        label = { Text("Tanggal Laporan", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = namaPenyusun,
                            onValueChange = { namaPenyusun = it },
                            label = { Text("Nama Penyusun", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = jabatan,
                            onValueChange = { jabatan = it },
                            label = { Text("Jabatan", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = keteranganUmum,
                        onValueChange = { keteranganUmum = it },
                        label = { Text("Keterangan Umum Laporan", fontSize = 12.sp) },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 1. PEMILIHAN FOTO UNTUK LAPORAN
                    val candidatePhotos = remember(photos, selectedWorkFilter, selectedLocationFilter) {
                        photos.filter { p ->
                            val matchWork = selectedWorkFilter == "SEMUA" || p.workCategory.contains(selectedWorkFilter, ignoreCase = true)
                            val matchLoc = selectedLocationFilter == "SEMUA" || p.locationName.equals(selectedLocationFilter, ignoreCase = true)
                            matchWork && matchLoc
                        }
                    }
                    val selectedPhotoIds by viewModel.selectedPhotoIdsForReport.collectAsState()
                    val selectedCount = if (selectedPhotoIds.isEmpty()) candidatePhotos.size else candidatePhotos.count { selectedPhotoIds.contains(it.photoId) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "PEMILIHAN FOTO LAPORAN",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PastelCharcoalText
                            )
                            Text(
                                text = "$selectedCount dari ${candidatePhotos.size} foto dipilih",
                                fontSize = 11.sp,
                                color = PastelSagePrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TextButton(
                                onClick = { viewModel.selectAllPhotosForReport(candidatePhotos.map { it.photoId }) }
                            ) {
                                Text("Pilih Semua", fontSize = 11.sp, color = PastelSagePrimary)
                            }
                            TextButton(
                                onClick = { viewModel.deselectAllPhotosForReport() }
                            ) {
                                Text("Kosongkan", fontSize = 11.sp, color = PastelMutedText)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (candidatePhotos.isEmpty()) {
                        Text(
                            text = "Belum ada foto yang sesuai dengan filter pekerjaan/lokasi di atas.",
                            fontSize = 11.5.sp,
                            color = PastelMutedText
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            candidatePhotos.forEach { photo ->
                                val isSelected = selectedPhotoIds.isEmpty() || selectedPhotoIds.contains(photo.photoId)
                                Card(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { viewModel.togglePhotoSelectionForReport(photo.photoId) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (isSelected) PastelSageContainer else Color(0xFFF3ECE0)),
                                    border = if (isSelected) CardDefaults.outlinedCardBorder().copy(width = 2.dp, brush = androidx.compose.ui.graphics.SolidColor(PastelSagePrimary)) else null
                                ) {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(90.dp)
                                        ) {
                                            AsyncImage(
                                                model = File(photo.filePath),
                                                contentDescription = photo.caption,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )

                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = { viewModel.togglePhotoSelectionForReport(photo.photoId) },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = PastelSagePrimary,
                                                    uncheckedColor = Color.White
                                                ),
                                                modifier = Modifier.align(Alignment.TopEnd)
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .padding(4.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color.Black.copy(alpha = 0.65f))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "Foto #${photo.photoNumber}",
                                                    fontSize = 8.5.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Column(modifier = Modifier.padding(6.dp)) {
                                            Text(
                                                text = photo.locationName,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PastelCharcoalText,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "${photo.subPekerjaan} (${photo.progress}%)",
                                                fontSize = 9.sp,
                                                color = PastelMutedText,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 2. KETERANGAN & CATATAN BERDASARKAN LOKASI
                    val activeLocations = remember(candidatePhotos, selectedPhotoIds) {
                        val includedPhotos = candidatePhotos.filter { selectedPhotoIds.isEmpty() || selectedPhotoIds.contains(it.photoId) }
                        includedPhotos.groupBy { it.locationName }
                    }
                    val manualNotes by viewModel.locationManualNotes.collectAsState()

                    Text(
                        text = "KETERANGAN & CATATAN PER LOKASI",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelCharcoalText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rangkuman sub-pekerjaan, progress fisik, dan catatan manual untuk masing-masing lokasi:",
                        fontSize = 11.sp,
                        color = PastelMutedText
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (activeLocations.isEmpty()) {
                        Text(
                            text = "Pilih minimal 1 foto untuk mengisi catatan lokasi.",
                            fontSize = 11.5.sp,
                            color = PastelMutedText
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            activeLocations.forEach { (locName, locPhotos) ->
                                val subSummary = locPhotos
                                    .groupBy { it.subPekerjaan.ifBlank { it.workCategory } }
                                    .map { (sub, pList) ->
                                        val avgProg = pList.map { it.progress }.average().toInt()
                                        "$sub: $avgProg%"
                                    }.joinToString("  •  ")

                                val workCat = locPhotos.firstOrNull()?.workCategory ?: selectedWorkFilter
                                val currentNote = manualNotes[locName] ?: ""

                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = PastelCreamBackground),
                                    border = CardDefaults.outlinedCardBorder(),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = PastelSagePrimary, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Lokasi: $locName",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PastelCharcoalText
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = PastelDustyBlueContainer
                                            ) {
                                                Text(
                                                    text = "Pekerjaan: $workCat",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PastelDustyBlue,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = "Sub Pekerjaan & Persentase Progress:",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PastelSagePrimary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))

                                        val subGroups = locPhotos
                                            .groupBy { it.subPekerjaan.ifBlank { it.workCategory } }
                                            .map { (sub, pList) ->
                                                val avgProg = pList.map { it.progress }.average().toInt()
                                                Pair(sub, avgProg)
                                            }

                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            subGroups.forEach { (sub, prog) ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(text = "• $sub", fontSize = 11.sp, color = PastelCharcoalText)
                                                    Text(text = "$prog%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PastelSagePrimary)
                                                }
                                                androidx.compose.material3.LinearProgressIndicator(
                                                    progress = { prog / 100f },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(4.dp)
                                                        .clip(RoundedCornerShape(2.dp)),
                                                    color = PastelSagePrimary,
                                                    trackColor = Color(0xFFE8E5DF)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = "Catatan yang diisi manual:",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PastelCharcoalText
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))

                                        OutlinedTextField(
                                            value = currentNote,
                                            onValueChange = { viewModel.updateLocationManualNote(locName, it) },
                                            placeholder = { Text("Isi catatan manual lapangan untuk lokasi $locName...", fontSize = 11.sp) },
                                            label = { Text("Catatan Lapangan Manual", fontSize = 11.sp) },
                                            minLines = 2,
                                            maxLines = 3,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Button PREVIEW LAPORAN
                    Button(
                        onClick = {
                            viewModel.prepareReportPreview(
                                workCategoryFilter = selectedWorkFilter,
                                locationFilter = selectedLocationFilter,
                                periodeLaporan = customPeriodeText,
                                tanggalLaporan = tanggalLaporanText,
                                namaPenyusun = namaPenyusun,
                                jabatan = jabatan,
                                keteranganUmum = keteranganUmum,
                                onReady = onOpenPreview
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PastelSagePrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_preview_report")
                    ) {
                        Icon(imageVector = Icons.Default.Preview, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PREVIEW LAPORAN PDF (A4 LANDSCAPE)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Section: Riwayat Laporan Terakhir
        if (reports.isNotEmpty()) {
            item {
                Text(
                    text = "Riwayat Laporan Telah Dibuat (${reports.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PastelCharcoalText
                )
            }

            items(reports) { rep ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PastelMutedPeachContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = PastelMutedPeach,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${rep.workCategoryFilter} • ${rep.periodeLaporan}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PastelCharcoalText
                                )
                                Text(
                                    text = "${rep.totalPhotos} Foto • ${rep.tanggalLaporan}",
                                    fontSize = 11.5.sp,
                                    color = PastelMutedText
                                )
                            }
                        }

                        Row {
                            val pdfFile = File(rep.pdfFilePath)
                            if (pdfFile.exists()) {
                                IconButton(
                                    onClick = {
                                        ReportPdfGenerator.sharePdf(context, pdfFile)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = PastelSagePrimary)
                                }
                            }
                            IconButton(
                                onClick = { viewModel.deleteReport(rep) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFC04E4E))
                            }
                        }
                    }
                }
            }
        }
    }
}
