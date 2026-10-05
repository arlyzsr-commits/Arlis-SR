package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ProgressPhoto
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getPhotosByProject(projectId: Long): Flow<List<ProgressPhoto>>

    @Query("SELECT * FROM photos WHERE projectId = :projectId AND workCategory = :category ORDER BY timestamp DESC")
    fun getPhotosByCategory(projectId: Long, category: String): Flow<List<ProgressPhoto>>

    @Query("SELECT * FROM photos WHERE projectId = :projectId AND locationId = :locationId ORDER BY timestamp DESC")
    fun getPhotosByLocation(projectId: Long, locationId: Long): Flow<List<ProgressPhoto>>

    @Query("SELECT * FROM photos WHERE projectId = :projectId")
    suspend fun getPhotosListByProject(projectId: Long): List<ProgressPhoto>

    @Query("SELECT * FROM photos WHERE photoId = :id LIMIT 1")
    suspend fun getPhotoById(id: Long): ProgressPhoto?

    @Query("SELECT COUNT(*) FROM photos WHERE projectId = :projectId")
    fun getPhotoCount(projectId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM photos WHERE projectId = :projectId AND locationId = :locationId")
    suspend fun getPhotoCountForLocation(projectId: Long, locationId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: ProgressPhoto): Long

    @Update
    suspend fun updatePhoto(photo: ProgressPhoto)

    @Delete
    suspend fun deletePhoto(photo: ProgressPhoto)

    @Query("DELETE FROM photos WHERE photoId = :id")
    suspend fun deletePhotoById(id: Long)
}
