package com.example.workouttracker.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.workouttracker.data.db.entities.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSetDao {

    @Insert
    suspend fun insert(set: WorkoutSetEntity): Long

    @Insert
    suspend fun insertAll(sets: List<WorkoutSetEntity>)

    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId")
    suspend fun getSetsForSession(sessionId: Long): List<WorkoutSetEntity>

    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId")
    fun getSetsForSessionFlow(sessionId: Long): Flow<List<WorkoutSetEntity>>
}
