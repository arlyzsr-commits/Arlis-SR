package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class ProjectLocation(
    @PrimaryKey(autoGenerate = true)
    val locationId: Long = 0,
    val projectId: Long,
    val namaLokasi: String,
    val keterangan: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
