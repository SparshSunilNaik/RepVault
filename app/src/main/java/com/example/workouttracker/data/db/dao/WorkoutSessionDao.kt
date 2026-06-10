package com.example.workouttracker.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.workouttracker.data.db.entities.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {

    @Insert
    suspend fun insert(session: WorkoutSessionEntity): Long

    @Query("SELECT * FROM workout_sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions ORDER BY date DESC")
    suspend fun getAllSessionsList(): List<WorkoutSessionEntity>

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getById(id: Long): WorkoutSessionEntity?

    @Query("UPDATE workout_sessions SET workoutType = :workoutType WHERE id = :id")
    suspend fun updateWorkoutType(id: Long, workoutType: String)

    @Query("DELETE FROM workout_sessions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM workout_sessions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    suspend fun getSessionsBetweenDates(startDate: Long, endDate: Long): List<WorkoutSessionEntity>

    @Query("SELECT COUNT(*) FROM workout_sessions")
    fun getTotalSessionsFlow(): Flow<Int>
}
