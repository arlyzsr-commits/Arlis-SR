package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ProgressPhoto
import com.example.data.model.WorkCategory
import com.example.ui.components.LapoorNavDestination
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.WorkCategoryCard
import com.example.ui.theme.PastelAmberWarning
import com.example.ui.theme.PastelBorderColor
import com.example.ui.theme.PastelCharcoalText
import com.example.ui.theme.PastelCreamBackground
import com.example.ui.theme.PastelDustyBlue
import com.example.ui.theme.PastelDustyBlueContainer
import com.example.ui.theme.PastelDustyBlueOnContainer
import com.example.ui.theme.PastelMutedPeach
import com.example.ui.theme.PastelMutedPeachContainer
import com.example.ui.theme.PastelMutedPeachOnContainer
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

@Composable
fun HomeScreen(
    viewModel: LapoorViewModel,
    onNavigate: (LapoorNavDestination) -> Unit,
    onOpenWorkCategory: (WorkCategory) -> Unit,
    onOpenPhotoDetail: (ProgressPhoto) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeProject by viewModel.activeProject.collectAsState()
    val locations by viewModel.locations.collectAsState()
    val photos by viewModel.photos.collectAsState()

    val totalPhotos = photos.size
    val totalLocations = locations.size
    val avgProgress = if (photos.isNotEmpty()) photos.map { it.progress }.average().toInt() else 0

    // Count photos per category
    val strukturPhotos = photos.filter { it.workCategory.contains("STRUKTUR", ignoreCase = true) }
    val arsitekPhotos = photos.filter { it.workCategory.contains("ARSITEK", ignoreCase = true) }
    val mepPhotos = photos.filter { it.workCategory.contains("MEP", ignoreCase = true) }

    val strukturProgress = if (strukturPhotos.isNotEmpty()) strukturPhotos.map { it.progress }.average().toInt() else 0
    val arsitekProgress = if (arsitekPhotos.isNotEmpty()) arsitekPhotos.map { it.progress }.average().toInt() else 0
    val mepProgress = if (mepPhotos.isNotEmpty()) mepPhotos.map { it.progress }.average().toInt() else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PastelCreamBackground)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // App Header Brand
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PastelSagePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "LAPOOR",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = PastelSagePrimary
                            )
                        }
                        Text(
                            text = "Laporan Progress Pekerjaan Proyek",
                            fontSize = 12.sp,
                            color = PastelMutedText,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Active badge button
                    Surface(
                        onClick = { onNavigate(LapoorNavDestination.PROYEK) },
                        shape = RoundedCornerShape(20.dp),
                        color = PastelSageContainer,
                        modifier = Modifier.testTag("btn_switch_project")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(PastelSagePrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ganti Proyek",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PastelSageOnContainer
                            )
                        }
                    }
                }
            }
        }

        // Active Project Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(22.dp)),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PastelSagePrimary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PastelSagePrimary, Color(0xFF385841))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "PROYEK AKTIF",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            Text(
                                text = activeProject?.tanggalLaporan ?: SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = activeProject?.namaProject ?: "Pilih Proyek Pekerjaan",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = activeProject?.lokasi.takeIf { !it.isNullOrBlank() } ?: "Lokasi Lapangan",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Progress bar in card
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Progress Keseluruhan",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "$avgProgress%",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { avgProgress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFFC7E7CE),
                                trackColor = Color.White.copy(alpha = 0.25f)
                            )
                        }
                    }
                }
            }
        }

        // Primary Action: "+ BUAT DOKUMENTASI (AMBIL FOTO)"
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = { onNavigate(LapoorNavDestination.FOTO) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("btn_capture_photo_main"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PastelDustyBlue
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "+ AMBIL FOTO REAL-TIME",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // 4 Stat Cards: PROYEK AKTIF, DOKUMENTASI, LOKASI, PROGRESS
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Dokumentasi",
                        value = "$totalPhotos",
                        subtitle = "Foto Real-time",
                        icon = Icons.Default.PhotoLibrary,
                        containerColor = PastelSageContainer,
                        contentColor = PastelSageOnContainer,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(LapoorNavDestination.GALERI) }
                    )

                    StatCard(
                        title = "Lokasi",
                        value = "$totalLocations",
                        subtitle = "Zona Proyek",
                        icon = Icons.Default.LocationOn,
                        containerColor = PastelDustyBlueContainer,
                        contentColor = PastelDustyBlueOnContainer,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(LapoorNavDestination.PROYEK) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Progress Rata-rata",
                        value = "$avgProgress%",
                        subtitle = "Fisik Terpasang",
                        icon = Icons.Default.TrendingUp,
                        containerColor = PastelMutedPeachContainer,
                        contentColor = PastelMutedPeachOnContainer,
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Laporan PDF",
                        value = "A4",
                        subtitle = "Siap Cetak & Share",
                        icon = Icons.Default.Description,
                        containerColor = Color(0xFFF3ECE0),
                        contentColor = Color(0xFF5A442A),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(LapoorNavDestination.LAPORAN) }
                    )
                }
            }
        }

        // Section Title: MENU PEKERJAAN
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Disiplin Pekerjaan",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelCharcoalText
                    )
                    Text(
                        text = "Lihat Detail",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PastelSagePrimary,
                        modifier = Modifier.clickable { onOpenWorkCategory(WorkCategory.STRUKTUR) }
                    )
                }
            }
        }

        // 3 Cards: STRUKTUR, ARSITEK, MEP
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WorkCategoryCard(
                    category = WorkCategory.STRUKTUR,
                    photoCount = strukturPhotos.size,
                    progressPercentage = strukturProgress,
                    onClick = { onOpenWorkCategory(WorkCategory.STRUKTUR) }
                )

                WorkCategoryCard(
                    category = WorkCategory.ARSITEK,
                    photoCount = arsitekPhotos.size,
                    progressPercentage = arsitekProgress,
                    onClick = { onOpenWorkCategory(WorkCategory.ARSITEK) }
                )

                WorkCategoryCard(
                    category = WorkCategory.MEP,
                    photoCount = mepPhotos.size,
                    progressPercentage = mepProgress,
                    onClick = { onOpenWorkCategory(WorkCategory.MEP) }
                )
            }
        }

        // Section: DOKUMENTASI TERBARU
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 26.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dokumentasi Lapangan Terbaru",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelCharcoalText
                    )
                    if (photos.isNotEmpty()) {
                        Text(
                            text = "Lihat Semua (${photos.size}) >",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelSagePrimary,
                            modifier = Modifier.clickable { onNavigate(LapoorNavDestination.GALERI) }
                        )
                    }
                }
            }
        }

        if (photos.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
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
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = PastelSagePrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Belum Ada Dokumentasi",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelCharcoalText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Mulai dokumentasi pekerjaan dengan mengambil foto melalui kamera real-time.",
                            fontSize = 12.sp,
                            color = PastelMutedText,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { onNavigate(LapoorNavDestination.FOTO) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PastelSagePrimary)
                        ) {
                            Text("AMBIL FOTO")
                        }
                    }
                }
            }
        } else {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(photos.take(6)) { photo ->
                        Card(
                            modifier = Modifier
                                .width(190.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onOpenPhotoDetail(photo) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .background(Color(0xFFE8E5DF))
                                ) {
                                    AsyncImage(
                                        model = File(photo.filePath),
                                        contentDescription = photo.caption,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Badge location top left
                                    Box(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .align(Alignment.TopStart)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.Black.copy(alpha = 0.6f))
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = photo.locationName,
                                            fontSize = 10.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    // Badge progress top right
                                    Box(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .align(Alignment.TopEnd)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(PastelSagePrimary.copy(alpha = 0.85f))
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
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
                                            text = "Foto #${photo.photoNumber}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PastelDustyBlue
                                        )
                                        StatusBadge(status = photo.status)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = photo.caption.ifBlank { photo.subPekerjaan.ifBlank { photo.workCategory } },
                                        fontSize = 12.sp,
                                        color = PastelCharcoalText,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Bottom Action Card for PDF
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onNavigate(LapoorNavDestination.LAPORAN) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PastelDustyBlueContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PastelDustyBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Siapkan Laporan Progress PDF",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelDustyBlueOnContainer
                        )
                        Text(
                            text = "Generate format A4 Landscape otomatis dengan tata letak cerdas 4 foto/halaman",
                            fontSize = 11.sp,
                            color = PastelDustyBlueOnContainer.copy(alpha = 0.8f)
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = PastelDustyBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
