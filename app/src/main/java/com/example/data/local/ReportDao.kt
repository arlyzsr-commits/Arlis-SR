package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.GeneratedReport
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getReportsByProject(projectId: Long): Flow<List<GeneratedReport>>

    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun getAllReports(): Flow<List<GeneratedReport>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: GeneratedReport): Long

    @Delete
    suspend fun deleteReport(report: GeneratedReport)
}
