package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Project
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE projectId = :id LIMIT 1")
    suspend fun getProjectById(id: Long): Project?

    @Query("SELECT * FROM projects WHERE isActive = 1 LIMIT 1")
    fun getActiveProjectFlow(): Flow<Project?>

    @Query("SELECT * FROM projects WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveProject(): Project?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Update
    suspend fun updateProject(project: Project)

    @Delete
    suspend fun deleteProject(project: Project)

    @Query("UPDATE projects SET isActive = 0")
    suspend fun clearActiveProjects()

    @Query("UPDATE projects SET isActive = 1 WHERE projectId = :id")
    suspend fun setActiveProject(id: Long)
}
