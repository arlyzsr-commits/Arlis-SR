package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class ProgressPhoto(
    @PrimaryKey(autoGenerate = true)
    val photoId: Long = 0,
    val projectId: Long,
    val workCategory: String, // STRUKTUR, ARSITEK, MEP, or custom
    val subPekerjaan: String,
    val locationId: Long,
    val locationName: String,
    val filePath: String,
    val timestamp: Long = System.currentTimeMillis(),
    val caption: String,
    val progress: Int = 0, // 0 to 100%
    val status: String = "Sedang Dikerjakan",
    val photoNumber: Int = 1
)
