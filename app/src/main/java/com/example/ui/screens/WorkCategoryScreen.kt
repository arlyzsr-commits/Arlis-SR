package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material.icons.outlined.Foundation
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
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.theme.PastelMutedPeach
import com.example.ui.theme.PastelMutedPeachContainer
import com.example.ui.theme.PastelMutedText
import com.example.ui.theme.PastelSageContainer
import com.example.ui.theme.PastelSageOnContainer
import com.example.ui.theme.PastelSagePrimary
import com.example.ui.theme.PastelSurfaceCard
import com.example.ui.viewmodel.LapoorViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkCategoryScreen(
    viewModel: LapoorViewModel,
    onBack: () -> Unit,
    onCapturePhotoForWork: (WorkCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedCategory by viewModel.selectedWorkCategory.collectAsState()
    val selectedLocationFilter by viewModel.selectedLocationFilter.collectAsState()
    val locations by viewModel.locations.collectAsState()
    val filteredPhotos by viewModel.filteredPhotos.collectAsState()

    var editingPhoto by remember { mutableStateOf<ProgressPhoto?>(null) }
    var viewingPhoto by remember { mutableStateOf<ProgressPhoto?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PastelCreamBackground)
            .testTag("work_category_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "DOKUMENTASI PEKERJAAN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PastelMutedText
                )
                Text(
                    text = selectedCategory.displayName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PastelCharcoalText
                )
            }
        }

        // Work Categories Selector Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WorkCategory.entries.forEach { cat ->
                val isSelected = selectedCategory == cat
                val (selBg, selColor, catIcon) = when (cat) {
                    WorkCategory.STRUKTUR -> Triple(PastelSageContainer, PastelSagePrimary, Icons.Outlined.Foundation)
                    WorkCategory.ARSITEK -> Triple(PastelDustyBlueContainer, PastelDustyBlue, Icons.Outlined.Architecture)
                    WorkCategory.MEP -> Triple(PastelMutedPeachContainer, PastelMutedPeach, Icons.Filled.ElectricBolt)
                }

                Surface(
                    onClick = { viewModel.selectWorkCategory(cat) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) selBg else Color.White,
                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(0.8.dp, PastelBorderColor),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = catIcon,
                            contentDescription = null,
                            tint = if (isSelected) selColor else PastelMutedText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = cat.name,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) selColor else PastelCharcoalText
                        )
                    }
                }
            }
        }

        // Subtask preview chips
        var showAddSubDialog by remember { mutableStateOf(false) }
        var newSubTaskInput by remember { mutableStateOf("") }
        val subTasks = viewModel.getSubTasksForCategory(selectedCategory)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Spacer(modifier = Modifier.width(16.dp))

            Surface(
                onClick = { showAddSubDialog = true },
                shape = RoundedCornerShape(6.dp),
                color = PastelSageContainer
            ) {
                Text(
                    text = "+ Sub Pekerjaan",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PastelSageOnContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            subTasks.forEach { sub ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.8f))
                        .border(0.6.dp, PastelBorderColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = sub, fontSize = 10.5.sp, color = PastelCharcoalText)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
        }

        if (showAddSubDialog) {
            AlertDialog(
                onDismissRequest = { showAddSubDialog = false },
                title = { Text("Tambah Sub Pekerjaan", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            "Tambahkan sub pekerjaan baru untuk jenis '${selectedCategory.displayName}':",
                            fontSize = 12.sp,
                            color = PastelMutedText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newSubTaskInput,
                            onValueChange = { newSubTaskInput = it },
                            placeholder = { Text("Contoh: Pembesian Balok B2") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newSubTaskInput.isNotBlank()) {
                                viewModel.addCustomSubTask(selectedCategory, newSubTaskInput.trim())
                                newSubTaskInput = ""
                                showAddSubDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PastelSagePrimary)
                    ) {
                        Text("Simpan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddSubDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }

        // Location Filter Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.width(16.dp))

            // "SEMUA LOKASI"
            val isAll = selectedLocationFilter == null || selectedLocationFilter == "SEMUA"
            FilterChip(
                selected = isAll,
                onClick = { viewModel.selectLocationFilter("SEMUA") },
                label = { Text("Semua Lokasi", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PastelSagePrimary,
                    selectedLabelColor = Color.White
                )
            )

            locations.forEach { loc ->
                val isSel = selectedLocationFilter == loc.namaLokasi
                FilterChip(
                    selected = isSel,
                    onClick = { viewModel.selectLocationFilter(loc.namaLokasi) },
                    label = { Text(loc.namaLokasi, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PastelSagePrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
        }

        // Action Bar for this Category: Count + "+ Ambil Foto"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredPhotos.size} Foto Terdata",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = PastelCharcoalText
            )

            Button(
                onClick = { onCapturePhotoForWork(selectedCategory) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PastelSagePrimary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("+ Ambil Foto", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Photo Grid
        if (filteredPhotos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(PastelSageContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = PastelSagePrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Belum Ada Dokumentasi ${selectedCategory.displayName}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelCharcoalText
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Gunakan kamera untuk mengambil dokumentasi real-time pekerjaan ini.",
                        fontSize = 12.sp,
                        color = PastelMutedText,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onCapturePhotoForWork(selectedCategory) },
                        colors = ButtonDefaults.buttonColors(containerColor = PastelSagePrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ambil Foto Sekarang")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredPhotos, key = { it.photoId }) { photo ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { viewingPhoto = photo },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.2f)
                                    .background(Color(0xFFE5E0D8))
                            ) {
                                AsyncImage(
                                    model = File(photo.filePath),
                                    contentDescription = photo.caption,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Number badge
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

                                // Progress badge
                                Box(
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .align(Alignment.TopEnd)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(PastelSagePrimary.copy(alpha = 0.85f))
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
                                Text(
                                    text = photo.locationName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PastelSagePrimary
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = photo.caption.ifBlank { photo.subPekerjaan.ifBlank { photo.workCategory } },
                                    fontSize = 11.sp,
                                    color = PastelCharcoalText,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StatusBadge(status = photo.status)

                                    Row {
                                        IconButton(
                                            onClick = { editingPhoto = photo },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = PastelMutedText, modifier = Modifier.size(14.dp))
                                        }
                                        IconButton(
                                            onClick = { viewModel.deletePhoto(photo) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFC45151), modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Viewing Photo Dialog
    viewingPhoto?.let { photo ->
        AlertDialog(
            onDismissRequest = { viewingPhoto = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Foto #${photo.photoNumber} - ${photo.locationName}", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    StatusBadge(status = photo.status)
                }
            },
            text = {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
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
                    Text(photo.caption.ifBlank { "(Tidak ada keterangan)" }, fontSize = 13.sp, color = PastelCharcoalText)

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sub Pekerjaan: ${photo.subPekerjaan}", fontSize = 12.sp, color = PastelMutedText)
                        Text("Progress: ${photo.progress}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PastelSagePrimary)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val dateStr = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault()).format(Date(photo.timestamp))
                    Text("Waktu Pengambilan: $dateStr", fontSize = 11.sp, color = PastelMutedText)
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
                    Text("Edit Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewingPhoto = null }) {
                    Text("Tutup")
                }
            }
        )
    }

    // Editing Photo Dialog
    editingPhoto?.let { photo ->
        var editCaption by remember { mutableStateOf(photo.caption) }
        var editProgress by remember { mutableIntStateOf(photo.progress) }
        var editStatus by remember { mutableStateOf(ProgressStatus.fromLabel(photo.status)) }

        AlertDialog(
            onDismissRequest = { editingPhoto = null },
            title = { Text("Edit Informasi Foto #${photo.photoNumber}", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Keterangan Foto:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = editCaption,
                        onValueChange = { editCaption = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    Spacer(modifier = Modifier.height(12.dp))

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

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Status Pekerjaan:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updatePhoto(
                            photo.copy(
                                caption = editCaption.trim(),
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
}
