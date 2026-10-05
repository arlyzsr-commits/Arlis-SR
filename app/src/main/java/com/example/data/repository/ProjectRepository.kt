package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.model.GeneratedReport
import com.example.data.model.Project
import com.example.data.model.ProjectLocation
import com.example.data.model.ProgressPhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class ProjectRepository(private val db: AppDatabase, private val context: Context) {
    private val projectDao = db.projectDao()
    private val locationDao = db.locationDao()
    private val photoDao = db.photoDao()
    private val reportDao = db.reportDao()
    private val subTaskDao = db.subTaskDao()

    val allProjects: Flow<List<Project>> = projectDao.getAllProjects()
    val activeProject: Flow<Project?> = projectDao.getActiveProjectFlow()

    fun getLocations(projectId: Long): Flow<List<ProjectLocation>> =
        locationDao.getLocationsByProject(projectId)

    fun getSubTasks(projectId: Long, category: String): Flow<List<com.example.data.model.CustomSubTask>> =
        subTaskDao.getSubTasksByCategory(projectId, category)

    fun getAllCustomSubTasks(projectId: Long): Flow<List<com.example.data.model.CustomSubTask>> =
        subTaskDao.getAllSubTasks(projectId)

    suspend fun insertSubTask(subTask: com.example.data.model.CustomSubTask): Long = withContext(Dispatchers.IO) {
        subTaskDao.insertSubTask(subTask)
    }

    suspend fun deleteSubTask(subTask: com.example.data.model.CustomSubTask) = withContext(Dispatchers.IO) {
        subTaskDao.deleteSubTask(subTask)
    }

    fun getPhotos(projectId: Long): Flow<List<ProgressPhoto>> =
        photoDao.getPhotosByProject(projectId)

    fun getPhotosByCategory(projectId: Long, category: String): Flow<List<ProgressPhoto>> =
        photoDao.getPhotosByCategory(projectId, category)

    fun getPhotosByLocation(projectId: Long, locationId: Long): Flow<List<ProgressPhoto>> =
        photoDao.getPhotosByLocation(projectId, locationId)

    fun getPhotoCount(projectId: Long): Flow<Int> =
        photoDao.getPhotoCount(projectId)

    fun getReports(projectId: Long): Flow<List<GeneratedReport>> =
        reportDao.getReportsByProject(projectId)

    suspend fun insertProject(project: Project): Long = withContext(Dispatchers.IO) {
        val id = projectDao.insertProject(project)
        if (project.isActive) {
            projectDao.clearActiveProjects()
            projectDao.setActiveProject(id)
        }
        id
    }

    suspend fun updateProject(project: Project) = withContext(Dispatchers.IO) {
        projectDao.updateProject(project)
    }

    suspend fun deleteProject(project: Project) = withContext(Dispatchers.IO) {
        projectDao.deleteProject(project)
    }

    suspend fun setActiveProject(projectId: Long) = withContext(Dispatchers.IO) {
        projectDao.clearActiveProjects()
        projectDao.setActiveProject(projectId)
    }

    suspend fun insertLocation(location: ProjectLocation): Long = withContext(Dispatchers.IO) {
        locationDao.insertLocation(location)
    }

    suspend fun insertLocations(locations: List<ProjectLocation>) = withContext(Dispatchers.IO) {
        locationDao.insertLocations(locations)
    }

    suspend fun deleteLocation(location: ProjectLocation) = withContext(Dispatchers.IO) {
        locationDao.deleteLocation(location)
    }

    suspend fun insertPhoto(photo: ProgressPhoto): Long = withContext(Dispatchers.IO) {
        val nextNumber = photoDao.getPhotoCountForLocation(photo.projectId, photo.locationId) + 1
        photoDao.insertPhoto(photo.copy(photoNumber = nextNumber))
    }

    suspend fun updatePhoto(photo: ProgressPhoto) = withContext(Dispatchers.IO) {
        photoDao.updatePhoto(photo)
    }

    suspend fun deletePhoto(photo: ProgressPhoto) = withContext(Dispatchers.IO) {
        photoDao.deletePhoto(photo)
    }

    suspend fun deletePhotoById(id: Long) = withContext(Dispatchers.IO) {
        photoDao.deletePhotoById(id)
    }

    suspend fun insertReport(report: GeneratedReport): Long = withContext(Dispatchers.IO) {
        reportDao.insertReport(report)
    }

    suspend fun deleteReport(report: GeneratedReport) = withContext(Dispatchers.IO) {
        reportDao.deleteReport(report)
    }

    suspend fun getProjectLocationsList(projectId: Long): List<ProjectLocation> = withContext(Dispatchers.IO) {
        locationDao.getLocationsList(projectId)
    }

    suspend fun getPhotosList(projectId: Long): List<ProgressPhoto> = withContext(Dispatchers.IO) {
        photoDao.getPhotosListByProject(projectId)
    }

    suspend fun seedInitialDataIfNeeded() = withContext(Dispatchers.IO) {
        val existing = projectDao.getAllProjects().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val sampleProject = Project(
                namaProject = "Pembangunan Balak Beach Resort & Hill Villas",
                owner = "PT Bali Surya Gemilang",
                perusahaan = "PT Surya Propertindo Tbk",
                nomorKontrak = "024/SPK/RESORT-BLI/X/2026",
                lokasi = "Kecamatan Kuta Selatan, Kab. Badung, Bali",
                kontraktor = "PT Nusantara Konstruksi Prima",
                konsultan = "PT Cipta Rancang Pratama",
                projectManager = "Ir. Hendra Gunawan, MT",
                siteManager = "Budi Santoso, ST",
                periodeLaporan = "Minggu ke-4 (Oktober 2026)",
                tanggalLaporan = "04 Oktober 2026",
                keterangan = "Proyek pembangunan 24 unit villa tropis, club house, dan area penunjang MEP lengkap.",
                isActive = true
            )
            val projId = projectDao.insertProject(sampleProject)

            val locations = listOf(
                "Villa 01",
                "Villa 02",
                "Villa 03",
                "Villa 04",
                "Hill Club",
                "Beach Club",
                "Marine Sport Center",
                "Jetty",
                "BOH (Back of House)",
                "Landscape & Infinity Pool"
            ).map {
                ProjectLocation(projectId = projId, namaLokasi = it)
            }
            locationDao.insertLocations(locations)
        }
    }
}
