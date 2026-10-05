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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Project
import com.example.data.model.ProjectLocation
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProjectScreen(
    viewModel: LapoorViewModel,
    modifier: Modifier = Modifier
) {
    val allProjects by viewModel.allProjects.collectAsState()
    val activeProject by viewModel.activeProject.collectAsState()
    val locations by viewModel.locations.collectAsState()

    var showProjectDialog by remember { mutableStateOf(false) }
    var projectToEdit by remember { mutableStateOf<Project?>(null) }

    var showAddLocationDialog by remember { mutableStateOf(false) }
    var newLocationInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PastelCreamBackground)
            .testTag("project_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MANAJEMEN DATA PROYEK",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelMutedText
                    )
                    Text(
                        text = "Daftar & Detail Proyek",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelCharcoalText
                    )
                }

                Button(
                    onClick = {
                        projectToEdit = null
                        showProjectDialog = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PastelSagePrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tambah", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Active Project Highlight Card
        activeProject?.let { proj ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PastelSageContainer)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "PROYEK AKTIF",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PastelSageOnContainer
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = {
                                        projectToEdit = proj
                                        showProjectDialog = true
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = PastelSagePrimary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = proj.namaProject,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelCharcoalText
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Owner: ${proj.owner.ifEmpty { "-" }}", fontSize = 12.sp, color = PastelMutedText)
                                Text("Kontraktor: ${proj.kontraktor.ifEmpty { "-" }}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PastelCharcoalText)
                                if (proj.scopePekerjaanKontraktor.isNotBlank()) {
                                    Text("Scope: ${proj.scopePekerjaanKontraktor}", fontSize = 11.sp, color = PastelSagePrimary)
                                }
                                Text("Konsultan: ${proj.konsultan.ifEmpty { "-" }}", fontSize = 12.sp, color = PastelMutedText)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Lokasi: ${proj.lokasi.ifEmpty { "-" }}", fontSize = 12.sp, color = PastelMutedText)
                                Text("PM MK/Owner: ${proj.projectManagerMK.ifEmpty { "-" }}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PastelCharcoalText)
                                Text("PM Kontraktor: ${proj.projectManager.ifEmpty { "-" }}", fontSize = 11.sp, color = PastelMutedText)
                                Text("Periode: ${proj.periodeLaporan.ifEmpty { "-" }}", fontSize = 12.sp, color = PastelMutedText)
                            }
                        }

                        if (proj.picOwnerStruktur.isNotBlank() || proj.picOwnerArsitek.isNotBlank() || proj.picOwnerMep.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PastelSageContainer.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("PIC OWNER SETIAP PEKERJAAN:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PastelSageOnContainer)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Struktur: ${proj.picOwnerStruktur.ifBlank { "-" }}", fontSize = 10.sp, color = PastelCharcoalText)
                                        Text("Arsitek: ${proj.picOwnerArsitek.ifBlank { "-" }}", fontSize = 10.sp, color = PastelCharcoalText)
                                        Text("MEP: ${proj.picOwnerMep.ifBlank { "-" }}", fontSize = 10.sp, color = PastelCharcoalText)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: KELOLA LOKASI PROYEK AKTIF
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PastelDustyBlueContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = PastelDustyBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Zona & Lokasi Proyek",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PastelCharcoalText
                            )
                        }

                        Button(
                            onClick = { showAddLocationDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PastelDustyBlue),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("+ Lokasi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Dokumentasi dikelompokkan berdasarkan zona ini. 1 halaman laporan memuat maks 4 foto per lokasi.",
                        fontSize = 11.sp,
                        color = PastelMutedText
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    locations.forEach { loc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF9F7F3))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = PastelSagePrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = loc.namaLokasi, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = PastelCharcoalText)
                            }

                            IconButton(
                                onClick = { viewModel.deleteLocation(loc) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFC04E4E), modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }

        // Section: SEMUA PROYEK
        item {
            Text(
                text = "Daftar Semua Proyek (${allProjects.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = PastelCharcoalText
            )
        }

        items(allProjects) { proj ->
            val isActive = proj.projectId == activeProject?.projectId
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { viewModel.setActiveProject(proj.projectId) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = if (isActive) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PastelSagePrimary), width = 2.dp) else null,
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = proj.namaProject,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PastelCharcoalText
                            )
                            if (isActive) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Aktif", tint = PastelSagePrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${proj.perusahaan} • ${proj.lokasi}",
                            fontSize = 11.5.sp,
                            color = PastelMutedText
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                projectToEdit = proj
                                showProjectDialog = true
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = PastelMutedText, modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = { viewModel.deleteProject(proj) },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFC04E4E), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }

    // Dialog Tambah / Edit Proyek
    if (showProjectDialog) {
        ProjectFormDialog(
            initialProject = projectToEdit,
            onDismiss = { showProjectDialog = false },
            onSave = { updated ->
                viewModel.saveProject(updated) {
                    showProjectDialog = false
                }
            }
        )
    }

    // Dialog Tambah Lokasi Baru
    if (showAddLocationDialog) {
        AlertDialog(
            onDismissRequest = { showAddLocationDialog = false },
            title = { Text("Tambah Lokasi / Zona Baru", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Nama lokasi atau zona kerja:", fontSize = 12.sp, color = PastelMutedText)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newLocationInput,
                        onValueChange = { newLocationInput = it },
                        placeholder = { Text("Contoh: Villa 05 / Landscape") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newLocationInput.isNotBlank()) {
                            viewModel.addNewLocation(newLocationInput.trim())
                            newLocationInput = ""
                            showAddLocationDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PastelSagePrimary)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddLocationDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun ProjectFormDialog(
    initialProject: Project?,
    onDismiss: () -> Unit,
    onSave: (Project) -> Unit
) {
    var namaProject by remember { mutableStateOf(initialProject?.namaProject ?: "") }
    var owner by remember { mutableStateOf(initialProject?.owner ?: "") }
    var perusahaan by remember { mutableStateOf(initialProject?.perusahaan ?: "") }
    var nomorKontrak by remember { mutableStateOf(initialProject?.nomorKontrak ?: "") }
    var lokasi by remember { mutableStateOf(initialProject?.lokasi ?: "") }
    var kontraktor by remember { mutableStateOf(initialProject?.kontraktor ?: "") }
    var scopePekerjaanKontraktor by remember { mutableStateOf(initialProject?.scopePekerjaanKontraktor ?: "") }
    var konsultan by remember { mutableStateOf(initialProject?.konsultan ?: "") }
    var projectManagerMK by remember { mutableStateOf(initialProject?.projectManagerMK ?: "") }
    var projectManager by remember { mutableStateOf(initialProject?.projectManager ?: "") }
    var siteManager by remember { mutableStateOf(initialProject?.siteManager ?: "") }
    var picOwnerStruktur by remember { mutableStateOf(initialProject?.picOwnerStruktur ?: "") }
    var picOwnerArsitek by remember { mutableStateOf(initialProject?.picOwnerArsitek ?: "") }
    var picOwnerMep by remember { mutableStateOf(initialProject?.picOwnerMep ?: "") }
    var periodeLaporan by remember { mutableStateOf(initialProject?.periodeLaporan ?: "Minggu ke-4") }
    var tanggalLaporan by remember { mutableStateOf(initialProject?.tanggalLaporan ?: SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())) }
    var keterangan by remember { mutableStateOf(initialProject?.keterangan ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialProject == null) "Tambah Data Proyek" else "Edit Data Proyek",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = namaProject,
                    onValueChange = { namaProject = it },
                    label = { Text("Nama Proyek *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = owner,
                    onValueChange = { owner = it },
                    label = { Text("Nama Owner") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = perusahaan,
                    onValueChange = { perusahaan = it },
                    label = { Text("Nama Perusahaan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = nomorKontrak,
                    onValueChange = { nomorKontrak = it },
                    label = { Text("Nomor Kontrak") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = lokasi,
                    onValueChange = { lokasi = it },
                    label = { Text("Lokasi Proyek") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Kontraktor & Scope Pekerjaan
                OutlinedTextField(
                    value = kontraktor,
                    onValueChange = { kontraktor = it },
                    label = { Text("Nama Kontraktor") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = scopePekerjaanKontraktor,
                    onValueChange = { scopePekerjaanKontraktor = it },
                    label = { Text("Scope Pekerjaan Kontraktor") },
                    placeholder = { Text("Contoh: Struktur, Arsitektur, MEP") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = konsultan,
                    onValueChange = { konsultan = it },
                    label = { Text("Nama Konsultan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Project Manager MK / Owner
                OutlinedTextField(
                    value = projectManagerMK,
                    onValueChange = { projectManagerMK = it },
                    label = { Text("Project Manager MK / Owner") },
                    placeholder = { Text("Nama PM dari pihak MK / Owner") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = projectManager,
                        onValueChange = { projectManager = it },
                        label = { Text("PM Kontraktor") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = siteManager,
                        onValueChange = { siteManager = it },
                        label = { Text("Site Mgr") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Section PIC Owner untuk setiap pekerjaan
                Text(
                    text = "PIC OWNER SETIAP PEKERJAAN",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = PastelSagePrimary
                )

                OutlinedTextField(
                    value = picOwnerStruktur,
                    onValueChange = { picOwnerStruktur = it },
                    label = { Text("PIC Owner - STRUKTUR") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = picOwnerArsitek,
                    onValueChange = { picOwnerArsitek = it },
                    label = { Text("PIC Owner - ARSITEK") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = picOwnerMep,
                    onValueChange = { picOwnerMep = it },
                    label = { Text("PIC Owner - MEP") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = periodeLaporan,
                        onValueChange = { periodeLaporan = it },
                        label = { Text("Periode") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = tanggalLaporan,
                        onValueChange = { tanggalLaporan = it },
                        label = { Text("Tanggal") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = keterangan,
                    onValueChange = { keterangan = it },
                    label = { Text("Keterangan Singkat") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (namaProject.isNotBlank()) {
                        val toSave = (initialProject ?: Project(namaProject = namaProject)).copy(
                            namaProject = namaProject.trim(),
                            owner = owner.trim(),
                            perusahaan = perusahaan.trim(),
                            nomorKontrak = nomorKontrak.trim(),
                            lokasi = lokasi.trim(),
                            kontraktor = kontraktor.trim(),
                            scopePekerjaanKontraktor = scopePekerjaanKontraktor.trim(),
                            konsultan = konsultan.trim(),
                            projectManagerMK = projectManagerMK.trim(),
                            projectManager = projectManager.trim(),
                            siteManager = siteManager.trim(),
                            picOwnerStruktur = picOwnerStruktur.trim(),
                            picOwnerArsitek = picOwnerArsitek.trim(),
                            picOwnerMep = picOwnerMep.trim(),
                            periodeLaporan = periodeLaporan.trim(),
                            tanggalLaporan = tanggalLaporan.trim(),
                            keterangan = keterangan.trim()
                        )
                        onSave(toSave)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PastelSagePrimary)
            ) {
                Text("Simpan Proyek")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
