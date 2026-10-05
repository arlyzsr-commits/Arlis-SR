package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class GeneratedReport(
    @PrimaryKey(autoGenerate = true)
    val reportId: Long = 0,
    val projectId: Long,
    val projectName: String,
    val workCategoryFilter: String, // "SEMUA", "STRUKTUR", "ARSITEK", "MEP"
    val locationFilter: String, // "SEMUA" or specific location
    val periodeLaporan: String,
    val tanggalLaporan: String,
    val namaPenyusun: String,
    val jabatan: String,
    val keteranganUmum: String,
    val pdfFilePath: String,
    val totalPhotos: Int,
    val createdAt: Long = System.currentTimeMillis()
)

data class ReportGenerationParam(
    val projectId: Long,
    val projectName: String,
    val owner: String,
    val perusahaan: String,
    val kontraktor: String,
    val scopePekerjaanKontraktor: String = "",
    val konsultan: String,
    val lokasiProyek: String,
    val nomorKontrak: String,
    val projectManager: String,
    val siteManager: String,
    val projectManagerMK: String = "",
    val picOwnerStruktur: String = "",
    val picOwnerArsitek: String = "",
    val picOwnerMep: String = "",
    val workCategoryFilter: String = "SEMUA",
    val locationFilter: String = "SEMUA",
    val periodeLaporan: String = "Periode Minggu ke-4",
    val tanggalLaporan: String = "04 Oktober 2026",
    val namaPenyusun: String = "Site Inspector",
    val jabatan: String = "Quality Control / Supervisor",
    val keteranganUmum: String = "Laporan dokumentasi progress fisik berkala pekerjaan lapangan.",
    val locationNotes: Map<String, String> = emptyMap(),
    val selectedPhotoIds: Set<Long> = emptySet()
)
