package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.data.model.ProgressStatus
import com.example.data.model.WorkCategory
import com.example.ui.components.StatusBadge
import com.example.ui.theme.PastelAmberWarning
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CameraCaptureScreen(
    viewModel: LapoorViewModel,
    onFinishCapture: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val activeProject by viewModel.activeProject.collectAsState()
    val locations by viewModel.locations.collectAsState()
    val captureDraft by viewModel.captureDraft.collectAsState()

    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    // CameraX instance
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var isCapturing by remember { mutableStateOf(false) }

    // Dialog for adding a new location directly during capture
    var showAddLocationDialog by remember { mutableStateOf(false) }
    var newLocationNameInput by remember { mutableStateOf("") }

    // Temporary URI for direct camera intent fallback
    var tempCameraFile: File? by remember { mutableStateOf(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraFile != null && tempCameraFile!!.exists()) {
            viewModel.onPhotoCaptured(tempCameraFile!!)
        }
    }

    if (!cameraPermissionState.status.isGranted) {
        // Request Permission View
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(PastelCreamBackground)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(PastelSageContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = PastelSagePrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Izin Kamera Real-Time Diperlukan",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelCharcoalText
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "LAPOOR menjamin integritas laporan proyek dengan hanya memperbolehkan foto langsung dari kamera (tanpa upload galeri). Mohon izinkan akses kamera perangkat.",
                        fontSize = 13.sp,
                        color = PastelMutedText,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { cameraPermissionState.launchPermissionRequest() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PastelSagePrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_grant_camera_permission")
                    ) {
                        Text("Izinkan Akses Kamera", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    // IF DRAFT IS CAPTURED -> SHOW FORM REVIEW
    if (captureDraft.tempFile != null) {
        PhotoReviewAndForm(
            viewModel = viewModel,
            draft = captureDraft,
            locations = locations,
            activeProjectName = activeProject?.namaProject ?: "Proyek Aktif",
            onRetake = { viewModel.cancelCapture() },
            onSaveAndContinue = {
                viewModel.saveCapturedPhoto {
                    // Ready to take another
                }
            },
            onSaveAndFinish = {
                viewModel.saveCapturedPhoto {
                    onFinishCapture()
                }
            },
            onShowAddLocation = { showAddLocationDialog = true }
        )

        if (showAddLocationDialog) {
            AlertDialog(
                onDismissRequest = { showAddLocationDialog = false },
                title = { Text("Tambah Lokasi Baru", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Masukkan nama lokasi/area pekerjaan baru:", fontSize = 13.sp, color = PastelMutedText)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newLocationNameInput,
                            onValueChange = { newLocationNameInput = it },
                            placeholder = { Text("Contoh: Villa 05 / Club House") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newLocationNameInput.isNotBlank()) {
                                viewModel.addNewLocation(newLocationNameInput.trim())
                                viewModel.updateCaptureDraft(
                                    captureDraft.copy(
                                        selectedLocationName = newLocationNameInput.trim()
                                    )
                                )
                                newLocationNameInput = ""
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
        return
    }

    // REAL-TIME CAMERAX VIEWFINDER
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("camera_viewfinder_screen")
    ) {
        // CameraX Preview View
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build()

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        // Top HUD Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF55DD77))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "KAMERA REAL-TIME",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Close button to go back
            IconButton(
                onClick = onFinishCapture,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Tutup",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Live Watermark Preview at Bottom Left
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 120.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.55f))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Column {
                Text(
                    text = "LAPOOR | ${activeProject?.namaProject ?: "Proyek"}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
                    color = Color(0xFFCBE5D1),
                    fontSize = 10.sp
                )
            }
        }

        // Bottom Shutter Controls
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(vertical = 24.dp, horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Secondary Button: Use system camera app directly
                IconButton(
                    onClick = {
                        val photoFile = File(context.cacheDir, "temp_capture_${System.currentTimeMillis()}.jpg")
                        tempCameraFile = photoFile
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            photoFile
                        )
                        takePictureLauncher.launch(uri)
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "Buka Kamera Sistem",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Main Shutter Button
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .border(width = 4.dp, color = Color.White, shape = CircleShape)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(if (isCapturing) Color.Gray else Color.White)
                        .clickable(enabled = !isCapturing) {
                            val capture = imageCapture
                            if (capture != null) {
                                isCapturing = true
                                val photoFile = File(context.cacheDir, "lapoor_raw_${System.currentTimeMillis()}.jpg")
                                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                                capture.takePicture(
                                    outputOptions,
                                    ContextCompat.getMainExecutor(context),
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                            isCapturing = false
                                            viewModel.onPhotoCaptured(photoFile)
                                        }

                                        override fun onError(exc: ImageCaptureException) {
                                            isCapturing = false
                                            // Fallback to system camera on error
                                            val fallbackFile = File(context.cacheDir, "fallback_${System.currentTimeMillis()}.jpg")
                                            tempCameraFile = fallbackFile
                                            val uri = FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.provider",
                                                fallbackFile
                                            )
                                            takePictureLauncher.launch(uri)
                                        }
                                    }
                                )
                            } else {
                                // Fallback
                                val photoFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
                                tempCameraFile = photoFile
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    photoFile
                                )
                                takePictureLauncher.launch(uri)
                            }
                        }
                        .testTag("btn_shutter_capture"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(PastelSagePrimary)
                    )
                }

                // Switch Camera Lens Button
                IconButton(
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.FlipCameraAndroid,
                        contentDescription = "Ganti Kamera",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoReviewAndForm(
    viewModel: LapoorViewModel,
    draft: com.example.ui.viewmodel.PhotoCaptureDraft,
    locations: List<com.example.data.model.ProjectLocation>,
    activeProjectName: String,
    onRetake: () -> Unit,
    onSaveAndContinue: () -> Unit,
    onSaveAndFinish: () -> Unit,
    onShowAddLocation: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PastelCreamBackground)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("photo_form_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "INFORMASI DOKUMENTASI",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = PastelCharcoalText
                )
                Text(
                    text = "Foto real-time berhasil diambil",
                    fontSize = 12.sp,
                    color = PastelMutedText
                )
            }

            OutlinedButton(
                onClick = onRetake,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC04E4E))
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ulangi", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Image Preview with Watermark Mockup
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (draft.tempFile != null) {
                    AsyncImage(
                        model = draft.tempFile,
                        contentDescription = "Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Overlay watermark preview
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "LAPOOR | $activeProjectName | ${draft.selectedLocationName} | ${draft.selectedWorkCategory.name}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // FORM FIELDS CARD
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                // 1. Lokasi Pekerjaan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LOKASI PEKERJAAN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelCharcoalText
                    )
                    Text(
                        text = "+ Lokasi Baru",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelSagePrimary,
                        modifier = Modifier.clickable { onShowAddLocation() }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Location Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    locations.forEach { loc ->
                        val isSelected = draft.selectedLocationName == loc.namaLokasi
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.updateCaptureDraft(
                                    draft.copy(
                                        selectedLocationId = loc.locationId,
                                        selectedLocationName = loc.namaLokasi
                                    )
                                )
                            },
                            label = { Text(loc.namaLokasi, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PastelSageContainer,
                                selectedLabelColor = PastelSageOnContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Jenis Pekerjaan (Struktur, Arsitek, MEP)
                Text(
                    text = "JENIS PEKERJAAN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PastelCharcoalText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WorkCategory.entries.forEach { cat ->
                        val isSelected = draft.selectedWorkCategory == cat && draft.customWorkCategory.isBlank()
                        val (selBg, selColor) = when (cat) {
                            WorkCategory.STRUKTUR -> PastelSageContainer to PastelSageOnContainer
                            WorkCategory.ARSITEK -> PastelDustyBlueContainer to PastelDustyBlue
                            WorkCategory.MEP -> PastelMutedPeachContainer to PastelMutedPeach
                        }

                        Surface(
                            onClick = {
                                val firstSub = WorkCategory.getSubTasks(cat).firstOrNull() ?: ""
                                viewModel.updateCaptureDraft(
                                    draft.copy(
                                        selectedWorkCategory = cat,
                                        customWorkCategory = "",
                                        selectedSubTask = firstSub
                                    )
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) selBg else Color(0xFFF5F3EF),
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(0.8.dp, PastelBorderColor),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) selColor else PastelCharcoalText
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Sub Pekerjaan
                var showAddSubTaskDialog by remember { mutableStateOf(false) }
                var newSubTaskInput by remember { mutableStateOf("") }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SUB PEKERJAAN (${draft.selectedWorkCategory.name})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelCharcoalText
                    )
                    Text(
                        text = "+ Tambah Sub",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelSagePrimary,
                        modifier = Modifier.clickable { showAddSubTaskDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val subTasks = viewModel.getSubTasksForCategory(draft.selectedWorkCategory)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subTasks.forEach { sub ->
                        val isSelected = draft.selectedSubTask == sub && draft.customSubTask.isBlank()
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.updateCaptureDraft(
                                    draft.copy(
                                        selectedSubTask = sub,
                                        customSubTask = ""
                                    )
                                )
                            },
                            label = { Text(sub, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PastelDustyBlueContainer,
                                selectedLabelColor = PastelDustyBlue
                            )
                        )
                    }
                }

                if (showAddSubTaskDialog) {
                    AlertDialog(
                        onDismissRequest = { showAddSubTaskDialog = false },
                        title = { Text("Tambah Sub Pekerjaan", fontWeight = FontWeight.Bold) },
                        text = {
                            Column {
                                Text(
                                    "Masukkan sub pekerjaan baru untuk jenis '${draft.selectedWorkCategory.displayName}':",
                                    fontSize = 12.sp,
                                    color = PastelMutedText
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = newSubTaskInput,
                                    onValueChange = { newSubTaskInput = it },
                                    placeholder = { Text("Contoh: Pembesian Kolom K1") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (newSubTaskInput.isNotBlank()) {
                                        val name = newSubTaskInput.trim()
                                        viewModel.addCustomSubTask(draft.selectedWorkCategory, name)
                                        viewModel.updateCaptureDraft(
                                            draft.copy(
                                                selectedSubTask = name,
                                                customSubTask = ""
                                            )
                                        )
                                        newSubTaskInput = ""
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

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = draft.customSubTask,
                    onValueChange = {
                        viewModel.updateCaptureDraft(draft.copy(customSubTask = it))
                    },
                    placeholder = { Text("Atau ketik sub pekerjaan khusus...", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Keterangan Foto
                Text(
                    text = "KETERANGAN FOTO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PastelCharcoalText
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = draft.caption,
                    onValueChange = {
                        viewModel.updateCaptureDraft(draft.copy(caption = it))
                    },
                    placeholder = {
                        Text(
                            "Contoh: Pekerjaan pemasangan rangka balok lantai 2 sedang berlangsung sesuai gambar kerja.",
                            fontSize = 12.sp
                        )
                    },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_photo_caption")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 5. Progress Pekerjaan Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PROGRESS PEKERJAAN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelCharcoalText
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PastelSageContainer
                    ) {
                        Text(
                            text = "${draft.progress}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelSageOnContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Slider(
                    value = draft.progress.toFloat(),
                    onValueChange = { viewModel.updateCaptureDraft(draft.copy(progress = it.toInt())) },
                    valueRange = 0f..100f,
                    steps = 19, // step 5%
                    colors = SliderDefaults.colors(
                        thumbColor = PastelSagePrimary,
                        activeTrackColor = PastelSagePrimary,
                        inactiveTrackColor = PastelSageContainer
                    ),
                    modifier = Modifier.testTag("slider_photo_progress")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 6. Status Pekerjaan
                Text(
                    text = "STATUS PEKERJAAN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PastelCharcoalText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProgressStatus.entries.forEach { st ->
                        val isSelected = draft.status == st
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.updateCaptureDraft(draft.copy(status = st))
                            },
                            label = { Text(st.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PastelSageContainer,
                                selectedLabelColor = PastelSageOnContainer
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons
        Button(
            onClick = onSaveAndFinish,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PastelSagePrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_save_photo")
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("SIMPAN DOKUMENTASI", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onSaveAndContinue,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("SIMPAN & AMBIL FOTO LAGI", fontSize = 13.sp)
        }
    }
}
