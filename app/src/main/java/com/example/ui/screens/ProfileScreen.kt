package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun ProfileScreen(
    viewModel: LapoorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeProject by viewModel.activeProject.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val reports by viewModel.reports.collectAsState()

    // Calculate disk space
    val photosDir = File(context.filesDir, "project_photos")
    val photosSize = (photosDir.listFiles()?.sumOf { it.length() } ?: 0L) / (1024 * 1024)

    val reportsDir = File(context.filesDir, "reports")
    val reportsSize = (reportsDir.listFiles()?.sumOf { it.length() } ?: 0L) / (1024 * 1024)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PastelCreamBackground)
            .testTag("profile_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Identity Header
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(PastelSageContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = PastelSagePrimary,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "LAPOOR",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = PastelSagePrimary
                    )

                    Text(
                        text = "Laporan Progress Pekerjaan Proyek",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = PastelMutedText
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PastelSageContainer
                    ) {
                        Text(
                            text = "Versi 1.0 • Offline-First Native",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelSageOnContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Integrity & Rules Info
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = PastelSagePrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "STANDAR INTEGRITAS LAPOOR",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelCharcoalText
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    RuleItem("Foto Kamera Real-time", "Dokumentasi hanya dapat diambil langsung melalui kamera handphone tanpa upload dari galeri untuk menjaga validitas data lapangan.")
                    RuleItem("Aturan 1 Lokasi = 4 Foto", "Setiap lembar laporan PDF dikelompokkan per lokasi dan memuat maksimal 4 foto terstruktur.")
                    RuleItem("Smart Layout Engine", "Komposisi 4 foto dipilih secara dinamis dari template A-F (2x2, besar kiri, besar atas, dsb.) dengan caption & progress.")
                    RuleItem("Format PDF Profesional", "Laporan digenerate dalam format A4 Landscape lengkap dengan Cover, Info Proyek, dan Ringkasan Progress.")
                    RuleItem("Watermark Otomatis", "Setiap foto dibubuhi stempel halus berisi nama proyek, lokasi, disiplin kerja, tanggal dan jam pengambilan.")
                }
            }
        }

        // Storage & Database Info
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Storage, contentDescription = null, tint = PastelDustyBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PENYIMPANAN DATA LOKAL",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelCharcoalText
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Database Proyek (Room):", fontSize = 12.sp, color = PastelMutedText)
                        Text("Aktif (${photos.size} Foto Terdata)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PastelCharcoalText)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Penyimpanan Foto Proyek:", fontSize = 12.sp, color = PastelMutedText)
                        Text("$photosSize MB", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PastelCharcoalText)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Berkas Laporan PDF:", fontSize = 12.sp, color = PastelMutedText)
                        Text("${reports.size} Berkas ($reportsSize MB)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PastelCharcoalText)
                    }
                }
            }
        }
    }
}

@Composable
private fun RuleItem(title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(PastelSagePrimary)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PastelCharcoalText)
            Text(text = desc, fontSize = 11.5.sp, color = PastelMutedText, lineHeight = 16.sp)
        }
    }
}
