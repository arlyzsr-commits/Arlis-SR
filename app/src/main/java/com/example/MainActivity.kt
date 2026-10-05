package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.ProgressPhoto
import com.example.data.model.WorkCategory
import com.example.ui.components.LapoorBottomBar
import com.example.ui.components.LapoorNavDestination
import com.example.ui.screens.CameraCaptureScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PhotoGalleryScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ProjectScreen
import com.example.ui.screens.ReportPreviewScreen
import com.example.ui.screens.ReportScreen
import com.example.ui.screens.WorkCategoryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PastelCreamBackground
import com.example.ui.viewmodel.LapoorViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: LapoorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                LapoorAppMain(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun LapoorAppMain(viewModel: LapoorViewModel) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var currentNavDestination by remember { mutableStateOf(LapoorNavDestination.HOME) }
    var activeWorkCategoryView by remember { mutableStateOf<WorkCategory?>(null) }
    var isPreviewingPdf by remember { mutableStateOf(false) }

    // Listen to messages
    val messageEvent by viewModel.messageEvent.collectAsState()
    LaunchedEffect(messageEvent) {
        messageEvent?.let { msg ->
            scope.launch {
                snackbarHostState.showSnackbar(msg)
                viewModel.clearMessage()
            }
        }
    }

    // Determine whether to show bottom navigation bar
    val showBottomBar = !isPreviewingPdf && currentNavDestination != LapoorNavDestination.FOTO

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                LapoorBottomBar(
                    currentDestination = currentNavDestination,
                    onNavigate = { dest ->
                        activeWorkCategoryView = null
                        isPreviewingPdf = false
                        currentNavDestination = dest
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PastelCreamBackground)
                .padding(innerPadding)
        ) {
            when {
                // 1. PDF Preview Screen
                isPreviewingPdf -> {
                    ReportPreviewScreen(
                        viewModel = viewModel,
                        onBack = { isPreviewingPdf = false }
                    )
                }

                // 2. Specific Work Category Documentation (Struktur, Arsitek, MEP)
                activeWorkCategoryView != null -> {
                    BackHandler { activeWorkCategoryView = null }
                    WorkCategoryScreen(
                        viewModel = viewModel,
                        onBack = { activeWorkCategoryView = null },
                        onCapturePhotoForWork = { cat ->
                            viewModel.selectWorkCategory(cat)
                            currentNavDestination = LapoorNavDestination.FOTO
                            activeWorkCategoryView = null
                        }
                    )
                }

                // 3. Tab Destinations
                else -> {
                    when (currentNavDestination) {
                        LapoorNavDestination.HOME -> {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigate = { dest ->
                                    currentNavDestination = dest
                                },
                                onOpenWorkCategory = { cat ->
                                    viewModel.selectWorkCategory(cat)
                                    activeWorkCategoryView = cat
                                },
                                onOpenPhotoDetail = { photo ->
                                    val cat = WorkCategory.fromString(photo.workCategory)
                                    viewModel.selectWorkCategory(cat)
                                    activeWorkCategoryView = cat
                                }
                            )
                        }

                        LapoorNavDestination.GALERI -> {
                            BackHandler { currentNavDestination = LapoorNavDestination.HOME }
                            PhotoGalleryScreen(
                                viewModel = viewModel,
                                onNavigateToCamera = {
                                    currentNavDestination = LapoorNavDestination.FOTO
                                }
                            )
                        }

                        LapoorNavDestination.PROYEK -> {
                            BackHandler { currentNavDestination = LapoorNavDestination.HOME }
                            ProjectScreen(viewModel = viewModel)
                        }

                        LapoorNavDestination.FOTO -> {
                            BackHandler { currentNavDestination = LapoorNavDestination.HOME }
                            CameraCaptureScreen(
                                viewModel = viewModel,
                                onFinishCapture = {
                                    currentNavDestination = LapoorNavDestination.HOME
                                }
                            )
                        }

                        LapoorNavDestination.LAPORAN -> {
                            BackHandler { currentNavDestination = LapoorNavDestination.HOME }
                            ReportScreen(
                                viewModel = viewModel,
                                onOpenPreview = {
                                    isPreviewingPdf = true
                                }
                            )
                        }

                        LapoorNavDestination.PROFIL -> {
                            BackHandler { currentNavDestination = LapoorNavDestination.HOME }
                            ProfileScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
