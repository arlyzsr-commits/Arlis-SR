package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ProjectLocation
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Query("SELECT * FROM locations WHERE projectId = :projectId ORDER BY namaLokasi ASC")
    fun getLocationsByProject(projectId: Long): Flow<List<ProjectLocation>>

    @Query("SELECT * FROM locations WHERE projectId = :projectId ORDER BY namaLokasi ASC")
    suspend fun getLocationsList(projectId: Long): List<ProjectLocation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: ProjectLocation): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<ProjectLocation>)

    @Delete
    suspend fun deleteLocation(location: ProjectLocation)
}
