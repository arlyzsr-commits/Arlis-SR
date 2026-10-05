package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ProgressPhoto
import com.example.data.model.ProgressStatus
import com.example.data.model.WorkCategory
import com.example.ui.components.StatusBadge
import com.example.ui.theme.PastelBorderColor
import com.example.ui.theme.PastelCharcoalText
import com.example.ui.theme.PastelCreamBackground
import com.example.ui.theme.PastelDustyBlue
import com.example.ui.theme.PastelDustyBlueContainer
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
fun PhotoGalleryScreen(
    viewModel: LapoorViewModel,
    onNavigateToCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeProject by viewModel.activeProject.collectAsState()
    val locations by viewModel.locations.collectAsState()
    val photosGroupedByDate by viewModel.photosGroupedByDate.collectAsState()
    val totalPhotos by viewModel.photos.collectAsState()

    var selectedWorkFilter by remember { mutableStateOf("SEMUA") }
    var selectedLocationFilter by remember { mutableStateOf("SEMUA") }

    var editingPhoto by remember { mutableStateOf<ProgressPhoto?>(null) }
    var viewingPhoto by remember { mutableStateOf<ProgressPhoto?>(null) }
    var photoToDelete by remember { mutableStateOf<ProgressPhoto?>(null) }

    // Filter photos based on category & location
    val filteredGroupedPhotos = remember(photosGroupedByDate, selectedWorkFilter, selectedLocationFilter) {
        photosGroupedByDate.mapValues { (_, photoList) ->
            photoList.filter { p ->
                val matchWork = selectedWorkFilter == "SEMUA" || p.workCategory.contains(selectedWorkFilter, ignoreCase = true)
                val matchLoc = selectedLocationFilter == "SEMUA" || p.locationName.equals(selectedLocationFilter, ignoreCase = true)
                matchWork && matchLoc
            }
        }.filter { it.value.isNotEmpty() }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PastelCreamBackground)
            .testTag("photo_gallery_screen"),
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
                Column {
                    Text(
                        text = "GALERI DOKUMENTASI",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelSagePrimary
                    )
                    Text(
                        text = "Lihat Foto per Tanggal",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelCharcoalText
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PastelSageContainer
                ) {
                    Text(
                        text = "${totalPhotos.size} Foto",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelSageOnContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Filter Pills
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Category Filter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("SEMUA", "STRUKTUR", "ARSITEK", "MEP").forEach { cat ->
                        FilterChip(
                            selected = selectedWorkFilter == cat,
                            onClick = { selectedWorkFilter = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PastelSagePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Location Filter
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
                            selectedContainerColor = PastelDustyBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                    locations.forEach { loc ->
                        FilterChip(
                            selected = selectedLocationFilter == loc.namaLokasi,
                            onClick = { selectedLocationFilter = loc.namaLokasi },
                            label = { Text(loc.namaLokasi, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PastelDustyBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        if (filteredGroupedPhotos.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(PastelSageContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = PastelSagePrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Tidak Ada Foto Ditemukan",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelCharcoalText
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Belum ada foto dokumentasi real-time pada kriteria filter ini.",
                            fontSize = 12.sp,
                            color = PastelMutedText
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onNavigateToCamera,
                            colors = ButtonDefaults.buttonColors(containerColor = PastelSagePrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ambil Foto Sekarang")
                        }
                    }
                }
            }
        } else {
            // Grouped by Date sections
            filteredGroupedPhotos.forEach { (dateHeader, datePhotos) ->
                item(key = "header_$dateHeader") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(PastelSageContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = PastelSagePrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = dateHeader,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelCharcoalText
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE8E5DF)
                        ) {
                            Text(
                                text = "${datePhotos.size} foto",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PastelMutedText,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Grid of photos for this date
                item(key = "photos_$dateHeader") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        datePhotos.chunked(2).forEach { rowPhotos ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowPhotos.forEach { photo ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        GalleryPhotoItemCard(
                                            photo = photo,
                                            onView = { viewingPhoto = photo },
                                            onEdit = { editingPhoto = photo },
                                            onDelete = { photoToDelete = photo }
                                        )
                                    }
                                }
                                if (rowPhotos.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Viewing Photo Fullsize
    viewingPhoto?.let { photo ->
        AlertDialog(
            onDismissRequest = { viewingPhoto = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Foto #${photo.photoNumber} • ${photo.locationName}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    StatusBadge(status = photo.status)
                }
            },
            text = {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black)
                    ) {
                        AsyncImage(
                            model = File(photo.filePath),
                            contentDescription = photo.caption,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Keterangan:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PastelMutedText)
                    Text(photo.caption.ifBlank { "(Tanpa keterangan)" }, fontSize = 13.sp, color = PastelCharcoalText)

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Pekerjaan: ${photo.workCategory}", fontSize = 11.5.sp, color = PastelMutedText)
                        Text("Sub: ${photo.subPekerjaan}", fontSize = 11.5.sp, color = PastelMutedText)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val timeStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(photo.timestamp))
                        Text("Waktu: $timeStr", fontSize = 11.sp, color = PastelMutedText)
                        Text("Progress: ${photo.progress}%", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = PastelSagePrimary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        editingPhoto = photo
                        viewingPhoto = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PastelSagePrimary)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit Foto")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewingPhoto = null }) {
                    Text("Tutup")
                }
            }
        )
    }

    // Modal Edit Photo
    editingPhoto?.let { photo ->
        var editCaption by remember { mutableStateOf(photo.caption) }
        var editWorkCategory by remember { mutableStateOf(WorkCategory.fromString(photo.workCategory)) }
        var editSubTask by remember { mutableStateOf(photo.subPekerjaan) }
        var editProgress by remember { mutableIntStateOf(photo.progress) }
        var editStatus by remember { mutableStateOf(ProgressStatus.fromLabel(photo.status)) }
        var editLocationName by remember { mutableStateOf(photo.locationName) }

        var showAddSubDialogInEdit by remember { mutableStateOf(false) }
        var newSubInputInEdit by remember { mutableStateOf("") }

        if (showAddSubDialogInEdit) {
            AlertDialog(
                onDismissRequest = { showAddSubDialogInEdit = false },
                title = { Text("Tambah Sub Pekerjaan", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Tambah sub pekerjaan untuk '${editWorkCategory.displayName}':", fontSize = 12.sp, color = PastelMutedText)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = newSubInputInEdit,
                            onValueChange = { newSubInputInEdit = it },
                            placeholder = { Text("Contoh: Pengecoran Plat Lantai 2") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newSubInputInEdit.isNotBlank()) {
                                viewModel.addCustomSubTask(editWorkCategory, newSubInputInEdit.trim())
                                editSubTask = newSubInputInEdit.trim()
                                newSubInputInEdit = ""
                                showAddSubDialogInEdit = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PastelSagePrimary)
                    ) {
                        Text("Simpan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddSubDialogInEdit = false }) {
                        Text("Batal")
                    }
                }
            )
        }

        AlertDialog(
            onDismissRequest = { editingPhoto = null },
            title = {
                Text("Edit Informasi Foto #${photo.photoNumber}", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Lokasi
                    Text("Lokasi Pekerjaan:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        locations.forEach { loc ->
                            FilterChip(
                                selected = editLocationName == loc.namaLokasi,
                                onClick = { editLocationName = loc.namaLokasi },
                                label = { Text(loc.namaLokasi, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PastelDustyBlueContainer,
                                    selectedLabelColor = PastelDustyBlue
                                )
                            )
                        }
                    }

                    // 2. Jenis Pekerjaan
                    Text("Jenis Pekerjaan:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        WorkCategory.entries.forEach { cat ->
                            FilterChip(
                                selected = editWorkCategory == cat,
                                onClick = {
                                    editWorkCategory = cat
                                    val firstSub = viewModel.getSubTasksForCategory(cat).firstOrNull() ?: ""
                                    editSubTask = firstSub
                                },
                                label = { Text(cat.name, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // 3. Sub Pekerjaan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sub Pekerjaan:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "+ Tambah Sub",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelSagePrimary,
                            modifier = Modifier.clickable { showAddSubDialogInEdit = true }
                        )
                    }

                    val availableSubs = viewModel.getSubTasksForCategory(editWorkCategory)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableSubs.forEach { sub ->
                            FilterChip(
                                selected = editSubTask == sub,
                                onClick = { editSubTask = sub },
                                label = { Text(sub, fontSize = 10.5.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = editSubTask,
                        onValueChange = { editSubTask = it },
                        label = { Text("Nama Sub Pekerjaan", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 4. Progress
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Progress Pekerjaan:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("$editProgress%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PastelSagePrimary)
                    }

                    Slider(
                        value = editProgress.toFloat(),
                        onValueChange = { editProgress = it.toInt() },
                        valueRange = 0f..100f,
                        steps = 19,
                        colors = SliderDefaults.colors(
                            thumbColor = PastelSagePrimary,
                            activeTrackColor = PastelSagePrimary
                        )
                    )

                    // 5. Status
                    Text("Status Pekerjaan:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ProgressStatus.entries.forEach { st ->
                            FilterChip(
                                selected = editStatus == st,
                                onClick = { editStatus = st },
                                label = { Text(st.label, fontSize = 10.5.sp) }
                            )
                        }
                    }

                    // 6. Keterangan Foto
                    Text("Keterangan Foto:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = editCaption,
                        onValueChange = { editCaption = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Keterangan atau deskripsi pekerjaan...") },
                        minLines = 2,
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val matchedLoc = locations.firstOrNull { it.namaLokasi == editLocationName }
                        viewModel.updatePhoto(
                            photo.copy(
                                locationName = editLocationName,
                                locationId = matchedLoc?.locationId ?: photo.locationId,
                                workCategory = editWorkCategory.name,
                                caption = editCaption.trim(),
                                subPekerjaan = editSubTask.trim(),
                                progress = editProgress,
                                status = editStatus.label
                            )
                        )
                        editingPhoto = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PastelSagePrimary)
                ) {
                    Text("Simpan Perubahan")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPhoto = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Modal Confirmation Delete
    photoToDelete?.let { photo ->
        AlertDialog(
            onDismissRequest = { photoToDelete = null },
            title = { Text("Hapus Foto Dokumentasi?") },
            text = { Text("Foto #${photo.photoNumber} di lokasi '${photo.locationName}' akan dihapus secara permanen.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePhoto(photo)
                        photoToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC04E4E))
                ) {
                    Text("Hapus Foto")
                }
            },
            dismissButton = {
                TextButton(onClick = { photoToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun GalleryPhotoItemCard(
    photo: ProgressPhoto,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onView() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.25f)
                    .background(Color(0xFFE5E0D8))
            ) {
                AsyncImage(
                    model = File(photo.filePath),
                    contentDescription = photo.caption,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Top badges
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.TopStart)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "#${photo.photoNumber}",
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(4.dp))
                        .background(PastelSagePrimary.copy(alpha = 0.9f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${photo.progress}%",
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = photo.locationName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelSagePrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    StatusBadge(status = photo.status)
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = photo.subPekerjaan.ifBlank { photo.workCategory },
                    fontSize = 10.5.sp,
                    color = PastelMutedText,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = photo.caption.ifBlank { "(Tanpa keterangan)" },
                    fontSize = 11.5.sp,
                    color = PastelCharcoalText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = PastelSagePrimary, modifier = Modifier.size(15.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFC04E4E), modifier = Modifier.size(15.dp))
                    }
                }
            }
        }
    }
}
