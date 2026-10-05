package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.GeneratedReport
import com.example.data.model.ProgressPhoto
import com.example.data.model.ProgressStatus
import com.example.data.model.Project
import com.example.data.model.ProjectLocation
import com.example.data.model.ReportGenerationParam
import com.example.data.model.WorkCategory
import com.example.data.repository.ProjectRepository
import com.example.engine.ImageWatermarkHelper
import com.example.engine.LocationReportPage
import com.example.engine.PhotoLayoutEngine
import com.example.engine.ReportPdfGenerator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PhotoCaptureDraft(
    val tempFile: File? = null,
    val previewBitmap: Bitmap? = null,
    val selectedWorkCategory: WorkCategory = WorkCategory.STRUKTUR,
    val customWorkCategory: String = "",
    val selectedSubTask: String = "",
    val customSubTask: String = "",
    val selectedLocationId: Long = 0,
    val selectedLocationName: String = "",
    val caption: String = "",
    val progress: Int = 50,
    val status: ProgressStatus = ProgressStatus.SEDANG_DIKERJAKAN
)

@OptIn(ExperimentalCoroutinesApi::class)
class LapoorViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = ProjectRepository(db, application)

    val allProjects: StateFlow<List<Project>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProject: StateFlow<Project?> = repository.activeProject
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val locations: StateFlow<List<ProjectLocation>> = activeProject.flatMapLatest { project ->
        if (project != null) repository.getLocations(project.projectId)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val photos: StateFlow<List<ProgressPhoto>> = activeProject.flatMapLatest { project ->
        if (project != null) repository.getPhotos(project.projectId)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reports: StateFlow<List<GeneratedReport>> = activeProject.flatMapLatest { project ->
        if (project != null) repository.getReports(project.projectId)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customSubTasks: StateFlow<List<com.example.data.model.CustomSubTask>> = activeProject.flatMapLatest { project ->
        if (project != null) repository.getAllCustomSubTasks(project.projectId)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Photos Grouped by Date (for Menu Lihat Photo)
    val photosGroupedByDate: StateFlow<Map<String, List<ProgressPhoto>>> = photos.map { photoList ->
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        photoList.groupBy { photo ->
            dateFormat.format(Date(photo.timestamp))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Report photo selection & Location manual notes
    private val _selectedPhotoIdsForReport = MutableStateFlow<Set<Long>>(emptySet())
    val selectedPhotoIdsForReport = _selectedPhotoIdsForReport.asStateFlow()

    private val _locationManualNotes = MutableStateFlow<Map<String, String>>(emptyMap())
    val locationManualNotes = _locationManualNotes.asStateFlow()

    // Work screen filter states
    private val _selectedWorkCategory = MutableStateFlow(WorkCategory.STRUKTUR)
    val selectedWorkCategory = _selectedWorkCategory.asStateFlow()

    private val _selectedLocationFilter = MutableStateFlow<String?>("SEMUA")
    val selectedLocationFilter = _selectedLocationFilter.asStateFlow()

    // Filtered photos for Work Screen
    val filteredPhotos: StateFlow<List<ProgressPhoto>> = combine(
        photos,
        selectedWorkCategory,
        selectedLocationFilter
    ) { photoList, category, locFilter ->
        photoList.filter { photo ->
            val matchCategory = photo.workCategory.equals(category.name, ignoreCase = true)
            val matchLocation = locFilter == null || locFilter == "SEMUA" || photo.locationName.equals(locFilter, ignoreCase = true)
            matchCategory && matchLocation
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Photo Capture Draft State
    private val _captureDraft = MutableStateFlow(PhotoCaptureDraft())
    val captureDraft = _captureDraft.asStateFlow()

    // PDF Preview & Generation State
    private val _reportParams = MutableStateFlow<ReportGenerationParam?>(null)
    val reportParams = _reportParams.asStateFlow()

    private val _previewPages = MutableStateFlow<List<LocationReportPage>>(emptyList())
    val previewPages = _previewPages.asStateFlow()

    private val _generatedPdfFile = MutableStateFlow<File?>(null)
    val generatedPdfFile = _generatedPdfFile.asStateFlow()

    private val _isGeneratingPdf = MutableStateFlow(false)
    val isGeneratingPdf = _isGeneratingPdf.asStateFlow()

    private val _messageEvent = MutableStateFlow<String?>(null)
    val messageEvent = _messageEvent.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
        }
    }

    fun clearMessage() {
        _messageEvent.value = null
    }

    fun selectWorkCategory(category: WorkCategory) {
        _selectedWorkCategory.value = category
    }

    fun selectLocationFilter(locationName: String?) {
        _selectedLocationFilter.value = locationName
    }

    fun setActiveProject(projectId: Long) {
        viewModelScope.launch {
            repository.setActiveProject(projectId)
        }
    }

    fun saveProject(project: Project, onComplete: () -> Unit) {
        viewModelScope.launch {
            if (project.projectId == 0L) {
                repository.insertProject(project.copy(isActive = true))
            } else {
                repository.updateProject(project)
            }
            onComplete()
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            repository.deleteProject(project)
        }
    }

    fun addNewLocation(namaLokasi: String) {
        val project = activeProject.value ?: return
        if (namaLokasi.isBlank()) return
        viewModelScope.launch {
            repository.insertLocation(
                ProjectLocation(
                    projectId = project.projectId,
                    namaLokasi = namaLokasi.trim()
                )
            )
            _messageEvent.value = "Lokasi '${namaLokasi.trim()}' berhasil ditambahkan"
        }
    }

    fun deleteLocation(location: ProjectLocation) {
        viewModelScope.launch {
            repository.deleteLocation(location)
            _messageEvent.value = "Lokasi dihapus"
        }
    }

    // --- REAL-TIME CAMERA PHOTO CAPTURE FLOW ---

    fun onPhotoCaptured(file: File) {
        val proj = activeProject.value
        val locs = locations.value
        val defaultLoc = locs.firstOrNull()

        // Read bitmap thumbnail for preview
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)

        _captureDraft.value = PhotoCaptureDraft(
            tempFile = file,
            previewBitmap = bitmap,
            selectedWorkCategory = _selectedWorkCategory.value,
            selectedSubTask = WorkCategory.getSubTasks(_selectedWorkCategory.value).firstOrNull() ?: "",
            selectedLocationId = defaultLoc?.locationId ?: 0L,
            selectedLocationName = defaultLoc?.namaLokasi ?: "Area Utama",
            caption = "",
            progress = 50,
            status = ProgressStatus.SEDANG_DIKERJAKAN
        )
    }

    fun updateCaptureDraft(draft: PhotoCaptureDraft) {
        _captureDraft.value = draft
    }

    fun saveCapturedPhoto(onSuccess: () -> Unit) {
        val draft = _captureDraft.value
        val file = draft.tempFile ?: return
        val project = activeProject.value ?: return

        viewModelScope.launch {
            try {
                val categoryName = if (draft.customWorkCategory.isNotBlank()) {
                    draft.customWorkCategory.trim()
                } else {
                    draft.selectedWorkCategory.name
                }

                val subPekerjaanName = if (draft.customSubTask.isNotBlank()) {
                    draft.customSubTask.trim()
                } else {
                    draft.selectedSubTask
                }

                val locationName = draft.selectedLocationName.ifBlank { "Lokasi Lapangan" }

                // Watermark and save to permanent app storage
                val watermarkedFile = ImageWatermarkHelper.applyWatermarkAndSave(
                    context = getApplication(),
                    originalFile = file,
                    projectName = project.namaProject,
                    workCategory = categoryName,
                    locationName = locationName,
                    photoNumber = 1
                )

                val progressPhoto = ProgressPhoto(
                    projectId = project.projectId,
                    workCategory = categoryName,
                    subPekerjaan = subPekerjaanName,
                    locationId = draft.selectedLocationId,
                    locationName = locationName,
                    filePath = watermarkedFile.absolutePath,
                    timestamp = System.currentTimeMillis(),
                    caption = draft.caption.trim(),
                    progress = draft.progress,
                    status = draft.status.label
                )

                repository.insertPhoto(progressPhoto)
                _messageEvent.value = "Foto dokumentasi berhasil disimpan!"

                // Clean draft preview bitmap
                draft.previewBitmap?.recycle()
                _captureDraft.value = PhotoCaptureDraft()

                onSuccess()
            } catch (e: Exception) {
                _messageEvent.value = "Gagal menyimpan foto: ${e.message}"
            }
        }
    }

    fun cancelCapture() {
        _captureDraft.value.previewBitmap?.recycle()
        _captureDraft.value = PhotoCaptureDraft()
    }

    fun deletePhoto(photo: ProgressPhoto) {
        viewModelScope.launch {
            try {
                File(photo.filePath).delete()
            } catch (_: Exception) {}
            repository.deletePhoto(photo)
            _messageEvent.value = "Foto berhasil dihapus"
        }
    }

    fun updatePhoto(photo: ProgressPhoto) {
        viewModelScope.launch {
            repository.updatePhoto(photo)
            _messageEvent.value = "Dokumentasi diperbarui"
        }
    }

    fun deleteReport(report: GeneratedReport) {
        viewModelScope.launch {
            try {
                File(report.pdfFilePath).delete()
            } catch (_: Exception) {}
            repository.deleteReport(report)
            _messageEvent.value = "Laporan berhasil dihapus"
        }
    }

    fun getSubTasksForCategory(category: WorkCategory): List<String> {
        val standard = WorkCategory.getSubTasks(category)
        val custom = customSubTasks.value
            .filter { it.workCategory.equals(category.name, ignoreCase = true) }
            .map { it.subTaskName }
        return (standard + custom).distinct()
    }

    fun addCustomSubTask(category: WorkCategory, subTaskName: String) {
        val project = activeProject.value ?: return
        if (subTaskName.isBlank()) return
        viewModelScope.launch {
            repository.insertSubTask(
                com.example.data.model.CustomSubTask(
                    projectId = project.projectId,
                    workCategory = category.name,
                    subTaskName = subTaskName.trim()
                )
            )
            _messageEvent.value = "Sub-Pekerjaan '${subTaskName.trim()}' berhasil ditambahkan ke ${category.displayName}"
        }
    }

    fun deleteCustomSubTask(subTask: com.example.data.model.CustomSubTask) {
        viewModelScope.launch {
            repository.deleteSubTask(subTask)
            _messageEvent.value = "Sub-Pekerjaan '${subTask.subTaskName}' dihapus"
        }
    }

    fun togglePhotoSelectionForReport(photoId: Long) {
        val current = _selectedPhotoIdsForReport.value.toMutableSet()
        if (current.contains(photoId)) current.remove(photoId) else current.add(photoId)
        _selectedPhotoIdsForReport.value = current
    }

    fun selectAllPhotosForReport(photoIds: List<Long>) {
        _selectedPhotoIdsForReport.value = photoIds.toSet()
    }

    fun deselectAllPhotosForReport() {
        _selectedPhotoIdsForReport.value = emptySet()
    }

    fun updateLocationManualNote(locationName: String, note: String) {
        val current = _locationManualNotes.value.toMutableMap()
        current[locationName] = note
        _locationManualNotes.value = current
    }

    // --- REPORT GENERATION & PREVIEW ---

    fun prepareReportPreview(
        workCategoryFilter: String,
        locationFilter: String,
        periodeLaporan: String,
        tanggalLaporan: String,
        namaPenyusun: String,
        jabatan: String,
        keteranganUmum: String,
        onReady: () -> Unit
    ) {
        val proj = activeProject.value ?: return

        // Filter photos based on selection criteria AND selectedPhotoIds
        val allCurrentPhotos = photos.value
        val selectedIds = _selectedPhotoIdsForReport.value
        val filtered = allCurrentPhotos.filter { p ->
            val matchWork = workCategoryFilter == "SEMUA" || p.workCategory.contains(workCategoryFilter, ignoreCase = true)
            val matchLoc = locationFilter == "SEMUA" || p.locationName.equals(locationFilter, ignoreCase = true)
            val matchSelection = selectedIds.isEmpty() || selectedIds.contains(p.photoId)
            matchWork && matchLoc && matchSelection
        }

        val notes = _locationManualNotes.value

        val params = ReportGenerationParam(
            projectId = proj.projectId,
            projectName = proj.namaProject,
            owner = proj.owner,
            perusahaan = proj.perusahaan,
            kontraktor = proj.kontraktor,
            scopePekerjaanKontraktor = proj.scopePekerjaanKontraktor,
            konsultan = proj.konsultan,
            lokasiProyek = proj.lokasi,
            nomorKontrak = proj.nomorKontrak,
            projectManager = proj.projectManager,
            siteManager = proj.siteManager,
            projectManagerMK = proj.projectManagerMK,
            picOwnerStruktur = proj.picOwnerStruktur,
            picOwnerArsitek = proj.picOwnerArsitek,
            picOwnerMep = proj.picOwnerMep,
            workCategoryFilter = workCategoryFilter,
            locationFilter = locationFilter,
            periodeLaporan = periodeLaporan,
            tanggalLaporan = tanggalLaporan,
            namaPenyusun = namaPenyusun,
            jabatan = jabatan,
            keteranganUmum = keteranganUmum,
            locationNotes = notes,
            selectedPhotoIds = selectedIds
        )

        _reportParams.value = params

        val picOwnerMap = mapOf(
            "STRUKTUR" to proj.picOwnerStruktur,
            "ARSITEK" to proj.picOwnerArsitek,
            "MEP" to proj.picOwnerMep
        )

        // Layout pages using PhotoLayoutEngine
        val pages = PhotoLayoutEngine.createLocationPages(
            photos = filtered,
            overrideWorkCategory = if (workCategoryFilter != "SEMUA") workCategoryFilter else null,
            locationNotes = notes,
            projectManagerMK = proj.projectManagerMK,
            picOwnerMap = picOwnerMap,
            kontraktor = proj.kontraktor,
            scopePekerjaan = proj.scopePekerjaanKontraktor
        )

        _previewPages.value = pages
        onReady()
    }

    fun generatePdf(onSuccess: (File) -> Unit) {
        val params = _reportParams.value ?: return
        val currentPhotos = photos.value.filter { p ->
            val matchWork = params.workCategoryFilter == "SEMUA" || p.workCategory.contains(params.workCategoryFilter, ignoreCase = true)
            val matchLoc = params.locationFilter == "SEMUA" || p.locationName.equals(params.locationFilter, ignoreCase = true)
            val matchSelection = params.selectedPhotoIds.isEmpty() || params.selectedPhotoIds.contains(p.photoId)
            matchWork && matchLoc && matchSelection
        }

        viewModelScope.launch {
            _isGeneratingPdf.value = true
            try {
                val pdfFile = ReportPdfGenerator.generateReportPdf(
                    context = getApplication(),
                    params = params,
                    photos = currentPhotos
                )
                _generatedPdfFile.value = pdfFile

                // Record to database
                repository.insertReport(
                    GeneratedReport(
                        projectId = params.projectId,
                        projectName = params.projectName,
                        workCategoryFilter = params.workCategoryFilter,
                        locationFilter = params.locationFilter,
                        periodeLaporan = params.periodeLaporan,
                        tanggalLaporan = params.tanggalLaporan,
                        namaPenyusun = params.namaPenyusun,
                        jabatan = params.jabatan,
                        keteranganUmum = params.keteranganUmum,
                        pdfFilePath = pdfFile.absolutePath,
                        totalPhotos = currentPhotos.size
                    )
                )

                _messageEvent.value = "PDF Laporan berhasil dibuat!"
                onSuccess(pdfFile)
            } catch (e: Exception) {
                _messageEvent.value = "Gagal membuat PDF: ${e.message}"
            } finally {
                _isGeneratingPdf.value = false
            }
        }
    }
}
