package com.example.workouttracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.workouttracker.data.db.dao.ExerciseDao
import com.example.workouttracker.data.db.dao.WorkoutSessionDao
import com.example.workouttracker.data.db.dao.WorkoutSetDao
import com.example.workouttracker.data.db.entities.ExerciseEntity
import com.example.workouttracker.data.db.entities.WorkoutSessionEntity
import com.example.workouttracker.data.db.entities.WorkoutSetEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutSessionEntity::class,
        WorkoutSetEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun workoutSetDao(): WorkoutSetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "repvault_database"
                )
                    .fallbackToDestructiveMigration(false)
                    .addCallback(SeedCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.let { database ->
                    if (database.exerciseDao().count() == 0) {
                        database.exerciseDao().insertAll(ExerciseSeeder.toEntities())
                    }
                }
            }
        }
    }
}
