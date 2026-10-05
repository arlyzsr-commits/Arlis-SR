package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.CustomSubTask
import com.example.data.model.GeneratedReport
import com.example.data.model.Project
import com.example.data.model.ProjectLocation
import com.example.data.model.ProgressPhoto

@Database(
    entities = [
        Project::class,
        ProjectLocation::class,
        ProgressPhoto::class,
        GeneratedReport::class,
        CustomSubTask::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun locationDao(): LocationDao
    abstract fun photoDao(): PhotoDao
    abstract fun reportDao(): ReportDao
    abstract fun subTaskDao(): SubTaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lapoor_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
