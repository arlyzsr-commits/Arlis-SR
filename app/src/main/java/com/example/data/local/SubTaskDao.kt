package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CustomSubTask
import kotlinx.coroutines.flow.Flow

@Dao
interface SubTaskDao {
    @Query("SELECT * FROM custom_sub_tasks WHERE projectId = :projectId AND workCategory = :category ORDER BY subTaskName ASC")
    fun getSubTasksByCategory(projectId: Long, category: String): Flow<List<CustomSubTask>>

    @Query("SELECT * FROM custom_sub_tasks WHERE projectId = :projectId ORDER BY subTaskName ASC")
    fun getAllSubTasks(projectId: Long): Flow<List<CustomSubTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubTask(subTask: CustomSubTask): Long

    @Delete
    suspend fun deleteSubTask(subTask: CustomSubTask)
}
