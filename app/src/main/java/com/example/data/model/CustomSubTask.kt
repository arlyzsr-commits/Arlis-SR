package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_sub_tasks")
data class CustomSubTask(
    @PrimaryKey(autoGenerate = true)
    val subTaskId: Long = 0,
    val projectId: Long,
    val workCategory: String, // STRUKTUR, ARSITEK, MEP
    val subTaskName: String
)

data class LocationReportDetail(
    val locationName: String,
    val workCategory: String,
    val subTasksSummary: List<Pair<String, Int>>, // List of (SubPekerjaan, avg progress %)
    var manualNote: String = ""
)
