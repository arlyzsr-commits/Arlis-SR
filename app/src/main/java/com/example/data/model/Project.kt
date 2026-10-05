package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true)
    val projectId: Long = 0,
    val namaProject: String,
    val owner: String = "",
    val perusahaan: String = "",
    val nomorKontrak: String = "",
    val lokasi: String = "",
    val kontraktor: String = "",
    val scopePekerjaanKontraktor: String = "",
    val konsultan: String = "",
    val projectManager: String = "",
    val siteManager: String = "",
    val projectManagerMK: String = "",
    val picOwnerStruktur: String = "",
    val picOwnerArsitek: String = "",
    val picOwnerMep: String = "",
    val periodeLaporan: String = "",
    val tanggalLaporan: String = "",
    val keterangan: String = "",
    val logoUri: String? = null,
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
